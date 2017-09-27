package com.microtracing;

import java.util.UUID;
public class Span{
	private String spanId;
	private Tracer tracer;
	private Span parentSpan;

	public Tracer getTracer() {
		return tracer;
	}

	public String getSpanId() {
		return spanId;
	}

	public String getOperationName() {
		return operationName;
	}

	private String operationName;
	
	private String genSpanId(){
		String[]  uuid = UUID.randomUUID().toString().split("-");
		return uuid[0]+uuid[3]; // 12 chars
	}
	
	public Span(Tracer tracer, Span parentSpan, String operationName){
		this.tracer = tracer;
		this.parentSpan = parentSpan;
		this.operationName = operationName;
		this.spanId = genSpanId();
	}
	
	public void start(){
		//TODO
	}
	
	public void finish(){
		//TODO
	}
		
	public Span buildChildSpan(String operationName){
		return new Span(this.tracer, this, operationName);
	}		
	
	public Span getParentSpan() {
		return this.parentSpan;
	}
}