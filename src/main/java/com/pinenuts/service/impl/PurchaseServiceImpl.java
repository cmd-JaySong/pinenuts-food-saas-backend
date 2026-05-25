package com.pinenuts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinenuts.common.ResultCode;
import com.pinenuts.common.exception.BusinessException;
import com.pinenuts.dto.inventory.InventoryInboundRequest;
import com.pinenuts.dto.purchase.*;
import com.pinenuts.entity.PurchaseApprovalLog;
import com.pinenuts.entity.PurchaseOrder;
import com.pinenuts.entity.PurchaseOrderItem;
import com.pinenuts.entity.Store;
import com.pinenuts.mapper.PurchaseApprovalLogMapper;
import com.pinenuts.mapper.PurchaseOrderItemMapper;
import com.pinenuts.mapper.PurchaseOrderMapper;
import com.pinenuts.mapper.StoreMapper;
import com.pinenuts.security.LoginUser;
import com.pinenuts.service.InventoryService;
import com.pinenuts.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final PurchaseApprovalLogMapper purchaseApprovalLogMapper;
    private final StoreMapper storeMapper;
    private final InventoryService inventoryService;

    @Override
    public IPage<PurchaseOrderListResponse> getList(PurchaseOrderQueryRequest query, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getPurchaseCode()), PurchaseOrder::getPurchaseCode, query.getPurchaseCode())
                .eq(query.getStoreId() != null, PurchaseOrder::getStoreId, query.getStoreId())
                .eq(query.getStatus() != null, PurchaseOrder::getStatus, query.getStatus());

        if (StringUtils.hasText(query.getStartTime())) {
            LocalDateTime startTime = parseDateTime(query.getStartTime());
            wrapper.ge(PurchaseOrder::getCreatedAt, startTime);
        }
        if (StringUtils.hasText(query.getEndTime())) {
            LocalDateTime endTime = parseDateTime(query.getEndTime());
            wrapper.le(PurchaseOrder::getCreatedAt, endTime);
        }

        wrapper.orderByDesc(PurchaseOrder::getCreatedAt);

        Page<PurchaseOrder> page = purchaseOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(this::convertToListResponse);
    }

    @Override
    public PurchaseOrderResponse getById(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.PURCHASE_NOT_FOUND);
        }

        // 查询明细列表
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id)
        );

        // 查询审批日志
        List<PurchaseApprovalLog> logs = purchaseApprovalLogMapper.selectList(
                new LambdaQueryWrapper<PurchaseApprovalLog>()
                        .eq(PurchaseApprovalLog::getOrderId, id)
                        .orderByAsc(PurchaseApprovalLog::getCreatedAt)
        );

        // 组装响应
        List<PurchaseOrderItemDTO> itemDTOs = items.stream().map(this::convertToItemDTO).collect(Collectors.toList());
        List<PurchaseApprovalLogResponse> logResponses = logs.stream().map(this::convertToLogResponse).collect(Collectors.toList());

        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .purchaseCode(order.getPurchaseCode())
                .storeId(order.getStoreId())
                .storeName(getStoreName(order.getStoreId()))
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .remark(order.getRemark())
                .applicantId(order.getApplicantId())
                .applicantName(order.getApplicantName())
                .approverName(order.getApproverName())
                .approvalTime(order.getApprovalTime())
                .approvalRemark(order.getApprovalRemark())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .approvalLogs(logResponses)
                .build();
    }

    @Override
    @Transactional
    public void create(PurchaseOrderCreateRequest request) {
        LoginUser loginUser = getCurrentUser();

        // 生成采购单号
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseOrderMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .likeRight(PurchaseOrder::getPurchaseCode, "PO-" + dateStr + "-")
        );
        String purchaseCode = "PO-" + dateStr + "-" + String.format("%04d", count + 1);

        // 计算总金额
        BigDecimal totalAmount = calculateTotalAmount(request.getItems());

        // 保存主表
        PurchaseOrder order = PurchaseOrder.builder()
                .storeId(request.getStoreId())
                .purchaseCode(purchaseCode)
                .status(0)
                .totalAmount(totalAmount)
                .remark(request.getRemark())
                .applicantId(loginUser.getUser().getId())
                .applicantName(loginUser.getUsername())
                .build();
        purchaseOrderMapper.insert(order);

        // 批量保存明细行
        Long tenantId = loginUser.getUser().getTenantId();
        for (PurchaseOrderItemDTO itemDTO : request.getItems()) {
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .tenantId(tenantId)
                    .orderId(order.getId())
                    .itemId(itemDTO.getItemId())
                    .itemName(itemDTO.getItemName())
                    .unit(itemDTO.getUnit())
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(itemDTO.getUnitPrice())
                    .totalPrice(calculateItemTotal(itemDTO))
                    .build();
            purchaseOrderItemMapper.insert(item);
        }

        // 创建审批日志
        createApprovalLog(order.getId(), 1, "创建采购单");

        log.info("创建采购单: {}", purchaseCode);
    }

    @Override
    @Transactional
    public void update(Long id, PurchaseOrderUpdateRequest request) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.PURCHASE_NOT_FOUND);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PURCHASE_INVALID_STATUS);
        }

        // 更新备注
        if (request.getRemark() != null) {
            order.setRemark(request.getRemark());
        }

        // 删除旧明细
        purchaseOrderItemMapper.delete(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id)
        );

        // 重新插入新明细行
        LoginUser loginUser = getCurrentUser();
        Long tenantId = loginUser.getUser().getTenantId();
        for (PurchaseOrderItemDTO itemDTO : request.getItems()) {
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .tenantId(tenantId)
                    .orderId(id)
                    .itemId(itemDTO.getItemId())
                    .itemName(itemDTO.getItemName())
                    .unit(itemDTO.getUnit())
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(itemDTO.getUnitPrice())
                    .totalPrice(calculateItemTotal(itemDTO))
                    .build();
            purchaseOrderItemMapper.insert(item);
        }

        // 重算总金额并更新主表
        order.setTotalAmount(calculateTotalAmount(request.getItems()));
        purchaseOrderMapper.updateById(order);

        log.info("更新采购单: id={}", id);
    }

    @Override
    public void delete(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.PURCHASE_NOT_FOUND);
        }
        if (order.getStatus() != 0 && order.getStatus() != 3) {
            throw new BusinessException(ResultCode.PURCHASE_INVALID_STATUS);
        }

        // 逻辑删除主表
        purchaseOrderMapper.deleteById(id);

        // 逻辑删除关联明细
        purchaseOrderItemMapper.delete(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id)
        );

        log.info("删除采购单: id={}", id);
    }

    @Override
    public void submit(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.PURCHASE_NOT_FOUND);
        }
        if (order.getStatus() != 0 && order.getStatus() != 3) {
            throw new BusinessException(ResultCode.PURCHASE_INVALID_STATUS);
        }

        // 校验明细非空
        long itemCount = purchaseOrderItemMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id)
        );
        if (itemCount == 0) {
            throw new BusinessException(ResultCode.PURCHASE_ITEMS_EMPTY);
        }

        // 更新状态
        order.setStatus(1);
        purchaseOrderMapper.updateById(order);

        // 创建审批日志
        createApprovalLog(order.getId(), 2, "提交审批");

        log.info("提交采购单审批: id={}", id);
    }

    @Override
    public void withdraw(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.PURCHASE_NOT_FOUND);
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PURCHASE_INVALID_STATUS);
        }

        // 校验当前用户 = 申请人
        LoginUser loginUser = getCurrentUser();
        if (!loginUser.getUser().getId().equals(order.getApplicantId())) {
            throw new BusinessException(ResultCode.PURCHASE_NO_PERMISSION);
        }

        // 更新状态
        order.setStatus(0);
        purchaseOrderMapper.updateById(order);

        // 创建审批日志
        createApprovalLog(order.getId(), 3, "撤回申请");

        log.info("撤回采购单: id={}", id);
    }

    @Override
    @Transactional
    public void approve(Long id, PurchaseApproveRequest request) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.PURCHASE_NOT_FOUND);
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PURCHASE_INVALID_STATUS);
        }

        // 遍历明细行，对每个 itemId 非空的明细，调用库存入库
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id)
        );
        for (PurchaseOrderItem item : items) {
            if (item.getItemId() != null) {
                InventoryInboundRequest inReq = new InventoryInboundRequest();
                inReq.setItemId(item.getItemId());
                inReq.setQuantity(item.getQuantity());
                inReq.setSourceType("purchase_in");
                inReq.setRemark("采购单: " + order.getPurchaseCode());
                inventoryService.inbound(inReq);
            }
        }

        // 更新采购单
        LoginUser loginUser = getCurrentUser();
        order.setStatus(2);
        order.setApproverId(loginUser.getUser().getId());
        order.setApproverName(loginUser.getUsername());
        order.setApprovalTime(LocalDateTime.now());
        order.setApprovalRemark(request.getRemark());
        purchaseOrderMapper.updateById(order);

        // 创建审批日志
        createApprovalLog(order.getId(), 4, request.getRemark());

        log.info("审批通过采购单: {}, 已触发库存入库", order.getPurchaseCode());
    }

    @Override
    public void reject(Long id, PurchaseApproveRequest request) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.PURCHASE_NOT_FOUND);
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PURCHASE_INVALID_STATUS);
        }

        // 更新采购单
        LoginUser loginUser = getCurrentUser();
        order.setStatus(3);
        order.setApproverId(loginUser.getUser().getId());
        order.setApproverName(loginUser.getUsername());
        order.setApprovalTime(LocalDateTime.now());
        order.setApprovalRemark(request.getRemark());
        purchaseOrderMapper.updateById(order);

        // 创建审批日志
        createApprovalLog(order.getId(), 5, request.getRemark());

        log.info("审批驳回采购单: id={}", id);
    }

    // --- 私有方法 ---

    private LoginUser getCurrentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void createApprovalLog(Long orderId, Integer action, String remark) {
        LoginUser user = getCurrentUser();
        PurchaseApprovalLog approvalLog = PurchaseApprovalLog.builder()
                .tenantId(user.getUser().getTenantId())
                .orderId(orderId)
                .action(action)
                .operatorId(user.getUser().getId())
                .operatorName(user.getUsername())
                .remark(remark)
                .build();
        purchaseApprovalLogMapper.insert(approvalLog);
    }

    private String getStoreName(Long storeId) {
        if (storeId == null) {
            return "";
        }
        Store store = storeMapper.selectById(storeId);
        return store != null ? store.getStoreName() : "";
    }

    private BigDecimal calculateTotalAmount(List<PurchaseOrderItemDTO> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemDTO item : items) {
            if (item.getUnitPrice() != null && item.getQuantity() != null) {
                total = total.add(item.getQuantity().multiply(item.getUnitPrice()));
            }
        }
        return total;
    }

    private BigDecimal calculateItemTotal(PurchaseOrderItemDTO itemDTO) {
        if (itemDTO.getUnitPrice() != null && itemDTO.getQuantity() != null) {
            return itemDTO.getQuantity().multiply(itemDTO.getUnitPrice());
        }
        return null;
    }

    private PurchaseOrderListResponse convertToListResponse(PurchaseOrder order) {
        return PurchaseOrderListResponse.builder()
                .id(order.getId())
                .purchaseCode(order.getPurchaseCode())
                .storeId(order.getStoreId())
                .storeName(getStoreName(order.getStoreId()))
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .remark(order.getRemark())
                .applicantId(order.getApplicantId())
                .applicantName(order.getApplicantName())
                .approverName(order.getApproverName())
                .approvalTime(order.getApprovalTime())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private PurchaseOrderItemDTO convertToItemDTO(PurchaseOrderItem item) {
        PurchaseOrderItemDTO dto = new PurchaseOrderItemDTO();
        dto.setId(item.getId());
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setUnit(item.getUnit());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }

    private PurchaseApprovalLogResponse convertToLogResponse(PurchaseApprovalLog logEntry) {
        return PurchaseApprovalLogResponse.builder()
                .id(logEntry.getId())
                .action(logEntry.getAction())
                .operatorName(logEntry.getOperatorName())
                .remark(logEntry.getRemark())
                .createdAt(logEntry.getCreatedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String timeStr) {
        if (timeStr.length() == 10) {
            return LocalDateTime.parse(timeStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
