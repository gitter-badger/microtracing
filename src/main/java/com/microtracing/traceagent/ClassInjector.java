package com.microtracing.traceagent;
public interface ClassInjector{
	public boolean isNeedInject(String className);
	public String[] getClassFields(String className);
}