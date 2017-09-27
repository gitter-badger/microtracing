package com.microtracing.logtrace.injectors;
import com.microtracing.logtrace.LogTraceConfig;
import com.microtracing.logtrace.MethodInjector;
public class TimerInjector implements MethodInjector{
	
	private final static String[][] methodVariables = new String[][]{
													{"long","_$startTime"}
	};
	
	private final static  String methodProcessStart = "  { \n"
													+ "    _$startTime = System.currentTimeMillis(); \n"
													+ "  } \n";
												 
	private final static  String methodProcessReturn   = "  { \n"
													+ "    long _$endTime = System.currentTimeMillis(); \n"
													+ "    long _$cost = _$endTime - _$startTime; \n"
													+ "    if (_$cost > %1$d){ \n"
													+ "      _$logger.info(\"PROCESS_LATENCY \" + _$cost); \n"
													+ "    } \n"
													+ "  } \n";

	private final static  String methodProcessException = "";	
	private final static  String methodProcessFinally   = "";	
	
	public  String[][] getMethodVariables(){
		return methodVariables;
	}
	
	public  String getMethodProcessStart(){
		return methodProcessStart;
	}
	
	public  String getMethodProcessReturn(){
		return String.format(methodProcessReturn, config.getLogMethodLatency());
	}	
	
	public  String getMethodProcessException(){
		return methodProcessException;
	}	

	public  String getMethodProcessFinally(){
		return methodProcessFinally;
	}	
	
	public TimerInjector(LogTraceConfig config){
		this.config = config;
	}
	
	public boolean isNeedInject(String className){
		return config.isNeedInject(className);
	}
	
	public boolean isNeedProcessInject(String className, String methodName){
		return config.isNeedInject(className) && !config.isNeedProcessInject(className, methodName); // loginjector done everything
	}
		
	
	private LogTraceConfig config;
}