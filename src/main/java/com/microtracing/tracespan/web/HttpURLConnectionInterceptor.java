package com.microtracing.tracespan.web;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.microtracing.tracespan.Span;
import com.microtracing.tracespan.ClientSpanInterceptor;

/*
 * client side injector
 */
public class HttpURLConnectionInterceptor implements ClientSpanInterceptor<HttpURLConnection, HttpURLConnection>{
	private static final org.apache.log4j.Logger logger =  org.apache.log4j.LogManager.getLogger(HttpURLConnectionInterceptor.class);  

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
		logger.info(serverSpan + " extracted.");
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
	}
}
