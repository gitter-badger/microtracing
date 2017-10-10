package com.microtracing.traceagent.injectors;

import com.microtracing.traceagent.CallInjector;
import com.microtracing.traceagent.LogTraceConfig;

public class JdbcInjector implements CallInjector {

	private static final java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(HttpURLConnectionSendInjector.class.getName());
		

	
	private final static  String methodCallBefore 
	  = "  com.microtracing.tracespan.Tracer _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
      + "  com.microtracing.tracespan.Span _$span =  _$tracer.getCurrentSpan(); \n"
      + "  String _$spanName = \"JDBC:\"+$0.getConnection().getCatalog(); \n"
      + "  if (!_$spanName.equals(_$span.getName())) { \n "
      + "    _$span = _$tracer.createSpan(_$spanName);  \n"
      + "    _$span.start();  \n"
      + "  } \n"
      + "  com.microtracing.tracespan.web.HttpURLConnectionInterceptor _$inter = new com.microtracing.tracespan.web.HttpURLConnectionInterceptor(); \n"
      + "  if(_$span != null) _$inter.inject(_$span, $0); \n"
      + "  if(_$span != null) _$span.logEvent(_$span.CLIENT_SEND); \n"
      + "  try{ \n";
	
	private final static  String methodCallAfter  
	  = "    if(_$span != null) _$span.logEvent(_$span.CLIENT_RECV);\n"
      + "  }catch(Exception _$e){ \n"
      + "    if(_$span != null) _$span.logException(_$e); \n"
      + "    throw _$e;  \n"
      + "  }finally{ \n"
      + "    if(_$span != null) _$span.stop(); \n"
      + "  }\n";
	
	private LogTraceConfig config;
	
	public JdbcInjector(LogTraceConfig config){
		this.config = config;
	}	

	
	@Override
	public  String getMethodCallBefore(String className, String methodName){
		String s = String.format(methodCallBefore,className, methodName);
		logger.fine(String.format("inject before %s.%s\n%s",className,methodName,s));
		return s;
	}
	
	@Override
	public  String getMethodCallAfter(String className, String methodName){
		String s = String.format(methodCallAfter,className, methodName);
		logger.fine(String.format("inject after %s.%s\n%s",className,methodName,s));
		return s;
	}	
	
	
	
	public boolean isNeedInject(String className) {
		return config.isEnableJdbcTrace() 
				&& ("java.sql.Statement".equals(className) || "java.sql.PreparedStatement".equals(className) || "java.sql.CallableStatement".equals(className) );
	}
	
	@Override
	public boolean isNeedCallInject(String className, String methodName){
		return config.isEnableJdbcTrace() 
				&& isNeedInject(className)
				&& ("execute".equals(methodName) || "executeQuery".equals(methodName) || "executeUpdate".equals(methodName) || "executeBatch".equals(methodName));
	}
	
	

}
