package com.microtracing.logagent.injectors;
import com.microtracing.logagent.CallInjector;
import com.microtracing.logagent.LogTraceConfig;
public class SpanCallInjector implements CallInjector{

	protected final static String DEFAULT_SPAN_NAME = "CALL:%1$s.%2$s(\"+$0.toString()+\")";

	protected  String initAndStartSpan 
        = "  _$tracer = com.microtracing.tracespan.Tracer.getTracer(); \n"
        + "  _$span =  _$tracer.getCurrentSpan(); \n"
        + "  String _$spanName = \"%1$s\"; \n"
        + "  if (!_$spanName.equals(_$span.getName())) { \n "
        +"     _$span = _$tracer.createSpan(_$spanName);  \n"
        +"     _$span.setAutoPrintLog(%2$b);  \n"
        +"     _$span.start();  \n"
        + "  } \n";

	protected  String methodCallBefore 
        = "  com.microtracing.tracespan.Tracer _$tracer; \n"
        + "  com.microtracing.tracespan.Span _$span ; \n"
        +    initAndStartSpan
        + "  try{ \n";

    
	protected   String methodCallAfter  
        = "  }catch(Exception _$e){ \n"
        + "    if(_$span != null) _$span.addException(_$e); \n"
        + "    throw _$e;  \n"
        + "  }finally{ \n"
        + "    if(_$span != null) _$span.stop(); \n"
        + "  }\n";
                            
    
    
    protected LogTraceConfig config;
    
	private String spanName = DEFAULT_SPAN_NAME;
	private boolean autoPrintLog = true;
	    
    public SpanCallInjector(LogTraceConfig config){
        this.config = config;
    }
    
    public void setSpanName(String spanName) {
    	this.spanName = spanName;
    }
    
    public void setAutoPrintLog(boolean autoPrintLog) {
    	this.autoPrintLog = autoPrintLog;
    }
        

    @Override
    public  String getMethodCallBefore(String className, String methodName){
    	String realSpanName =  String.format(spanName, className, methodName);
        return String.format(methodCallBefore, realSpanName, autoPrintLog);
    }
    
    @Override
    public  String getMethodCallAfter(String className, String methodName){
    	String realSpanName =  String.format(spanName, className, methodName);
        return String.format(methodCallAfter, realSpanName, autoPrintLog);
    }    
    
    
    
    @Override
    public boolean isNeedCallInject(String className, String methodName){
        return config.isNeedCallInject(className, methodName);
    }
    
}