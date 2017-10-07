
package com.microtracing.tracespan.web;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import com.microtracing.tracespan.Span;


class TraceServletOutputStream extends ServletOutputStream {
	private final ServletOutputStream delegate;
	private final Span span;

	TraceServletOutputStream(ServletOutputStream delegate, Span span) {
		this.delegate = delegate;
		this.span = span;
	}

	//since 3.1.0
	public boolean isReady() {
		return this.delegate.isReady();
	}

	//since 3.1.0
	public void setWriteListener(WriteListener listener) {
		this.delegate.setWriteListener(listener);
	}

	@Override public void write(int b) throws IOException {
		this.delegate.write(b);
	}

	@Override public void print(String s) throws IOException {
		this.delegate.print(s);
	}

	@Override public void print(boolean b) throws IOException {
		this.delegate.print(b);
	}

	@Override public void print(char c) throws IOException {
		this.delegate.print(c);
	}

	@Override public void print(int i) throws IOException {
		this.delegate.print(i);
	}

	@Override public void print(long l) throws IOException {
		this.delegate.print(l);
	}

	@Override public void print(float f) throws IOException {
		this.delegate.print(f);
	}

	@Override public void print(double d) throws IOException {
		this.delegate.print(d);
	}

	@Override public void println() throws IOException {
		this.delegate.println();
	}

	@Override public void println(String s) throws IOException {
		this.delegate.println(s);
	}

	@Override public void println(boolean b) throws IOException {
		this.delegate.println(b);
	}

	@Override public void println(char c) throws IOException {
		this.delegate.println(c);
	}

	@Override public void println(int i) throws IOException {
		this.delegate.println(i);
	}

	@Override public void println(long l) throws IOException {
		this.delegate.println(l);
	}

	@Override public void println(float f) throws IOException {
		this.delegate.println(f);
	}

	@Override public void println(double d) throws IOException {
		this.delegate.println(d);
	}

	@Override public void write(byte[] b) throws IOException {
		this.delegate.write(b);
	}

	@Override public void write(byte[] b, int off, int len) throws IOException {
		this.delegate.write(b, off, len);
	}

	@Override public void flush() throws IOException {
		span.logEvent(Span.SERVER_SEND);
		this.delegate.flush();
	}

	@Override public void close() throws IOException {
		span.logEvent(Span.SERVER_SEND);
		this.delegate.close();
	}
}
