package com.microtracing.logtrace.injectors;
import com.microtracing.logtrace.CallInjector;
import com.microtracing.logtrace.ClassInjector;
import com.microtracing.logtrace.LogTraceConfig;
import com.microtracing.logtrace.MethodInjector;
public class Log4jInjector implements ClassInjector,CallInjector,MethodInjector{
	private final static String[] classFields = new String[]{
		//"private final static java.util.logging.Logger _$logger = java.util.logging.Logger.getLogger(\"%1$s\");"
	    "private final static org.apache.log4j.Logger _$logger = org.apache.log4j.LogManager.getLogger(\"%1$s\");"
	};

	private final static  String methodCallBefore 
		= "  long _$startTime = System.currentTimeMillis(); \n"
		+ "  _$logger.info(\"CALL_START %1$s.%2$s \"); \n"
		+ "  try{ \n";
	
	private final static  String methodCallAfter  
		= "  }catch(Exception _$e){ \n"
		+ "     _$logger.info(\"CALL_EXCEPTION %1$s.%2$s \" + _$e); \n"
		+ "     throw _$e; \n"
		+ "  }finally{ \n"
		+ "    long _$endTime = System.currentTimeMillis(); \n"
		+ "    long _$cost = _$endTime - _$startTime; \n"
		+ "    _$logger.info(\"CALL_END %1$s.%2$s \" + _$cost); \n"
		+ "  }\n";
	
	private final static String[][] methodVariables = new String[][]{
													{"long","_$startTime"}
	};
	
	private final static  String methodProcessStart 
		= "  { \n"
		+ "    _$startTime = System.currentTimeMillis(); \n"
		+ "    _$logger.info(\"PROCESS_START \"); \n"
		+ "  } \n";
												 
	private final static  String methodProcessReturn   
		= "  { \n"
		+ "    long _$endTime = System.currentTimeMillis(); \n"
		+ "    long _$cost = _$endTime - _$startTime; \n"
		+ "    _$logger.info(\"PROCESS_DURATION \" + _$cost); \n"
		+ "  } \n";
													
	private final static  String methodProcessException 
		= "  { \n"
		+ "    _$logger.info(\"PROCESS_EXCEPTION \" + _$e); \n"
		+ "    throw _$e;  \n"
		+ "  } \n";
													
	private final static  String methodProcessFinally   
		= "  { \n"
		+ "    _$logger.info(\"PROCESS_END \" );  \n"
		+ "  } \n";
													
	
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
	
	public Log4jInjector(LogTraceConfig config){
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