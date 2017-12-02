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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microtracing.tracespan.Span;

public class TraceFilter implements Filter {
	private static final Logger logger =  LoggerFactory.getLogger(TraceFilter.class);
	
	private FilterConfig filterConfig;
	private TraceHelper helper;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		this.helper = new TraceHelper();
	}
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		if (helper.ignoreTrace(request)){
			chain.doFilter(request, response);
			return;
		}
		
		logger.debug("start trace");

		Span span = helper.beforeService(request, response);
		try {
			chain.doFilter(helper.wrapRequest(request), helper.wrapResponse(request, response));
		} finally {
			helper.afterService(request, response, span);
		}
	}
	
	
	@Override
	public void destroy() {
	}

}
