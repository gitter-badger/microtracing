package com.microtracing.tracespan;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tracer{
	private static final Logger logger =  LoggerFactory.getLogger(Tracer.class);
	
	private String traceId;

	private Span clientSpan;
	private Span threadRootSpan;
	private Span currentSpan;
	
	public Tracer(String traceId){
		if (traceId == null){
			traceId = genTraceId();
			this.traceId = traceId;
			//use traceId as rootSpanId
			this.threadRootSpan = new Span(traceId, null, traceId, Thread.currentThread().getName());
		}else {
			this.traceId = traceId;
			this.threadRootSpan = new Span(traceId, null, null, Thread.currentThread().getName());
		}
		setCurrentSpan(this.threadRootSpan);
	}
	
	private static String genTraceId(){
		String[]  uuid = UUID.randomUUID().toString().split("-"); //8-4-4-4-12
		return uuid[0]+uuid[2]+uuid[3]; // 16 chars
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
		MDCHelper.put(Span.SPAN_ID_NAME, currentSpan.getSpanId());
	}

	public Span getThreadRootSpan() {
		return threadRootSpan;
	} 
	
	public void clear(){
		if (!threadRootSpan.isStopped()) threadRootSpan.stop();
		tracerLocal.set(null);
		logger.debug(this.toString()+" clear from current thread.");
		MDCHelper.remove(Span.SPAN_ID_NAME);
		MDCHelper.remove(Span.TRACE_ID_NAME);
	}
	
	public Span createSpan(String operationName){
		Span span = this.currentSpan.createChildSpan(operationName);
		this.currentSpan = span;
		return span;
	}
	
	
	
	
	
	private static ThreadLocal<Tracer> tracerLocal = new ThreadLocal<Tracer>();
	
	public static Tracer getTracer(String traceId){
		Tracer tracer = (Tracer)tracerLocal.get();
		if (tracer == null || (traceId!=null && !traceId.equals(tracer.getTraceId()))) {
			tracer = new Tracer(traceId);
			tracerLocal.set(tracer);
			MDCHelper.put(Span.TRACE_ID_NAME, tracer.getTraceId());
			logger.debug(tracer.toString()+" generated.");
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