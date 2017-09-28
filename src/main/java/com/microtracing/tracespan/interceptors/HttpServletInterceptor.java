package com.microtracing.tracespan.interceptors;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microtracing.tracespan.Span;
import com.microtracing.tracespan.SpanInterceptor;
import com.microtracing.tracespan.Tracer;

/*
 * server side injector
 */
public class HttpServletInterceptor  implements SpanInterceptor<HttpServletRequest,HttpServletResponse>{

	/**
	 * extract client span from request 
	 */
	public Span extract(HttpServletRequest req){
		Map<String,String> carrier = new HashMap<String,String>();
		for (String headerName : Span.SPAN_HEADERS) {
			if (req.getHeader(headerName) != null)
				carrier.put(headerName, req.getHeader(headerName));
		}

		Span clientSpan = Span.buildSpan(carrier);
		clientSpan.setRemote(true);
		
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
