package com.microtracing.tracespan.interceptors;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.microtracing.tracespan.Span;
import com.microtracing.tracespan.ClientSpanInterceptor;

/*
 * client side injector
 */
public class HttpURLConnectionInterceptor implements ClientSpanInterceptor<HttpURLConnection, HttpURLConnection>{
	
	/**
	 * extract server span from urlconnection response 
	 */
	public  Span extract(HttpURLConnection conn){
		Map<String,String> carrier = new HashMap<String,String>();
		for (String headerName : Span.SPAN_HEADERS) {
			if (conn.getHeaderField(headerName) != null)
				carrier.put(headerName, conn.getHeaderField(headerName));
		}
		Span serverSpan = Span.buildSpan(carrier);		
		serverSpan.setRemote(true);
		
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
