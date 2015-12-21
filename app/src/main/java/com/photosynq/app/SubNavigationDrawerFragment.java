package com.photosynq.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.photosynq.app.db.DatabaseHelper;
import com.photosynq.app.model.AppSettings;
import com.photosynq.app.model.ProjectResult;
import com.photosynq.app.utils.CommonUtils;
import com.photosynq.app.utils.Constants;
import com.photosynq.app.utils.PrefUtils;
import com.photosynq.app.utils.SyncHandler;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Set;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class SubNavigationDrawerFragment extends Fragment {

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_SUB_DRAWER = "sub_navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private SubNavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private SubNavigationDrawerAdapter mNavigationAdapter;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    TextView tvDeviceName;
    TextView tvDeviceAddress;
    //TourGuide mTourGuideHandler;
    private DatabaseHelper db;
    private String mProjectId;
    private Button totalDataPointsBtn;

    public SubNavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_SUB_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(Constants.STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        //selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(
                R.layout.fragment_sub_navigation_drawer, container, false);

        TextView tvUserName = (TextView) linearLayout.findViewById(R.id.tvUserName);
        tvUserName.setTypeface(CommonUtils.getInstance().getFontRobotoLight());
        tvUserName.setText(PrefUtils.getFromPrefs(getActivity(), PrefUtils.PREFS_NAME_KEY, ""));
        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem(4);
            }
        });

        String imageUrl = PrefUtils.getFromPrefs(getActivity(), PrefUtils.PREFS_THUMB_URL_KEY, PrefUtils.PREFS_DEFAULT_VAL);
        ImageView profileImage = (ImageView) linearLayout.findViewById(R.id.user_profile_image);
        Picasso.with(getActivity())
                .load(imageUrl)
                .error(R.drawable.ic_launcher1)
                .into(profileImage);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem(4);
            }
        });

        tvDeviceName = (TextView) linearLayout.findViewById(R.id.tvDeviceName);
        tvDeviceName.setTypeface(CommonUtils.getInstance().getFontRobotoMedium());
        tvDeviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem(3);
                //mTourGuideHandler.cleanUp();
            }
        });

        tvDeviceAddress = (TextView) linearLayout.findViewById(R.id.tvDeviceAddress);
        tvDeviceAddress.setTypeface(CommonUtils.getInstance().getFontRobotoRegular());
        tvDeviceAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem(3);
            }
        });

        db = DatabaseHelper.getHelper(getActivity());
        //int recordCount = db.getAllUnuploadedResultsCount(mProjectId);
        totalDataPointsBtn = (Button) linearLayout.findViewById(R.id.totalCachedDataPointsBtn);
        //totalDataPointsBtn.setText("" + recordCount);

        totalDataPointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int recordCount = db.getAllUnuploadedResultsCount(null);
                if(recordCount == 0) {
                    Toast.makeText(getActivity(), "No cached data point", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(getActivity(), DisplayCachedDataPoints.class);
                    startActivity(intent);
                }
                selectItem(6);
            }
        });

        TableRow syncWithServerBtnMenuBar = (TableRow) linearLayout.findViewById(R.id.sync_with_server_btn_menu_bar);
        syncWithServerBtnMenuBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MainActivity mainActivity = (MainActivity) getActivity();
                SyncHandler syncHandler = new SyncHandler(getActivity(), MainActivity.getProgressBar());
                syncHandler.DoSync();

                Toast.makeText(getActivity(), "Sync started!", Toast.LENGTH_LONG).show();

                selectItem(6);
            }
        });

        mDrawerListView = (ListView) linearLayout.findViewById(R.id.navigation_item_list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        mNavigationAdapter = new SubNavigationDrawerAdapter(
                getActivity(),
                R.layout.navigation_drawer_item,
                new String[]{
                        "Project Home",
                        "Main Menu"
                });
        mDrawerListView.setAdapter(mNavigationAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        return linearLayout;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, String projectId) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mProjectId = projectId;

        //List<ProjectResult> listRecords = db.getAllUnUploadedResults(mProjectId);
        //totalDataPointsBtn.setText("" + listRecords.size());

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        if(null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.back_arrow,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                DatabaseHelper databaseHelper = DatabaseHelper.getHelper(getActivity());
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(null == bluetoothAdapter)
                    return;
                Set<BluetoothDevice> btDevices =  bluetoothAdapter.getBondedDevices();
                String userId = PrefUtils.getFromPrefs(getActivity(), PrefUtils.PREFS_LOGIN_USERNAME_KEY, PrefUtils.PREFS_DEFAULT_VAL);
                AppSettings appSettings = databaseHelper.getSettings(userId);
                for (BluetoothDevice device : btDevices) {
                    if(null != appSettings.getConnectionId() && appSettings.getConnectionId().equals(device.getAddress()))
                    {
                        setDeviceConnected(device.getName(), appSettings.getConnectionId());
                    }
                }

                int resultsCount = db.getAllUnuploadedResultsCount(mProjectId);
                totalDataPointsBtn.setText("" + resultsCount);


                if(appSettings.getConnectionId() == null){
                    ((MainActivity) getActivity()).setDeviceConnected("Tap to connect device", "");
                }


                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_SUB_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            //??mDrawerLayout.openDrawer(mFragmentContainerView);

//            CommonUtils.showShowCaseView(getActivity(), R.id.tvDeviceName, "Welcome to PhotosynQ!", "First, pair your measurement device")
//                    .setOnShowcaseEventListener(new OnShowcaseEventListener() {
//                        @Override
//                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
//
//                        }
//
//                        @Override
//                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
//
//                            CommonUtils.showShowCaseView(getActivity(), R.id.navigation_item_list, "Search for new projects in Discover or projects you created or joined in My Projects", "")
//                                    .setOnShowcaseEventListener(new OnShowcaseEventListener() {
//                                        @Override
//                                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
//
//                                        }
//
//                                        @Override
//                                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
//
//                                            CommonUtils.showShowCaseView(getActivity(), R.id.tvDeviceName, "If collecting data offline, sync saved data + update projects once you have internet access", "");
//                                        }
//
//                                        @Override
//                                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
//
//                                        }
//                                    });
//
//                        }
//
//                        @Override
//                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
//
//                        }
//                    });

        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void openDrawer(){
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }
    }

    public void closeDrawer(){
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if(position < 3) {
            if (mDrawerListView != null) {
                mDrawerListView.setItemChecked(position, true);
                mNavigationAdapter.setItemSelected(position);
            }
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (SubNavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
//        if (mDrawerLayout != null && isDrawerOpen()) {
//            inflater.inflate(R.menu.menu_discover, menu);
//            showGlobalContextActionBar();
//        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public void setDeviceConnected(String deviceName, String deviceAddress) {
        tvDeviceName.setText(deviceName);
        tvDeviceAddress.setText(deviceAddress);

//        if (deviceName.equals("Tap to connect device")) {
//            mTourGuideHandler = CommonUtils.showShowCaseView(getActivity(), R.id.tvDeviceName, "First, pair your measurement device", "");
//        }

    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface SubNavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    private class SubNavigationDrawerAdapter extends ArrayAdapter<String> {

        private Context mContext;
        private int id;
        private String[] items;
        private int mSelectedPosition;

        public SubNavigationDrawerAdapter(Context context, int resourceId , String[] list )
        {
            super(context, resourceId, list);
            mContext = context;
            id = resourceId;
            items = list;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent)
        {
            View mView = v ;
            if(mView == null){
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = vi.inflate(id, null);
            }

            TextView text = (TextView) mView.findViewById(R.id.tvNavigationItem);

            if(items[position] != null )
            {
                text.setTextColor(Color.WHITE);
                text.setText(items[position]);
                text.setTypeface(CommonUtils.getInstance().getFontRobotoLight());

                if(position == mSelectedPosition)
                    ((View)text.getParent()).setBackgroundColor(mContext.getResources().getColor( R.color.green));
                else
                    ((View)text.getParent()).setBackgroundColor(mContext.getResources().getColor( R.color.transparent));

            }

            return mView;
        }

        public void setItemSelected(int position) {
            mSelectedPosition = position;
        }
    }

    public void onResume() {
        super.onResume();
//        Thread t = new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    while (!isInterrupted()) {
//                        Thread.sleep(1000);
//                        if (getActivity() != null) {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    db = DatabaseHelper.getHelper(getActivity());
//                                    List<ProjectResult> listRecords = db.getAllUnUploadedResults(mProjectId);
//                                    totalDataPointsBtn.setText("" + listRecords.size());
//                                }
//                            });
//                        }
//                    }
//                } catch (InterruptedException e) {
//                }
//            }
//        };
//
//        t.start();
    }
}
