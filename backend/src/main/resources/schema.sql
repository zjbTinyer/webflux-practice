-- WebFlux 练习项目 - 数据库初始化脚本
-- R2DBC 会在启动时自动执行此脚本

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '用户名',
    email VARCHAR(200) NOT NULL COMMENT '邮箱',
    age INT COMMENT '年龄',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);
