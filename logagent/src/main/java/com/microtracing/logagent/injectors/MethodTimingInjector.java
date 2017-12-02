package com.microtracing.logagent.injectors;
import com.microtracing.logagent.LogTraceConfig;
import com.microtracing.logagent.MethodInjector;
public class MethodTimingInjector implements MethodInjector{
	
	private final static String[][] methodVariables = new String[][]{
													{"long","_$startTime"}
	};
	
	private final static  String methodProcessStart 
		= "  { \n"
		+ "    _$startTime = System.currentTimeMillis(); \n"
		+ "  } \n";
												 
	private final static  String methodProcessReturn   
		= "  { \n"
		+ "    long _$endTime = System.currentTimeMillis(); \n"
		+ "    long _$cost = _$endTime - _$startTime; \n"
		+ "    if (_$cost > %3$d){ \n"
		+ "      _$logger.info(\"PROCESS_LATENCY \" + _$cost + \" %1$s.%2$s\"); \n"
		+ "    } \n"
		+ "  } \n";

	private final static  String methodProcessException = "";
	
	private final static  String methodProcessFinally   = "";	
	
	private LogTraceConfig config;
	
	public MethodTimingInjector(LogTraceConfig config){
		this.config = config;
	}
	
	@Override
	public  String[][] getMethodVariables(String className, String methodName){
		return methodVariables;
	}
	
	@Override
	public  String getMethodProcessStart(String className, String methodName){
		return methodProcessStart;
	}
	
	@Override
	public  String getMethodProcessReturn(String className, String methodName){
		return String.format(methodProcessReturn, className, methodName, config.getTimingThreshold());
	}	
	
	@Override
	public  String getMethodProcessException(String className, String methodName){
		return String.format(methodProcessException, className, methodName, config.getTimingThreshold());
	}	

	@Override
	public  String getMethodProcessFinally(String className, String methodName){
		return String.format(methodProcessFinally, className, methodName, config.getTimingThreshold());
	}	
	
	public boolean isNeedInject(String className){
		return config.isEnableTimingLog() && config.isNeedInject(className);
	}
	
	@Override
	public boolean isNeedProcessInject(String className, String methodName){
		return config.isEnableTimingLog() && config.isNeedInject(className);// && !config.isNeedProcessInject(className, methodName); // loginjector done everything
	}
		
	
	
}