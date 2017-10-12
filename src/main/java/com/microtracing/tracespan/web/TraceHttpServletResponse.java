
package com.microtracing.tracespan.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.microtracing.tracespan.Span;


/**
 * We want to set SS as fast as possible after the response was sent back. The response
 * can be sent back by calling either an {@link ServletOutputStream} or {@link PrintWriter}.
 */
class TraceHttpServletResponse extends HttpServletResponseWrapper {

	private final Span span;

	TraceHttpServletResponse(HttpServletResponse response, Span span) {
		super(response);
		this.span = span;
	}

	@Override public void flushBuffer() throws IOException {
		span.addEvent(Span.SERVER_SEND);
		super.flushBuffer();
	}

	@Override public ServletOutputStream getOutputStream() throws IOException {
		return new TraceServletOutputStream(super.getOutputStream(), this.span);
	}

	@Override public PrintWriter getWriter() throws IOException {
		return new TracePrintWriter(super.getWriter(), this.span);
	}
}
