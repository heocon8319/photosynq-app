package com.photosynq.app.questions;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.photosynq.app.R;
import com.photosynq.app.SelectedOptions;
import com.photosynq.app.model.Question;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shekhar on 8/19/15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context _context;
    private List<Question> questionList;
    private ArrayList<SelectedOptions> selectedOptions;
    private ExpandableListView exp;

    public ArrayList<SelectedOptions> getSelectedOptions()
    {
        return selectedOptions;
    }
    public ExpandableListAdapter(Context context, List<Question> questionList,ExpandableListView exp
                                 ) {
        this._context = context;
        this.questionList = questionList;
        this.selectedOptions = new ArrayList<SelectedOptions>();
        for(int i = 0; i < questionList.size(); i++){
            SelectedOptions selectedoption= new SelectedOptions();
            Question que = questionList.get(i);
            selectedoption.setProjectId(que.getProjectId());
            selectedoption.setQuestionType(que.getQuestionType());
            selectedoption.setQuestionId(que.getQuestionId());
            selectedoption.setSelectedValue("Tap To Select Answer");
            selectedOptions.add(i,selectedoption);
//            selectedOptions.set("Tap To Select Answer");
        }
        this.exp = exp;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        //return this.questionList.get(groupPosition).getOptions().get(childPosititon);
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, final ViewGroup parent) {
        //final String option = (String) getChild(groupPosition, childPosition);

        Question question = getGroup(groupPosition);
        if(null != question) {
            switch (question.getQuestionType()) {
                case Question.USER_DEFINED:

                    //if (convertView == null) {
                    LayoutInflater user_def_infalInflater = (LayoutInflater) this._context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = user_def_infalInflater.inflate(R.layout.user_selected_main_layout, null);
                    //}

                    Spinner optionType = (Spinner) convertView.findViewById(R.id.option_type);
                    optionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            final LinearLayout mainLayout = (LinearLayout)(parent.getParent().getParent());

                            LinearLayout userEnteredLayout = (LinearLayout)mainLayout.findViewById(R.id.layout_user_entered);
                            LinearLayout scanLayout = (LinearLayout)mainLayout.findViewById(R.id.layout_scan_code);
                            LinearLayout autoIncLayout = (LinearLayout)mainLayout.findViewById(R.id.layout_auto_inc);
                            SelectedOptions so = selectedOptions.get(groupPosition);
                            switch (position) {
                                case 0:
                                    userEnteredLayout.setVisibility(View.VISIBLE);
                                    scanLayout.setVisibility(View.GONE);
                                    autoIncLayout.setVisibility(View.GONE);
                                    so.setOptionType(0);
                                    selectedOptions.set(groupPosition, so);
                                    EditText txtListChild = (EditText) userEnteredLayout
                                            .findViewById(R.id.user_input_edit_text);
                                    CheckBox remember = (CheckBox)mainLayout.findViewById(R.id.remember_check_box);
                                    remember.setVisibility(View.VISIBLE);
                                    txtListChild.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {}

                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            SelectedOptions so = selectedOptions.get(groupPosition);
                                            so.setSelectedValue(s.toString());
                                            selectedOptions.set(groupPosition, so);

                                            ExpandableListView explist = (ExpandableListView) mainLayout.getParent();
                                            LinearLayout ll2 = (LinearLayout) explist.findViewWithTag(groupPosition);
                                            TextView selectedAnswer = (TextView) ll2.findViewById(R.id.selectedAnswer);
                                            selectedAnswer.setText(s.toString());
                                            checkMeasurementButton();

                                            final int sdk = android.os.Build.VERSION.SDK_INT;
                                            if(s.length() > 0)
                                            {
                                                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                                    ll2.setBackgroundDrawable(_context.getResources().getDrawable(R.color.green_light));
                                                } else {
                                                    ll2.setBackground(_context.getResources().getDrawable(R.color.green_light));
                                                }
                                            }
                                            else
                                            {
                                                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                                    ll2.setBackgroundDrawable(_context.getResources().getDrawable(R.color.gray_light));
                                                } else {
                                                    ll2.setBackground(_context.getResources().getDrawable(R.color.gray_light));
                                                }
                                            }


                                        }
                                    });
                                    if(so.isReset())
                                    {
                                        txtListChild.setText("");
                                        so.setReset(false);
                                        selectedOptions.set(groupPosition, so);

                                    }else {
                                        if (null != selectedOptions.get(groupPosition) && !selectedOptions.get(groupPosition).getSelectedValue().equals("Tap To Select Answer")) {
                                            txtListChild.setText(selectedOptions.get(groupPosition).getSelectedValue());
                                        }
                                    }
                                    if(txtListChild.requestFocus()) {
                                        InputMethodManager imm = (InputMethodManager) _context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(txtListChild, InputMethodManager.SHOW_IMPLICIT);
                                        //((QuestionsList)_context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                    }

                                    break;
                                case 1:
                                    userEnteredLayout.setVisibility(View.GONE);
                                    scanLayout.setVisibility(View.GONE);
                                    autoIncLayout.setVisibility(View.VISIBLE);
                                    so.setOptionType(1);
                                    so.setRemember(true);
                                    CheckBox remember1 = (CheckBox)mainLayout.findViewById(R.id.remember_check_box);
                                    remember1.setVisibility(View.GONE);
                                    EditText fromNumber = (EditText) autoIncLayout
                                            .findViewById(R.id.auto_inc_from);
                                    EditText toNumber = (EditText) autoIncLayout
                                            .findViewById(R.id.auto_inc_to);
                                    EditText repeatNumber = (EditText) autoIncLayout
                                            .findViewById(R.id.auto_inc_repeat);

                                    fromNumber.addTextChangedListener(new GenericTextWatcher(fromNumber, groupPosition));
                                    toNumber.addTextChangedListener(new GenericTextWatcher(toNumber, groupPosition));
                                    repeatNumber.addTextChangedListener(new GenericTextWatcher(repeatNumber, groupPosition));

                                    if (null != selectedOptions.get(groupPosition) && !selectedOptions.get(groupPosition).getSelectedValue().equals("Tap To Select Answer")) {
                                        fromNumber.setTag("Wait");
                                        toNumber.setTag("Wait");
                                        repeatNumber.setTag("Wait");
                                        fromNumber.setText(selectedOptions.get(groupPosition).getRangeFrom());
                                        toNumber.setText(selectedOptions.get(groupPosition).getRangeTo());
                                        repeatNumber.setText(selectedOptions.get(groupPosition).getRangeRepeat());
                                        fromNumber.setTag(null);
                                        toNumber.setTag(null);
                                        repeatNumber.setTag(null);
                                    }
                                    checkMeasurementButton();
                                    selectedOptions.set(groupPosition, so);
                                    break;
                                case 2:
                                    EditText scannedValue = (EditText) scanLayout
                                            .findViewById(R.id.scanned_input_edit_text);
                                    Button scanButton = (Button)scanLayout.findViewById(R.id.scan_button);
                                    scanButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(_context, CaptureActivity.class);
                                            intent.setAction("com.google.zxing.client.android.SCAN");
                                            // this stops saving ur barcode in barcode scanner app's history
                                            intent.putExtra("SAVE_HISTORY", false);
                                            ((QuestionsList) _context).startActivityForResult(intent, groupPosition);
                                        }
                                    });
                                    userEnteredLayout.setVisibility(View.GONE);
                                    scanLayout.setVisibility(View.VISIBLE);
                                    autoIncLayout.setVisibility(View.GONE);
                                    so.setOptionType(2);
                                    CheckBox remember2 = (CheckBox)mainLayout.findViewById(R.id.remember_check_box);
                                    remember2.setVisibility(View.VISIBLE);
                                    scannedValue.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            SelectedOptions so = selectedOptions.get(groupPosition);
                                            so.setSelectedValue(s.toString());
                                            selectedOptions.set(groupPosition, so);

                                            ExpandableListView explist = (ExpandableListView) mainLayout.getParent();
                                            LinearLayout ll2 = (LinearLayout) explist.findViewWithTag(groupPosition);
                                            TextView selectedAnswer = (TextView) ll2.findViewById(R.id.selectedAnswer);
                                            selectedAnswer.setText(s.toString());
                                            checkMeasurementButton();

                                            final int sdk = android.os.Build.VERSION.SDK_INT;
                                            if (s.length() > 0) {
                                                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                                    ll2.setBackgroundDrawable(_context.getResources().getDrawable(R.color.green_light));
                                                } else {
                                                    ll2.setBackground(_context.getResources().getDrawable(R.color.green_light));
                                                }
                                            } else {
                                                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                                    ll2.setBackgroundDrawable(_context.getResources().getDrawable(R.color.gray_light));
                                                } else {
                                                    ll2.setBackground(_context.getResources().getDrawable(R.color.gray_light));
                                                }
                                            }


                                        }
                                    });
                                    selectedOptions.set(groupPosition, so);
                                    if(so.isReset())
                                    {
                                        scannedValue.setText("");
                                        so.setReset(false);
                                        selectedOptions.set(groupPosition, so);
                                    }else {
                                        if (null != selectedOptions.get(groupPosition) && !selectedOptions.get(groupPosition).getSelectedValue().equals("Tap To Select Answer")) {
                                            scannedValue.setText(selectedOptions.get(groupPosition).getSelectedValue());
                                        }
                                    }
                                    break;
                                    default:
                                        userEnteredLayout.setVisibility(View.VISIBLE);
                                        scanLayout.setVisibility(View.GONE);
                                        autoIncLayout.setVisibility(View.GONE);
                                        so.setOptionType(0);
                                        selectedOptions.set(groupPosition,so);
                                        break;

                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });



                    CheckBox remember = (CheckBox)convertView.findViewById(R.id.remember_check_box);

                    SelectedOptions so = selectedOptions.get(groupPosition);

                    if(so.isRemember())
                    {
                        remember.setChecked(true);
                    }else {
                        remember.setChecked(false);
                    }

                    optionType.setSelection(so.getOptionType());

                    remember.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((CheckBox) v).isChecked()) {
                                SelectedOptions so = selectedOptions.get(groupPosition);
                                so.setRemember(true);
                                selectedOptions.set(groupPosition, so);
                            } else {
                                SelectedOptions so = selectedOptions.get(groupPosition);
                                so.setRemember(false);
                                selectedOptions.set(groupPosition, so);
                            }

                        }
                    });



                    return convertView;

                case Question.PROJECT_DEFINED:
                   // if (convertView == null) {
                        LayoutInflater proj_def_infalInflater = (LayoutInflater) this._context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = proj_def_infalInflater.inflate(R.layout.project_defined_option, null);
                    //}
                    CheckBox remember1 = (CheckBox)convertView.findViewById(R.id.remember_check_box);
                    SelectedOptions so1 = selectedOptions.get(groupPosition);
                    if(so1.isRemember())
                    {
                        remember1.setChecked(true);
                    }else {
                        remember1.setChecked(false);
                    }



                    remember1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((CheckBox) v).isChecked()) {
                                SelectedOptions so = selectedOptions.get(groupPosition);
                                so.setRemember(true);
                                selectedOptions.set(groupPosition, so);
                            }else {
                                SelectedOptions so = selectedOptions.get(groupPosition);
                                so.setRemember(false);
                                selectedOptions.set(groupPosition, so);
                            }

                        }
                    });

                    NoDefaultSpinner projectDefinedOptionsSpinner = (NoDefaultSpinner) convertView
                            .findViewById(R.id.project_defined_options_spinner);

                    List<String> list = getGroup(groupPosition).getOptions();
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(convertView.getContext(),
                            R.layout.simple_spinner_item,list );
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    projectDefinedOptionsSpinner.setAdapter(dataAdapter);
                    projectDefinedOptionsSpinner.setTag(groupPosition+"-"+childPosition);
                    projectDefinedOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String[] ids = ((String)parent.getTag()).split("-");
                            int questionNumber =  Integer.parseInt(ids[0]);
                            //int questionNumber = (int)parent.getTag();
                            SelectedOptions so = selectedOptions.get(questionNumber);
                            so.setSelectedValue(parent.getItemAtPosition(position).toString());
                            selectedOptions.set(questionNumber, so);

                            LinearLayout ll = (LinearLayout) parent.getParent();
                            ExpandableListView explist = (ExpandableListView) ll.getParent();

                            LinearLayout ll2 = (LinearLayout) explist.findViewWithTag(questionNumber);

                            if(ll2 != null){
                                TextView selectedAnswer = (TextView) ll2.findViewById(R.id.selectedAnswer);
                                selectedAnswer.setText(parent.getItemAtPosition(position).toString());
                            }

                            final int sdk = android.os.Build.VERSION.SDK_INT;
                            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                ll2.setBackgroundDrawable(_context.getResources().getDrawable(R.color.green_light));
                            } else {
                                ll2.setBackground(_context.getResources().getDrawable(R.color.green_light));
                            }
                            checkMeasurementButton();

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    if(so1.isReset())
                    {
                        projectDefinedOptionsSpinner.setSelection(-1);
                        so1.setReset(false);
                        selectedOptions.set(groupPosition, so1);
                    }else {
                        projectDefinedOptionsSpinner.setSelection(dataAdapter.getPosition(selectedOptions.get(groupPosition).getSelectedValue()));
                    }
                    return convertView;
                case Question.PHOTO_TYPE_DEFINED:
                    //if (convertView == null) {
                        LayoutInflater photo_infalInflater = (LayoutInflater) this._context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = photo_infalInflater.inflate(R.layout.image_options, null);
                    //}

                    CheckBox remember2 = (CheckBox)convertView.findViewById(R.id.remember_check_box);
                    SelectedOptions so2 = selectedOptions.get(groupPosition);
                    if(so2.isRemember())
                    {
                        remember2.setChecked(true);
                    }else {
                        remember2.setChecked(false);
                    }

                    remember2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((CheckBox) v).isChecked()) {
                                SelectedOptions so = selectedOptions.get(groupPosition);
                                so.setRemember(true);
                                selectedOptions.set(groupPosition, so);
                            }else {
                                SelectedOptions so = selectedOptions.get(groupPosition);
                                so.setRemember(false);
                                selectedOptions.set(groupPosition, so);
                            }

                        }
                    });
                    NoDefaultSpinner photoDefinedOptionsSpinner = (NoDefaultSpinner) convertView
                            .findViewById(R.id.image_options_spinner);
                    List<String> list1 = getGroup(groupPosition).getOptions();
                    ImageSpinnerAdapter dataAdapter1 = new ImageSpinnerAdapter(convertView.getContext(),
                            R.layout.spinner_image_text,list1 );

                    dataAdapter1.setDropDownViewResource(R.layout.spinner_image_text);

                    photoDefinedOptionsSpinner.setAdapter(dataAdapter1);
                    photoDefinedOptionsSpinner.setTag(groupPosition+"-"+childPosition);
                    photoDefinedOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String[] ids = ((String)parent.getTag()).split("-");
                            int questionNumber =  Integer.parseInt(ids[0]);
                            SelectedOptions so = selectedOptions.get(questionNumber);
                            so.setSelectedValue(parent.getItemAtPosition(position).toString());
                            selectedOptions.set(questionNumber, so);

//                            selectedOptions.set(questionNumber, parent.getItemAtPosition(position).toString());
                            LinearLayout ll = (LinearLayout) parent.getParent();
                            ExpandableListView explist = (ExpandableListView) ll.getParent();

                            LinearLayout ll2 = (LinearLayout) explist.findViewWithTag(questionNumber);

                            ImageView lblListHeader_image = (ImageView) ll2.findViewById(R.id.lblListHeader);
                            TextView selectedAnswer = (TextView) ll2.findViewById(R.id.selectedAnswer);
                            selectedAnswer.setText("");

                            String[] splitOptionText = selectedOptions.get(questionNumber).getSelectedValue().toString().split(",");
                            Picasso.with(_context)
                                    .load(splitOptionText[1])
                                    .placeholder(R.drawable.ic_launcher1)
                                    .resize(60, 60)
                                    .error(R.drawable.ic_launcher1)
                                    .into(lblListHeader_image);

                            final int sdk = android.os.Build.VERSION.SDK_INT;
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                ll2.setBackgroundDrawable(_context.getResources().getDrawable(R.color.green_light));
                            } else {
                                ll2.setBackground(_context.getResources().getDrawable(R.color.green_light));
                            }

                            checkMeasurementButton();

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    if(so2.isReset())
                    {
                        photoDefinedOptionsSpinner.setSelection(-1);
                        so2.setReset(false);
                        selectedOptions.set(groupPosition, so2);
                    }else{
                        photoDefinedOptionsSpinner.setSelection(dataAdapter1.getPosition(selectedOptions.get(groupPosition).getSelectedValue()));
                    }


//                    TextView txtListChild3 = (TextView) convertView
//                            .findViewById(R.id.lblListItem);
//
//                    txtListChild3.setText("Please handle me 3");

                    return convertView;
                default:
                    if (convertView == null) {
                        LayoutInflater infalInflater = (LayoutInflater) this._context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = infalInflater.inflate(R.layout.exp_question_list_item, null);
                    }

                    TextView txtListChildDefault = (TextView) convertView
                            .findViewById(R.id.lblListItem);

                    txtListChildDefault.setText("No options found !");
                    return convertView;

            }

        }else {
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.exp_question_list_item, null);
            }

            TextView txtListChildDefault = (TextView) convertView
                    .findViewById(R.id.lblListItem);

            txtListChildDefault.setText("No options found !");
            return convertView;
        }

    }


    @Override
    public int getChildrenCount(int groupPosition) {
//        return questionList.get(groupPosition).getOptions().size();
        return 1;
    }

    @Override
    public Question getGroup(int groupPosition) {
        return questionList.size() > 0 ?questionList.get(groupPosition):null;
    }

    @Override
    public int getGroupCount() {
        return questionList.size()>0?questionList.size():1;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Question question = getGroup(groupPosition);
        if(null != question) {
            switch (question.getQuestionType()) {
                case Question.USER_DEFINED:
                case Question.PROJECT_DEFINED:
                    LayoutInflater infalInflater = (LayoutInflater) this._context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = infalInflater.inflate(R.layout.list_group, null);
                    //}
                    convertView.setTag(groupPosition);

                    TextView lblListHeader = (TextView) convertView
                            .findViewById(R.id.lblListHeader);
                    TextView selectedAnswer = (TextView) convertView
                            .findViewById(R.id.selectedAnswer);
                    lblListHeader.setTypeface(null, Typeface.BOLD);
                    if (null != question) {
                        lblListHeader.setText(question.getQuestionText());
                        if (null != selectedOptions.get(groupPosition)) {
                            selectedAnswer.setText(selectedOptions.get(groupPosition).getSelectedValue());
                        }

                    } else {
                        lblListHeader.setText("No Questions Provided");
                    }

                    if (null != selectedOptions.get(groupPosition) && !selectedOptions.get(groupPosition).getSelectedValue().equals("Tap To Select Answer")) {
                        final int sdk = android.os.Build.VERSION.SDK_INT;
                        if(selectedOptions.get(groupPosition).getSelectedValue().length() > 0)
                        {
                            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                 convertView.setBackgroundDrawable(_context.getResources().getDrawable(R.color.green_light));
                            } else {
                                convertView.setBackground(_context.getResources().getDrawable(R.color.green_light));
                            }
                        }
                        else
                        {
                            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                convertView.setBackgroundDrawable(_context.getResources().getDrawable(R.color.gray_light));
                            } else {
                                convertView.setBackground(_context.getResources().getDrawable(R.color.gray_light));
                            }
                        }



                    }

                    break;
                case Question.PHOTO_TYPE_DEFINED:
                        LayoutInflater image_infalInflater = (LayoutInflater) this._context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = image_infalInflater.inflate(R.layout.list_group_image, null);
                        //}
                        convertView.setTag(groupPosition);



                    if (null != question) {
                        if (null != selectedOptions.get(groupPosition) && !selectedOptions.get(groupPosition).getSelectedValue().equals("Tap To Select Answer")) {
                        ImageView lblListHeader_image = (ImageView) convertView
                                .findViewById(R.id.lblListHeader);
                        String[] splitOptionText = selectedOptions.get(groupPosition).getSelectedValue().toString().split(",");
                        Picasso.with(_context)
                                .load(splitOptionText[1])
                                .placeholder(R.drawable.ic_launcher1)
                                .resize(60,60)
                                .error(R.drawable.ic_launcher1)
                                .into(lblListHeader_image);
                            TextView selectedAnswer1 = (TextView) convertView.findViewById(R.id.selectedAnswer);
                            selectedAnswer1.setText("");


                        }else {
                            TextView selectedAnswer1 = (TextView) convertView.findViewById(R.id.selectedAnswer);
                            selectedAnswer1.setText("Tap To Select Answer");

                        }
                        TextView selectedAnswer_image = (TextView) convertView
                                .findViewById(R.id.question);
                        selectedAnswer_image.setTypeface(null, Typeface.BOLD);
                        selectedAnswer_image.setText(question.getQuestionText());

                        if (null != selectedOptions.get(groupPosition) && !selectedOptions.get(groupPosition).getSelectedValue().equals("Tap To Select Answer")) {
                            final int sdk = android.os.Build.VERSION.SDK_INT;
                            if(selectedOptions.get(groupPosition).getSelectedValue().length() > 0)
                            {
                                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    convertView.setBackgroundDrawable(_context.getResources().getDrawable(R.color.green_light));
                                } else {
                                    convertView.setBackground(_context.getResources().getDrawable(R.color.green_light));
                                }
                            }
                            else
                            {
                                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    convertView.setBackgroundDrawable(_context.getResources().getDrawable(R.color.gray_light));
                                } else {
                                    convertView.setBackground(_context.getResources().getDrawable(R.color.gray_light));
                                }
                            }



                        }
                    }
//                    else {
//                       // lblListHeader.setText("No Questions Provided");
//                    }

                    break;
                default:
                    LayoutInflater infalInflater1 = (LayoutInflater) this._context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = infalInflater1.inflate(R.layout.list_group, null);
                    //}
                    convertView.setTag(groupPosition);

                    TextView lblListHeader1 = (TextView) convertView
                            .findViewById(R.id.lblListHeader);
                    TextView selectedAnswer1 = (TextView) convertView
                            .findViewById(R.id.selectedAnswer);
                    lblListHeader1.setTypeface(null, Typeface.BOLD);
                    lblListHeader1.setText("No Questions Provided");
                    break;
            }
        }else
        {
            LayoutInflater infalInflater1 = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater1.inflate(R.layout.list_group, null);
            //}
            convertView.setTag(groupPosition);

            TextView lblListHeader1 = (TextView) convertView
                    .findViewById(R.id.lblListHeader);
            TextView selectedAnswer1 = (TextView) convertView
                    .findViewById(R.id.selectedAnswer);
            lblListHeader1.setTypeface(null, Typeface.BOLD);
            lblListHeader1.setText("No Questions Provided");
        }
        //if (convertView == null) {
         convertView.setPadding(0, 20, 0, 0);
        exp.setDividerHeight(20);
        checkMeasurementButton();
        return convertView;
    }

    private void checkMeasurementButton()
    {

        ViewParent v = exp.getParent().getParent();
        Button btnTakeMeasurement = (Button) ((RelativeLayout)v).findViewById(R.id.btn_take_measurement);
        boolean flag = false;
        for (SelectedOptions option:selectedOptions
             ) {
            if (option.getSelectedValue().equals("Tap To Select Answer") || option.getSelectedValue().isEmpty()) {
                flag = true;
                break;
            }
        }

        if(flag) {
            btnTakeMeasurement.setText("Answer All Questions");
            btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_gray_light);
        }
        else
        {
            if(!btnTakeMeasurement.getText().equals("Cancel")) {
                btnTakeMeasurement.setText("+ Take Measurement");
                btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_orange);
            }

        }

    }
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setSelectedOptions(ArrayList<SelectedOptions> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }


    private class GenericTextWatcher implements TextWatcher {

        private View view;
        private int groupPosition;

        private GenericTextWatcher(View view, int groupPosition) {
            this.view = view;
            this.groupPosition = groupPosition;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            ExpandableListView exp = (ExpandableListView) (view.getParent().getParent().getParent().getParent());
            View headerView = exp.findViewWithTag(groupPosition);
            String s = editable.toString();
            SelectedOptions so = selectedOptions.get(groupPosition);
            if (!s.isEmpty()) {

                switch (view.getId()) {
                    case R.id.auto_inc_from:

                        so.setRangeFrom(s);

                        selectedOptions.set(groupPosition, so);
                        break;
                    case R.id.auto_inc_to:
                        so.setRangeTo(s);
                        selectedOptions.set(groupPosition, so);
                        break;
                    case R.id.auto_inc_repeat:
                        so.setRangeRepeat(s);
                        selectedOptions.set(groupPosition, so);
                        break;
                }
            } else {
                switch (view.getId()) {
                    case R.id.auto_inc_from:
                        so.setRangeFrom("");
                        selectedOptions.set(groupPosition, so);
                        break;
                    case R.id.auto_inc_to:
                        so.setRangeTo("");
                        selectedOptions.set(groupPosition, so);
                        break;
                    case R.id.auto_inc_repeat:
                        so.setRangeRepeat("");
                        selectedOptions.set(groupPosition, so);
                        break;

                }

            }
            TextView selectedAnswer = (TextView) headerView.findViewById(R.id.selectedAnswer);
            if (!so.getRangeFrom().isEmpty() && !so.getRangeTo().isEmpty() && !so.getRangeRepeat().isEmpty()) {
                if( view.getTag() == null )
                {
                    so.setSelectedValue(so.getRangeFrom());
                    selectedAnswer.setText(so.getRangeFrom());
                    so.setAutoIncIndex(0);
                }
            }
            else {
                so.setSelectedValue("");
                selectedAnswer.setText("");

            }
            selectedOptions.set(groupPosition, so);
            checkMeasurementButton();


            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (!so.getRangeFrom().isEmpty() && !so.getRangeTo().isEmpty() && !so.getRangeRepeat().isEmpty()) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    headerView.setBackgroundDrawable(_context.getResources().getDrawable(R.color.green_light));
                } else {
                    headerView.setBackground(_context.getResources().getDrawable(R.color.green_light));
                }
            } else {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    headerView.setBackgroundDrawable(_context.getResources().getDrawable(R.color.gray_light));
                } else {
                    headerView.setBackground(_context.getResources().getDrawable(R.color.gray_light));
                }
            }
        }
    }
}
