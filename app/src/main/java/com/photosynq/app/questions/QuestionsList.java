package com.photosynq.app.questions;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.photosynq.app.DirectionsActivity;
import com.photosynq.app.DisplayResultsActivity;
import com.photosynq.app.R;
import com.photosynq.app.SelectDeviceDialog;
import com.photosynq.app.SelectDeviceDialogDelegate;
import com.photosynq.app.SelectedOptions;
import com.photosynq.app.db.DatabaseHelper;
import com.photosynq.app.model.BluetoothMessage;
import com.photosynq.app.model.Macro;
import com.photosynq.app.model.Protocol;
import com.photosynq.app.model.Question;
import com.photosynq.app.model.RememberAnswers;
import com.photosynq.app.model.ResearchProject;
import com.photosynq.app.utils.BluetoothService;
import com.photosynq.app.utils.CommonUtils;
import com.photosynq.app.utils.Constants;
import com.photosynq.app.utils.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionsList extends ActionBarActivity implements SelectDeviceDialogDelegate {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    private int lastExpandedPosition = -1;
    private Handler mHandler;
    public String message;
    private DatabaseHelper dbHelper;
    private boolean mIsCancelMeasureBtnClicked = false;
    private boolean mIsMeasureBtnClicked = false;
    private boolean scanMode = false;
    BluetoothMessage bluetoothMessage;
    private String deviceAddress;
    private BluetoothAdapter mBluetoothAdapter = null;
    private TextView outputTextView;


    private String mConnectedDeviceName;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_questions_list);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            projectId = extras.getString(DatabaseHelper.C_PROJECT_ID);
        }
        createHandler();

        selectDevice();

        showDirection();

        dbHelper = DatabaseHelper.getHelper(this);
        List<Question> questions = dbHelper.getAllQuestionForProject(projectId);
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.queExpandableList);
        expListView.setGroupIndicator(null);

        listAdapter = new ExpandableListAdapter(this, questions, expListView);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {


            //Collapse all group except selected group
            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                if (expListView != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(expListView.getWindowToken(), 0);
                }
            }
        });
        expListView.setAdapter(listAdapter);


        final Button btnTakeMeasurement = (Button) findViewById(R.id.btn_take_measurement);
        btnTakeMeasurement.setText("+ Take Measurement");
        btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_orange);

        btnTakeMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bluetoothMessage = new BluetoothMessage();

                if (btnTakeMeasurement.getText().equals("+ Take Measurement")) {
                    mIsCancelMeasureBtnClicked = false;
                    mIsMeasureBtnClicked = true;
                    //??
//                    if (mBluetoothService == null) {
//                        mBluetoothService = BluetoothService.getInstance(ProjectMeasurmentActivity.this, mHandler);
//                    }

                    BluetoothService mBluetoothService = BluetoothService.getInstance(bluetoothMessage, mHandler);
                    if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                        // Get the BLuetoothDevice object
                        if (mBluetoothAdapter == null)
                            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                        deviceAddress = CommonUtils.getDeviceAddress(QuestionsList.this);
                        if (null == deviceAddress) {
                            Toast.makeText(QuestionsList.this, "Measurement device not configured, Please configure measurement device (bluetooth).", Toast.LENGTH_SHORT).show();
                            selectDevice();
                            return;
                        } else {
                            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                            mBluetoothService.connect(device);
                        }
                    } else {
                        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, BluetoothService.STATE_CONNECTED, 1, bluetoothMessage).sendToTarget();
                    }
                    btnTakeMeasurement.setText("Cancel");
                    btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_red);
                } else if (btnTakeMeasurement.getText().equals("Cancel")) {
                    mIsCancelMeasureBtnClicked = true;
                    mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, BluetoothService.STATE_CONNECTED, 0, bluetoothMessage).sendToTarget();
                    //btnTakeMeasurement.setText("+ Take Measurement");
                    btnTakeMeasurement.setEnabled(false);
                    btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_gray_light);

                    //??
//                    if (null != timer)
//                        timer.cancel(); // Cancel count down timer.

                }
            }
        });

        Button directionsButton = (Button) findViewById(R.id.btn_directions);
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openMainActivity = new Intent(QuestionsList.this, DirectionsActivity.class);
                openMainActivity.putExtra(DatabaseHelper.C_PROJECT_ID, projectId);
                startActivity(openMainActivity);
            }
        });

        Button outputButton = (Button) findViewById(R.id.btn_output);
        outputTextView = (TextView) findViewById(R.id.outputtv);
        final ScrollView listscroll = (ScrollView) findViewById(R.id.questionlistscroll);
        final ScrollView outputscroll = (ScrollView) findViewById(R.id.outputscroll);
        outputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listscroll.getVisibility() == View.GONE) {
                    ((Button) view).setText("+ O/P");
                    outputscroll.setVisibility(View.GONE);
                    listscroll.setVisibility(View.VISIBLE);
                } else {
                    ((Button) view).setText("- O/P");
                    outputscroll.setVisibility(View.VISIBLE);
                    listscroll.setVisibility(View.GONE);
                }
            }
        });
    }

    private synchronized void createHandler() {
        mHandler = null;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                TextView mtvStatusMessage = (TextView) findViewById(R.id.tv_status_message);
                ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
                Button btnTakeMeasurement = (Button) findViewById(R.id.btn_take_measurement);

                switch (msg.what) {
                    case Constants.MESSAGE_STREAM:
                        String oldmdg = outputTextView.getText().toString();
                        outputTextView.setText(oldmdg + (CharSequence) msg.obj);
                        break;
                    case Constants.MESSAGE_STATE_CHANGE:
                        if (Constants.D) Log.i("PHOTOSYNC", "MESSAGE_STATE_CHANGE: " + msg.arg1);
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:
                                BluetoothMessage bluetoothMessagee = (BluetoothMessage) msg.obj;
                                if (msg.arg2 == 0) {//Sending cancel request to the device
                                    sendData("-1+-1+");
                                    //setDeviceTimeOut();
                                    mtvStatusMessage.setText("Cancelling measurement, please wait");
                                } else if (msg.arg2 == 1) { //Send measurement request
                                    mtvStatusMessage.setText(R.string.title_connected_to);
                                    if (mConnectedDeviceName == null || mConnectedDeviceName == "") {

                                        mConnectedDeviceName = PrefUtils.getFromPrefs(QuestionsList.this, Constants.DEVICE_NAME, "");
                                    }

                                    mtvStatusMessage.append(mConnectedDeviceName);

                                    ResearchProject researchProject = dbHelper.getResearchProject(projectId);
                                    if (null == researchProject) {
                                        Toast.makeText(QuestionsList.this, "Project not selected, Please select the project.", Toast.LENGTH_LONG).show();
                                        break;
                                    }

                                    try {
                                        String protocolJson = "";
                                        StringBuffer dataString = new StringBuffer();
                                        String[] projectProtocols = researchProject.getProtocols_ids().split(",");
                                        if (projectProtocols.length >= 1) {
                                            int protocol_total = 0;
                                            int protocol_measurements = 1;
                                            StringBuffer dataStringMacro = new StringBuffer();

                                            for (String protocolId : projectProtocols) {
                                                if (protocolId.equals(""))
                                                    continue;
                                                Protocol protocol = dbHelper.getProtocol(protocolId);
                                                JSONObject detailProtocolObject = new JSONObject();
                                                detailProtocolObject.put("protocolid", protocol.getId());
                                                detailProtocolObject.put("protocol_name", protocol.getName());
                                                detailProtocolObject.put("macro_id", protocol.getMacroId());
                                                dataString.append("\"" + protocol.getId() + "\"" + ":" + detailProtocolObject.toString() + ",");

                                                String tempProtocolJson = protocol.getProtocol_json().trim();
                                                if (tempProtocolJson.length() > 1) {
                                                    protocolJson += "{" + tempProtocolJson.substring(1, tempProtocolJson.length() - 1) + "},";
                                                }

                                                protocol_measurements = protocol.getMeasurements();
                                                if (protocol.getProtocols() > 0) {
                                                    protocol_total += protocol.getProtocols();
                                                } else {
                                                    protocol_total += 1;
                                                }

                                                // Writing macros.js file with all macro functions
                                                List<Macro> macros = dbHelper.getAllMacros();
                                                for (Macro macro : macros) {
                                                    if (macro.getId().equals(protocol.getMacroId())) {
                                                        dataStringMacro.append("function macro_" + macro.getId() + "(json){");
                                                        dataStringMacro.append(System.getProperty("line.separator"));
                                                        dataStringMacro.append("try{");
                                                        dataStringMacro.append(System.getProperty("line.separator"));
//                                                        String evalStr = macro.getJavascriptCode().replaceAll("\\r\\n", " ");
//                                                        evalStr = evalStr.replaceAll("\"", "\\\\\"");
//                                                        dataStringMacro.append("eval(\"" + evalStr + "\");");
//                                                        dataStringMacro.append(System.getProperty("line.separator"));
                                                        dataStringMacro.append(macro.getJavascriptCode().replaceAll("\\r\\n", System.getProperty("line.separator"))); //replacing ctrl+m characters
                                                        dataStringMacro.append(System.getProperty("line.separator"));
                                                        dataStringMacro.append("}"); //try closing
                                                        dataStringMacro.append(System.getProperty("line.separator"));
                                                        dataStringMacro.append("catch(err) {}");
                                                        dataStringMacro.append(System.getProperty("line.separator") + " }");
                                                        dataStringMacro.append(System.getProperty("line.separator"));
                                                        dataStringMacro.append(System.getProperty("line.separator"));

                                                        break;
                                                    }
                                                }
                                                System.out.println("###### writing macros :......");

                                            }

                                            CommonUtils.writeStringToFile(QuestionsList.this, "macros.js", dataStringMacro.toString());

                                            protocol_total *= protocol_measurements;

                                            if (protocol_total == 0) {
                                                protocol_total = 100;
                                            }
                                            mProgressBar.setMax(protocol_total);
                                            mProgressBar.setProgress(0);

                                            if (dataString.length() > 0) {
                                                String data = "var protocols={" + dataString.substring(0, dataString.length() - 1) + "}";

                                                // Writing macros_variable.js file with protocol and macro relations
                                                System.out.println("######Writing macros_variable.js file:" + data);
                                                CommonUtils.writeStringToFile(QuestionsList.this, "macros_variable.js", data);

                                                //protocolJson = "[" + protocolJson.substring(0, protocolJson.length() - 1) + "]"; // remove last comma and add suqare brackets and start and end.

                                                System.out.println("$$$$$$$$$$$$$$ protocol json sending to device :" + researchProject.getProtocol_json() + "length:" + researchProject.getProtocol_json().length());

                                                sendData(researchProject.getProtocol_json());
                                                setDeviceTimeOut();

                                                mtvStatusMessage.setText("Initializing measurement please wait ...");

                                            } else {

                                                mtvStatusMessage.setText("No protocol defined for this project.");
                                                Toast.makeText(QuestionsList.this, "No protocol defined for this project.", Toast.LENGTH_LONG).show();

                                                if (btnTakeMeasurement != null) {
                                                    if (btnTakeMeasurement.getText().equals("Cancel")) {
                                                        btnTakeMeasurement.setText("+ Take Measurement");
                                                        btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_orange);
                                                    }
                                                }
                                            }
                                        } else {

                                            mtvStatusMessage.setText("No protocol defined for this project.");
                                            Toast.makeText(QuestionsList.this, "No protocol defined for this project.", Toast.LENGTH_LONG).show();
                                            if (btnTakeMeasurement != null) {
                                                if (btnTakeMeasurement.getText().equals("Cancel")) {
                                                    btnTakeMeasurement.setText("+ Take Measurement");
                                                    btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_orange);
                                                }
                                            }
                                            break;
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    if (mIsMeasureBtnClicked) {
                                        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, BluetoothService.STATE_CONNECTED, 1, bluetoothMessagee).sendToTarget();
                                    }
                                }

                                break;
                            case BluetoothService.STATE_CONNECTING:
                                mtvStatusMessage.setText(R.string.title_connecting);
                                break;
                            case BluetoothService.STATE_LISTEN:
                            case BluetoothService.STATE_NONE:
                                mtvStatusMessage.setText(R.string.title_not_connected);
                                break;
                            case BluetoothService.STATE_FIRST_RESP:
                                BluetoothMessage bluetoothMessage1 = (BluetoothMessage) msg.obj;
                                PrefUtils.saveToPrefs(QuestionsList.this, "isGetResponse", "true");
                                final String measurement = bluetoothMessage1.message;

                                if (mIsCancelMeasureBtnClicked == true) {

                                    mProgressBar.setProgress(0);
                                    break;
                                }

                                if (measurement != null) {
//                                    TextView txtOutput = (TextView) findViewById(R.id.tvOutput);
                                    String tempStrOutput = measurement;
                                    int totalChars = tempStrOutput.length();
                                    int startIdx = totalChars - 8000;
                                    if (startIdx < 0) {

                                        startIdx = 0;
                                    }
                                    String strOutput = tempStrOutput.substring(startIdx);
//                                    txtOutput.setText(strOutput);
//                                    txtOutput.invalidate();

                                    int progress = mProgressBar.getProgress();
                                    int maxProgress = mProgressBar.getMax();


                                    try {
                                        progress = measurement.split("\\}").length - 1;
                                        if (progress < maxProgress) {
                                            mProgressBar.setProgress(progress + 1);
                                            mtvStatusMessage.setText("Measurement " + (progress + 1) + " of " + maxProgress);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

//                                    if (measurement.indexOf("}") > 0 && progress > 0) {
//
//                                        if (progress < maxProgress) {
//                                            mProgressBar.setProgress(progress + 1);
//                                            mtvStatusMessage.setText("Measurement " + (progress + 1) + " of " + maxProgress);
//                                        }
//
//                                    } else {
//
//                                        if (progress == 0) {
//                                            mProgressBar.setProgress(progress + 1);
//                                            mtvStatusMessage.setText("Measurement " + (1) + " of " + maxProgress);
//                                        }
//
//                                    }
                                }

                                break;
                        }
                        break;
                    case Constants.MESSAGE_WRITE:
                        break;
                    case Constants.MESSAGE_READ:
                        PrefUtils.saveToPrefs(QuestionsList.this, "isGetResponse", "true");
                        if (mIsCancelMeasureBtnClicked == false) {
                            BluetoothMessage bluetoothMessage2 = (BluetoothMessage) msg.obj;
                            String measurement = bluetoothMessage2.message;
                            // Do not process the message if contain pwr_off from device
                            Log.d("QuestionList", measurement);
                            if (measurement.contains("sample") || measurement.contains("user_questions")) {
                                String dataString;
                                StringBuffer options = new StringBuffer();
                                options.append("\"user_answers\": {");
                                //options.append("\"user_answers\": [");


                                HashMap<Question, SelectedOptions> allOptions = listAdapter.mSelectedOptions;

                                int count = 0;
                                for (Map.Entry<Question, SelectedOptions> e : listAdapter.mSelectedOptions.entrySet()) {
                                    options.append('"')
                                            .append(e.getKey().getQuestionId())
                                            .append('"')
                                            .append(':')
                                            .append('"')
                                            .append(e.getValue().getSelectedValue())
                                            .append('"');
                                    if (count < allOptions.size() - 1)
                                        options.append(",");

                                    count++;
                                }

                                //options.append(" ],");
                                options.append(" },");
                                final long time = System.currentTimeMillis();
                                String currentLocation = PrefUtils.getFromPrefs(QuestionsList.this, PrefUtils.PREFS_CURRENT_LOCATION, "");
                                if (allOptions.size() <= 0) {
                                    dataString = "var data = [\n" + measurement.replaceAll("\\r\\n", "").replaceFirst("\\{", "{" + "\"location\":[" + currentLocation + "],") + "\n];";
                                    System.out.println("All Options" + dataString);
                                } else {

                                    if (!currentLocation.equals("")) {
                                        options = options.append("\"location\":[" + currentLocation + "],");
                                        dataString = "var data = [\n" + measurement.replaceAll("\\r\\n", "").replaceFirst("\\{", "{" + options) + "\n];";
                                        System.out.println("All Options" + dataString);
                                    } else {
                                        dataString = "var data = [\n" + measurement.replaceAll("\\r\\n", "").replaceFirst("\\{", "{" + options) + "\n];";
                                        System.out.println("All Options" + dataString);
                                    }
                                }
                                System.out.println("###### writing data.js :" + dataString);
                                CommonUtils.writeStringToFile(QuestionsList.this, "data.js", dataString);

                                final String reading = measurement.replaceAll("\\r\\n", "").replaceFirst("\\{", "{" + options);
                                outputTextView.setText("");
                                new CountDownTimer(1000, 1000) {

                                    @Override
                                    public void onTick(long l) {

                                    }

                                    @Override
                                    public void onFinish() {

                                        Intent intent = new Intent(QuestionsList.this, DisplayResultsActivity.class);
                                        intent.putExtra(DatabaseHelper.C_PROJECT_ID, projectId);
                                        intent.putExtra(Constants.APP_MODE, Constants.APP_MODE_PROJECT_MEASURE);
                                        intent.putExtra(DatabaseHelper.C_READING, reading);
                                        startActivity(intent);
                                    }
                                }.start();


                            }
                        } else {
                            BluetoothMessage bluetoothMessage3 = (BluetoothMessage) msg.obj;
                            String measurement = bluetoothMessage3.message;
                            //if(measurement.toString().contains("\\r\\n\\r\\n")) {
                            mIsCancelMeasureBtnClicked = false;
                            if (btnTakeMeasurement != null) {
                                if (btnTakeMeasurement.getText().equals("Cancel")) {
                                    mtvStatusMessage.setText("Measurement cancelled");
//                                    TextView txtOutput = (TextView) findViewById(R.id.tvOutput);
//                                    txtOutput.setText("");
//                                    txtOutput.invalidate();
                                    btnTakeMeasurement.setEnabled(true);
                                    btnTakeMeasurement.setText("+ Take Measurement");
                                    btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_orange);
                                }
                            }
                            //}
                        }
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_LONG).show();

                        PrefUtils.saveToPrefs(QuestionsList.this, Constants.DEVICE_NAME, mConnectedDeviceName);

                        break;
                    case Constants.MESSAGE_TOAST:
                        if (mIsCancelMeasureBtnClicked == false || mIsCancelMeasureBtnClicked == true) {
                            Toast.makeText(QuestionsList.this, msg.getData().getString(Constants.TOAST),
                                    Toast.LENGTH_LONG).show();
                        }
                        mIsMeasureBtnClicked = false;
                        mIsCancelMeasureBtnClicked = false;
                        if (btnTakeMeasurement != null) {
                            if (btnTakeMeasurement.getText().equals("Cancel")) {
                                btnTakeMeasurement.setText("+ Take Measurement");
                                btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_orange);
                            }
                        }
                        break;
                    case Constants.MESSAGE_FIRST_RESP:


                        //timer.cancel();
                        break;
                    case Constants.MESSAGE_STOP:
                        //Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                        //        Toast.LENGTH_LONG).show();
                        //??mBluetoothService.stop();
                        break;
                }

                super.handleMessage(msg);
            }
        };
    }

    private void sendData(String data) {
        // Check that we're actually connected before trying anything

        BluetoothService mBluetoothService = BluetoothService.getInstance(bluetoothMessage, mHandler);
        if (null != mBluetoothService && mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (null != mBluetoothService) {
            // Check that there's actually something to send
            if (data.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send;
                send = data.getBytes();
                mBluetoothService.write(send);
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Caution")
                    .setMessage("Are you sure you want to leave this screen?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Stop the activity
                            PrefUtils.saveToPrefs(QuestionsList.this, PrefUtils.PREFS_PREV_SELECTED_POSITION, "0");
                            BluetoothService mBluetoothService = BluetoothService.getInstance(bluetoothMessage, mHandler);
                            mBluetoothService.stop();
                            QuestionsList.this.finish();

                        }

                    })
                    .setNegativeButton("No", null)
                    .show();

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!scanMode) {
            TextView mtvStatusMessage = (TextView) findViewById(R.id.tv_status_message);
            mtvStatusMessage.setText(R.string.start_measurement);
            ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
            mProgressBar.setProgress(0);

            final Button btnTakeMeasurement = (Button) findViewById(R.id.btn_take_measurement);
            btnTakeMeasurement.setText("+ Take Measurement");
            btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_orange);

            HashMap<Question, SelectedOptions> options = listAdapter.getSelectedOptions();
            for (Map.Entry<Question, SelectedOptions> e : options.entrySet()) {
                SelectedOptions option = e.getValue();
                if (option.isRemember()) {

                    option.setReset(false);
                } else {
                    option.setSelectedValue("Tap To Select Answer");
                    option.setReset(true);
                }

                if (option.getQuestionType() == Question.USER_DEFINED) {
                    if (option.getOptionType() == 1) {
                        option.setReset(false);
                        try {
                            int from = Integer.parseInt(option.getRangeFrom());
                            int to = Integer.parseInt(option.getRangeTo());
                            int repeat = Integer.parseInt(option.getRangeRepeat());
                            ArrayList<Integer> populatedValues = new ArrayList<Integer>();
                            for (int i = from; i <= to; i++) {
                                for (int j = 0; j < repeat; j++) {
                                    populatedValues.add(i);

                                }
                            }
                            int currentIndex = option.getAutoIncIndex();
                            currentIndex++;
                            if (currentIndex > populatedValues.size() - 1) {
                                option.setSelectedValue("Loop completed");
                            } else {
                                option.setSelectedValue(populatedValues.get(currentIndex).toString());
                                option.setAutoIncIndex(currentIndex);
                            }
                        } catch (Exception ex) {
                            //eating exception
                        }


                    }

                }
            }
            listAdapter.notifyDataSetChanged();
            mIsMeasureBtnClicked = false;
            mIsCancelMeasureBtnClicked = false;
        }
        scanMode = false;

        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            expListView.collapseGroup(i);
        }
    }

    void setDeviceTimeOut() {

        PrefUtils.saveToPrefs(QuestionsList.this, "isGetResponse", "false");
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                System.out.print("@@@@@@@@@@@@@@ test tick on send protocol");
            }

            public void onFinish() {

                String isGetResponse = PrefUtils.getFromPrefs(QuestionsList.this, "isGetResponse", "false");
                if (isGetResponse.equals("false")) {
                    sendData("-1+-1+");

//                                                    Toast.makeText(getApplicationContext(), "Timer finished - ", Toast.LENGTH_SHORT).show();
                    Log.d("DeviceTimeout", "Device - timeout");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {

                                new AlertDialog.Builder(QuestionsList.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Device - timeout")
                                        .setMessage("Please try again.  Restart device if problem persists")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int which) {
                                                        sendData("-1+-1+"); // Send cancel request
                                                        //  finish();
                                                        //??sendData("1027"); // Restart teensy device
                                                    }

                                                }

                                        )
                                        .show();
                            }
                        }
                    });
                }

            }
        }.start();
    }

    private void selectDevice() {

        deviceAddress = CommonUtils.getDeviceAddress(this);
        if (null == deviceAddress) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            SelectDeviceDialog selectDeviceDialog = new SelectDeviceDialog();
            selectDeviceDialog.show(fragmentManager, "Select Measurement Device", this);
        }
    }

    private void showDirection() {

        String showDirections = PrefUtils.getFromPrefs(this, PrefUtils.PREFS_SHOW_DIRECTIONS, "YES");
        if (showDirections.equals("YES")) {
            if (null != projectId) {
                Intent directionIntent = new Intent(this, DirectionsActivity.class);
                directionIntent.putExtra(DatabaseHelper.C_PROJECT_ID, projectId);
                startActivity(directionIntent);
            }
        }
    }

    @Override
    public void onDeviceSelected(String result) {

        deviceAddress = result;
        if (null == deviceAddress) {
            Toast.makeText(this, "Measurement device not configured, Please configure measurement device (bluetooth).", Toast.LENGTH_SHORT).show();

            selectDevice();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            if (null != intent) {
                String contents = intent.getStringExtra("SCAN_RESULT");

                Toast.makeText(this, contents + " " + requestCode, Toast.LENGTH_SHORT).show();
                HashMap<Question, SelectedOptions> options = listAdapter.getSelectedOptions();

                // TODO we need to mark this not with the IDX of the list but with a reference to the
                // HashMap created

                /**
                SelectedOptions so = options.get(requestCode);
                so.setSelectedValue(contents);
                options.set(requestCode, so);
                listAdapter.setSelectedOptions(options);
                listAdapter.notifyDataSetChanged();

                 **/

            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // Handle cancel
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
        scanMode = true;

    }
}
