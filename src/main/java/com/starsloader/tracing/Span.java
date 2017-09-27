import java.util.UUID;
public class Span{
	private Tracer tracer;
	private Span parentSpan;
	
	private String spanId;
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
}