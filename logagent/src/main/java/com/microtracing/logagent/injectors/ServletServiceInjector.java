package com.microtracing.logagent.injectors;

import com.microtracing.logagent.LogTraceConfig;


import com.microtracing.logagent.CallInjector;

public class ServletServiceInjector implements CallInjector {
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ServletServiceInjector.class.getName());
	
	LogTraceConfig config;

	protected  String methodCallBefore 
        = "  com.microtracing.tracespan.web.TraceHelper _$helper = new com.microtracing.tracespan.web.TraceHelper(); \n"
        + "  javax.servlet.http.HttpServletRequest _$request = (javax.servlet.http.HttpServletRequest) $1; \n"
        + "  javax.servlet.http.HttpServletResponse _$response = (javax.servlet.http.HttpServletResponse) $2;  \n"
        + "  boolean _$ignoreTrace = false; \n"
        + "  com.microtracing.tracespan.Span _$span = null; \n"
        + "  if ($0.getClass().getName().startsWith(\"weblogic.\")||$0.getClass().getName().startsWith(\"apache.\")) { _$ignoreTrace = true; } \n"
        + "  if (!_$ignoreTrace) _$ignoreTrace = _$helper.ignoreTrace(_$request); \n"
        + "  if (!_$ignoreTrace){ \n"
        + "     _$span = _$helper.beforeService(_$request, _$response);  \n"
        + "     $1 = _$helper.wrapRequest(_$request);  \n"
        + "     $2 = _$helper.wrapResponse(_$request, _$response);  \n"
        + "  }  \n"
        + "  try{ \n";

    
	protected   String methodCallAfter  
        = "  }finally{ \n"
        + "    if (!_$ignoreTrace) _$helper.afterService(_$request, _$response, _$span); \n"
        + "  }\n";
                            
	public ServletServiceInjector(LogTraceConfig config) {
		this.config = config;
	}
	
    @Override
    public boolean isNeedCallInject(String className, String methodName){
        return ("javax.servlet.Servlet".equals(className) && "service".equals(methodName)) ||
        		("javax.servlet.FilterChain".equals(className) && "doFilter".equals(methodName));
    }

	@Override
	public String getMethodCallBefore(String className, String methodName) {
		return methodCallBefore;
	}

	@Override
	public String getMethodCallAfter(String className, String methodName) {
		return methodCallAfter;
	}	

}
