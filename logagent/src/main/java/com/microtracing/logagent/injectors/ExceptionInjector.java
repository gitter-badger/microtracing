package com.microtracing.logagent.injectors;
import com.microtracing.logagent.LogTraceConfig;
import com.microtracing.logagent.MethodInjector;
public class ExceptionInjector implements MethodInjector{
	
	private final static String[][] methodVariables = new String[0][0];
	
	private final static  String methodProcessStart = "";
												 
	private final static  String methodProcessReturn   = "";

	private final static  String methodProcessException 
		= "  { \n"
		+ "    StackTraceElement[] _$trace  =  _$e.getStackTrace(); \n"
		+ "    StringBuffer _sb = new StringBuffer(); \n"
		+ "    _sb.append(\"PROCESS_EXCEPTION %1$s.%2$s\"); "
		+ "    for(int i=0; i<_$trace.length; i++){ \n"
		+ "      if(\"%1$s\".equals(_$trace[i].getClassName()) && \"%2$s\".equals(_$trace[i].getMethodName())){ \n"
		+ "        _sb.append(\"(\").append(_$trace[i].getFileName()).append(\":\").append(_$trace[i].getLineNumber());"
		+ "        if(i>0){ \n"
		+ "          _sb.append(\" root \").append(_$trace[0].getFileName()).append(\":\").append(_$trace[0].getLineNumber());"
		+ "        } \n"
		+ "        _sb.append(\") \"); \n"
		+ "        break; \n"
		+ "      } \n"
		+ "    } \n"
		+ "    _sb.append(_$e.toString()); \n"
		+ "    _$logger.warn(_sb.toString()); \n"
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
		return methodProcessReturn;
	}	
	
	@Override
	public  String getMethodProcessException(String className, String methodName){
		return String.format(methodProcessException, className, methodName);
	}	

	@Override
	public  String getMethodProcessFinally(String className, String methodName){
		return methodProcessFinally;
	}	
	
	@Override
	public boolean isNeedProcessInject(String className, String methodName){
		return config.isEnableExceptionLog() && config.isNeedInject(className) ;//&& config.isNeedProcessInject(className, methodName);
	}
	
	public boolean isNeedInject(String className){
		return config.isEnableExceptionLog() && config.isNeedInject(className);
	}
	
}