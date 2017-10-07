
package com.microtracing.tracespan;


public class SpanEvent {

	private final long timestamp;
	private final String event;
	private String msg;
	
	public SpanEvent(long timestamp,String event) {
		this(timestamp, event, null);
	}
	
	public SpanEvent(long timestamp,String event,String msg) {
		if (event == null) throw new NullPointerException("event");
		this.timestamp = timestamp;
		this.event = event;
		this.msg = msg;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public String getEvent() {
		return this.event;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return this.msg;
	}

	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof SpanEvent) {
			SpanEvent that = (SpanEvent) o;
			return (this.timestamp == that.timestamp)
					&& (this.event.equals(that.event));
		}
		return false;
	}

	@Override 
	public String toString() {
		StringBuffer sb = new StringBuffer("SpanEvent{")   ;
		sb.append("timestamp=").append(this.timestamp);
		sb.append(", event=").append(this.event);
		if (this.msg!=null) sb.append(", msg=\"").append(this.msg).append("\"");
		sb.append("}");
		return sb.toString();
	}
}
