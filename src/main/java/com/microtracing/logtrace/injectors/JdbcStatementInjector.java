package com.microtracing.logtrace.injectors;

import com.microtracing.logtrace.LogTraceConfig;

public class JdbcStatementInjector extends SpanCallInjector {


	protected final static String STMT_SPAN_NAME = "JDBC:\" + $type.getName() +\"";
	
	public JdbcStatementInjector(LogTraceConfig config){
		super(config);
		super.setSpanName(STMT_SPAN_NAME);
		
		super.methodCallAfter  
//			= "    if(_$span != null && $args.length>0) _$span.addTag(\"sql\", $1); \n"
			= "    if(_$span != null && $args.length>0 && $args[0]!=null) _$span.addTag(\"sql\", $args[0].toString()); \n"
			+ "  }catch(Exception _$e){ \n"
			+ "    if(_$span != null) _$span.addException(_$e); \n"
			+ "    if(_$span != null) _$span.stop(); \n" 
			+ "    throw _$e;  \n"
			+ "  }\n";		
	    //stop after execute in JdbcExecuteInjector
	}
	
	public boolean isNeedInject(String className) {
		return config.isEnableJdbcTrace() 
				&& ("java.sql.Connection".equals(className) );
	}
	
	@Override
	public boolean isNeedCallInject(String className, String methodName){
		return config.isEnableJdbcTrace() 
				&& isNeedInject(className)
				&& ("createStatement".equals(methodName) || "prepareStatement".equals(methodName) || "prepareCall".equals(methodName) );
	}
	
	

}
