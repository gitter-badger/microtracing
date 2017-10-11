package com.microtracing.logtrace.injectors;
import com.microtracing.logtrace.LogTraceConfig;
import com.microtracing.logtrace.MethodInjector;
public class ExceptionInjector implements MethodInjector{
	
	private final static String[][] methodVariables = new String[0][0];
	
	private final static  String methodProcessStart = "";
												 
	private final static  String methodProcessReturn   = "";

	private final static  String methodProcessException = "  { \n"
													    + "    _$logger.warn(\"PROCESS_EXCEPTION \" + _$e); \n"
														+ "    throw _$e;  \n"
													    + "  } \n";
	private final static  String methodProcessFinally   = "";	
	
	private LogTraceConfig config;
	
	public ExceptionInjector(LogTraceConfig config){
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
		return String.format(methodProcessReturn, config.getLogMethodLatency());
	}	
	
	@Override
	public  String getMethodProcessException(String className, String methodName){
		return methodProcessException;
	}	

	@Override
	public  String getMethodProcessFinally(String className, String methodName){
		return methodProcessFinally;
	}	
	
	@Override
	public boolean isNeedProcessInject(String className, String methodName){
		return config.isEnableExceptionLog() && config.isNeedInject(className) ;//&& !config.isNeedProcessInject(className, methodName); // loginjector done everything
	}
	
	public boolean isNeedInject(String className){
		return config.isEnableExceptionLog() && config.isNeedInject(className);
	}
	
}