import java.util.UUID;
import java.util.Map;
public class Tracer{
	public static final String HEADER_TRACE_ID = "X-B3-TraceId";
	public static final String HEADER_SPAN_ID = "X-B3-SpanId";
	
	private String tracerId;
	private String clientSpanId;
	
	private String genTracerId(){
		String[]  uuid = UUID.randomUUID().toString().split("-"); //8-4-4-4-12
		return uuid[0]+uuid[3]; // 12 chars
	}
	
	public Tracer(){
		tracerId = genTracerId();
	}
	
	public Tracer(String tracerId, String clientSpanId){
		this.tracerId = tracerId;
		this.clientSpanId = clientSpanId;
	}	
	
	public Span buildSpan(String operationName){
		return new Span(this, null, operationName);
	}
	
	private static ThreadLocal<Tracer> tracerLocal = new ThreadLocal<Tracer>();
	
	public static Tracer getTracer(Map<String, String> headers){
		Tracer tracer = new Tracer(headers.get(HEADER_TRACE_ID), headers.get(HEADER_SPAN_ID));
		tracerLocal.set(tracer);
		return tracer;
	}
	
	public static Tracer getTracer(){
		Tracer tracer = (Tracer)tracerLocal.get();
		if (tracer == null){
			tracer = new Tracer();
			tracerLocal.set(tracer);
		}
		return tracer;
	}
}