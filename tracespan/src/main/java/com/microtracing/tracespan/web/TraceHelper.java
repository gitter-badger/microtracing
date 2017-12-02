package com.microtracing.tracespan.web;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microtracing.tracespan.Span;
import com.microtracing.tracespan.Tracer;

public class TraceHelper {

	private static final Logger logger =  LoggerFactory.getLogger(TraceHelper.class);
	
	protected static final String TRACER_REQUEST_ATTR = TraceHelper.class.getName() + ".TRACER";
	
	protected static final String IGNORE_URIS = ".*\\.(js|css|gif|jpg|jpeg|png|bmp|ico|swf|mp3|mp4|mov|avi|zip|rar|jar|pdf|doc|docx|xls|xlsx|ppt|pptx)$";
	protected static final Pattern IGNORE_URIS_PATTERN = Pattern.compile(IGNORE_URIS);

	private static final int MAX_TRY_COUNT = 100;
	
	private static String userSessionKey = null;
	private static AtomicInteger tryCount = new AtomicInteger(0);
	
	
	public static boolean ignoreTrace(HttpServletRequest request){
		return ("GET".equals(request.getMethod()) 
				&& IGNORE_URIS_PATTERN.matcher( request.getRequestURI() ).find());
	}
	
	public static boolean isTraced(HttpServletRequest request){
		return request.getAttribute(TRACER_REQUEST_ATTR)!=null;
	}
	
	public static HttpServletRequest wrapRequest(HttpServletRequest request){
		return request;
	}
	
	public static HttpServletResponse wrapResponse(HttpServletRequest request, HttpServletResponse response){
		Tracer tracer = (Tracer)request.getAttribute(TRACER_REQUEST_ATTR);
		if (tracer == null) return response;
		
		Span clientSpan = tracer.getClientSpan();
		if (clientSpan != null){
			response = new TraceHttpServletResponse(response, clientSpan);
		};
		return response;
	}
	
	/**
	 * start trace for new request 
	 * @param request
	 * @param response
	 * @return if new request trace started
	 */
	public static Span beforeService(HttpServletRequest request, HttpServletResponse response){
		try{
			if (ignoreTrace(request)){
				return null;
			}
	
			if (isTraced(request)){
				logRequestInfo(request, false);
				return null;
			}
	
			setStdoutLog();
			
			Tracer tracer = null;
			tracer = getTracer(request);
			Span span = tracer.getCurrentSpan();
			span.start();
			
			logRequestInfo(request, true);
			
			return span;
		}catch(Exception ex){
			logger.warn("start error", ex);
			//ignore
			return null;
		}		
	}
	
	/**
	 * stop trace for new request
	 * @param request
	 * @param response
	 * @param finish if stop trace span 
	 */
	public  static void afterService(HttpServletRequest request, HttpServletResponse response, Span span){
		if (ignoreTrace(request)){
			return ;
		}
		
		logResponseInfo(request, response, span!=null);
		
		if (span!=null){
			Tracer tracer = (Tracer)request.getAttribute(TRACER_REQUEST_ATTR);
			request.removeAttribute(TRACER_REQUEST_ATTR);
			span.stop();
		}
	}
	
	
	private  static void setStdoutLog(){
	    if (!(System.out instanceof LogPrintStream)) {
			logger.debug("add System.out to log");
	    	LogPrintStream out = new LogPrintStream(System.out);
	    	System.setOut(out);
	    }
		/*
	    if (!(System.err instanceof LogPrintStream)) {
			logger.info("add System.err to log");
	    	LogPrintStream err = new LogPrintStream(System.err);
	    	System.setErr(err);
	    }
		*/
	}
	
	private static Tracer getTracer(HttpServletRequest request) {
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
			Span span = tracer.getCurrentSpan();
			if (!span.isStarted()){
				span.setName(name);
			}else if (!name.equals(span.getName())){
				span = tracer.createSpan(name);
			}
			request.setAttribute(TRACER_REQUEST_ATTR, tracer);
		}
		return tracer;
	}

	
	private static void logRequestInfo(HttpServletRequest request, boolean logDetail){
		StringBuffer sb = request.getRequestURL();
		sb.insert(0," url=\"").append("\"");
	
		if (logDetail){
			String appName = getAppName(request);
			sb.append(" appName=\"").append(appName).append("\"");
			
			String clientIp = getClientIpAddr(request);
			sb.append(" clientIp=\"").append(clientIp).append("\"");
			
			sb.append(" remoteAddr=\"").append(request.getRemoteAddr()).append("\"");
			sb.append(" localAddr=\"").append(request.getLocalAddr()).append("\"");
			sb.append(" localPort=\"").append(request.getLocalPort()).append("\"");
			
			HttpSession session = request.getSession(false);
			if (session != null) {
				sb.append(" sessionId=\"").append(session.getId()).append("\"");
				
				String userName = getUserName(session);
				if (userName!=null){
					sb.append(" userName=").append(userName).append("");
				}
			}
			
			sb.append(" reqHeaders={ ");
			for (Enumeration<String> en = request.getHeaderNames();en.hasMoreElements();) {
				String name = en.nextElement();
				sb.append(name).append("=\"").append(request.getHeader(name)).append("\", ");
			}
			sb.append("}");
		}
		if (sb.length()>0) logger.debug(sb.toString());
	}
	
	private static String getUserName(String key, Object value){
		if (value == null) return null;
		
		String userName = null;
		if (value instanceof String) {
			if (key.toLowerCase().equals("username")) {
				userName = (String) value;
			}
		} else{
			try {
				java.lang.reflect.Method getUserNameMethod = value.getClass().getMethod("getUserName", new Class[0]);
				userName = (String) getUserNameMethod.invoke(value, new Object[0]);
			} catch (Exception ex) {
				// ignore
			}
		}
		return userName;
	}
	
	private static String getUserName(HttpSession session){
		if (session == null) return null;
		
		String userName = null;
		if (userSessionKey != null) {
			userName = getUserName(userSessionKey, session.getAttribute(userSessionKey));
		}else if (tryCount.get() < MAX_TRY_COUNT ){
			tryCount.addAndGet(1);
			//detect userName
			Enumeration<String> en = session.getAttributeNames();
			while (en.hasMoreElements()){
				String key = en.nextElement();
				userName = getUserName(key, session.getAttribute(key));
				if (userName != null){
					userSessionKey = key;
					break;
				}
			}
		}	
		return userName;
	}
	
	private static void append(StringBuffer sb, String name, String[] values){
		sb.append(name).append("=");
		if(values.length==1){
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
	}
	
	private static void logResponseInfo(HttpServletRequest request, HttpServletResponse response, boolean logDetail){
		if (!logDetail) return;
		
		StringBuffer sb = new StringBuffer();
		for (Enumeration<String> en =request.getParameterNames();en.hasMoreElements();) {
			String name = en.nextElement();
			String s = name.toLowerCase();
			if (s.endsWith("id") || s.indexOf("action")>-1 ||  s.indexOf("method")>-1 ){
				String[] values = request.getParameterValues(name);
				if (values == null) continue;
				append(sb, name, values);
				sb.append(" ");
			}
		}
		
		
		try{
			int status = response.getStatus();
			sb.append(" status=").append(status); //since servlet3.0
			sb.append(" respHeaders={ ");
			for (String name : response.getHeaderNames()) {
				sb.append(name).append("=\"").append(response.getHeader(name)).append("\" ");
			}
			sb.append("}");
		}catch(NoSuchMethodError  ex){ 
			//ignored
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
	
	private static String getAppName (HttpServletRequest request){
		String appName = null;
		try{
			appName = request.getServletContext().getServletContextName();
		}catch(NoSuchMethodError ne){
			//ignore
		}
		if (appName == null){
			appName = request.getContextPath();
			try{
				String server = request.getServerName();
				if (!Character.isDigit(server.charAt(0))){// string host name
					String[] names = server.split(".");
					String host = names[0];
					if ("wwww".equals(names[0])){
						host = names[1];
					}
					appName = host + appName;
				}
			}catch(Exception ex){
				//ignore
				//logger.debug("Parse host name failed.", ex);
			}
			if (appName.startsWith("/")){
				appName = appName.substring(1);
			}
		}
		return appName;
	}
	

}
