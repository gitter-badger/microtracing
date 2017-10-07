package com.microtracing.tracespan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
public class Span{
	private static final Logger log = Logger.getLogger(Span.class.getName());
	
	public static final String TRACE_ID_NAME = "X-B3-TraceId";
	public static final String SPAN_ID_NAME = "X-B3-SpanId";
	public static final String PARENT_ID_NAME = "X-B3-ParentSpanId";
	public static final String SPAN_NAME_NAME = "X-Span-Name";
	
	public static final Set<String> SPAN_HEADERS = new HashSet<String>(
			Arrays.asList(PARENT_ID_NAME, TRACE_ID_NAME,SPAN_ID_NAME, SPAN_NAME_NAME));
	

	public static final String SPAN_START = "SPAN_START";
	public static final String SPAN_END = "SPAN_END";
	
	public static final String CLIENT_RECV = "cr";
	public static final String CLIENT_SEND = "cs";
	public static final String SERVER_RECV = "sr";
	public static final String SERVER_SEND = "ss";
	
	
	private String traceId;
	private String spanId;
	private String name;
	
	private boolean remote;

	private Span parentSpan;
	private Set<Span> childSpans = new HashSet<Span>();
	
	private long startTime;
	private long endTime;

	
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public void setSpanId(String spanId) {
		this.spanId = spanId;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public boolean isRemote() {
		return remote;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	public String getTraceId() {
		return traceId;
	}
	
	public String getParentSpanId() {
		return this.parentSpan == null? null:parentSpan.getSpanId();
	}
	
	public String getSpanId() {
		return spanId;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public String getName() {
		return name;
	}

	private String genSpanId(){
		String[]  uuid = UUID.randomUUID().toString().split("-");
		return uuid[0]+uuid[3]; // 12 chars
	}	
	
	public Span(String traceId, Span parentSpan, String spanId, String operationName){
		this.traceId = traceId;
		this.spanId = spanId == null?genSpanId():spanId;
		this.name = operationName;
		this.parentSpan = parentSpan;
	}

	public Span createChildSpan(String operationName){
		Span child = new Span(this.traceId, this, null, operationName);
		childSpans.add(child);
		return child;
	}		
	
	
	public void setParentSpan(Span parentSpan) {
		this.parentSpan= parentSpan;
	}
	
	public Span getParentSpan() {
		return this.parentSpan;
	}
	
	public Set<Span> getChildSpans() {
		return childSpans;
	}
	
	
	public void start(){
		startTime = System.currentTimeMillis();
		Tracer.getTracer().setCurrentSpan(this);
		log.info(SPAN_START + " " + this.toString());
	}
	
	public void finish(){
		endTime = System.currentTimeMillis();
		log.info(SPAN_END + " spanId=" +this.spanId + " duration=" + (endTime-startTime) );
	}
	
	public void logEvent(String event) {
		log.info(event);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof Span) {
			Span span = (Span) o;		
			return (span != null 
					&& span.getTraceId()!=null && span.getTraceId().equals(this.traceId) 
					&& span.getSpanId()!=null && span.getSpanId().equals(this.getSpanId()) );
		}
		return false;
	}
	
	
	public Map<String, String> toMap(){
		Map<String,String> carrier = new HashMap<String,String>();
		carrier.put(Span.TRACE_ID_NAME, this.getTraceId());
		carrier.put(Span.PARENT_ID_NAME, this.getParentSpanId());
		carrier.put(Span.SPAN_ID_NAME, this.getSpanId());
		carrier.put(Span.SPAN_NAME_NAME, this.getName());
		return carrier;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Span(");
		sb.append("spanId=").append(this.spanId).append(" ");
		sb.append("traceId=").append(this.traceId).append(" ");
		if(this.parentSpan!=null) sb.append("parentId=").append(this.getParentSpanId()).append(" ");
		sb.append("spanName=").append(this.name).append(" ");
		sb.append(")");
		return sb.toString();
	}
	
	public static Span buildSpan(Map<String, String> carrier) {
		String traceId = carrier.get(Span.TRACE_ID_NAME);
		String parentId = carrier.get(Span.PARENT_ID_NAME);
		String spanId = carrier.get(Span.SPAN_ID_NAME);
		String name = carrier.get(Span.SPAN_NAME_NAME);		
		
		Span parentSpan = parentId==null?null:new Span(traceId,null,parentId, null);
		Span span = new Span(traceId,parentSpan, spanId, name);
		
		return span;
	}
	
	
	
}