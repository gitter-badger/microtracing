package com.microtracing.logtrace.injectors;

import com.microtracing.logtrace.CallInjector;
import com.microtracing.logtrace.ClassInjector;
import com.microtracing.logtrace.LogTraceConfig;


public class HttpURLConnectionSendInjector implements ClassInjector,CallInjector{
	//private static final org.apache.log4j.Logger logger =  org.apache.log4j.LogManager.getLogger(HttpURLConnectionRecvInjector.class);
	private static final java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(HttpURLConnectionSendInjector.class.getName());
		
	private final static String[] classFields = new String[]{
			"private final static java.util.logging.Logger _$logger = java.util.logging.Logger.getLogger(\"%1$s\");"
		};
	
	private final static  String methodCallBefore = "  com.microtracing.tracespan.Tracer _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
                                                  + "  com.microtracing.tracespan.Span _$span =  _$tracer.getCurrentSpan(); \n"
                                                  + "  String _$spanName = \"HttpURLConnection:\"+$0.getURL().toString(); \n"
                                                  + "  if (!_$spanName.equals(_$span.getName())) { \n "
                                                  + "    _$span = _$tracer.createSpan(_$spanName);  \n"
                                                  + "    _$span.start();  \n"
                                                  + "  } \n"
                                                  + "  com.microtracing.tracespan.web.HttpURLConnectionInterceptor _$inter = new com.microtracing.tracespan.web.HttpURLConnectionInterceptor(); \n"
                                                  + "  if(_$span != null) _$inter.inject(_$span, $0); \n"
                                                  + "  if(_$span != null) _$span.logEvent(_$span.CLIENT_SEND); \n"
                                                  + "  try{ \n";
	
	private final static  String methodCallAfter  = "  \n"
                                                  + "  }catch(Exception _$e){ \n"
                                                  + "    if(_$span != null) _$span.logException(_$e); \n"
                                                  + "    if(_$span != null) _$span.stop(); \n"
                                                  + "    throw _$e;  \n"
                                                  + "  }finally{ \n"
                                                  + "  }\n";
	
	public HttpURLConnectionSendInjector(LogTraceConfig config){
		this.config = config;
	}	
						
	@Override
	public boolean isNeedInject(String className) {
		return "java.net.HttpURLConnection".equals(className);
	}

	@Override
	public String[] getClassFields(String className) {
		return classFields;
	}
	
	@Override
	public  String getMethodCallBefore(String className, String methodName){
		String s = String.format(methodCallBefore,className, methodName);
		logger.fine(s);
		return s;
	}
	
	@Override
	public  String getMethodCallAfter(String className, String methodName){
		String s = String.format(methodCallAfter,className, methodName);
		logger.fine(s);		
		return s;
	}	
	
	
	@Override
	public boolean isNeedCallInject(String className, String methodName){
		return "java.net.HttpURLConnection".equals(className)
				&& ("connect".equals(methodName) || "getOutputStream".equals(methodName));
	}
	
	
	private LogTraceConfig config;


	
}