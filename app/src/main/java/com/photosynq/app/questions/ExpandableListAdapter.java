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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

import java.util.HashMap;
import java.util.List;

/**
 * Created by shekhar on 8/19/15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private final List<Question> mQuestionList;
    public final HashMap<Question, SelectedOptions> mSelectedOptions = new HashMap<>();
    private final ExpandableListView mExpandableListView;
    private final LayoutInflater mLayoutInflater;


    public HashMap<Question, SelectedOptions> getSelectedOptions() {
        return mSelectedOptions;
    }

    public ExpandableListAdapter(Context context, List<Question> questionList, ExpandableListView exp) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mExpandableListView = exp;

        this.mQuestionList = questionList;
        for (Question question : questionList) {
            SelectedOptions selectedOptions = new SelectedOptions();
            selectedOptions.setProjectId(question.getProjectId());
            selectedOptions.setQuestionType(question.getQuestionType());
            selectedOptions.setQuestionId(question.getQuestionId());
            selectedOptions.setSelectedValue("Tap To Select Answer");
            mSelectedOptions.put(question, selectedOptions);
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        //return this.mQuestionList.get(groupPosition).getOptions().get(childPosititon);
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

        final Question question = getGroup(groupPosition);
        if (null != question) {
            switch (question.getQuestionType()) {
                case Question.USER_DEFINED:

                    //if (convertView == null) {
                    LayoutInflater user_def_infalInflater = (LayoutInflater) this.mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = user_def_infalInflater.inflate(R.layout.user_selected_main_layout, null);
                    //}

                    Spinner optionType = (Spinner) convertView.findViewById(R.id.option_type);
                    optionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            final LinearLayout mainLayout = (LinearLayout) (parent.getParent().getParent());

                            LinearLayout userEnteredLayout = (LinearLayout) mainLayout.findViewById(R.id.layout_user_entered);
                            LinearLayout scanLayout = (LinearLayout) mainLayout.findViewById(R.id.layout_scan_code);
                            LinearLayout autoIncLayout = (LinearLayout) mainLayout.findViewById(R.id.layout_auto_inc);
                            final SelectedOptions selectedOption = mSelectedOptions.get(question);

                            switch (position) {
                                case 0:
                                    userEnteredLayout.setVisibility(View.VISIBLE);
                                    scanLayout.setVisibility(View.GONE);
                                    autoIncLayout.setVisibility(View.GONE);
                                    selectedOption.setOptionType(0);
                                    EditText txtListChild = (EditText) userEnteredLayout
                                            .findViewById(R.id.user_input_edit_text);
                                    CheckBox remember = (CheckBox) mainLayout.findViewById(R.id.remember_check_box);
                                    remember.setVisibility(View.VISIBLE);

                                    txtListChild.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            selectedOption.setSelectedValue(s.toString());

                                            ExpandableListView explist = (ExpandableListView) mainLayout.getParent();
                                            LinearLayout ll2 = (LinearLayout) explist.findViewWithTag(groupPosition);
                                            if (null != ll2) {
                                                TextView selectedAnswer = (TextView) ll2.findViewById(R.id.selectedAnswer);
                                                selectedAnswer.setText(s.toString());

                                                final int sdk = android.os.Build.VERSION.SDK_INT;
                                                if (s.length() > 0) {
                                                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                                        ll2.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.green_light));
                                                    } else {
                                                        ll2.setBackground(mContext.getResources().getDrawable(R.color.green_light));
                                                    }
                                                } else {
                                                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                                        ll2.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.gray_light));
                                                    } else {
                                                        ll2.setBackground(mContext.getResources().getDrawable(R.color.gray_light));
                                                    }
                                                }
                                            }
                                            checkMeasurementButton();

                                        }
                                    });
                                    if (selectedOption.isReset()) {
                                        txtListChild.setText("");
                                        selectedOption.setReset(false);

                                    } else {
                                        if (null != mSelectedOptions.get(groupPosition) && !mSelectedOptions.get(groupPosition).getSelectedValue().equals("Tap To Select Answer")) {
                                            txtListChild.setText(mSelectedOptions.get(groupPosition).getSelectedValue());
                                        }
                                    }
                                    if (txtListChild.requestFocus()) {
                                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(txtListChild, InputMethodManager.SHOW_IMPLICIT);
                                        //((QuestionsList)mContext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                    }

                                    break;
                                case 1:
                                    userEnteredLayout.setVisibility(View.GONE);
                                    scanLayout.setVisibility(View.GONE);
                                    autoIncLayout.setVisibility(View.VISIBLE);
                                    selectedOption.setOptionType(1);
                                    selectedOption.setRemember(true);
                                    CheckBox remember1 = (CheckBox) mainLayout.findViewById(R.id.remember_check_box);
                                    remember1.setVisibility(View.GONE);
                                    EditText fromNumber = (EditText) autoIncLayout
                                            .findViewById(R.id.auto_inc_from);
                                    EditText toNumber = (EditText) autoIncLayout
                                            .findViewById(R.id.auto_inc_to);
                                    EditText repeatNumber = (EditText) autoIncLayout
                                            .findViewById(R.id.auto_inc_repeat);

                                    fromNumber.addTextChangedListener(new GenericTextWatcher(fromNumber, question));
                                    toNumber.addTextChangedListener(new GenericTextWatcher(toNumber, question));
                                    repeatNumber.addTextChangedListener(new GenericTextWatcher(repeatNumber, question));

                                    if (null != mSelectedOptions.get(question) && !mSelectedOptions.get(question).getSelectedValue().equals("Tap To Select Answer")) {
                                        fromNumber.setTag("Wait");
                                        toNumber.setTag("Wait");
                                        repeatNumber.setTag("Wait");
                                        fromNumber.setText(mSelectedOptions.get(question).getRangeFrom());
                                        toNumber.setText(mSelectedOptions.get(question).getRangeTo());
                                        repeatNumber.setText(mSelectedOptions.get(question).getRangeRepeat());
                                        fromNumber.setTag(null);
                                        toNumber.setTag(null);
                                        repeatNumber.setTag(null);
                                    }
                                    checkMeasurementButton();

                                    break;
                                case 2:
                                    EditText scannedValue = (EditText) scanLayout
                                            .findViewById(R.id.scanned_input_edit_text);
                                    Button scanButton = (Button) scanLayout.findViewById(R.id.scan_button);
                                    scanButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(mContext, CaptureActivity.class);
                                            intent.setAction("com.google.zxing.client.android.SCAN");
                                            // this stops saving ur barcode in barcode scanner app's history
                                            intent.putExtra("SAVE_HISTORY", false);
                                            ((QuestionsList) mContext).startActivityForResult(intent, groupPosition);
                                        }
                                    });
                                    userEnteredLayout.setVisibility(View.GONE);
                                    scanLayout.setVisibility(View.VISIBLE);
                                    autoIncLayout.setVisibility(View.GONE);
                                    selectedOption.setOptionType(2);
                                    CheckBox remember2 = (CheckBox) mainLayout.findViewById(R.id.remember_check_box);
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
                                            SelectedOptions so = mSelectedOptions.get(question);
                                            so.setSelectedValue(s.toString());

                                            ExpandableListView explist = (ExpandableListView) mainLayout.getParent();
                                            LinearLayout ll2 = (LinearLayout) explist.findViewWithTag(groupPosition);
                                            if (null != ll2) {
                                                TextView selectedAnswer = (TextView) ll2.findViewById(R.id.selectedAnswer);
                                                selectedAnswer.setText(s.toString());
                                                checkMeasurementButton();

                                                final int sdk = android.os.Build.VERSION.SDK_INT;
                                                if (s.length() > 0) {
                                                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                                        ll2.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.green_light));
                                                    } else {
                                                        ll2.setBackground(mContext.getResources().getDrawable(R.color.green_light));
                                                    }
                                                } else {
                                                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                                        ll2.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.gray_light));
                                                    } else {
                                                        ll2.setBackground(mContext.getResources().getDrawable(R.color.gray_light));
                                                    }
                                                }
                                            }


                                        }
                                    });
                                    if (selectedOption.isReset()) {
                                        scannedValue.setText("");
                                        selectedOption.setReset(false);
                                    } else {
                                        if (null != mSelectedOptions.get(question) && !mSelectedOptions.get(question).getSelectedValue().equals("Tap To Select Answer")) {
                                            scannedValue.setText(mSelectedOptions.get(question).getSelectedValue());
                                        }
                                    }
                                    break;
                                default:
                                    userEnteredLayout.setVisibility(View.VISIBLE);
                                    scanLayout.setVisibility(View.GONE);
                                    autoIncLayout.setVisibility(View.GONE);
                                    selectedOption.setOptionType(0);
                                    break;

                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });


                    CheckBox remember = (CheckBox) convertView.findViewById(R.id.remember_check_box);

                    SelectedOptions so = mSelectedOptions.get(question);

                    if (so.isRemember()) {
                        remember.setChecked(true);
                    } else {
                        remember.setChecked(false);
                    }

                    optionType.setSelection(so.getOptionType());

                    remember.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((CheckBox) v).isChecked()) {
                                SelectedOptions so = mSelectedOptions.get(question);
                                so.setRemember(true);
                            } else {
                                SelectedOptions so = mSelectedOptions.get(question);
                                so.setRemember(false);
                            }

                        }
                    });


                    return convertView;

                case Question.PROJECT_DEFINED:
                    return inflateProjectDefined(question, convertView);
                case Question.PHOTO_TYPE_DEFINED:
                    final boolean[] collapse1 = {true};
                    //if (convertView == null) {
                    LayoutInflater photo_infalInflater = (LayoutInflater) this.mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = photo_infalInflater.inflate(R.layout.image_options, null);
                    //}

                    CheckBox remember2 = (CheckBox) convertView.findViewById(R.id.remember_check_box);
                    final SelectedOptions so2 = mSelectedOptions.get(question);

                    if (so2.isRemember()) {
                        remember2.setChecked(true);
                    } else {
                        remember2.setChecked(false);
                    }

                    remember2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((CheckBox) v).isChecked()) {
                                mSelectedOptions.get(question).setRemember(true);
                            } else {
                                mSelectedOptions.get(question).setRemember(false);
                            }

                        }
                    });
                    NoDefaultSpinner photoDefinedOptionsSpinner = (NoDefaultSpinner) convertView
                            .findViewById(R.id.image_options_spinner);
                    List<String> list1 = getGroup(groupPosition).getOptions();
                    ImageSpinnerAdapter dataAdapter1 = new ImageSpinnerAdapter(convertView.getContext(),
                            R.layout.spinner_image_text, list1);

                    dataAdapter1.setDropDownViewResource(R.layout.spinner_image_text);

                    final SelectedOptions selectedOption = mSelectedOptions.get(question);

                    photoDefinedOptionsSpinner.setAdapter(dataAdapter1);
                    photoDefinedOptionsSpinner.setTag(groupPosition + "-" + childPosition);
                    photoDefinedOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String[] ids = ((String) parent.getTag()).split("-");
                            int questionNumber = Integer.parseInt(ids[0]);
                            selectedOption.setSelectedValue(parent.getItemAtPosition(position).toString());

//                            mSelectedOptions.set(questionNumber, parent.getItemAtPosition(position).toString());
                            LinearLayout ll = (LinearLayout) parent.getParent();
                            ExpandableListView explist = (ExpandableListView) ll.getParent();

                            LinearLayout ll2 = (LinearLayout) explist.findViewWithTag(questionNumber);
                            if (null != ll2) {
                                ImageView lblListHeader_image = (ImageView) ll2.findViewById(R.id.lblListHeader);
                                TextView selectedAnswer = (TextView) ll2.findViewById(R.id.selectedAnswer);
                                selectedAnswer.setText("");

                                String[] splitOptionText = mSelectedOptions.get(question).getSelectedValue().toString().split(",");
                                Picasso.with(mContext)
                                        .load(splitOptionText[1])
                                        .placeholder(R.drawable.ic_launcher1)
                                        .resize(60, 60)
                                        .error(R.drawable.ic_launcher1)
                                        .into(lblListHeader_image);

                                final int sdk = android.os.Build.VERSION.SDK_INT;
                                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    ll2.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.green_light));
                                } else {
                                    ll2.setBackground(mContext.getResources().getDrawable(R.color.green_light));
                                }
                            }
                            checkMeasurementButton();
                            if (collapse1[0]) {
                                mExpandableListView.collapseGroup(groupPosition);
                            } else {
                                collapse1[0] = true;
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    if (so2.isReset()) {
                        photoDefinedOptionsSpinner.setSelection(-1);
                        so2.setReset(false);
                    } else {
                        photoDefinedOptionsSpinner.setSelection(dataAdapter1.getPosition(mSelectedOptions.get(groupPosition).getSelectedValue()));
                        collapse1[0] = false;
                    }


//                    TextView txtListChild3 = (TextView) convertView
//                            .findViewById(R.id.lblListItem);
//
//                    txtListChild3.setText("Please handle me 3");
                    return convertView;
                default:
                    if (convertView == null) {
                        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = infalInflater.inflate(R.layout.exp_question_list_item, null);
                    }

                    TextView txtListChildDefault = (TextView) convertView
                            .findViewById(R.id.lblListItem);

                    txtListChildDefault.setText("No options found !");
                    return convertView;

            }

        } else {
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.mContext
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
//        return mQuestionList.get(groupPosition).getOptions().size();
        return 1;
    }

    @Override
    public Question getGroup(int groupPosition) {
        return mQuestionList.size() > 0 ? mQuestionList.get(groupPosition) : null;
    }

    @Override
    public int getGroupCount() {
        return mQuestionList.size() > 0 ? mQuestionList.size() : 1;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Question question = getGroup(groupPosition);
        if (null != question) {
            switch (question.getQuestionType()) {
                case Question.USER_DEFINED:
                case Question.PROJECT_DEFINED:
                    LayoutInflater infalInflater = (LayoutInflater) this.mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = infalInflater.inflate(R.layout.list_group, null);
                    //}
                    convertView.setTag(question);

                    TextView lblListHeader = (TextView) convertView
                            .findViewById(R.id.lblListHeader);
                    TextView selectedAnswer = (TextView) convertView
                            .findViewById(R.id.selectedAnswer);
                    lblListHeader.setTypeface(null, Typeface.BOLD);
                    if (null != question) {
                        lblListHeader.setText(question.getQuestionText());
                        if (null != mSelectedOptions.get(question)) {
                            selectedAnswer.setText(mSelectedOptions.get(question).getSelectedValue());
                        }

                    } else {
                        lblListHeader.setText("No Questions Provided");
                    }

                    if (null != mSelectedOptions.get(question) && !mSelectedOptions.get(question).getSelectedValue().equals("Tap To Select Answer")) {
                        final int sdk = android.os.Build.VERSION.SDK_INT;
                        if (mSelectedOptions.get(question).getSelectedValue().length() > 0) {
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                convertView.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.green_light));
                            } else {
                                convertView.setBackground(mContext.getResources().getDrawable(R.color.green_light));
                            }
                        } else {
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                convertView.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.gray_light));
                            } else {
                                convertView.setBackground(mContext.getResources().getDrawable(R.color.gray_light));
                            }
                        }


                    }

                    break;
                case Question.PHOTO_TYPE_DEFINED:
                    LayoutInflater image_infalInflater = (LayoutInflater) this.mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = image_infalInflater.inflate(R.layout.list_group_image, null);
                    //}
                    convertView.setTag(groupPosition);


                    if (null != question) {
                        if (null != mSelectedOptions.get(question) && !mSelectedOptions.get(question).getSelectedValue().equals("Tap To Select Answer")) {
                            ImageView lblListHeader_image = (ImageView) convertView
                                    .findViewById(R.id.lblListHeader);
                            String[] splitOptionText = mSelectedOptions.get(question).getSelectedValue().toString().split(",");
                            Picasso.with(mContext)
                                    .load(splitOptionText[1])
                                    .placeholder(R.drawable.ic_launcher1)
                                    .resize(60, 60)
                                    .error(R.drawable.ic_launcher1)
                                    .into(lblListHeader_image);
                            TextView selectedAnswer1 = (TextView) convertView.findViewById(R.id.selectedAnswer);
                            selectedAnswer1.setText("");


                        } else {
                            TextView selectedAnswer1 = (TextView) convertView.findViewById(R.id.selectedAnswer);
                            selectedAnswer1.setText("Tap To Select Answer");

                        }
                        TextView selectedAnswer_image = (TextView) convertView
                                .findViewById(R.id.question);
                        selectedAnswer_image.setTypeface(null, Typeface.BOLD);
                        selectedAnswer_image.setText(question.getQuestionText());

                        if (null != mSelectedOptions.get(question) && !mSelectedOptions.get(question).getSelectedValue().equals("Tap To Select Answer")) {
                            final int sdk = android.os.Build.VERSION.SDK_INT;
                            if (mSelectedOptions.get(question).getSelectedValue().length() > 0) {
                                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    convertView.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.green_light));
                                } else {
                                    convertView.setBackground(mContext.getResources().getDrawable(R.color.green_light));
                                }
                            } else {
                                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    convertView.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.gray_light));
                                } else {
                                    convertView.setBackground(mContext.getResources().getDrawable(R.color.gray_light));
                                }
                            }


                        }
                    }
//                    else {
//                       // lblListHeader.setText("No Questions Provided");
//                    }

                    break;
                default:
                    LayoutInflater infalInflater1 = (LayoutInflater) this.mContext
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
        } else {
            LayoutInflater infalInflater1 = (LayoutInflater) this.mContext
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
        mExpandableListView.setDividerHeight(20);
        checkMeasurementButton();
        return convertView;
    }

    private void checkMeasurementButton() {

        ViewParent v = mExpandableListView.getParent().getParent();
        Button btnTakeMeasurement = (Button) ((RelativeLayout) v).findViewById(R.id.btn_take_measurement);
        boolean flag = false;
        for (SelectedOptions option : mSelectedOptions.values()) {
            if (option.getSelectedValue().equals("Tap To Select Answer") || option.getSelectedValue().isEmpty()) {
                flag = true;
                break;
            }
        }

        if (flag) {
            btnTakeMeasurement.setText("Answer All Questions");
            btnTakeMeasurement.setBackgroundResource(R.drawable.btn_layout_gray_light);
        } else {
            if (!btnTakeMeasurement.getText().equals("Cancel")) {
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

//    public void setSelectedOptions(ArrayList<SelectedOptions> selectedOptions) {
//        this.mSelectedOptions = selectedOptions;
//    }


    private class GenericTextWatcher implements TextWatcher {

        private final View mView;
        private final Question mQuestion;

        private GenericTextWatcher(View view, Question question) {
            this.mView = view;
            this.mQuestion = question;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            ExpandableListView exp = (ExpandableListView) (mView.getParent().getParent().getParent().getParent());
            View headerView = exp.findViewWithTag(mQuestion);
            String s = editable.toString();
            SelectedOptions selectedOption = mSelectedOptions.get(mQuestion);
            if (!s.isEmpty()) {

                switch (mView.getId()) {
                    case R.id.auto_inc_from:
                        selectedOption.setRangeFrom(s);
                        break;
                    case R.id.auto_inc_to:
                        selectedOption.setRangeTo(s);
                        break;
                    case R.id.auto_inc_repeat:
                        selectedOption.setRangeRepeat(s);
                        break;
                }
            } else {
                switch (mView.getId()) {
                    case R.id.auto_inc_from:
                        selectedOption.setRangeFrom("");
                        break;
                    case R.id.auto_inc_to:
                        selectedOption.setRangeTo("");
                        break;
                    case R.id.auto_inc_repeat:
                        selectedOption.setRangeRepeat("");
                        break;

                }

            }
            TextView selectedAnswer = (TextView) headerView.findViewById(R.id.selectedAnswer);
            if (!selectedOption.getRangeFrom().isEmpty() && !selectedOption.getRangeTo().isEmpty() && !selectedOption.getRangeRepeat().isEmpty()) {
                if (mView.getTag() == null) {
                    selectedOption.setSelectedValue(selectedOption.getRangeFrom());
                    selectedAnswer.setText(selectedOption.getRangeFrom());
                    selectedOption.setAutoIncIndex(0);
                }
            } else {
                selectedOption.setSelectedValue("");
                selectedAnswer.setText("");

            }

            checkMeasurementButton();


            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (!selectedOption.getRangeFrom().isEmpty() && !selectedOption.getRangeTo().isEmpty() && !selectedOption.getRangeRepeat().isEmpty()) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    headerView.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.green_light));
                } else {
                    headerView.setBackground(mContext.getResources().getDrawable(R.color.green_light));
                }
            } else {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    headerView.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.gray_light));
                } else {
                    headerView.setBackground(mContext.getResources().getDrawable(R.color.gray_light));
                }
            }
        }
    }

    public View inflateProjectDefined(final Question question, View convertView) {
        convertView = mLayoutInflater.inflate(R.layout.project_defined_option, null);
        CheckBox chkRemember = (CheckBox) convertView.findViewById(R.id.remember_check_box);
        SelectedOptions currentOption = mSelectedOptions.get(question);
        chkRemember.setChecked(currentOption != null && currentOption.isRemember());

        if (currentOption == null) {
            currentOption = new SelectedOptions();
            currentOption.setOptionType(Question.PROJECT_DEFINED);
        }

        chkRemember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedOptions.get(question).setRemember(((CheckBox) v).isChecked());
            }
        });


        LinearLayout currentLayout = null;
        for (int i = 0; i < question.getOptions().size(); i++) {
            if (i % 2 == 0) {
                currentLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.user_answer_text_row, null);
                ((LinearLayout) convertView).addView(currentLayout);
                TextView tv = (TextView) currentLayout.findViewById(R.id.view_left);
                tv.setText(question.getOptions().get(i));

            } else {
                TextView tv = (TextView) currentLayout.findViewById(R.id.view_right);
                tv.setText(question.getOptions().get(i));
            }
        }
                    /* changes MDC Nexus-Computing */

        /**
         NoDefaultSpinner projectDefinedOptionsSpinner = (NoDefaultSpinner) convertView
         .findViewById(R.id.project_defined_options_spinner);

         List<String> list = getGroup(groupPosition).getOptions();
         ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(convertView.getContext(),
         R.layout.simple_spinner_item, list);

         dataAdapter.setDropDownViewResource(R.layout.spinner_text);
         projectDefinedOptionsSpinner.setAdapter(dataAdapter);
         projectDefinedOptionsSpinner.setTag(groupPosition + "-" + childPosition);
         projectDefinedOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> parent, View mView, int position, long id) {
        String[] ids = ((String) parent.getTag()).split("-");
        int questionNumber = Integer.parseInt(ids[0]);
        //int questionNumber = (int)parent.getTag();
        SelectedOptions so = mSelectedOptions.get(questionNumber);
        so.setSelectedValue(parent.getItemAtPosition(position).toString());
        mSelectedOptions.set(questionNumber, so);

        LinearLayout ll = (LinearLayout) parent.getParent();
        ExpandableListView explist = (ExpandableListView) ll.getParent();

        LinearLayout ll2 = (LinearLayout) explist.findViewWithTag(questionNumber);
        if (null != ll2) {
        TextView selectedAnswer = (TextView) ll2.findViewById(R.id.selectedAnswer);
        selectedAnswer.setText(parent.getItemAtPosition(position).toString());

        final int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        ll2.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.green_light));
        } else {
        ll2.setBackground(mContext.getResources().getDrawable(R.color.green_light));
        }
        }
        checkMeasurementButton();
        if (collapse[0]) {
        mExpandableListView.collapseGroup(groupPosition);
        } else {
        collapse[0] = true;
        }

        }

        @Override public void onNothingSelected(AdapterView<?> parent) {

        }
        });

         if (so1.isReset()) {
         projectDefinedOptionsSpinner.setSelection(-1);
         so1.setReset(false);
         mSelectedOptions.set(groupPosition, so1);

         } else {
         projectDefinedOptionsSpinner.setSelection(dataAdapter.getPosition(mSelectedOptions.get(groupPosition).getSelectedValue()));
         collapse[0] = false;
         }
         **/


        return convertView;
    }

    private final View.OnClickListener mImageOptionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mProjectDefinedOptionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mUserDefinedOptionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };


}
