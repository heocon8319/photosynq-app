package com.photosynq.app.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.photosynq.app.utils.CommonUtils;
import com.photosynq.app.utils.Constants;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class HTTPConnection extends AsyncTask<Object, Object, String>{
    private String username;
	private String password;
	private StringEntity input = null;
    public PhotosynqResponse delegate = null;

	public HTTPConnection(String username, String password)
	{
		this.username=username;
		this.password=password;
	}
	
	public HTTPConnection(StringEntity input)
	{
		this.input = input;
	}
	
	public HTTPConnection()
	{

	}
	@Override
	protected void onPreExecute() {
		if(null != username && null != password)
		{
			JSONObject credentials = new JSONObject();
			JSONObject user = new JSONObject();	
			try {
					credentials.put("email", username);
					credentials.put("password", password);
					user.put("user", credentials);
					input = new StringEntity(user.toString());
					input.setContentType("application/json");
			} catch (JSONException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        }
		super.onPreExecute();	
	}

	@Override
    protected String doInBackground(Object... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        Context context = (Context)uri[0];
        HttpResponse response = null;
        HttpPost postRequest;
        HttpGet getRequest;
        String responseString = null;
        if(!CommonUtils.isConnected(context))
        {
            return Constants.SERVER_NOT_ACCESSIBLE;
        }
        Log.d("PHTTPC", "in async task");
        try {
            Log.d("PHTTPC", "$$$$ URI"+uri[1]);
            if("POST".equals(uri[2]) )
            {
                postRequest = new HttpPost((String)uri[1]);
                if(null!=input)
                {
                    postRequest.setEntity(input);
                }
                Log.d("PHTTPC", "$$$$ Executing POST request");
                response = httpclient.execute(postRequest);
            }else if ("GET".equals(uri[2]) ) {
                getRequest = new HttpGet((String) uri[1]);
                Log.d("PHTTPC", "$$$$ Executing GET request");
                response = httpclient.execute(getRequest);
            }
            Log.d("PHTTPC", "in async task");

            if (null != response) {
                try {
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        out.close();
                        responseString = out.toString();
                    } else {
                        //Closes the connection.
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
        if(null!=delegate)
        {
        	delegate.onResponseReceived(result);
        }
        if (null == result)
        {
        	Log.d("PHTTPC","No results returned");
        }
    }
}
