# Microtracing
A distributed systems tracing and logging infrastructure based on javaagent, trace trees and spans(see Google Dapper), slf4j and log4j2. 

基于javaagent、trace trees and spans(参考Google Dapper)、slf4j和log4j2实现的分布式系统全链路调用跟踪及日志框架。

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
- 使用高性能日志框架输出trace trees and spans
- 应用日志框架均桥接至slf4j, 通过slf4j + log4j2进行统一输出
- 标准化应用日志输出格式，增加traceId和spanId，用于关联用户一次请求所有相关日志事件
- 日志可使用ELK(Elasticsearch,Logstash,Kibana)或Splunk进行收集、搜索、提取、关联、分析、展现

## 模块说明
- **tracespan**: 实现trace trees and spans的核心代码，并包含用于web应用的TraceFilter
- **logagent**: 使用javaagent和javassit技术自动注入日志和跟踪代码
- **logtrace**: 用于将tracespan、logagent及相关依赖库打包集成
- **demo**: 演示程序

## 依赖关系
- **javassist**: 3.22.0-GA或以上版本，用于类加载时动态修改字节码注入日志和跟踪代码
- **slf4j**: （slf4j-api, jcl-over-slf4j,log4j-over-slf4j, jul-to-slf4j）1.7.25或以上版本，提供日志接口
- **log4j2**: （log4j-api, log4j-core, log4j-web, log4j-slf4j-impl）2.3或以上版本，提供日志实现
- **disruptor**: 3.3.7或以上版本，支持log4j2日志框架实现高性能并发异步输出

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

  *注：{VERSION}修改为实际版本号*

  - **方法一：** 通过Webligc Administrator Console配置相应server的服务器启动参数，增加：
  
  > -javaagent:${DOMAIN_HOME}/logtrace/logtrace-{VERSION}-jar-with-dependencies.jar=${DOMAIN_HOME}/logtrace/logtrace.properties -Dlog4j.configurationFile=${DOMAIN_HOME}/logtrace/log4j2.xml  -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector

  - **方法二：** 直接修改启动脚本，修改JAVA_OPTIONS增加启动参数，例如修改bin/startWeblogic.sh：
  
  > JAVA_OPTIONS=" -javaagent:${DOMAIN_HOME}/logtrace/logtrace-{VERSION}-jar-with-dependencies.jar=${DOMAIN_HOME}/logtrace/logtrace.properties -Dlog4j.configurationFile=${DOMAIN_HOME}/logtrace/log4j2.xml  -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector ${SAVE_JAVA_OPTIONS} "	  

- 从webapp的WEB-INF/lib下移除所有日志库，如commons-logging*.jar, log4j*.jar等（已在logtrace-{VERSION}-jar-with-dependencies.jar中包含）
- 框架自动追踪weblogic server调用servlet，无需配置filter


### 其他应用服务器
- 在应用服务器工作目录下建立logtrace目录，复制以下文件至logtrace目录：

```
logtrace-*-jar-with-dependencies.jar (from logtrace\target\)
logtrace.properties (from logagent\src\main\resources\)
log4j2.xml (from tracespan\src\main\resources\)
```

- 修改应用服务器启动脚本，在java之后添加启动参数：

  *注：{ABSOLUTE PATH}修改为logtrace目录所在绝对路径； {VERSION}修改为实际版本号*
  
>  -javaagent:{ABSOLUTE PATH}/logtrace/logtrace-{VERSION}-jar-with-dependencies.jar={ABSOLUTE PATH}/logtrace/logtrace.properties -Dlog4j.configurationFile={ABSOLUTE PATH}/logtrace/log4j2.xml  -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector


- 配置webapp的WEB-INF/web.xml增加TraceFilter
```
  <filter>
    <display-name>TraceFilter</display-name>
    <filter-name>TraceFilter</filter-name>
    <filter-class>com.microtracing.tracespan.web.TraceFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>TraceFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```
- 从webapp的WEB-INF/lib下移除所有日志库，如commons-logging*.jar, log4j*.jar等（已在logtrace-{VERSION}-jar-with-dependencies.jar中包含）


## 配置文件
### logtrace.properties
  配置需要跟踪的包、类、方法及启用的特性等

- 配置说明

  - logtrace.includePackages 注入跟踪的包
  - logtrace.excludePackages 排除跟踪的包
  - logtrace.traceMethodCall 跟踪指定方法被调用情况（从includePackages里发起调用）
  - logtrace.traceMethodProcess 跟踪指定方法执行情况
  - logtrace.enableHttpURLConnectionTrace 启用HttpURLConnection注入跟踪远程访问
  - logtrace.enableServletTrace 启用Servlet执行跟踪
  - logtrace.enableJdbcTrace 启用JDBC执行跟踪
  - logtrace.enableExceptionLog 启用异常跟踪
  - logtrace.enableTimingLog 启用耗时记录（方法执行时间超过指定阈值则记录相关信息）
  - logtrace.timingThreshold 启用耗时记录的阈值
  
- 参考配置
  - 默认配置: logagent\src\main\resources\logtrace.properties
  - 演示配置: demo\src\main\resources\logtrace.properties
  
### log4j2.xml
  配置日志输出策略、文件及格式等

- 配置说明

  默认配置输出终端：Console, tracelog, applog. 默认等级WARN及以上输出至Console和applog.
  - tracelog  输出跟踪框架日志，默认等级DEBUG.  日志文件：logs\logtrace.log
  - applog  输出应用日志，默认等级WARN. 日志文件：logs\logapp.log
  - 根据实际需要配置添加应用Logger，例如
  
```
        <Logger name="com.mycompany.myapp" level="INFO" additivity="false">  
             <AppenderRef ref="applog"/>  
        </Logger>  
```
  注：System.out.println内容会自动转换为log以DEBUG等级输出至logapp
  
- 参考配置
  - 默认配置: tracespan\src\main\resources\log4j2.xml
  - 演示配置: demo\src\main\resources\log4j2.xml
 
- 日志文件
  - logs\logtrace.log
  - logs\logapp.log

- 日志输出格式

```
2017-12-05 16:32:23.019 INFO  [68a35c074328b600,ce4fffc041b09a35] com.microtracing.tracespan.Span : SpanEvent{event="cs", spanId=ce4fffc041b09a35, timestamp=1512462743019}
```

## 常见问题

### Slf4j初始化失败
检查classpath和WEB-INF/lib是否有其他slf4j实现库，进行清除。
