package com.microtracing.logtrace.injectors;
import com.microtracing.logtrace.CallInjector;
import com.microtracing.logtrace.LogTraceConfig;
public class SpanInjector implements CallInjector{


	private final static  String methodCallBefore 
		= "  com.microtracing.tracespan.Tracer _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
		+ "  com.microtracing.tracespan.Span _$span =  _$tracer.getCurrentSpan(); \n"
        + "  String _$spanName = \"CALL:%1$s.%2$s\"; \n"
		+ "  if (!_$spanName.equals(_$span.getName())) { \n "
        +"     _$span = _$tracer.createSpan(_$spanName);  \n"
        +"     _$span.start();  \n"
		+ "  } \n"
		+ "  \n"
		+ "  try{ \n";

	
	private final static  String methodCallAfter  
        = "  }catch(Exception _$e){ \n"
        + "    if(_$span != null) _$span.logException(_$e); \n"
        + "    throw _$e;  \n"
        + "  }finally{ \n"
        + "    if(_$span != null) _$span.stop(); \n"
        + "  }\n";
							
	
		
	public  String getMethodCallBefore(String className, String methodName){
		return String.format(methodCallBefore,className,methodName);
	}
	
	public  String getMethodCallAfter(String className, String methodName){
		return String.format(methodCallAfter,className,methodName);
	}	
	
	
	public SpanInjector(LogTraceConfig config){
		this.config = config;
	}
	
	public boolean isNeedInject(String className){
		return config.isNeedInject(className);
	}
	
	public boolean isNeedCallInject(String className, String methodName){
		return config.isNeedCallInject(className, methodName);
	}
	
	public boolean isNeedProcessInject(String className, String methodName){
		return config.isNeedProcessInject(className, methodName);
	}
	
	private LogTraceConfig config;
	
}