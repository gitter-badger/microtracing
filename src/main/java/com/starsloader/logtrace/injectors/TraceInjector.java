package com.starsloader.logtrace.injectors;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;  
import java.util.logging.Logger;
import com.starsloader.logtrace.ClassInjector;
import com.starsloader.logtrace.CallInjector;
import com.starsloader.logtrace.MethodInjector;
import com.starsloader.logtrace.LogTraceConfig;
public class TraceInjector implements ClassInjector,CallInjector,MethodInjector{
	private final static String[] classFields = new String[]{
		"private final static ThreadLocal _$traceId = new ThreadLocal();"
	};

	private final static  String methodCallBefore = " ";
	
	private final static  String methodCallAfter  = " ";
	
	private final static String[][] methodVariables = new String[][]{
													{"long","_$startTime"}
	};
	
	private final static  String methodProcessStart = " ";
												 
	private final static  String methodProcessReturn   = " ";
													
	private final static  String methodProcessException = " ";
													
	private final static  String methodProcessFinally   = " ";
													
	
	public  String[] getClassFields(String className){
		String[] fields = new String[classFields.length];
		for (int i=0; i<fields.length; i++){
			fields[i] = String.format(classFields[i], className);
		}
		return fields;
	}
		
	public  String getMethodCallBefore(String className, String methodName){
		return String.format(methodCallBefore,className,methodName);
	}
	
	public  String getMethodCallAfter(String className, String methodName){
		return String.format(methodCallAfter,className,methodName);
	}	
	
	public  String[][] getMethodVariables(){
		return methodVariables;
	}
	
	public  String getMethodProcessStart(){
		return methodProcessStart;
	}
	
	public  String getMethodProcessReturn(){
		return methodProcessReturn;
	}	
	
	public  String getMethodProcessException(){
		return methodProcessException;
	}	

	public  String getMethodProcessFinally(){
		return methodProcessFinally;
	}	
	
	public TraceInjector(LogTraceConfig config){
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