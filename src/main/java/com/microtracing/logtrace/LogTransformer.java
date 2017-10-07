package com.microtracing.logtrace;
import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain; 

import com.microtracing.logtrace.injectors.ExceptionInjector;
import com.microtracing.logtrace.injectors.HttpURLConnectionRecvInjector;
import com.microtracing.logtrace.injectors.HttpURLConnectionSendInjector;
import com.microtracing.logtrace.injectors.LogInjector;
import com.microtracing.logtrace.injectors.TimerInjector;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
public class LogTransformer  implements ClassFileTransformer{
	//private static final org.apache.log4j.Logger logger =  org.apache.log4j.LogManager.getLogger(LogTransformer.class);
	private static final java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(LogTransformer.class.getName());
	
	private LogTraceConfig config;
	
	public LogTransformer(LogTraceConfig config){
		this.config = config;
	}
	
	private CtClass interceptClass(CtClass ctclass, ClassInjector injector){
		if (!injector.isNeedInject(ctclass.getName())) return ctclass;
		try{
			for (String fieldStr : injector.getClassFields(ctclass.getName())){
				CtField ctfield = CtField.make(fieldStr, ctclass);
				ctclass.addField(ctfield);
			}
		}catch(CannotCompileException ce){
			ce.printStackTrace();
		}
		return ctclass;
	}

	private CtMethod interceptCall(CtMethod ctmethod, final CallInjector injector){
		if (cannotInject(ctmethod)) return ctmethod;
		try{
			ctmethod.instrument(
				new ExprEditor() {
					public void edit(MethodCall m)
								  throws CannotCompileException{
						if (injector.isNeedCallInject(m.getClassName(), m.getMethodName())){
							String wrap =  String.format("{\n%1$s\n\t  $_ = $proceed($$); \n%2$s\n}",  
										injector.getMethodCallBefore(m.getClassName(), m.getMethodName()),
										injector.getMethodCallAfter(m.getClassName(), m.getMethodName()));
							//System.out.println(wrap);
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
		
		boolean needTraceInject = injector.isNeedProcessInject(ctmethod.getDeclaringClass().getName(), ctmethod.getName());					
		if (!needTraceInject)  return ctmethod;
		
		ClassPool classPool = ClassPool.getDefault();
		try{
			String[][] vars = injector.getMethodVariables();
			if (vars != null && vars.length > 0) for (String[] var : injector.getMethodVariables()){
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

			String start = injector.getMethodProcessStart();
			if (start!=null && start.trim().length()>0) ctmethod.insertBefore(start);

			String end = injector.getMethodProcessReturn();
			if (end!=null && end.trim().length()>0) ctmethod.insertAfter(end);

			String ex = injector.getMethodProcessException();
			if (ex!=null && ex.trim().length()>0) ctmethod.addCatch(ex, classPool.get("java.lang.Exception"), "_$e"); 
			
			String fin = injector.getMethodProcessFinally();
			if (fin!=null && fin.trim().length()>0) ctmethod.insertAfter(fin, true);
		}catch(NotFoundException ne){
			logger.warning(ne + " method: " + ctmethod + " injector: " + injector);
		}catch(CannotCompileException ce){
			logger.warning(ce + " method: " + ctmethod + " injector: " + injector);
		}catch(Exception ex){
			logger.warning(ex + " method: " + ctmethod + " injector: " + injector);
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
			
			LogInjector logInjector = new LogInjector(config);
			TimerInjector timerInjector = new TimerInjector(config);
			ExceptionInjector exInjector = new ExceptionInjector(config);
			
			HttpURLConnectionSendInjector urlSendInjector = new HttpURLConnectionSendInjector(config);
			HttpURLConnectionRecvInjector urlRecvInjector = new HttpURLConnectionRecvInjector(config);
			
			ClassInjector[] classInjectors = new ClassInjector[]{logInjector};
			CallInjector[] callInjectors = new CallInjector[]{logInjector, urlSendInjector, urlRecvInjector};
			MethodInjector[] methodInjectors = new MethodInjector[]{logInjector, timerInjector, exInjector};
			
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
