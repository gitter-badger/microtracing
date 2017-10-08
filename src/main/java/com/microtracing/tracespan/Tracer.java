package com.microtracing.tracespan;

import java.util.UUID;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.MDC;  

public class Tracer{
	private static final Logger logger = LogManager.getLogger(Tracer.class);  
	
	private String traceId;

	private Span clientSpan;
	private Span threadRootSpan;
	private Span currentSpan;
	
	public Tracer(String traceId){
		this.traceId = traceId;
		this.threadRootSpan = new Span(traceId, null, null, Thread.currentThread().getName());
		setCurrentSpan(this.threadRootSpan);
	}
	
	private static String genTraceId(){
		String[]  uuid = UUID.randomUUID().toString().split("-"); //8-4-4-4-12
		return uuid[0]+uuid[3]; // 12 chars
	}
	
	
	public String getTraceId() {
		return traceId;
	}

	public void setClientSpan(Span span) {
		this.clientSpan = span;
		this.threadRootSpan.setParentSpan(span);
	}

	public Span getClientSpan() {
		return clientSpan;
	}

	
	public Span getCurrentSpan() {
		return currentSpan;
	}

	public void setCurrentSpan(Span currentSpan) {
		if (currentSpan == null) currentSpan = this.threadRootSpan;
		this.currentSpan = currentSpan;
		MDC.put(Span.SPAN_ID_NAME, currentSpan.getSpanId());
	}

	public Span getThreadRootSpan() {
		return threadRootSpan;
	} 
	
	public void closeThreadRootSpan() {
		threadRootSpan.stop();
	}

	
	public Span createSpan(String operationName){
		Span span = this.currentSpan.createChildSpan(operationName);
		return span;
	}
	
	
	
	
	
	private static ThreadLocal<Tracer> tracerLocal = new ThreadLocal<Tracer>();
	
	public static Tracer getTracer(String traceId){
		Tracer tracer = (Tracer)tracerLocal.get();
		if (tracer == null || (traceId!=null && !traceId.equals(tracer.getTraceId()))) {
			if (traceId == null) traceId = genTraceId();
			tracer = new Tracer(traceId);
			tracerLocal.set(tracer);
			MDC.put(Span.TRACE_ID_NAME, traceId);
			logger.info(tracer.toString()+" generated.");
		}
		return tracer;
	}
	
	public static Tracer getTracer(){
		return getTracer(null);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof Span) {
			Tracer tracer = (Tracer) o;	
			return (tracer != null && tracer.getTraceId()!=null 
					&& tracer.getTraceId().equals(this.traceId)  );
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Tracer{")   ;
		sb.append("traceId=").append(this.traceId);
		sb.append("}");
		return sb.toString();
	}
	
		
			
}