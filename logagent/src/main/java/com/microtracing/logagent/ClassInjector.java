package com.microtracing.logagent;
public interface ClassInjector{
	public boolean isNeedInject(String className);
	public String[] getClassFields(String className);
}