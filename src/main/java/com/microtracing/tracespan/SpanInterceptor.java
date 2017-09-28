package com.microtracing.tracespan;

public interface SpanInterceptor<Tin, Tout> {
	public Span extract(Tin in);
	public void inject(Span span, Tout out);
}
