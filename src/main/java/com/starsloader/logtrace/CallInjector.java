package com.starsloader.logtrace;
public interface CallInjector {
	public boolean isNeedCallInject(String className, String methodName);
	public String getMethodCallBefore(String className, String methodName);
	public String getMethodCallAfter(String className, String methodName);
}