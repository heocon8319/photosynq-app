package com.photosynq.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.photosynq.app.MainActivity;
import com.photosynq.app.R;
import com.photosynq.app.SyncFragment;
import com.photosynq.app.db.DatabaseHelper;
import com.photosynq.app.http.HTTPConnection;
import com.photosynq.app.model.AppSettings;
import com.photosynq.app.model.Data;
import com.photosynq.app.model.ProjectResult;
import com.photosynq.app.model.Question;
import com.photosynq.app.response.UpdateData;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by kalpesh on 24/01/15.
 */
public class CommonUtils {

    // App context
    private Context mContext;

    // Singletone class
    private static CommonUtils instance = null;

    // Holds fonts instance
    Typeface uifontFace;
    Typeface openSansLightFace;
    Typeface robotoLightFace;
    Typeface robotoMediumFace;
    Typeface robotoRegularFace;

    private CommonUtils(Context context) {
        mContext = context;
    }

    public static CommonUtils getInstance(Context context){
        if (instance == null)
            instance = new CommonUtils(context);

        return instance;
    }

    public Typeface getFontUiFontSolid() {

        if(uifontFace == null)
            uifontFace = Typeface.createFromAsset(mContext.getAssets(), "uifont-solid.otf");

        return uifontFace;
    }
    public Typeface getFontOpenSansLight() {
        if(openSansLightFace == null)
            openSansLightFace = Typeface.createFromAsset(mContext.getAssets(), "opensans-light.ttf");

        return openSansLightFace;
    }
    public Typeface getFontRobotoLight() {
        if(robotoLightFace == null)
            robotoLightFace = Typeface.createFromAsset(mContext.getAssets(), "roboto-light.ttf");

        return robotoLightFace;
    }
    public Typeface getFontRobotoMedium() {
        if(robotoMediumFace == null)
            robotoMediumFace = Typeface.createFromAsset(mContext.getAssets(), "roboto-medium.ttf");

        return robotoMediumFace;
    }
    public Typeface getFontRobotoRegular() {
        if(robotoRegularFace == null)
            robotoRegularFace = Typeface.createFromAsset(mContext.getAssets(), "roboto-regular.ttf");

        return robotoRegularFace;
    }

    // Invoke this method only on Async task. Do not invoke on UI thread. it will throw exceptions anyway ;)
    public static boolean isConnected(final Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL(Constants.SERVER_URL).openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                //PrefUtils.saveToPrefs(context, PrefUtils.PREFS_IS_SYNC_IN_PROGRESS, "false");
                Log.e("Connectivity", "Error checking internet connection", e);
                Toast.makeText(context, "You are not connect to a network.\n" +
                                        "\n" +
                                        "Check if wifi is turned on \n" +
                                        "and if networks are available in your system settings screen. ", Toast.LENGTH_LONG).show();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                // Check whether keep button is clicked or not
                                String getKeepBtnStatus = PrefUtils.getFromPrefs(context, PrefUtils.PREFS_KEEP_BTN_CLICK, "");
                                if (getKeepBtnStatus.equals("KeepBtnCLickYes")) {
                                    Toast.makeText(context, "Success!\nCached ", Toast.LENGTH_LONG).show();
                                    PrefUtils.saveToPrefs(context, PrefUtils.PREFS_KEEP_BTN_CLICK, "KeepBtnCLickNo");

                                } else {

                                    Toast.makeText(context, "You are not connect to a network.\n" +
                                            "\n" +
                                            "Check if wifi is turned on \n" +
                                            "and if networks are available in your system settings screen. ", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                );
            }
        } else {
            //PrefUtils.saveToPrefs(context, PrefUtils.PREFS_IS_SYNC_IN_PROGRESS, "false");
            Log.d("Connectivity", "You are not connect to a network.");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // Check whether keep button is clicked or not
                            String getKeepBtnStatus = PrefUtils.getFromPrefs(context, PrefUtils.PREFS_KEEP_BTN_CLICK, "");
                            if(getKeepBtnStatus.equals("KeepBtnCLickYes")){
                                Toast.makeText(context, "Success!\nCached ", Toast.LENGTH_LONG).show();
                                PrefUtils.saveToPrefs(context, PrefUtils.PREFS_KEEP_BTN_CLICK, "KeepBtnCLickNo");

                            }else {

                                Toast.makeText(context, "You are not connect to a network.\n" +
                                        "\n" +
                                        "Check if wifi is turned on \n" +
                                        "and if networks are available in your system settings screen. ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );
        }
        return false;
    }


    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    //Generate a MD5 hash from given string
    public static String getMD5EncryptedString(String encTarget){
        MessageDigest mdEnc = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        } // Encryption algorithm
        mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while ( md5.length() < 32 ) {
            md5 = "0"+md5;
        }
        return md5;
    }

    public synchronized static void uploadResults(final Context context, final int projectId){

        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object[] objects) {
                DatabaseHelper db = DatabaseHelper.getHelper(context);
                String offsetid = "0";
                int totalrecords = db.getAllUnuploadedResultsCount(null);
                //TODO change batch from 5 to 100 -shekhar
                for(int recordsloop = 0; recordsloop < Math.ceil((double)totalrecords/5); recordsloop++) {
                    int index = 1;

                    List<ProjectResult> listRecords = db.getAllUnUploadedResults(5, offsetid);
                    for (ProjectResult projectResult : listRecords) {

                        String project_id = projectResult.getProjectId();

                        if (projectId != -1) {

                            if (!project_id.equals("" + projectId)) {

                                continue;
                            }
                        }

                        if (index == listRecords.size()) {
                            offsetid = projectResult.getId();
                        }
                        String row_id = projectResult.getId();
                        String result = projectResult.getReading();

                        if (!result.contains("user_answers")) {

                            Log.d("PhotosynQ", result);
                        }


                        String authToken = PrefUtils.getFromPrefs(context, PrefUtils.PREFS_AUTH_TOKEN_KEY, PrefUtils.PREFS_DEFAULT_VAL);
                        String email = PrefUtils.getFromPrefs(context, PrefUtils.PREFS_LOGIN_USERNAME_KEY, PrefUtils.PREFS_DEFAULT_VAL);
                        StringEntity input = null;
                        String responseString = null;
                        JSONObject request_data = new JSONObject();

                        try {
                            JSONObject jo = new JSONObject(result);
                            request_data.put("user_email", email);
                            request_data.put("user_token", authToken);
                            request_data.put("data", jo);
                            input = new StringEntity(request_data.toString());
                            input.setContentType("application/json");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            continue;
                            //??return Constants.SERVER_NOT_ACCESSIBLE;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            continue;
                            //??return Constants.SERVER_NOT_ACCESSIBLE;
                        }

                        String strDataURI = Constants.PHOTOSYNQ_DATA_URL
                                + project_id + "/data.json";

                        Log.d("commonutils", "$$$$ URI" + strDataURI);

                        HttpPost postRequest = new HttpPost(strDataURI);
                        if (null != input) {
                            postRequest.setEntity(input);
                        }
                        Log.d("commonutils", "$$$$ Executing POST request");
                        HttpClient httpclient = new DefaultHttpClient();
                        try {
                            HttpResponse response = httpclient.execute(postRequest);

                            if (null != response) {
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
                            }

                            UpdateData updateData = new UpdateData(context, row_id);
                            updateData.onResponseReceived(responseString);

                        } catch (ClientProtocolException e) {
                            continue;
                            //??return Constants.SERVER_NOT_ACCESSIBLE;
                        } catch (IOException e) {
                            continue;
                            //??return Constants.SERVER_NOT_ACCESSIBLE;
                        }

                        index++;

                    }
                }
                return Constants.SUCCESS;
            }

        }.execute();

    }

    public static void writeStringToFile(Context context,String fileName, String dataString)
    {
        try {
            File myFile = new File(context.getExternalFilesDir(null), fileName);
            if (myFile.exists()){
                myFile.delete();
            }

            myFile.createNewFile();

            FileOutputStream fos;
            //dataString = dataString.replaceAll("\\{", "{\"time\":\""+time+"\",");
            byte[] data = dataString.getBytes();
            try {
                fos = new FileOutputStream(myFile);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getDeviceAddress(Context context){
        String userId = PrefUtils.getFromPrefs(context, PrefUtils.PREFS_LOGIN_USERNAME_KEY, PrefUtils.PREFS_DEFAULT_VAL);
        AppSettings appSettings = DatabaseHelper.getHelper(context).getSettings(userId);

        return appSettings.getConnectionId();
    }

//    public static String getAutoIncrementedValue(Context ctx,String question_id, String index) {
//        if(Integer.parseInt(index) == -1) {
//            return "-2";
//        }
//
//        String userId = PrefUtils.getFromPrefs(ctx , PrefUtils.PREFS_LOGIN_USERNAME_KEY, PrefUtils.PREFS_DEFAULT_VAL);
//        DatabaseHelper db = DatabaseHelper.getHelper(ctx);
//        String projectId = db.getSettings(userId).getProjectId();
//        Question question = db.getQuestionForProject(projectId, question_id);
//        Data data = db.getData(userId, projectId, question.getQuestionId());
//        String[] items = data.getValue().split(",");
//        int from = Integer.parseInt(items[0]);
//        int to = Integer.parseInt(items[1]);
//        int repeat = Integer.parseInt(items[2]);
//        ArrayList<Integer> populatedValues = new ArrayList<Integer>();
//        for(int i=from;i<=to;i++){
//            for(int j=0;j<repeat;j++){
//                populatedValues.add(i);
//
//            }
//        }
//
//        if(Integer.parseInt(index) > populatedValues.size()-1)
//            return "-1";
//
//        return populatedValues.get(Integer.parseInt(index)).toString();
//    }

    public static Date convertToDate(String rawdate)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(rawdate);
            System.out.println(convertedDate);
            return convertedDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }



    public static void setProgress(final Activity context, ProgressDialog progressDialog, int progressValue){
       if(progressDialog != null && context != null) {


           int getProgress = progressDialog.getProgress();
           progressDialog.setProgress(getProgress + progressValue);
           if (getProgress >= 100) {
               progressDialog.dismiss();

               progressDialog.setProgress(0);

             //  PrefUtils.saveToPrefs(context, PrefUtils.PREFS_IS_SYNC_IN_PROGRESS, "false");

               context.runOnUiThread(new Runnable() {
                   @Override
                   public void run() {

                       String totalCachedDataPoints = PrefUtils.getFromPrefs(context, PrefUtils.PREFS_TOTAL_CACHED_DATA_POINTS, "0");
                       new AlertDialog.Builder(context)
                               .setIcon(android.R.drawable.ic_dialog_alert)
                               .setTitle("Syncing")
                               .setMessage("Pushed data points\n\nProjects updates complete")
                               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {

                                       try {
                                           MainActivity navigationDrawer = (MainActivity) context;
                                           FragmentManager fragmentManager = navigationDrawer.getSupportFragmentManager();

                                           SyncFragment syncFragment = (SyncFragment) fragmentManager.findFragmentByTag(SyncFragment.class.getName());
                                           if (syncFragment != null) {

                                               syncFragment.refresh();
                                           }
                                       }catch (Exception e){}

                                   }

                               })
                               .show();


                   }
               });

           }
       }
    }

}
