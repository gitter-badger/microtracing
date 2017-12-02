package com.microtracing.tracespan.web;

import java.io.OutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.spi.LocationAwareLogger;

import com.microtracing.tracespan.Span;

//should not print to ConsoleAppender
public class LogPrintStream extends PrintStream {
	private final static String FQCN = LogPrintStream.class.getName();
	private static final Logger logger =  LoggerFactory.getLogger(LogPrintStream.class);
	
	private static LocationAwareLogger locationAwareLogger = null;
	static {
	    if (logger instanceof LocationAwareLogger) {
	    	locationAwareLogger = (LocationAwareLogger) logger;
	    }
	}
	
	private boolean print2Console = true;
	private boolean print2Log = true;
	
	private void log(String log) {
		if (print2Console){
			super.println(log);
		}
		if (print2Log && logger.isDebugEnabled()) {
	        if (locationAwareLogger != null) {
	        	locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, log, null, null);
	        }else {
	        	logger.debug(log);
	        }
		}
	}
	
	
	public LogPrintStream(PrintStream out) {
		super(out);
		print2Console = true; // will call super.println
		if (out instanceof LogPrintStream){
			print2Log = false; // printed in super LogPrintStream
		}else{
			print2Log = true; // will call logger.debug
		}
	}

    public void println(boolean x) {
        log(String.valueOf(x));
    }
    public void println(char x) {
    	 log(String.valueOf(x));
    }
    public void println(char[] x) {
    	log(x == null ? null : new String(x));
    }
    public void println(double x) {
    	log(String.valueOf(x));
    }
    public void println(float x) {
    	log(String.valueOf(x));
    }
    public void println(int x) {
    	log(String.valueOf(x));
    }
    public void println(long x) {
    	log(String.valueOf(x));
    }
    public void println(Object x) {
    	log(x == null ? null : x.toString());
    }
    public void println(String x) {
    	log(x);
    }
}
