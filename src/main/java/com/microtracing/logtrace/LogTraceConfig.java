package com.microtracing.logtrace;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class LogTraceConfig{
	//private static final org.slf4j.Logger logger =  org.slf4j.LoggerFactory.getLogger(LogTraceConfig.class);
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LogTraceConfig.class.getName());
	
	private static final String CONFIG_FILE_NAME = "logtrace.properties";
	private static File DEFAULT_CONFIG_FILE = new File(System.getProperty("user.home"), "/.logtrace/" + CONFIG_FILE_NAME);
	
	
	private boolean enableHttpURLConnectionTrace = true;
	private boolean enableJdbcTrace = false;
	private boolean enableTimingLog = false;
	private boolean enableExceptionLog = false;
	private int logMethodLatency = 500;
	
	private  Set<String> includePackages = new HashSet<String>();
	private  Set<String> excludePackages = new HashSet<String>();
	private  Map<String, List<String>> traceMethodCall = new HashMap<String, List<String>>(); // class, methods
	private  Map<String, List<String>> traceMethodProcess = new HashMap<String, List<String>>(); // class, methods
	
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
    
    public boolean isEnableHttpURLConnectionTrace() {
    	return enableHttpURLConnectionTrace;
    }
   
    public boolean isEnableJdbcTrace() {
    	return enableJdbcTrace;
    }
    
	public boolean isEnableLog() {
		return enableTimingLog || enableExceptionLog ;
	}

	public boolean isEnableTimingLog() {
		return enableTimingLog;
	}

	public boolean isEnableExceptionLog() {
		return enableExceptionLog;
	}

	public  boolean isNeedInject(String className) {
		if (traceMethodCall.containsKey(className)){
			return true;
		}
		for (String v : excludePackages) {
			if (className.startsWith(v)) {
				return false;
			}
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
	
	private URL getURL(String path){
		URL url = null;
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			logger.fine(e.toString());
		}
		if (url == null) try {
			File file = new File(path);
			if (file != null && file.exists() && file.isFile()) {
				url = file.toURI().toURL();
			}else{
				logger.warning("file not exists: " + path);
			}
		} catch (MalformedURLException ex) {
			logger.warning(ex.toString());
		}
		return url;
	}
	
	public LogTraceConfig(String configFilePath) {
		URL configURL = null;
		
		if (configFilePath!=null){
			logger.info(String.format("try load config from %s", configFilePath));
			configURL = getURL(configFilePath);
		}
		
		//given config file path
		if (configURL == null) {
			String givenConfigFileName = System.getProperty("logtrace.config");
			if (givenConfigFileName != null) {
				logger.info(String.format("try load config from %s", givenConfigFileName));
				configURL = getURL(givenConfigFileName);
			}
		}
		
		//work dir
		if (configURL == null) {
			logger.info("try load config from workdir: " + CONFIG_FILE_NAME);
			configURL = getURL(CONFIG_FILE_NAME);
		}
		
		//classes root
		if (configURL == null) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl!=null){
				logger.info("try load config from classpath: " + cl);
				configURL = Thread.currentThread().getContextClassLoader().getResource(CONFIG_FILE_NAME);
			}
		}
		
		//home default dir
		if (configURL == null) {
			logger.info("try load config from homedir: " + DEFAULT_CONFIG_FILE);
			if (DEFAULT_CONFIG_FILE != null && DEFAULT_CONFIG_FILE.exists()) {
				try {
					configURL = DEFAULT_CONFIG_FILE.toURI().toURL();
				} catch (MalformedURLException e) {
				}
			}
		}

		// load default
		if (configURL == null) {
			logger.info("extract default configuration to " + DEFAULT_CONFIG_FILE.getAbsolutePath());
			try {
				extractDefaultProfile();
				configURL = DEFAULT_CONFIG_FILE.toURI().toURL();
			} catch (IOException e) {
				throw new RuntimeException("error load config file " + DEFAULT_CONFIG_FILE, e);
			}
		}	
		
		logger.info("load configuration from " + configURL.toString());
		parseProperty(configURL);
		
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

	
	private void parseProperty(URL url) {
		Properties properties = new Properties();
		InputStream in = null;
		try {
			in = url.openStream();
			properties.load(in); 
			properties.list(System.out);
			
			Properties context = new Properties(); 
			context.putAll(System.getProperties());
			context.putAll(properties);
			
			loadConfig(properties);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (in!=null) try{
				in.close();
			}catch(IOException ioe) {}
		}
	}

	private void loadConfig(Properties properties)  {
		this.enableHttpURLConnectionTrace = Boolean.parseBoolean(properties.getProperty("logtrace.enableHttpURLConnectionTrace"));
		this.enableJdbcTrace = Boolean.parseBoolean(properties.getProperty("logtrace.enableJdbcTrace"));
		this.enableTimingLog = Boolean.parseBoolean(properties.getProperty("logtrace.enableTimingLog"));
		this.enableExceptionLog = Boolean.parseBoolean(properties.getProperty("logtrace.enableExceptionLog"));
	
		String slogMethodLatency = properties.getProperty("logtrace.logMethodLatency");
		if (slogMethodLatency!=null) this.logMethodLatency = Integer.parseInt(slogMethodLatency);
		
		String sIncludePackages = properties.getProperty("logtrace.includePackages");
		if (sIncludePackages!=null && sIncludePackages.trim().length()>0){
			String[] _includes = sIncludePackages.split(";");
			for (String pack : _includes) {
				addProfileClass(pack);
			}
		}
		
		String sExcludePackages = properties.getProperty("logtrace.excludePackages");
		if (sExcludePackages!=null && sExcludePackages.trim().length()>0){
			String[] _excludes = sExcludePackages.split(";");
			for (String pack : _excludes) {
				excludePackages.add(pack);
			}
		}		
		
		String sTraceMethodCall = properties.getProperty("logtrace.traceMethodCall");
		if (sTraceMethodCall!=null && sTraceMethodCall.trim().length()>0){
			String[] _methods = sTraceMethodCall.split(";");
			for (String pack : _methods) {
				addTraceMethodCall(pack);
			}
		}
		
		String sTraceMethodProcess = properties.getProperty("logtrace.traceMethodProcess");
		if (sTraceMethodProcess!=null && sTraceMethodProcess.trim().length()>0){
			String[] _methods = sTraceMethodProcess.split(";");
			for (String pack : _methods) {
				addTraceMethodProcess(pack);
			}
		}
	}

}