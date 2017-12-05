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
  - 调用方记录traceId, parentId（调用方当前处理过程spanId）, rpc spanId, client send time, client receive time
  - 服务方记录traceId, rpc spanId, server receive time, server send time. 服务方处理过程Span的parentId设为rpc spanId
- 标准化应用日志输出格式，输出当前traceId和spanId

## 模块说明
- **tracespan**: 实现tracing span的核心代码，并包含用于web应用的TraceFilter
- **logagent**: 使用javaagent和javassit技术自动注入日志和跟踪代码
- **logtrace**: 用于将tracespan、logagent及相关依赖库打包集成
- **demo**: 演示程序

## 配置文件
- **logtrace.properties**: 配置需要跟踪的包、类、方法及启用的特性等
  - 默认配置: logagent\src\main\resources\logtrace.properties
  - 演示配置: demo\src\main\resources\logtrace.properties
- **log4j2.xml**: 配置日志输出策略、文件及格式等
  - 默认配置: tracespan\src\main\resources\log4j2.xml
  - 演示配置: demo\src\main\resources\log4j2.xml
  - 默认输出日志文件: logs\logtrace.log
  
## 编译及演示

```
mvn clean package
demo.bat
```

## 安装部署

### Weblogic应用服务器
- 在${DOMAIN_HOME}下建立logtrace目录，复制以下文件至logtrace目录：

```
logtrace-*-jar-with-dependencies.jar (from logtrace\target\)
logtrace.properties (from logagent\src\main\resources\)
log4j2.xml (from tracespan\src\main\resources\)
```

- 配置Weblogic Server启动参数

  - **方法一：** 通过Webligc Administrator Console配置相应server的服务器启动参数，增加：
  
  > -javaagent:${DOMAIN_HOME}/logtrace/logtrace-{VERSION}-jar-with-dependencies.jar=${DOMAIN_HOME}/logtrace/logtrace.properties -Dlog4j.configurationFile=${DOMAIN_HOME}/logtrace/log4j2.xml  -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector

  - **方法二：** 直接修改启动脚本，修改JAVA_OPTIONS增加启动参数，例如修改bin/startWeblogic.sh：
  
  > JAVA_OPTIONS=" -javaagent:${DOMAIN_HOME}/logtrace/logtrace-{VERSION}-jar-with-dependencies.jar=${DOMAIN_HOME}/logtrace/logtrace.properties -Dlog4j.configurationFile=${DOMAIN_HOME}/logtrace/log4j2.xml  -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector ${SAVE_JAVA_OPTIONS} "	  

- 从webapp的WEB-INF/lib下移除所有日志库，如commons-logging*.jar, log4j*.jar等（已在logtrace-{VERSION}-jar-with-dependencies.jar中包含）
- 框架自动追踪weblogic server调用servlet，无需配置filter

*注：{VERSION}修改为实际版本号*

### 其他应用服务器
- 在应用服务器工作目录下建立logtrace目录，复制以下文件至logtrace目录：

```
logtrace-*-jar-with-dependencies.jar (from logtrace\target\)
logtrace.properties (from logagent\src\main\resources\)
log4j2.xml (from tracespan\src\main\resources\)
```

- 修改应用服务器启动脚本，在java之后添加启动参数：

>  -javaagent:{ABSOLUTE PATH}/logtrace/logtrace-{VERSION}-jar-with-dependencies.jar={ABSOLUTE PATH}/logtrace/logtrace.properties -Dlog4j.configurationFile=<ABSOLUTE PATH>/logtrace/log4j2.xml  -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector

*注：{ABSOLUTE PATH}修改为logtrace目录所在绝对路径； {VERSION}修改为实际版本号*

- 配置webapp增加TraceFilter
  - 复制`tracespan\target\tracespan-*.jar`及依赖库(from tracespan\target\lib)至webapp的lib目录
  - 复制`tracespan\src\main\resources\log4j2.xml`至webapp的classes目录
  - 修改web.xml配置TraceFilter
```
  <filter>
    <display-name>TraceFilter</display-name>
    <filter-name>TraceFilter</filter-name>
    <filter-class>com.cntaiping.microtracing.tracespan.web.TraceFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>TraceFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```

