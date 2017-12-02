package com.microtracing.logagent.injectors;

import com.microtracing.logagent.LogTraceConfig;

public class JdbcExecuteInjector extends SpanCallInjector {


	protected final static String STMT_SPAN_NAME = "JDBC:%1$s";
	
	public JdbcExecuteInjector(LogTraceConfig config){
		super(config);
		super.setSpanName(STMT_SPAN_NAME);
		
		super.initAndStartSpan = ""; //started in JdbcStatementInjector
		
		super.methodCallBefore 
				= super.methodCallBefore
				+ "    if(_$span != null) _$span.addEvent(_$span.CLIENT_SEND); \n";
		
		super.methodCallAfter  
				= "    if(_$span != null && $args.length>0 && $args[0]!=null) _$span.addTag(\"sql\", $args[0].toString()); \n"
				+ "    if(_$span != null) _$span.addEvent(_$span.CLIENT_RECV);\n"
				+ super.methodCallAfter;
		
	}
	
	public boolean isNeedInject(String className) {
		return config.isEnableJdbcTrace() 
				&& ("java.sql.Statement".equals(className) || "java.sql.PreparedStatement".equals(className) || "java.sql.CallableStatement".equals(className) );
	}
	
	@Override
	public boolean isNeedCallInject(String className, String methodName){
		return config.isEnableJdbcTrace() 
				&& isNeedInject(className)
				&& ("execute".equals(methodName) || "executeQuery".equals(methodName) || "executeUpdate".equals(methodName) || "executeBatch".equals(methodName));
	}
	
	

}
