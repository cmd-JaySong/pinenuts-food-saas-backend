# 松籽餐饮数字化管理平台 - 后端服务

面向中小餐饮连锁企业的 SaaS 管理平台后端，基于 Spring Boot 3 构建，提供 RESTful API 服务。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 运行环境 |
| Spring Boot | 3.3.5 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7 | 缓存 |
| SpringDoc OpenAPI | 2.6.0 | API 文档 |
| Lombok | - | 代码简化 |

## 项目结构

```
src/main/java/com/pinenuts/
├── common/                # 公共模块（统一响应、异常处理、错误码）
├── config/                # 配置类（CORS、MyBatis-Plus 等）
├── controller/            # 控制器层（接口定义）
├── service/               # 业务逻辑层
├── mapper/                # 数据访问层
├── entity/                # 实体类
└── PinenutsFoodSaasApplication.java  # 启动类
```

## 本地开发

### 环境要求

- Java 21
- Maven 3.9+
- MySQL 8.0（运行中）
- Redis 7（运行中）

### 启动步骤

1. 确保 MySQL 和 Redis 服务已启动

2. 修改 `src/main/resources/application-dev.yml` 中的数据库配置，或通过环境变量设置：
   ```bash
   export MYSQL_HOST=localhost
   export MYSQL_PORT=3306
   export MYSQL_DATABASE=pinenuts_food
   export MYSQL_USERNAME=root
   export MYSQL_PASSWORD=root123456
   ```

3. 初始化数据库（首次启动）：
   ```bash
   mysql -u root -p < sql/init.sql
   ```

4. 启动服务：
   ```bash
   mvn spring-boot:run
   ```

5. 验证服务：访问 http://localhost:8080/api/health

6. API 文档：访问 http://localhost:8080/swagger-ui.html

## Docker 启动

参考根目录 `docker-compose.yml`，一键启动全部服务：

```bash
docker-compose up -d
```

## Git 提交规范

| 前缀 | 说明 |
|------|------|
| `feat:` | 新功能 |
| `fix:` | 修复 Bug |
| `refactor:` | 代码重构 |
| `docs:` | 文档变更 |
| `style:` | 代码格式调整 |
| `test:` | 测试相关 |
| `chore:` | 构建/工具变更 |

示例：`feat: 添加门店管理 CRUD 接口`
