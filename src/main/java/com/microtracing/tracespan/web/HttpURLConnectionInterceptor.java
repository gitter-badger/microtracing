package com.microtracing.tracespan.web;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microtracing.tracespan.Span;
import com.microtracing.tracespan.Tracer;
import com.microtracing.tracespan.ClientSpanInterceptor;

/*
 * client side injector
 */
public class HttpURLConnectionInterceptor implements ClientSpanInterceptor<HttpURLConnection, HttpURLConnection>{
	private static final Logger logger =  LoggerFactory.getLogger(HttpURLConnectionInterceptor.class);
	
	/**
	 * extract server span from urlconnection response 
	 */
	public  Span extract(HttpURLConnection conn){
		if (conn == null || conn.getHeaderField(Span.SPAN_ID_NAME) == null)
			return null;
		
		Map<String,String> carrier = new HashMap<String,String>();
		for (String headerName : Span.SPAN_HEADERS) {
			if (conn.getHeaderField(headerName) != null)
				carrier.put(headerName, conn.getHeaderField(headerName));
		}
		Span serverSpan = Span.buildSpan(carrier);		
		serverSpan.setRemote(true);
		logger.debug(serverSpan + " extracted.");
		//should be currentthread tracer
		//Tracer tracer = Tracer.getTracer(serverSpan.getTraceId());
		
		return serverSpan;	
	}
	
	/**
	 * inject into urlconnection request 
	 */
	public  void inject(Span span, HttpURLConnection conn){
		Map<String,String> carrier = span.toMap();
		for (String headerName : carrier.keySet()) {
			conn.setRequestProperty(headerName, carrier.get(headerName));
		}
		logger.debug("{} injected into HttpURLConnection request. {}={} {}={} ", span, Span.TRACE_ID_NAME,conn.getRequestProperty(Span.TRACE_ID_NAME), Span.SPAN_ID_NAME,conn.getRequestProperty(Span.SPAN_ID_NAME));
	}
}
