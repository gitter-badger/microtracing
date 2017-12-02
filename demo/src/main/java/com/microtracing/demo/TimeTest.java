package com.microtracing.demo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.HessianProxyFactory;
import com.microtracing.tracespan.Span;
import com.microtracing.tracespan.Tracer;  

public class TimeTest {
	private static final Logger logger =  LoggerFactory.getLogger(TimeTest.class);
    public static void main(String[] args) throws Exception{
    	Tracer tracer = Tracer.getTracer();
    	Span span = tracer.getCurrentSpan();
    	span.start();
    	TimeTest t = new TimeTest();
    	try {
	        t.sayHello();
	        Foo foo = t.new Foo();
	        foo.bar();
			try{
				t.httpConnect();
			}catch(Exception ex){
				ex.printStackTrace();
			}
    	}finally {
    		span.stop();
    	}
    }

    public  void sayHello() {
        try {
             Thread.sleep(500);
             logger.info("hello world!" );
			 
        } catch (InterruptedException e) {
             e.printStackTrace();
        }
   }
   
   public  void httpConnect() throws Exception{
        String requestUrl = "http://baidu.com";
        Map<String, Object> requestParamsMap = new HashMap<String, Object>();  
        requestParamsMap.put("areaCode", "001");  
        requestParamsMap.put("areaCode1", "中国");  
        PrintWriter printWriter = null;  
        BufferedReader bufferedReader = null;  
        // BufferedReader bufferedReader = null;  
        StringBuffer responseResult = new StringBuffer();  
        StringBuffer params = new StringBuffer();  
        HttpURLConnection httpURLConnection = null;  
        // 组织请求参数  
        Iterator it = requestParamsMap.entrySet().iterator();  
        while (it.hasNext()) {  
            Map.Entry element = (Map.Entry) it.next();  
            params.append(element.getKey());  
            params.append("=");  
            params.append(element.getValue());  
            params.append("&");  
        }  
        if (params.length() > 0) {  
            params.deleteCharAt(params.length() - 1);  
        }  	   
		try {  
            URL realUrl = new URL(requestUrl);  
            // 打开和URL之间的连接  
            httpURLConnection = (HttpURLConnection) realUrl.openConnection();  
            // 设置通用的请求属性  
            httpURLConnection.setRequestProperty("accept", "*/*");  
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");  
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(params.length()));  
            // 发送POST请求必须设置如下两行  
            httpURLConnection.setDoOutput(true);  
            httpURLConnection.setDoInput(true);  
            httpURLConnection.setConnectTimeout(1000);
            // 获取URLConnection对象对应的输出流  
            printWriter = new PrintWriter(httpURLConnection.getOutputStream());  
            // 发送请求参数  
            printWriter.write(params.toString());  
            // flush输出流的缓冲  
            printWriter.flush();  
            // 根据ResponseCode判断连接是否成功  
            int responseCode = httpURLConnection.getResponseCode();  
            if (responseCode != 200) {  
                logger.warn(" Error===" + responseCode);  
            } else {  
                logger.info("Post Success!");  
            }  
            // 定义BufferedReader输入流来读取URL的ResponseData  
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));  
            String line;  
            while ((line = bufferedReader.readLine()) != null) {  
                responseResult.append("\n").append(line);  
            }  
            logger.info(responseResult.toString());
        } catch (Exception e) {  
            logger.warn("send post request error!" + e);  
			throw e;
        } finally {  
            if (httpURLConnection!=null) httpURLConnection.disconnect();  
            try {  
                if (printWriter != null) {  
                    printWriter.close();  
                }  
                if (bufferedReader != null) {  
                    bufferedReader.close();  
                }  
            } catch (IOException ex) {  
                ex.printStackTrace();  
            }  
  
        }  
   }
   
   public  String doSomething() {
       try {
           Thread.sleep(2000);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       logger.info("gen uuid");
	   return java.util.UUID.randomUUID().toString();
   }	   
   
   class Foo{
	   public Foo(){
		   
	   }
	   
	   public void bar(){
		   long time = System.currentTimeMillis();
		   logger.info("time:"+time);
		   doSomething();
	   }
   }
   
   
}
