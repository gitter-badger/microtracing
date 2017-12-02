package com.microtracing.logagent.injectors;
import com.microtracing.logagent.CallInjector;
import com.microtracing.logagent.LogTraceConfig;
public class CallTimingInjector implements CallInjector{
	

	
	private final static  String methodCallBefore 
		= "  long  _$startTime = System.currentTimeMillis(); \n"
		+ "  try { \n";
												 
	private final static  String methodCallAfter   
		= "  }finally{ \n"
		+ "    long _$endTime = System.currentTimeMillis(); \n"
		+ "    long _$cost = _$endTime - _$startTime; \n"
		+ "    if (_$cost > %3$d){ \n"
		+ "      _$logger.info(\"CALL_LATENCY \" + _$cost + \" %1$s.%2$s\"); \n"
		+ "    } \n"
		+ "  } \n";

	private LogTraceConfig config;
	
	public CallTimingInjector(LogTraceConfig config){
		this.config = config;
	}
	
	@Override
	public  String getMethodCallBefore(String className, String methodName){
		return String.format(methodCallBefore, className, methodName, config.getTimingThreshold());
	}
	
	@Override
	public  String getMethodCallAfter(String className, String methodName){
		return String.format(methodCallAfter, className, methodName, config.getTimingThreshold());
	}	
	
	
	public boolean isNeedInject(String className){
		return config.isEnableTimingLog() && config.isNeedInject(className);
	}
	
	@Override
	public boolean isNeedCallInject(String className, String methodName){
		return config.isEnableTimingLog() && config.isNeedInject(className)  && config.isNeedCallInject(className, methodName); 
	}
		
	
	
}