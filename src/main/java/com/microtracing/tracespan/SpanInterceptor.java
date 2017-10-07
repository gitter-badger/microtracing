package com.microtracing.tracespan;

public interface SpanInterceptor<T1, T2> {
	public Span extract(T1 in);
	public void inject(Span span, T2 out);
}
