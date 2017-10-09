package com.microtracing.traceagent.injectors;
import com.microtracing.traceagent.CallInjector;
import com.microtracing.traceagent.ClassInjector;
import com.microtracing.traceagent.LogTraceConfig;
import com.microtracing.traceagent.MethodInjector;
public class LogInjector implements ClassInjector,CallInjector,MethodInjector{
    private final static String[] classFields = new String[]{
        //"private final static java.util.logging.Logger _$logger = java.util.logging.Logger.getLogger(\"%1$s\");"
        //"private final static org.apache.log4j.Logger _$logger = org.apache.log4j.LogManager.getLogger(\"%1$s\");"
        "private final static org.slf4j.Logger _$logger = org.slf4j.LoggerFactory.getLogger(\"%1$s\");"

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
                                                    
    
    private LogTraceConfig config;
        
    public LogInjector(LogTraceConfig config){
        this.config = config;
    }    
    
    @Override
    public  String[] getClassFields(String className){
        String[] fields = new String[classFields.length];
        for (int i=0; i<fields.length; i++){
            fields[i] = String.format(classFields[i], className);
        }
        return fields;
    }
        
    @Override
    public  String getMethodCallBefore(String className, String methodName){
        return String.format(methodCallBefore,className,methodName);
    }
    
    @Override
    public  String getMethodCallAfter(String className, String methodName){
        return String.format(methodCallAfter,className,methodName);
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
        return methodProcessException;
    }    

    @Override
    public  String getMethodProcessFinally(String className, String methodName){
        return methodProcessFinally;
    }    
    
    @Override
    public boolean isNeedInject(String className){
        return config.isNeedInject(className);
    }
    
    @Override
    public boolean isNeedCallInject(String className, String methodName){
        return config.isNeedCallInject(className, methodName);
    }
    
    @Override
    public boolean isNeedProcessInject(String className, String methodName){
        return config.isNeedProcessInject(className, methodName);
    }
    

}