-- 松籽餐饮数字化管理平台 - 数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `pinenuts_food` 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `pinenuts_food`;

-- ============================================
-- 第二周：认证与权限模块表结构
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=正常 0=禁用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=正常 0=禁用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 权限表（菜单+操作）
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID，0表示顶级',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码，如 store:list',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `type` TINYINT NOT NULL COMMENT '类型：1=菜单 2=按钮/操作',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=正常 0=禁用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================
-- 初始数据
-- ============================================

-- 初始角色
INSERT INTO `sys_role` (`id`, `tenant_id`, `role_code`, `role_name`, `description`) VALUES
(1, 0, 'SUPER_ADMIN', '超级管理员', '拥有系统所有权限'),
(2, 0, 'STORE_MANAGER', '门店管理员', '管理单个门店的日常运营'),
(3, 0, 'STAFF', '普通员工', '基础操作权限');

-- 初始用户（密码 admin123 的 BCrypt 加密）
INSERT INTO `sys_user` (`id`, `tenant_id`, `username`, `password`, `nickname`, `status`) VALUES
(1, 0, 'admin', '$2a$10$EqKcp1WFKVQISheBxmTxaOLSleDfBMOiMr0gFGuhSEJBEesMsSXoW', '超级管理员', 1);

-- 用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 权限菜单（type=1 菜单, type=2 按钮操作）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `icon`, `sort_order`) VALUES
-- 顶级菜单
(1, 0, 'dashboard', '首页', 1, '/', 'HomeFilled', 1),
(2, 0, 'store', '门店管理', 1, '/store', 'OfficeBuilding', 2),
(3, 0, 'dish', '菜品管理', 1, '/dish', 'Food', 3),
(4, 0, 'inventory', '库存管理', 1, '/inventory', 'Box', 4),
(5, 0, 'purchase', '采购管理', 1, '/purchase', 'ShoppingCart', 5),
(6, 0, 'report', '营收报表', 1, '/report', 'DataAnalysis', 6),
(7, 0, 'ai', 'AI 助手', 1, '/ai', 'ChatDotRound', 7),
-- 门店管理子操作
(10, 2, 'store:list', '门店列表', 2, NULL, NULL, 1),
(11, 2, 'store:create', '新增门店', 2, NULL, NULL, 2),
(12, 2, 'store:update', '编辑门店', 2, NULL, NULL, 3),
(13, 2, 'store:delete', '删除门店', 2, NULL, NULL, 4),
-- 菜品管理子操作
(20, 3, 'dish:list', '菜品列表', 2, NULL, NULL, 1),
(21, 3, 'dish:create', '新增菜品', 2, NULL, NULL, 2),
(22, 3, 'dish:update', '编辑菜品', 2, NULL, NULL, 3),
(23, 3, 'dish:delete', '删除菜品', 2, NULL, NULL, 4),
-- 库存管理子操作
(30, 4, 'inventory:list', '库存列表', 2, NULL, NULL, 1),
(31, 4, 'inventory:inbound', '入库操作', 2, NULL, NULL, 2),
(32, 4, 'inventory:outbound', '出库操作', 2, NULL, NULL, 3),
-- 采购管理子操作
(40, 5, 'purchase:list', '采购列表', 2, NULL, NULL, 1),
(41, 5, 'purchase:create', '新建采购单', 2, NULL, NULL, 2),
(42, 5, 'purchase:approve', '审批操作', 2, NULL, NULL, 3);

-- 超级管理员拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),
(1,10),(1,11),(1,12),(1,13),
(1,20),(1,21),(1,22),(1,23),
(1,30),(1,31),(1,32),
(1,40),(1,41),(1,42);

-- 门店管理员权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2,1),(2,2),(2,3),(2,4),(2,5),(2,6),
(2,10),(2,20),(2,21),(2,22),(2,30),(2,31),(2,32),(2,40),(2,41);

-- 普通员工权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3,1),(3,3),(3,4),(3,20),(3,30);

-- ============================================
-- Week 3: 门店与员工管理
-- ============================================

-- 门店表
CREATE TABLE IF NOT EXISTS `t_store` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `store_code` VARCHAR(50) NOT NULL COMMENT '门店编号',
  `store_name` VARCHAR(100) NOT NULL COMMENT '门店名称',
  `address` VARCHAR(255) COMMENT '门店地址',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `business_hours` VARCHAR(100) COMMENT '营业时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-营业中 0-停业',
  `deleted` TINYINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_tenant_store_code` (`tenant_id`, `store_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';

-- 员工表
CREATE TABLE IF NOT EXISTS `t_staff` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `store_id` BIGINT NOT NULL COMMENT '所属门店',
  `user_id` BIGINT COMMENT '关联系统用户（可选）',
  `staff_name` VARCHAR(50) NOT NULL COMMENT '员工姓名',
  `phone` VARCHAR(20) COMMENT '手机号',
  `position` VARCHAR(50) COMMENT '职位',
  `entry_date` DATE COMMENT '入职日期',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-在职 0-离职',
  `deleted` TINYINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_store_id` (`store_id`),
  UNIQUE KEY `uk_tenant_store_phone` (`tenant_id`, `store_id`, `phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 调整菜单排序：为员工管理腾出位置（插入到门店管理之后）
UPDATE `sys_permission` SET `sort_order` = `sort_order` + 1 WHERE `type` = 1 AND `sort_order` >= 3 AND `parent_id` = 0;

-- 员工管理顶级菜单
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `icon`, `sort_order`, `status`, `deleted`) VALUES
(8, 0, 'staff', '员工管理', 1, '/staff', 'User', 3, 1, 0);

-- 员工管理子权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `icon`, `sort_order`, `status`, `deleted`) VALUES
(14, 8, 'staff:list', '查看员工', 2, NULL, NULL, 1, 1, 0),
(15, 8, 'staff:create', '新增员工', 2, NULL, NULL, 2, 1, 0),
(16, 8, 'staff:update', '编辑员工', 2, NULL, NULL, 3, 1, 0),
(17, 8, 'staff:delete', '删除员工', 2, NULL, NULL, 4, 1, 0);

-- 为超级管理员角色分配员工管理权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 8), (1, 14), (1, 15), (1, 16), (1, 17);

-- 为门店管理员角色分配员工管理权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 8), (2, 14), (2, 15), (2, 16), (2, 17);

-- ============================================
-- Week 4: 菜品管理
-- ============================================

-- 菜品分类表（支持递归树）
CREATE TABLE IF NOT EXISTS t_dish_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0为顶级',
  category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tenant_parent_name (tenant_id, parent_id, category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 菜品表
CREATE TABLE IF NOT EXISTS t_dish (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL COMMENT '所属分类',
  dish_name VARCHAR(100) NOT NULL COMMENT '菜品名称',
  dish_code VARCHAR(50) COMMENT '菜品编号',
  price DECIMAL(10,2) NOT NULL COMMENT '价格',
  image_url VARCHAR(500) COMMENT '菜品图片URL',
  description VARCHAR(500) COMMENT '菜品描述',
  specifications JSON COMMENT '规格信息JSON',
  status TINYINT DEFAULT 1 COMMENT '状态：1-上架 0-下架',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category_id (category_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 更新菜品管理菜单路径
UPDATE sys_permission SET path = '/dish' WHERE permission_code = 'dish' AND type = 1;

-- ============================================
-- Week 5: 库存管理
-- ============================================

-- 库存物料表
CREATE TABLE IF NOT EXISTS `t_inventory_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `store_id` BIGINT NOT NULL COMMENT '所属门店ID',
    `item_name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `item_code` VARCHAR(50) NOT NULL COMMENT '物料编码',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '物料分类（蔬菜/肉类/调味品/粮油等）',
    `unit` VARCHAR(20) NOT NULL COMMENT '计量单位（斤/个/瓶/箱）',
    `quantity` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '当前库存量',
    `cost_price` DECIMAL(10,2) DEFAULT NULL COMMENT '成本单价',
    `alert_threshold` DECIMAL(10,2) DEFAULT NULL COMMENT '预警阈值',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-正常 0-停用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_tenant_store_code` (`tenant_id`, `store_id`, `item_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存物料表';

-- 库存流水表
CREATE TABLE IF NOT EXISTS `t_inventory_flow` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `store_id` BIGINT NOT NULL COMMENT '门店ID',
    `item_id` BIGINT NOT NULL COMMENT '关联库存物料ID',
    `flow_type` TINYINT NOT NULL COMMENT '流水类型 1-入库 2-出库 3-盘点调整',
    `source_type` VARCHAR(30) DEFAULT NULL COMMENT '来源类型（purchase_in/manual_in/sale_out/loss_out/check_adjust）',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '变动数量（正数入/负数出）',
    `before_quantity` DECIMAL(10,2) NOT NULL COMMENT '变动前库存',
    `after_quantity` DECIMAL(10,2) NOT NULL COMMENT '变动后库存',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_item_id` (`item_id`),
    KEY `idx_store_id` (`store_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- 库存预警记录表
CREATE TABLE IF NOT EXISTS `t_inventory_alert` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `store_id` BIGINT NOT NULL COMMENT '门店ID',
    `item_id` BIGINT NOT NULL COMMENT '关联库存物料ID',
    `item_name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `current_quantity` DECIMAL(10,2) NOT NULL COMMENT '当前库存量',
    `alert_threshold` DECIMAL(10,2) NOT NULL COMMENT '预警阈值',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-未处理 1-已处理',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_item_id` (`item_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警记录表';

-- 补充库存预警权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `icon`, `sort_order`) VALUES
(33, 4, 'inventory:alert', '库存预警', 2, NULL, NULL, 4);

-- 给超级管理员和门店管理员分配库存预警权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES (1, 33), (2, 33);

-- ===================== 第六周：采购管理 =====================

-- 采购单主表
CREATE TABLE IF NOT EXISTS `t_purchase_order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `store_id` BIGINT NOT NULL COMMENT '门店ID',
    `purchase_code` VARCHAR(50) NOT NULL COMMENT '采购单号（系统生成）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-草稿 1-待审批 2-已完成 3-已驳回',
    `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '采购总金额',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '申请备注',
    `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
    `applicant_name` VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `approver_name` VARCHAR(50) DEFAULT NULL COMMENT '审批人姓名',
    `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approval_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_tenant_purchase_code` (`tenant_id`, `purchase_code`),
    KEY `idx_store_id` (`store_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购单主表';

-- 采购明细表
CREATE TABLE IF NOT EXISTS `t_purchase_order_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `order_id` BIGINT NOT NULL COMMENT '关联采购单ID',
    `item_id` BIGINT DEFAULT NULL COMMENT '关联库存物料ID（可为空，表示新物料）',
    `item_name` VARCHAR(100) NOT NULL COMMENT '物料名称（冗余存储）',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '采购数量',
    `unit_price` DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    `total_price` DECIMAL(12,2) DEFAULT NULL COMMENT '小计',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购明细表';

-- 采购审批日志表
CREATE TABLE IF NOT EXISTS `t_purchase_approval_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `order_id` BIGINT NOT NULL COMMENT '关联采购单ID',
    `action` TINYINT NOT NULL COMMENT '1-创建 2-提交审批 3-撤回 4-审批通过 5-审批驳回',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '操作备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购审批日志表';

-- 补充采购操作权限
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `icon`, `sort_order`) VALUES
(43, 5, 'purchase:submit', '提交审批', 2, NULL, NULL, 3),
(44, 5, 'purchase:withdraw', '撤回申请', 2, NULL, NULL, 4),
(45, 5, 'purchase:delete', '删除采购单', 2, NULL, NULL, 5);

-- 为超级管理员和门店管理员分配新增的采购权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 43), (1, 44), (1, 45),
(2, 40), (2, 41), (2, 42), (2, 43), (2, 44), (2, 45);

-- 后续周次将在此添加表结构
-- Week 7: 销售记录表
