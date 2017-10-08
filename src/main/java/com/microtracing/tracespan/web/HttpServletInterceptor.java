package com.microtracing.tracespan.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microtracing.tracespan.ServerSpanInterceptor;
import com.microtracing.tracespan.Span;
import com.microtracing.tracespan.Tracer;

/*
 * server side injector
 */
public class HttpServletInterceptor  implements ServerSpanInterceptor<HttpServletRequest,HttpServletResponse>{
	private static final org.apache.log4j.Logger logger =  org.apache.log4j.LogManager.getLogger(HttpServletInterceptor.class);  

	/**
	 * extract client span from request 
	 */
	public Span extract(HttpServletRequest req){
		if (req == null || req.getHeader(Span.SPAN_ID_NAME) == null)
			return null;
		
		Map<String,String> carrier = new HashMap<String,String>();
		for (String headerName : Span.SPAN_HEADERS) {
			if (req.getHeader(headerName) != null)
				carrier.put(headerName, req.getHeader(headerName));
		}

		Span clientSpan = Span.buildSpan(carrier);
		clientSpan.setRemote(true);
		logger.info(clientSpan.toString() + " extracted.");
		
		clientSpan.logEvent(Span.SERVER_RECV);
		
		Tracer tracer = Tracer.getTracer(clientSpan.getTraceId());
		tracer.setClientSpan(clientSpan);
		
		return clientSpan;	
		
	}
	
	/**
	 * inject span into response 
	 */
	public void inject(Span span, HttpServletResponse resp){
		Map<String,String> carrier = span.toMap();
		for (String headerName :  carrier.keySet()) {
			resp.setHeader(headerName, carrier.get(headerName));
		}
	}

}
