package com.starsloader.logtrace;
import java.lang.instrument.Instrumentation;
public class LogAgent{
    public static void premain(String agentOps, Instrumentation inst) {
		LogTraceConfig config = new LogTraceConfig();
        inst.addTransformer(new LogTransformer(config));
    }
}
