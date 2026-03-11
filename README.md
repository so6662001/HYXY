# HYXY — 钢铁行业交易平台

## 项目说明

钢铁行业 B2B 询报价交易平台，包含企业信用评级与风险预警系统。

## 模块

| 模块 | 目录 | 技术栈 | 说明 |
|------|------|--------|------|
| 后端服务 | [steel-credit/](steel-credit/) | Java 17 + Spring Boot 3.2 + MyBatis-Plus | 信用评分引擎、风险预警、ERP同步、API |
| 前端应用 | [steel-credit-ui/](steel-credit-ui/) | Vue3 + TypeScript + Element Plus + ECharts | 信用名片、详情页、风险预警、评价表单 |

## 文档

- [后端 README](steel-credit/README.md) — 快速开始、技术栈、API 一览、项目结构
- [前端 README](steel-credit-ui/README.md) — 快速开始、页面说明、组件结构
- [详细设计文档](steel-credit/docs/DESIGN.md) — 业务背景、评分模型、数据流转、前端对接指南
- [API 接口文档](steel-credit/docs/API.md) — 完整的接口说明与请求/响应示例
- [数据库设计文档](steel-credit/docs/DATABASE.md) — 17 张表的字段说明与 ER 关系
