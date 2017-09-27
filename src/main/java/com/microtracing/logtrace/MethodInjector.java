package com.microtracing.logtrace;
public interface MethodInjector {
	public boolean isNeedProcessInject(String className, String methodName);
	public String[][] getMethodVariables();
	public String getMethodProcessStart();
	public String getMethodProcessReturn();
	public String getMethodProcessException();
	public String getMethodProcessFinally();
}