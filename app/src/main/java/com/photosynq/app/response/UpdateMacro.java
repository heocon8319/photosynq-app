package com.photosynq.app.response;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.photosynq.app.MainActivity;
import com.photosynq.app.QuickModeFragment;
import com.photosynq.app.http.PhotosynqResponse;
import com.photosynq.app.R;
import com.photosynq.app.db.DatabaseHelper;
import com.photosynq.app.model.Macro;
import com.photosynq.app.utils.CommonUtils;
import com.photosynq.app.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by shekhar on 9/19/14.
 */
public class UpdateMacro implements PhotosynqResponse {

    private MainActivity navigationDrawer;

    public UpdateMacro(MainActivity navigationDrawer)
    {
        this.navigationDrawer = navigationDrawer;
    }
    @Override
    public void onResponseReceived(final String result) {

        Thread t = new Thread(new Runnable() {
            public void run() {
                processResult(result);
            }
        });

        t.start();

    }

    private void processResult(String result) {

        if(null != navigationDrawer) {
            navigationDrawer.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    navigationDrawer.setProgressBarVisibility(View.VISIBLE);
                }
            });
        }

        Date date = new Date();
        System.out.println("UpdateMacro Start onResponseReceived: " + date.getTime());

        DatabaseHelper db = DatabaseHelper.getHelper(navigationDrawer);
        db.openWriteDatabase();
        db.openReadDatabase();
        JSONArray jArray;

        if (null != result) {
            if(result.equals(Constants.SERVER_NOT_ACCESSIBLE))
            {
                Toast.makeText(navigationDrawer, R.string.server_not_reachable, Toast.LENGTH_LONG).show();
                db.closeWriteDatabase();
                db.closeReadDatabase();
                return;
            }

            try {
                jArray = new JSONArray(result);
                for (int i = 0; i < jArray.length(); i++) {

                    JSONObject obj = jArray.getJSONObject(i);
                    Macro macro = new Macro(obj.getString("id"),
                            obj.getString("name"),
                            obj.getString("description"),
                            obj.getString("default_x_axis"),
                            obj.getString("default_y_axis"),
                            obj.getString("javascript_code"),
                            "slug");
                    db.updateMacro(macro);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Writing macros.js file with all macro functions
        StringBuffer dataString = new StringBuffer();
        List<Macro> macros = db.getAllMacros();
        for (Macro macro : macros) {
            dataString.append("function macro_" + macro.getId() + "(json){");
            dataString.append(System.getProperty("line.separator"));
            dataString.append(macro.getJavascriptCode().replaceAll("\\r\\n", System.getProperty("line.separator"))); //replacing ctrl+m characters
            dataString.append(System.getProperty("line.separator") + " }");
            dataString.append(System.getProperty("line.separator"));
            dataString.append(System.getProperty("line.separator"));
        }
        System.out.println("###### writing macros :......");
        CommonUtils.writeStringToFile(navigationDrawer, "macros.js", dataString.toString());
        db.closeWriteDatabase();
        db.closeReadDatabase();
        Date date1 = new Date();

        if(null != navigationDrawer) {
            navigationDrawer.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    navigationDrawer.setProgressBarVisibility(View.INVISIBLE);
                }
            });
        }

        System.out.println("UpdateMacro End onResponseReceived: " + date1.getTime());
    }
}