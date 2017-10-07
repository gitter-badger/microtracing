package com.microtracing.tracespan;

import java.util.UUID;
public class Tracer{

	private String traceId;

	private Span clientSpan;
	private Span threadRootSpan;
	private Span currentSpan;
	
	
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

	private static String genTraceId(){
		String[]  uuid = UUID.randomUUID().toString().split("-"); //8-4-4-4-12
		return uuid[0]+uuid[3]; // 12 chars
	}
	
	
	public Span getCurrentSpan() {
		return currentSpan;
	}

	public void setCurrentSpan(Span currentSpan) {
		this.currentSpan = currentSpan;
	}

	public Tracer(String traceId){
		this.traceId = traceId;
		this.threadRootSpan = new Span(traceId, null, null, Thread.currentThread().getName());
		this.threadRootSpan.start();
		this.currentSpan = this.threadRootSpan;
	}
	
	
	public Span createSpan(String operationName){
		Span span = this.currentSpan.createChildSpan(operationName);
		return span;
	}
	
	public Span startSpan(String operationName) {
		Span span = createSpan(operationName);
		span.start();
		return span;
	}
	
	
	private static ThreadLocal<Tracer> tracerLocal = new ThreadLocal<Tracer>();
	
	public static Tracer getTracer(String traceId){
		Tracer tracer = (Tracer)tracerLocal.get();
		if (tracer == null || (traceId!=null && !traceId.equals(tracer.getTraceId()))) {
			if (traceId == null) traceId = genTraceId();
			tracer = new Tracer(traceId);
			tracerLocal.set(tracer);
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
			
}