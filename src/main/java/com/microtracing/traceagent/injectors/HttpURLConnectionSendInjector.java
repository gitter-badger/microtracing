package com.microtracing.traceagent.injectors;

import com.microtracing.traceagent.CallInjector;
import com.microtracing.traceagent.ClassInjector;
import com.microtracing.traceagent.LogTraceConfig;


public class HttpURLConnectionSendInjector implements CallInjector{
	//private static final org.apache.log4j.Logger logger =  org.apache.log4j.LogManager.getLogger(HttpURLConnectionRecvInjector.class);
	private static final java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(HttpURLConnectionSendInjector.class.getName());
		

	
	private final static  String methodCallBefore 
	  = "  com.microtracing.tracespan.Tracer _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
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
		return config.isEnableHttpURLConnectionTrace() 
				&& "java.net.URLConnection".equals(className)||"java.net.HttpURLConnection".equals(className)||"sun.net.www.protocol.http.HttpURLConnection".equals(className);
	}
	
	@Override
	public boolean isNeedCallInject(String className, String methodName){
		return config.isEnableHttpURLConnectionTrace() 
				&& isNeedInject(className)
				&& ("connect".equals(methodName) || "getOutputStream".equals(methodName));
	}
	
	
	private LogTraceConfig config;


	
}