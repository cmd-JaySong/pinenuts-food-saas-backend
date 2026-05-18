package com.pinenuts.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinenuts.entity.InventoryAlert;
import com.pinenuts.entity.InventoryItem;
import com.pinenuts.mapper.InventoryAlertMapper;
import com.pinenuts.mapper.InventoryItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAlertTask {

    private final InventoryItemMapper inventoryItemMapper;
    private final InventoryAlertMapper inventoryAlertMapper;

    /**
     * 每日8点扫描低库存物料并生成预警
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scanLowInventory() {
        log.info("开始执行库存预警扫描任务...");

        // 查询所有 alert_threshold IS NOT NULL 且 quantity < alert_threshold 且 status=1 的物料
        // 定时任务无请求上下文，TenantContext 为 null，拦截器会跳过租户过滤
        LambdaQueryWrapper<InventoryItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(InventoryItem::getAlertThreshold)
                .apply("quantity < alert_threshold")
                .eq(InventoryItem::getStatus, 1);

        List<InventoryItem> lowStockItems = inventoryItemMapper.selectList(wrapper);

        if (lowStockItems.isEmpty()) {
            log.info("库存预警扫描完成，无低库存物料");
            return;
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        int alertCount = 0;

        for (InventoryItem item : lowStockItems) {
            // 检查今日是否已存在该物料的未处理预警（避免重复）
            LambdaQueryWrapper<InventoryAlert> alertWrapper = new LambdaQueryWrapper<>();
            alertWrapper.eq(InventoryAlert::getItemId, item.getId())
                    .eq(InventoryAlert::getStatus, 0)
                    .ge(InventoryAlert::getCreatedAt, todayStart);

            Long existCount = inventoryAlertMapper.selectCount(alertWrapper);
            if (existCount > 0) {
                continue;
            }

            // 创建预警记录
            InventoryAlert alert = InventoryAlert.builder()
                    .tenantId(item.getTenantId())
                    .storeId(item.getStoreId())
                    .itemId(item.getId())
                    .itemName(item.getItemName())
                    .currentQuantity(item.getQuantity())
                    .alertThreshold(item.getAlertThreshold())
                    .status(0)
                    .build();

            inventoryAlertMapper.insert(alert);
            alertCount++;
        }

        log.info("库存预警扫描完成，发现 {} 个低库存物料，新增 {} 条预警记录", lowStockItems.size(), alertCount);
    }
}
