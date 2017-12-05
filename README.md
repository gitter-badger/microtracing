# Microtracing
基于javaagent、span(see Google Dapper)和slf4j实现的分布式全链路调用日志跟踪框架

## 核心功能
- 跟踪用户请求在服务器端处理全过程，包括跨多个应用多级服务调用，便于故障诊断和性能分析
- 将各应用输出日志与用户请求关联，串联用户一次请求输出的所有日志，便于进行问题根源分析

## 基本思路
- 为每次用户请求生成全局唯一的traceId
- 将traceId在服务器端传递至调用链中所有应用服务
- 每个应用服务的处理过程（称为Span）均生成全局唯一的spanId，记录traceId, spanId, startTime, endTime
- 每次远程服务调用过程（称为RPC Span）均生成全局唯一的rpc spanId
    - 调用方记录traceId, parentId（调用方当前处理过程spanId）, rpc spanId, client send time, client recive time
    - 服务方记录traceId, rpc spanId, server recive time, server send time. 服务方处理过程Span的parentId设为rpc spanId
- 标准化应用日志输出格式，输出当前traceId和spanId

## 模块说明
- **tracespan**: 实现tracing span的核心代码，并包含用于web应用的TraceFilter
- **logagent**: 使用javaagent和javassit技术自动注入日志和跟踪代码
- **logtrace**: 用于将tracespan、logagent及相关依赖库打包集成
- **demo**: 演示程序

## 编译及演示

```
mvn clean package
demo.bat
```

