package com.microtracing.traceagent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.net.*;
public class LogTraceConfig{
	private static final String CONFIG_FILE_NAME = "logtrace.properties";
	private static File DEFAULT_CONFIG_FILE = new File(System.getProperty("user.home"), "/.logtrace/" + CONFIG_FILE_NAME);
	
	protected  int logMethodLatency = 100; 
	protected  Set<String> includePackages = new HashSet<String>();
    protected  Map<String, List<String>> traceMethodCall = new HashMap<String, List<String>>(); // class, methods
    protected  Map<String, List<String>> traceMethodProcess = new HashMap<String, List<String>>(); // class, methods
	
	public int getLogMethodLatency(){
		return logMethodLatency;
	}
	
	public  void addProfileClass(String className) {
		includePackages.add(className);
	}	
	
    public  void addTraceMethodCall(String methodString) {
        String className = methodString.substring(0, methodString.lastIndexOf("."));
        String methodName = methodString.substring(methodString.lastIndexOf(".") + 1);
        List<String> list = traceMethodCall.get(className);
        if (list == null) {
            list = new ArrayList<String>();
            traceMethodCall.put(className, list);
        }
        list.add(methodName);
		addProfileClass(className);
    }
	
    public  void addTraceMethodProcess(String methodString) {
        String className = methodString.substring(0, methodString.lastIndexOf("."));
        String methodName = methodString.substring(methodString.lastIndexOf(".") + 1);
        List<String> list = traceMethodProcess.get(className);
        if (list == null) {
            list = new ArrayList<String>();
            traceMethodProcess.put(className, list);
        }
        list.add(methodName);
		addProfileClass(className);
    }
	
	public  boolean isNeedInject(String className) {
		if (traceMethodCall.containsKey(className)){
			return true;
		}
		for (String v : includePackages) {
			if (className.startsWith(v)) {
				return true;
			}
		}
		return false;
	}
	
	public  boolean isNeedCallInject(String className, String methodName) {
		List<String> methods = traceMethodCall.get(className);
		if (methods !=null)	for (String v : methods) {
			if (methodName.equals(v)) {
				return true;
			}
		}
		return false;
	}	

	public  boolean isNeedProcessInject(String className, String methodName) {
		List<String> methods = traceMethodProcess.get(className);
		if (methods !=null)	for (String v : methods) {
			if (methodName.equals(v)) {
				return true;
			}
		}
		return false;
	}	
	
	public LogTraceConfig() {
		String specifiedConfigFileName = System.getProperty(CONFIG_FILE_NAME);

		File givenConfigFile = specifiedConfigFileName == null ? null : new File(specifiedConfigFileName);
		File classConfigFile = null;
		try{
			URL url = Thread.currentThread().getContextClassLoader().getResource(CONFIG_FILE_NAME);
			if (url!=null){
				classConfigFile = new File(url.toURI());
			}
		}catch (URISyntaxException ue){
		}
		
		File configFiles[] = {
					  givenConfigFile,  // given path 
			          classConfigFile,  // class path
					  new File(CONFIG_FILE_NAME), // current work path 
					  DEFAULT_CONFIG_FILE // default path
		};

		for (File file : configFiles){
			if (file != null && file.exists() && file.isFile()) {
				System.out.println(String.format("load configuration from \"%s\".", file.getAbsolutePath()));
				parseProperty(file);
				return;
			}
		} 
		// load default
		System.out.println(String.format("load configuration from \"%s\".", DEFAULT_CONFIG_FILE.getAbsolutePath()));
		try {
			extractDefaultProfile();
			parseProperty(DEFAULT_CONFIG_FILE);
		} catch (IOException e) {
			throw new RuntimeException("error load config file " + DEFAULT_CONFIG_FILE, e);
		}		
	}
	

	
	private void extractDefaultProfile() throws IOException {
		InputStream in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));
		OutputStream out = null;
		try{
		  File profileDirectory = DEFAULT_CONFIG_FILE.getParentFile();
		  if (!profileDirectory.exists()){
			profileDirectory.mkdirs();
		  }
		  out = new BufferedOutputStream(new FileOutputStream(DEFAULT_CONFIG_FILE));
		  byte[] buffer = new byte[1024];
		  for (int len = -1; (len = in.read(buffer)) != -1;){
			out.write(buffer, 0, len);
		  }
		}finally{
		  in.close();
		  out.close();
		}
	}

	
	private void parseProperty(File path) {
		Properties properties = new Properties();
		try {
			properties.load(new FileReader(path)); 
			
			Properties context = new Properties(); 
			context.putAll(System.getProperties());
			context.putAll(properties);
			
			loadConfig(properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadConfig(Properties properties)  {
		String slogMethodLatency = properties.getProperty("logMethodLatency");
		if (slogMethodLatency!=null) this.logMethodLatency = Integer.parseInt(slogMethodLatency);
		
		String sIncludePackages = properties.getProperty("includePackages");
		if (sIncludePackages!=null && sIncludePackages.trim().length()>0){
			String[] _includes = sIncludePackages.split(";");
			for (String pack : _includes) {
				addProfileClass(pack);
			}
		}
		
		String sTraceMethodCall = properties.getProperty("traceMethodCall");
		if (sTraceMethodCall!=null && sTraceMethodCall.trim().length()>0){
			String[] _methods = sTraceMethodCall.split(";");
			for (String pack : _methods) {
				addTraceMethodCall(pack);
			}
		}
		
		String sTraceMethodProcess = properties.getProperty("traceMethodProcess");
		if (sTraceMethodProcess!=null && sTraceMethodProcess.trim().length()>0){
			String[] _methods = sTraceMethodProcess.split(";");
			for (String pack : _methods) {
				addTraceMethodProcess(pack);
			}
		}
	}

}