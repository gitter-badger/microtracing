package com.microtracing.logagent;
import java.lang.instrument.Instrumentation;
public class LogTraceAgent{
    public static void premain(String agentOps, Instrumentation inst) {
		LogTraceConfig config = new LogTraceConfig(agentOps);
        inst.addTransformer(new LogTraceTransformer(config));
    }
}
