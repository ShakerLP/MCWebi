package me.ShakerLP.Functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
/*
 * (Coptight) MCWebi by ShakerLP 
 * http://creativecommons.org/licenses/by-nd/4.0/
 */
public class FileUpload {
	   private static String executeRequest(HttpRequestBase requestBase)
	    {
	        String responseString = "";
	        InputStream responseStream = null;
	        HttpClient client = new DefaultHttpClient();
	        try{
	        HttpResponse response = client.execute(requestBase);
	        if(response != null)
	        {
	            HttpEntity responseEntity = response.getEntity();
	            if (responseEntity != null)
	            {
	                responseStream = responseEntity.getContent();
	                if (responseStream != null)
	                {
	                    BufferedReader br = new BufferedReader (new InputStreamReader (responseStream));
	                    String responseLine = br.readLine();
	                    String tempResponseString = "";
	                    while (responseLine != null)
	                    {
	                        tempResponseString = tempResponseString + responseLine + System.getProperty("line.separator");
	                        responseLine = br.readLine();
	                    }
	                    br.close();
	                    if (tempResponseString.length()>0)
	                    {
	                        responseString = tempResponseString;
	                    }
	                }
	            }
	        }
	        }catch (UnsupportedEncodingException e)
	        {
	        }catch (ClientProtocolException e)
	        {
	        }catch (IllegalStateException e)
	        {
	        }catch (IOException e)
	        {
	        }finally
	        {
	            if (responseStream != null)
	            {
	                try {
	                    responseStream.close();
	                    } catch (IOException e)
	                    {
	                        e.printStackTrace();
	                    }
	            }
	        }
	        client.getConnectionManager().shutdown();
	        return responseString;
	    }

    public String executeMultiPartRequest(String urlString, File link, String fileName, String fileDescription) {
        HttpPost postRequest = new HttpPost(urlString);
        try {
            MultipartEntity multiPartEntity = new MultipartEntity();
            multiPartEntity.addPart("fileDescription", (ContentBody)new StringBody(fileDescription != null ? fileDescription : ""));
            multiPartEntity.addPart("fileName", (ContentBody)new StringBody(fileName != null ? fileName : link.getName()));
            FileBody fileBody = new FileBody(link, "application/octect-stream");
            multiPartEntity.addPart("file", (ContentBody)fileBody);
            postRequest.setEntity((HttpEntity)multiPartEntity);
        }
        catch (UnsupportedEncodingException ex) {
        }
        return FileUpload.executeRequest((HttpRequestBase)postRequest);
    }
}

