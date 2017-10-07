package com.microtracing.logtrace.injectors;

import com.microtracing.logtrace.CallInjector;
import com.microtracing.logtrace.ClassInjector;
import com.microtracing.logtrace.LogTraceConfig;
import com.microtracing.logtrace.LogTransformer;


public class HttpURLConnectionRecvInjector implements ClassInjector,CallInjector{
	private static final org.apache.log4j.Logger logger =  org.apache.log4j.LogManager.getLogger(HttpURLConnectionRecvInjector.class);
	
	private final static String[] classFields = new String[]{
			"private final static java.util.logging.Logger _$logger = java.util.logging.Logger.getLogger(\"%1$s\");"
		};

	private final static  String methodCallBefore 
	  = "  com.microtracing.tracespan.Tracer _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
      + "   com.microtracing.tracespan.Span _$span =  _$tracer.getCurrentSpan(); \n"
      + "   if (!\"HttpURLConnection.connect\".equals(_$span.getName())) { \n "
      + "     _$span = null;  \n"
      + "   } \n"
      + "   com.microtracing.tracespan.web.HttpURLConnectionInterceptor _$inter = new com.microtracing.tracespan.web.HttpURLConnectionInterceptor(); \n"
      + "   \n"
      + "   try{ \n";
	
	private final static  String methodCallAfter  
	  = "    if(_$span != null) _$span.logEvent(_$span.CLIENT_RECV);\n"
      + "  }catch(Exception _$e){ \n"
      + "    throw _$e;  \n"
      + "  }finally{ \n"
      + "    if(_$span != null) _$span.stop(); \n"
      + "  }\n";

	
	private LogTraceConfig config;
	 
	public HttpURLConnectionRecvInjector(LogTraceConfig config){
		this.config = config;
	}
	
	@Override
	public  String getMethodCallBefore(String className, String methodName){
		String s = String.format(methodCallBefore,className,methodName);
		logger.debug(s);				
		return s;
	}
	
	@Override
	public  String getMethodCallAfter(String className, String methodName){
		String s = String.format(methodCallAfter,className,methodName);
		logger.debug(s);				
		return s;
	}	
	
	@Override
	public boolean isNeedCallInject(String className, String methodName){
		return "java.net.HttpURLConnection".equals(className)
				&& ("getResponseCode".equals(methodName) || "getInputStream".equals(methodName) || "getContent".equals(methodName) );
	}
	
	@Override
	public boolean isNeedInject(String className) {
		return "java.net.HttpURLConnection".equals(className);
	}

	@Override
	public String[] getClassFields(String className) {
		return classFields;
	}
	
}