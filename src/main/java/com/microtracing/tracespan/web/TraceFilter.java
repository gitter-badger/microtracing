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
import com.microtracing.tracespan.web.interceptors.HttpServletInterceptor;

public class TraceFilter implements Filter {
	protected static final String TRACE_REQUEST_ATTR = TraceFilter.class.getName() + ".TRACE";
	
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
		Span span = startSpan(request);
		try {
			chain.doFilter(request, new TraceHttpServletResponse(response, span));
		} finally {
			span.finish();
		}
	}

	
	private Span startSpan(HttpServletRequest request) {
		Span span = (Span)request.getAttribute(TRACE_REQUEST_ATTR);
		if (span == null) {
			HttpServletInterceptor inter = new HttpServletInterceptor();
			Span clientSpan = inter.extract(request);
			Tracer tracer;
			if (clientSpan != null) {
				tracer = Tracer.getTracer(clientSpan.getTraceId());
				tracer.setClientSpan(clientSpan);
			}else {
				tracer = Tracer.getTracer();
			}
			span = tracer.getCurrentSpan();
			String name = request.getRequestURL().toString();
			span.setName(name);
			span.start();
			request.setAttribute(TRACE_REQUEST_ATTR, span);
		}
		return span;
	}
	
	@Override
	public void destroy() {
	}

}
