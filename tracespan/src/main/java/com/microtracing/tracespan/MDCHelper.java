package com.microtracing.tracespan;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

public class MDCHelper {

	private static boolean injectOthers = true;
	
	private static List<MDCAdapter> mdcas = new ArrayList<MDCAdapter>();
	static{
		org.slf4j.spi.MDCAdapter mdca = MDC.getMDCAdapter();
		mdcas.add(mdca);
		if (injectOthers){
			String name = mdca.getClass().getName();
			/*
			try{
				if (!name.equals("ch.qos.logback.classic.util.LogbackMDCAdapter"))
						mdcas.add(new ch.qos.logback.classic.util.LogbackMDCAdapter());
			}catch(Throwable t){}
			*/
			try{
				if (!name.equals("org.slf4j.impl.Log4jMDCAdapter"))
					mdcas.add(new Log4jMDCAdapter());
			}catch(Throwable t){}
		}
	}
	
	public static void put(String key, String value){
		if (!injectOthers){
			MDC.put(key, value);
		}else for (MDCAdapter mdca : mdcas){
			try{
				mdca.put(key, value);
			}catch(Throwable t){}
		}
	}
	
	public static void remove(String key){
		if (!injectOthers){
			MDC.remove(key);
		}else for (MDCAdapter mdca : mdcas){
			try{
				mdca.remove(key);
			}catch(Throwable t){}
		}
	}
	
	
	static class Log4jMDCAdapter implements MDCAdapter {
		@SuppressWarnings("rawtypes")
		private Class log4jMDC;
		
		@SuppressWarnings("unchecked")
		private Method getMethod(String methodName, Class<?>... parameterTypes){
			try {
				if (log4jMDC == null) log4jMDC = Thread.currentThread().getContextClassLoader().loadClass("org.apache.log4j.MDC");
				if (log4jMDC == null) return null;
				
				Method method = log4jMDC.getMethod(methodName, parameterTypes);
				
				return method;
			} catch (Exception e) {
				//ignore
				return null;
			}
			
		}

		@SuppressWarnings("rawtypes")
	    public void clear() {
			Method m = getMethod("getContext");
			if (m==null) return;
	        Map map;
			try {
				map = (Map)m.invoke(log4jMDC);
		        if (map != null) {
		            map.clear();
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }

	    public String get(String key) {
			Method m = getMethod("get", String.class);
			if (m==null) return null;
			String value = null;
			try {
				value = (String)m.invoke(log4jMDC, key);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return value;
			//return org.apache.log4j.MDC.get(key);
	    }

	    public void put(String key, String val) {
			Method m = getMethod("put", String.class, Object.class);
			if (m==null) return ;
			try {
				m.invoke(log4jMDC, key, val);
			} catch (Exception e) {
				e.printStackTrace();
			}
	        //org.apache.log4j.MDC.put(key, val);
	    }

	    public void remove(String key) {
			Method m = getMethod("remove", String.class);
			if (m==null) return;
			try {
				m.invoke(log4jMDC, key);
			} catch (Exception e) {
				e.printStackTrace();
			}
	        //org.apache.log4j.MDC.remove(key);
	    }

		@Override
		public Map<String, String> getCopyOfContextMap() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setContextMap(Map<String, String> contextMap) {
			// TODO Auto-generated method stub
			
		}

	}
}
