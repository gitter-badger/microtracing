package com.microtracing.tracespan.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;

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
import com.microtracing.tracespan.Tracer;

public class TraceFilter implements Filter {
	private static final Logger logger =  LoggerFactory.getLogger(TraceFilter.class);
	
	protected static final String TRACER_REQUEST_ATTR = TraceFilter.class.getName() + ".TRACER";
	
	protected static final String IGNORE_URIS = ".*\\.(js|css|gif|jpg|jpeg|png|bmp|ico|swf|mp3|mp4|mov|avi|zip|rar|jar|pdf|doc|docx|xls|xlsx|ppt|pptx)$";
	protected static final Pattern IGNORE_URIS_PATTERN = Pattern.compile(IGNORE_URIS);
	
	private FilterConfig filterConfig;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		
	}
	
	private boolean ignoreTrace(HttpServletRequest request){
		return ("GET".equals(request.getMethod()) 
				&& IGNORE_URIS_PATTERN.matcher( request.getRequestURI() ).find());
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		if (ignoreTrace(request)){
			chain.doFilter(request, response);
			return;
		}
		
		boolean isFirst = (request.getAttribute(TRACER_REQUEST_ATTR)==null);
		Tracer tracer = getTracer(request);
		Span rootSpan = tracer.getThreadRootSpan();
		if (isFirst) rootSpan.start();
		logRequestInfo(request);
		try {
			Span clientSpan = tracer.getClientSpan();
			if (clientSpan != null) {
				//log ServerSend event to clientSpan
				chain.doFilter(request, new TraceHttpServletResponse(response, clientSpan));
			}else {
				chain.doFilter(request, response);
			}
		} finally {
			logResponseInfo(request, response);
			//rootSpan.stop();
			if (isFirst) {
				request.removeAttribute(TRACER_REQUEST_ATTR);
				tracer.closeThreadRootSpan();
			}
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
			String name = "WEB:"+request.getRequestURL().toString();
			Span span = tracer.getThreadRootSpan();
			span.setName(name);
			request.setAttribute(TRACER_REQUEST_ATTR, tracer);
		}
		return tracer;
	}

	
	private void logRequestInfo(HttpServletRequest request){
		StringBuffer sb = new StringBuffer();
		
		String url = request.getRequestURL().toString();
		String clientIp = getClientIpAddr(request);
		String remoteAddr = request.getRemoteAddr();
		
		sb.append(" url=").append(url);
		sb.append(" clientIp=").append(clientIp);
		sb.append(" remoteAddr=").append(remoteAddr);
		sb.append(" headers={ ");
		for (Enumeration<String> en = request.getHeaderNames();en.hasMoreElements();) {
			String name = en.nextElement();
			sb.append(name).append("=\"").append(request.getHeader(name)).append("\", ");
		}
		sb.append("}");
		if (sb.length()>0) logger.debug(sb.toString());
	}
	
	private void logResponseInfo(HttpServletRequest request, HttpServletResponse response){
		StringBuffer sb = new StringBuffer();
		for (Enumeration<String> en =request.getParameterNames();en.hasMoreElements();) {
			String name = en.nextElement();
			if (name.toLowerCase().endsWith("id")){
				sb.append(name + "=");
				String[] values = request.getParameterValues(name);
				if (values!=null && values.length==1){
					sb.append("\"").append(values[0]).append("\"");
				}else {
					sb.append("[");
					for (int i=0; i<values.length; i++){
						sb.append("\"").append(values[i]).append("\"");
						if (i<values.length-1){
							sb.append(",");
						}
					}
					sb.append("]");
				}
				sb.append(" ");
			}
		}
		if (sb.length()>0)  logger.debug(sb.toString());
	}


	public static String getClientIpAddr(HttpServletRequest request){
	       String ip = request.getHeader("x-forwarded-for");
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getHeader("Proxy-Client-IP");
	       }
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getHeader("WL-Proxy-Client-IP");
	       }
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getRemoteAddr();
	       }
	       if (ip==null) ip="-";
	       return ip;
	}	
	
	@Override
	public void destroy() {
	}

}
