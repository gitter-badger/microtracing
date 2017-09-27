package com.microtracing.logtrace.injectors;
import com.microtracing.logtrace.LogTraceConfig;
import com.microtracing.logtrace.MethodInjector;
public class ExceptionInjector implements MethodInjector{
	
	private final static String[][] methodVariables = new String[0][0];
	
	private final static  String methodProcessStart = "";
												 
	private final static  String methodProcessReturn   = "";

	private final static  String methodProcessException = "  { \n"
													    + "    _$logger.info(\"PROCESS_EXCEPTION \" + _$e); \n"
														+ "    throw _$e;  \n"
													    + "  } \n";
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
	
	public ExceptionInjector(LogTraceConfig config){
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