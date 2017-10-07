package com.microtracing.tracespan.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microtracing.tracespan.Span;
import com.microtracing.tracespan.Tracer;

public class TraceFilter implements Filter {
	protected static final String TRACER_REQUEST_ATTR = TraceFilter.class.getName() + ".TRACER";
	
	private FilterConfig filterConfig;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		Tracer tracer = getTracer(request);
		Span rootSpan = tracer.getThreadRootSpan();
		rootSpan.start();
		try {
			Span clientSpan = tracer.getClientSpan();
			if (clientSpan != null) {
				//log ServerSend event to clientSpan
				chain.doFilter(request, new TraceHttpServletResponse(response, clientSpan));
			}else {
				chain.doFilter(request, response);
			}
		} finally {
			rootSpan.stop();
		}
	}
	
	private Tracer getTracer(HttpServletRequest request) {
		Tracer tracer = (Tracer)request.getAttribute(TRACER_REQUEST_ATTR);
		if (tracer == null) {
			HttpServletInterceptor inter = new HttpServletInterceptor();
			Span clientSpan = inter.extract(request);
			if (clientSpan != null) {
				tracer = Tracer.getTracer(clientSpan.getTraceId());
				tracer.setClientSpan(clientSpan);
			}else {
				tracer = Tracer.getTracer();
			}
			String name = request.getRequestURL().toString();
			Span span = tracer.getThreadRootSpan();
			span.setName(name);
			request.setAttribute(TRACER_REQUEST_ATTR, tracer);
		}
		return tracer;
	}
	
	@Override
	public void destroy() {
	}

}
