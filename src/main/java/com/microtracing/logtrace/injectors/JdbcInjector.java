package com.microtracing.logtrace.injectors;

import com.microtracing.logtrace.LogTraceConfig;

public class JdbcInjector extends SpanCallInjector {


	private static final  String JDBC_SPAN_NAME = "JDBC_CALL:%1$s.%2$s";
			
	
	public JdbcInjector(LogTraceConfig config){
		super(config);
		super.setSpanName(JDBC_SPAN_NAME);
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
