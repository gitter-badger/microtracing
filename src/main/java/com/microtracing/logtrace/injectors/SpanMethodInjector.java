package com.microtracing.logtrace.injectors;
import com.microtracing.logtrace.CallInjector;
import com.microtracing.logtrace.LogTraceConfig;
import com.microtracing.logtrace.MethodInjector;
public class SpanMethodInjector implements MethodInjector{

    private final static String[][] methodVariables = new String[][]{
        {"com.microtracing.tracespan.Tracer","_$tracer"},
        {"com.microtracing.tracespan.Span","_$span"}
    };
    
	private final static String initAndStartSpan 
        = "  _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
        + "  _$span =  _$tracer.getCurrentSpan(); \n"
        + "  String _$spanName = \"%1$s\"; \n"
        + "  if (!_$spanName.equals(_$span.getName())) { \n "
        +"     _$span = _$tracer.createSpan(_$spanName);  \n"
        +"     _$span.start();  \n"
        + "  } \n";

	private final static String getSpan
        = "  com.microtracing.tracespan.Tracer _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
        + "  com.microtracing.tracespan.Span _$span =  _$tracer.getCurrentSpan(); \n"
        + "  String _$spanName = \"%1$s\"; \n"
        + "  if (!_$spanName.equals(_$span.getName())) { \n "
        +"     _$span = null;  \n"
        + "  } \n";


    private final static  String methodProcessStart 
        = "  { \n"
        +      initAndStartSpan
        + "  } \n";
                                                 
    private final static  String methodProcessReturn  
        = "  { \n"
        // can use methodVariables
        //+      getSpan 
        + "    if(_$span != null) _$span.stop(); \n"
        + "  } \n";
                                                    
    private final static  String methodProcessException 
        = "  { \n"
        // cannot use methodVariables
        +      getSpan 
        + "    if(_$span != null) { \n"
        + "       _$span.addException(_$e); \n"
        + "       _$span.stop(); \n"
        + "    } \n"
        + "    throw _$e;  \n"
        + "  } \n";
                                                    
    private final static  String methodProcessFinally ="";
    /* stopped in return & exception catch block
        = "  { \n"
        +      getSpan 
        + "    if(_$span != null) _$span.stop(); \n"
        + "  } \n";
    */                            
    
    
    private LogTraceConfig config;
	private String spanName = "METHOD:%1$s.%2$s";
	    
    public SpanMethodInjector(LogTraceConfig config){
        this.config = config;
    }
    
    public void setSpanName(String spanName) {
    	this.spanName = spanName;
    }
    
    
    @Override
    public  String[][] getMethodVariables(String className, String methodName){
        return methodVariables;
    }
    
    @Override
    public  String getMethodProcessStart(String className, String methodName){
    	String realSpanName =  String.format(spanName, className, methodName);
        return String.format(methodProcessStart, realSpanName);
    }
    
    @Override
    public  String getMethodProcessReturn(String className, String methodName){
    	String realSpanName =  String.format(spanName, className, methodName);
        return String.format(methodProcessReturn, realSpanName);
    }    
    
    @Override
    public  String getMethodProcessException(String className, String methodName){
    	String realSpanName =  String.format(spanName, className, methodName);
        return String.format(methodProcessException, realSpanName);
    }    

    @Override
    public  String getMethodProcessFinally(String className, String methodName){
    	String realSpanName =  String.format(spanName, className, methodName);
        return String.format(methodProcessFinally, realSpanName);
    }    
    
    
    
    @Override
    public boolean isNeedProcessInject(String className, String methodName){
        return config.isNeedProcessInject(className, methodName);
    }
    
}