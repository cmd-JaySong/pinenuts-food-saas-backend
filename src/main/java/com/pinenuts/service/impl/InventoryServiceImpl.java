package com.pinenuts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinenuts.common.ResultCode;
import com.pinenuts.common.exception.BusinessException;
import com.pinenuts.dto.inventory.*;
import com.pinenuts.entity.InventoryAlert;
import com.pinenuts.entity.InventoryFlow;
import com.pinenuts.entity.InventoryItem;
import com.pinenuts.entity.Store;
import com.pinenuts.mapper.InventoryAlertMapper;
import com.pinenuts.mapper.InventoryFlowMapper;
import com.pinenuts.mapper.InventoryItemMapper;
import com.pinenuts.mapper.StoreMapper;
import com.pinenuts.security.LoginUser;
import com.pinenuts.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemMapper inventoryItemMapper;
    private final InventoryFlowMapper inventoryFlowMapper;
    private final InventoryAlertMapper inventoryAlertMapper;
    private final StoreMapper storeMapper;

    // --- 库存台账 CRUD ---

    @Override
    @Cacheable(value = "inventory", key = "'list_' + #query.hashCode() + '_' + #pageNum + '_' + #pageSize")
    public IPage<InventoryItemResponse> getInventoryList(InventoryItemQueryRequest query, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<InventoryItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getItemName()), InventoryItem::getItemName, query.getItemName())
                .eq(StringUtils.hasText(query.getCategory()), InventoryItem::getCategory, query.getCategory())
                .eq(query.getStoreId() != null, InventoryItem::getStoreId, query.getStoreId())
                .eq(query.getStatus() != null, InventoryItem::getStatus, query.getStatus());

        // 低库存筛选
        if (Boolean.TRUE.equals(query.getLowStock())) {
            wrapper.isNotNull(InventoryItem::getAlertThreshold)
                    .apply("quantity < alert_threshold");
        }

        wrapper.orderByDesc(InventoryItem::getCreatedAt);

        Page<InventoryItem> page = inventoryItemMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(this::convertToItemResponse);
    }

    @Override
    public InventoryItemResponse getInventoryById(Long id) {
        InventoryItem item = inventoryItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }
        return convertToItemResponse(item);
    }

    @Override
    @CacheEvict(value = "inventory", allEntries = true)
    public void createInventoryItem(InventoryItemCreateRequest request) {
        // 校验门店存在
        Store store = storeMapper.selectById(request.getStoreId());
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }

        // 校验编码唯一性：同 tenant_id + store_id + item_code 不能重复
        LambdaQueryWrapper<InventoryItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryItem::getStoreId, request.getStoreId())
                .eq(InventoryItem::getItemCode, request.getItemCode());
        if (inventoryItemMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.INVENTORY_CODE_DUPLICATE);
        }

        // 构建实体
        BigDecimal initQuantity = request.getQuantity() != null ? request.getQuantity() : BigDecimal.ZERO;
        InventoryItem item = InventoryItem.builder()
                .storeId(request.getStoreId())
                .itemName(request.getItemName())
                .itemCode(request.getItemCode())
                .category(request.getCategory())
                .unit(request.getUnit())
                .quantity(initQuantity)
                .costPrice(request.getCostPrice())
                .alertThreshold(request.getAlertThreshold())
                .status(1)
                .build();

        inventoryItemMapper.insert(item);
        log.info("新增物料成功: {}", request.getItemName());

        // 如果初始 quantity > 0，创建一条入库流水
        if (initQuantity.compareTo(BigDecimal.ZERO) > 0) {
            LoginUser loginUser = getCurrentUser();
            InventoryFlow flow = InventoryFlow.builder()
                    .tenantId(item.getTenantId())
                    .storeId(item.getStoreId())
                    .itemId(item.getId())
                    .flowType(1)
                    .sourceType("manual_in")
                    .quantity(initQuantity)
                    .beforeQuantity(BigDecimal.ZERO)
                    .afterQuantity(initQuantity)
                    .operatorId(loginUser.getUser().getId())
                    .operatorName(loginUser.getUsername())
                    .remark("新建物料初始入库")
                    .build();
            inventoryFlowMapper.insert(flow);
        }
    }

    @Override
    @CacheEvict(value = "inventory", allEntries = true)
    public void updateInventoryItem(Long id, InventoryItemUpdateRequest request) {
        InventoryItem item = inventoryItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        // 只更新非空字段（不允许修改 quantity）
        if (StringUtils.hasText(request.getItemName())) {
            item.setItemName(request.getItemName());
        }
        if (StringUtils.hasText(request.getCategory())) {
            item.setCategory(request.getCategory());
        }
        if (StringUtils.hasText(request.getUnit())) {
            item.setUnit(request.getUnit());
        }
        if (request.getCostPrice() != null) {
            item.setCostPrice(request.getCostPrice());
        }
        if (request.getAlertThreshold() != null) {
            item.setAlertThreshold(request.getAlertThreshold());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }

        inventoryItemMapper.updateById(item);
        log.info("更新物料成功: id={}", id);
    }

    @Override
    @CacheEvict(value = "inventory", allEntries = true)
    public void deleteInventoryItem(Long id) {
        InventoryItem item = inventoryItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        // 校验 quantity == 0 才允许删除
        if (item.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(ResultCode.INVENTORY_CANNOT_DELETE);
        }

        inventoryItemMapper.deleteById(id);
        log.info("删除物料成功: id={}", id);
    }

    // --- 入库/出库/盘点 ---

    @Override
    @Transactional
    @CacheEvict(value = "inventory", allEntries = true)
    public void inbound(InventoryInboundRequest request) {
        InventoryItem item = inventoryItemMapper.selectById(request.getItemId());
        if (item == null || item.getStatus() != 1) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        BigDecimal beforeQuantity = item.getQuantity();
        BigDecimal afterQuantity = beforeQuantity.add(request.getQuantity());

        // 更新库存
        item.setQuantity(afterQuantity);
        inventoryItemMapper.updateById(item);

        // 创建流水
        LoginUser loginUser = getCurrentUser();
        InventoryFlow flow = InventoryFlow.builder()
                .tenantId(item.getTenantId())
                .storeId(item.getStoreId())
                .itemId(item.getId())
                .flowType(1)
                .sourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType() : "manual_in")
                .quantity(request.getQuantity())
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .operatorId(loginUser.getUser().getId())
                .operatorName(loginUser.getUsername())
                .remark(request.getRemark())
                .build();
        inventoryFlowMapper.insert(flow);

        log.info("入库操作: 物料={}, 数量={}", item.getItemName(), request.getQuantity());
    }

    @Override
    @Transactional
    @CacheEvict(value = "inventory", allEntries = true)
    public void outbound(InventoryOutboundRequest request) {
        InventoryItem item = inventoryItemMapper.selectById(request.getItemId());
        if (item == null || item.getStatus() != 1) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        // 校验库存充足
        if (item.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new BusinessException(ResultCode.INVENTORY_INSUFFICIENT);
        }

        BigDecimal beforeQuantity = item.getQuantity();
        BigDecimal afterQuantity = beforeQuantity.subtract(request.getQuantity());

        // 更新库存
        item.setQuantity(afterQuantity);
        inventoryItemMapper.updateById(item);

        // 创建流水
        LoginUser loginUser = getCurrentUser();
        InventoryFlow flow = InventoryFlow.builder()
                .tenantId(item.getTenantId())
                .storeId(item.getStoreId())
                .itemId(item.getId())
                .flowType(2)
                .sourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType() : "manual_out")
                .quantity(request.getQuantity())
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .operatorId(loginUser.getUser().getId())
                .operatorName(loginUser.getUsername())
                .remark(request.getRemark())
                .build();
        inventoryFlowMapper.insert(flow);

        log.info("出库操作: 物料={}, 数量={}", item.getItemName(), request.getQuantity());
    }

    @Override
    @Transactional
    @CacheEvict(value = "inventory", allEntries = true)
    public void stockCheck(InventoryCheckRequest request) {
        InventoryItem item = inventoryItemMapper.selectById(request.getItemId());
        if (item == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        BigDecimal currentQuantity = item.getQuantity();
        BigDecimal diff = request.getActualQuantity().subtract(currentQuantity);

        // 如果差异为0则无需操作
        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // 更新库存
        item.setQuantity(request.getActualQuantity());
        inventoryItemMapper.updateById(item);

        // 创建流水
        LoginUser loginUser = getCurrentUser();
        InventoryFlow flow = InventoryFlow.builder()
                .tenantId(item.getTenantId())
                .storeId(item.getStoreId())
                .itemId(item.getId())
                .flowType(3)
                .sourceType("check_adjust")
                .quantity(diff)
                .beforeQuantity(currentQuantity)
                .afterQuantity(request.getActualQuantity())
                .operatorId(loginUser.getUser().getId())
                .operatorName(loginUser.getUsername())
                .remark(request.getRemark())
                .build();
        inventoryFlowMapper.insert(flow);

        log.info("盘点操作: 物料={}, 差异={}", item.getItemName(), diff);
    }

    // --- 流水与预警 ---

    @Override
    public IPage<InventoryFlowResponse> getFlowList(InventoryFlowQueryRequest query, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<InventoryFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getItemId() != null, InventoryFlow::getItemId, query.getItemId())
                .eq(query.getFlowType() != null, InventoryFlow::getFlowType, query.getFlowType());

        // 时间范围筛选
        if (StringUtils.hasText(query.getStartTime())) {
            LocalDateTime startTime = parseDateTime(query.getStartTime());
            wrapper.ge(InventoryFlow::getCreatedAt, startTime);
        }
        if (StringUtils.hasText(query.getEndTime())) {
            LocalDateTime endTime = parseDateTime(query.getEndTime());
            wrapper.le(InventoryFlow::getCreatedAt, endTime);
        }

        wrapper.orderByDesc(InventoryFlow::getCreatedAt);

        Page<InventoryFlow> page = inventoryFlowMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(this::convertToFlowResponse);
    }

    @Override
    public IPage<InventoryAlertResponse> getAlertList(Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<InventoryAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, InventoryAlert::getStatus, status)
                .orderByDesc(InventoryAlert::getCreatedAt);

        Page<InventoryAlert> page = inventoryAlertMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(this::convertToAlertResponse);
    }

    @Override
    @CacheEvict(value = "inventory", allEntries = true)
    public void handleAlert(Long id) {
        InventoryAlert alert = inventoryAlertMapper.selectById(id);
        if (alert == null) {
            throw new BusinessException(ResultCode.INVENTORY_ALERT_NOT_FOUND);
        }
        alert.setStatus(1);
        inventoryAlertMapper.updateById(alert);
        log.info("预警处理完成: id={}", id);
    }

    // --- 私有方法 ---

    private LoginUser getCurrentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getStoreName(Long storeId) {
        if (storeId == null) {
            return "";
        }
        Store store = storeMapper.selectById(storeId);
        return store != null ? store.getStoreName() : "";
    }

    private InventoryItemResponse convertToItemResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .storeId(item.getStoreId())
                .storeName(getStoreName(item.getStoreId()))
                .itemName(item.getItemName())
                .itemCode(item.getItemCode())
                .category(item.getCategory())
                .unit(item.getUnit())
                .quantity(item.getQuantity())
                .costPrice(item.getCostPrice())
                .alertThreshold(item.getAlertThreshold())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private InventoryFlowResponse convertToFlowResponse(InventoryFlow flow) {
        String itemName = "";
        InventoryItem item = inventoryItemMapper.selectById(flow.getItemId());
        if (item != null) {
            itemName = item.getItemName();
        }

        return InventoryFlowResponse.builder()
                .id(flow.getId())
                .itemId(flow.getItemId())
                .itemName(itemName)
                .flowType(flow.getFlowType())
                .sourceType(flow.getSourceType())
                .quantity(flow.getQuantity())
                .beforeQuantity(flow.getBeforeQuantity())
                .afterQuantity(flow.getAfterQuantity())
                .operatorName(flow.getOperatorName())
                .remark(flow.getRemark())
                .createdAt(flow.getCreatedAt())
                .build();
    }

    private InventoryAlertResponse convertToAlertResponse(InventoryAlert alert) {
        return InventoryAlertResponse.builder()
                .id(alert.getId())
                .storeId(alert.getStoreId())
                .storeName(getStoreName(alert.getStoreId()))
                .itemId(alert.getItemId())
                .itemName(alert.getItemName())
                .currentQuantity(alert.getCurrentQuantity())
                .alertThreshold(alert.getAlertThreshold())
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String timeStr) {
        if (timeStr.length() == 10) {
            // yyyy-MM-dd 格式
            return LocalDateTime.parse(timeStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
