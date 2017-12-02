package com.microtracing.tracespan.web;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HandlesTypes({ TraceFilter.class }) 
public class TraceServletContainerInitializer implements ServletContainerInitializer {
	private static final Logger logger =  LoggerFactory.getLogger(TraceFilter.class);

	private static final String FILTER_MAPPING = "/*"; 
	
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
	    logger.info("add filter : " + TraceFilter.class.getName()); 
	    FilterRegistration.Dynamic filter = ctx.addFilter( 
	    		TraceFilter.class.getSimpleName(), TraceFilter.class); 

	    EnumSet<DispatcherType> dispatcherTypes = EnumSet 
	        .allOf(DispatcherType.class); 
	    dispatcherTypes.add(DispatcherType.REQUEST); 
	    dispatcherTypes.add(DispatcherType.FORWARD); 

	    filter.addMappingForUrlPatterns(dispatcherTypes, true, FILTER_MAPPING); 
	    
	    logger.info("add System.out & System.err to log");
	    if (!(System.out instanceof LogPrintStream)) {
	    	LogPrintStream out = new LogPrintStream(System.out);
	    	System.setOut(out);
	    }
		/*
	    if (!(System.err instanceof LogPrintStream)) {
	    	LogPrintStream err = new LogPrintStream(System.err);
	    	System.setErr(err);
	    }
		*/
	}

}
