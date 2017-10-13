package com.microtracing.logtrace.injectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microtracing.logtrace.CallInjector;
import com.microtracing.logtrace.LogTraceConfig;


public class HttpURLConnectionRecvInjector implements CallInjector{
	private static final Logger logger =  LoggerFactory.getLogger(HttpURLConnectionRecvInjector.class);
	
	private final static  String methodCallBefore 
      = "   com.microtracing.tracespan.Tracer _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
      + "   com.microtracing.tracespan.Span _$span =  _$tracer.getCurrentSpan(); \n"
      + "   String _$spanName = \"HttpURLConnection:\"+$0.getURL().toString(); \n"
      + "   if (!_$spanName.equals(_$span.getName())) { \n "
      + "     _$span = null;  \n"
      + "   } \n"
      + "   com.microtracing.tracespan.web.HttpURLConnectionInterceptor _$inter = new com.microtracing.tracespan.web.HttpURLConnectionInterceptor(); \n"
      + "   _$inter.extract($0);\n"
      + "   try{ \n";
	
	private final static  String methodCallAfter  
	  = "    if(_$span != null) _$span.addEvent(_$span.CLIENT_RECV);\n"
      + "  }catch(Exception _$e){ \n"
      + "    if(_$span != null) _$span.addException(_$e); \n"
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
		logger.debug(String.format("inject before %s.%s\n%s",className,methodName,s));
		return s;
	}
	
	@Override
	public  String getMethodCallAfter(String className, String methodName){
		String s = String.format(methodCallAfter,className,methodName);
		logger.debug(String.format("inject after %s.%s\n%s",className,methodName,s));
		return s;
	}	
	
	
	public boolean isNeedInject(String className) {
		return config.isEnableHttpURLConnectionTrace() &&  "java.net.URLConnection".equals(className)||"java.net.HttpURLConnection".equals(className)||"sun.net.www.protocol.http.HttpURLConnection".equals(className);
	}

	@Override
	public boolean isNeedCallInject(String className, String methodName){
		return config.isEnableHttpURLConnectionTrace() 
				&& isNeedInject(className)
				&& ("getResponseCode".equals(methodName) || "getInputStream".equals(methodName) || "getContent".equals(methodName) );
	}
	

	
}