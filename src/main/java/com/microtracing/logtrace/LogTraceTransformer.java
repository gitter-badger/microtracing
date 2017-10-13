package com.microtracing.logtrace;
import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import com.microtracing.logtrace.injectors.ExceptionInjector;
import com.microtracing.logtrace.injectors.HttpURLConnectionRecvInjector;
import com.microtracing.logtrace.injectors.HttpURLConnectionSendInjector;
import com.microtracing.logtrace.injectors.JdbcExecuteInjector;
import com.microtracing.logtrace.injectors.JdbcStatementInjector;
import com.microtracing.logtrace.injectors.LogInjector;
import com.microtracing.logtrace.injectors.SpanCallInjector;
import com.microtracing.logtrace.injectors.SpanMethodInjector;
import com.microtracing.logtrace.injectors.TimerInjector;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
public class LogTraceTransformer  implements ClassFileTransformer{
	//private static final org.slf4j.Logger logger =  org.slf4j.LoggerFactory.getLogger(ClassFileTransformer.class);
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ClassFileTransformer.class.getName());
			
	private LogTraceConfig config;
	
	private Set<ClassInjector> classInjectors = new HashSet<ClassInjector>();
	private Set<CallInjector> callInjectors = new HashSet<CallInjector>();
	private Set<MethodInjector> methodInjectors = new HashSet<MethodInjector>();
	
	public LogTraceTransformer(LogTraceConfig config){
		this.config = config;
		initInjectors();
	}
	

	private void initInjectors() {
		LogInjector logInjector = new LogInjector(config);
		TimerInjector timerInjector = new TimerInjector(config);
		ExceptionInjector exInjector = new ExceptionInjector(config);
		
		SpanCallInjector spanCallInjector = new SpanCallInjector(config);
		SpanMethodInjector spanMethodInjector = new SpanMethodInjector(config);
		
		HttpURLConnectionSendInjector urlSendInjector = new HttpURLConnectionSendInjector(config);
		HttpURLConnectionRecvInjector urlRecvInjector = new HttpURLConnectionRecvInjector(config);
		
		JdbcStatementInjector jdbcStmtInjector = new JdbcStatementInjector(config);
		JdbcExecuteInjector jdbcExeInjector = new JdbcExecuteInjector(config);
		
		if (config.isEnableLog()) classInjectors.add(logInjector);
		logger.fine("ClassInjector:"+classInjectors.toString());		

		callInjectors.add(spanCallInjector);
		if (config.isEnableHttpURLConnectionTrace()) {
			callInjectors.add(urlSendInjector);
			callInjectors.add(urlRecvInjector);
		}
		if (config.isEnableJdbcTrace()) {
			callInjectors.add(jdbcStmtInjector);
			callInjectors.add(jdbcExeInjector);
		}
		logger.fine("CallInjector:"+callInjectors.toString());
		
		methodInjectors.add(spanMethodInjector);
		if (config.isEnableTimingLog()) {
			methodInjectors.add(timerInjector);
		}
		if (config.isEnableExceptionLog()) {
			methodInjectors.add(exInjector);
		}
		logger.fine("MethodInjector:"+methodInjectors.toString());		
	}
		
	private CtClass interceptClass(CtClass ctclass, ClassInjector injector){
		if (!injector.isNeedInject(ctclass.getName())) return ctclass;
		try{
			for (String fieldStr : injector.getClassFields(ctclass.getName())){
				CtField ctfield = CtField.make(fieldStr, ctclass);
				ctclass.addField(ctfield);
			}
			//logger.finest("Inject into " + ctclass.getName());
		}catch(CannotCompileException ce){
			ce.printStackTrace();
		}
		return ctclass;
	}

	private CtMethod interceptCall(CtMethod ctmethod, final CallInjector injector){
		if (cannotInject(ctmethod)) return ctmethod;
		final String className = ctmethod.getDeclaringClass().getName();
		final String methodName = ctmethod.getName();
		try{
			ctmethod.instrument(
				new ExprEditor() {
					public void edit(MethodCall m)
								  throws CannotCompileException{
						if (injector.isNeedCallInject(m.getClassName(), m.getMethodName())){
							String callClassName = m.getClassName();
							String callMethodName = m.getMethodName();
							String wrap =  String.format("{\n%1$s\n\t  $_ = $proceed($$); \n%2$s\n}",  
										injector.getMethodCallBefore(callClassName, callMethodName),
										injector.getMethodCallAfter(callClassName, callMethodName));

		
							//logger.finest(String.format("Inject method call  %s.%s in %s.%s \n%s", callClassName, callMethodName, className, methodName, wrap));
							m.replace(wrap);
						}
					}
				});
		}catch(CannotCompileException ce){
			ce.printStackTrace();
		}
		return ctmethod;
	}
	
	private CtMethod interceptMethod(CtMethod ctmethod, MethodInjector injector){
		if (cannotInject(ctmethod)) return ctmethod;
		
		String className = ctmethod.getDeclaringClass().getName();
		String methodName = ctmethod.getName();
		
		//logger.finest("Inject into " + className + "." + methodName);
		
		boolean needTraceInject = injector.isNeedProcessInject(className, methodName);					
		if (!needTraceInject)  return ctmethod;
		
		ClassPool classPool = ClassPool.getDefault();
		try{
			String[][] vars = injector.getMethodVariables(className, methodName);
			if (vars != null && vars.length > 0) for (String[] var : vars){
				String type = var[0];
				CtClass cttype;
				if ("boolean".equals(type)){
					cttype = CtClass.booleanType;
				}else if("byte".equals(type)){
					cttype = CtClass.	byteType;
				}else if("char".equals(type)){
					cttype = CtClass.charType;
				}else if("double".equals(type)){
					cttype = CtClass.doubleType;
				}else if("float".equals(type)){
					cttype = CtClass.floatType;
				}else if("int".equals(type)){
					cttype = CtClass.intType;
				}else if("long".equals(type)){
					cttype = CtClass.longType;
				}else if("short".equals(type)){
					cttype = CtClass.shortType;
				}else if("void".equals(type)){
					cttype = CtClass.voidType;
				}else{
					cttype = classPool.get(type);
				}
				ctmethod.addLocalVariable(var[1], cttype);
			}

			String start = injector.getMethodProcessStart(className, methodName);
			String end = injector.getMethodProcessReturn(className, methodName);
			String ex = injector.getMethodProcessException(className, methodName);
			String fin = injector.getMethodProcessFinally(className, methodName);
			
			//logger.finest(String.format("Inject method process  %s.%s \n%s \n...\n%s \n...\n%s \n...\n%s", className, methodName, start, end, ex, fin));
			
			if (start!=null && start.trim().length()>0) ctmethod.insertBefore(start);
			if (end!=null && end.trim().length()>0) ctmethod.insertAfter(end);
			if (ex!=null && ex.trim().length()>0) ctmethod.addCatch(ex, classPool.get("java.lang.Exception"), "_$e"); 
			if (fin!=null && fin.trim().length()>0) ctmethod.insertAfter(fin, true);
		}catch(NotFoundException ne){
			logger.warning(ne + " method: " + className +"." + methodName + " injector: " + injector);
		}catch(CannotCompileException ce){
			logger.warning(ce + " method: " + className +"." + methodName + " injector: " + injector);
		}catch(Exception ex){
			logger.warning(ex + " method: " + className +"." + methodName + " injector: " + injector);
		}
		return ctmethod;
	}	
	
	private boolean cannotInject(CtMethod ctmethod){
		if (ctmethod.isEmpty() || Modifier.isNative(ctmethod.getModifiers()) || Modifier.isAbstract(ctmethod.getModifiers())) return true;
		return false;
	}
	
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/", ".");
        if (!config.isNeedInject(className)) {
            return classfileBuffer;
        }
		CtClass ctclass = null;
        try {
            ClassPool classPool = ClassPool.getDefault();
            ctclass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
			ctclass.setName(className);
			
			if (ctclass.isInterface()){
			    return classfileBuffer;
			}
			
			for(ClassInjector injector : classInjectors){
				interceptClass(ctclass, injector);
			}
            for (CtMethod ctmethod : ctclass.getDeclaredMethods()) {
				if (cannotInject(ctmethod)) continue;
				for(CallInjector injector : callInjectors){
					interceptCall(ctmethod, injector);
				}
				for(MethodInjector injector : methodInjectors){
					interceptMethod(ctmethod, injector);
				}
            }
			
            byte[] byteCode = ctclass.toBytecode();
            return byteCode;
        } catch (Exception ex) {
            ex.printStackTrace();
            return classfileBuffer;
        } finally{
			if (ctclass != null){
				ctclass.detach();
			}
		}
    }

}
