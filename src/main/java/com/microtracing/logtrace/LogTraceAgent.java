package com.microtracing.logtrace;
import java.lang.instrument.Instrumentation;
public class LogTraceAgent{
    public static void premain(String agentOps, Instrumentation inst) {
		LogTraceConfig config = new LogTraceConfig();
        inst.addTransformer(new LogTraceTransformer(config));
    }
}
