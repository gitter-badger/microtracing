
package com.microtracing.tracespan;


public class SpanEvent {

	private final long timestamp;
	private final String event;
	public SpanEvent(long timestamp,String event) {
		if (event == null) throw new NullPointerException("event");
		this.timestamp = timestamp;
		this.event = event;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public String getEvent() {
		return this.event;
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
		return "SpanEvent{" +
				"timestamp=" + this.timestamp +
				", event='" + this.event + '\'' +
				'}';
	}
}
