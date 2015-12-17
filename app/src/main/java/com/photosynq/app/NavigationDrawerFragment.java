package com.photosynq.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import com.photosynq.app.utils.CommonUtils;
import com.photosynq.app.utils.Constants;
import com.photosynq.app.utils.PrefUtils;
import com.photosynq.app.utils.SyncHandler;
import com.squareup.picasso.Picasso;

import java.util.Set;

import static com.photosynq.app.utils.NxDebugEngine.log;

//import tourguide.tourguide.TourGuide;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    public static final int ACTION_PROFILE = 0;
    public static final int ACTION_PROJECTS = 1;
    public static final int ACTION_DISCOVER = 2;
    public static final int ACTION_QUICK_MEASUREMENT = 3;
    public static final int ACTION_SYNC_SETTINGS = 4;
    public static final int ACTION_ABOUT = 5;
    public static final int ACTION_SEND_DEBUG = 6;
    public static final int ACTION_SYNC_DATA = 7;
    public static final int ACTION_CACHED = 8;
    public static final int ACTION_DEVICE = 9;

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private NavigationDrawerAdapter mNavigationAdapter;
    private View mFragmentContainerView;

    private int mCurrentSelectedAction = ACTION_PROJECTS;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    TextView tvDeviceName;
    TextView tvDeviceAddress;
    //TourGuide mTourGuideHandler;
    private DatabaseHelper db;
    private Button totalDataPointsBtn;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedAction = savedInstanceState.getInt(Constants.STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedAction);
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
                R.layout.fragment_navigation_drawer, container, false);

        TextView tvUserName = (TextView) linearLayout.findViewById(R.id.tvUserName);
        tvUserName.setTypeface(CommonUtils.getInstance(getActivity()).getFontRobotoLight());
        tvUserName.setText(PrefUtils.getFromPrefs(getActivity(), PrefUtils.PREFS_NAME_KEY, ""));
        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem(ACTION_PROFILE);
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
                selectItem(ACTION_PROFILE);
            }
        });

        tvDeviceName = (TextView) linearLayout.findViewById(R.id.tvDeviceName);
        tvDeviceName.setTypeface(CommonUtils.getInstance(getActivity()).getFontRobotoMedium());
        tvDeviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem(ACTION_DEVICE);
                //mTourGuideHandler.cleanUp();
            }
        });

        tvDeviceAddress = (TextView) linearLayout.findViewById(R.id.tvDeviceAddress);
        tvDeviceAddress.setTypeface(CommonUtils.getInstance(getActivity()).getFontRobotoRegular());
        tvDeviceAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem(ACTION_DEVICE);
            }
        });

        db = DatabaseHelper.getHelper(getActivity());
        //List<ProjectResult> listRecords = db.getAllUnUploadedResults();
        //int recordCount = db.getAllUnuploadedResultsCount(null);
        //PrefUtils.saveToPrefs(getActivity(), PrefUtils.PREFS_TOTAL_CACHED_DATA_POINTS, "" + recordCount);
        totalDataPointsBtn = (Button) linearLayout.findViewById(R.id.totalCachedDataPointsBtn);
        //totalDataPointsBtn.setText("" + recordCount);

        totalDataPointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int recordCount = db.getAllUnuploadedResultsCount(null);
                if (recordCount == 0) {
                    Toast.makeText(getActivity(), "No cached data point", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), DisplayCachedDataPoints.class);
                    startActivity(intent);
                }
                selectItem(ACTION_CACHED);
            }
        });

        TableRow syncWithServerBtnMenuBar = (TableRow) linearLayout.findViewById(R.id.sync_with_server_btn_menu_bar);
        syncWithServerBtnMenuBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                SyncHandler syncHandler = new SyncHandler(mainActivity, MainActivity.getProgressBar());
                syncHandler.DoSync();

                Toast.makeText(getActivity(), "Sync started!", Toast.LENGTH_LONG).show();

                selectItem(ACTION_SYNC_DATA);
            }
        });

        mDrawerListView = (ListView) linearLayout.findViewById(R.id.navigation_item_list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int action = ACTION_ABOUT;
                log("pos is %d", position);
                switch (position) {
                    case 0:
                        action = ACTION_PROJECTS;
                        break;
                    case 1:
                        action = ACTION_DISCOVER;
                        break;
                    case 2:
                        action = ACTION_QUICK_MEASUREMENT;
                        break;
                    case 3:
                        action = ACTION_SYNC_SETTINGS;
                        break;
                    case 4:
                        action = ACTION_ABOUT;
                        break;
                    case 5:
                        action = ACTION_SEND_DEBUG;
                        break;
                    default:
                        break;
                }
                selectItem(action);
            }
        });
        mNavigationAdapter = new NavigationDrawerAdapter(
                getActivity(),
                R.layout.navigation_drawer_item,
                new String[]{
                        getString(R.string.my_projects_title),
                        getString(R.string.discover_title),
                        getString(R.string.quick_measurement_title),
                        getString(R.string.sync_settings_title),
                        getString(R.string.about),
                        "Send Debug",
                });
        mDrawerListView.setAdapter(mNavigationAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedAction, true);

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
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_action_navigation_menu,             /* nav drawer image to replace 'Up' caret */
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
                if (null == bluetoothAdapter)
                    return;
                Set<BluetoothDevice> btDevices = bluetoothAdapter.getBondedDevices();
                String userId = PrefUtils.getFromPrefs(getActivity(), PrefUtils.PREFS_LOGIN_USERNAME_KEY, PrefUtils.PREFS_DEFAULT_VAL);
                AppSettings appSettings = databaseHelper.getSettings(userId);
                for (BluetoothDevice device : btDevices) {
                    if (null != appSettings.getConnectionId() && appSettings.getConnectionId().equals(device.getAddress())) {
                        setDeviceConnected(device.getName(), appSettings.getConnectionId());
                    }
                }

                //final List<ProjectResult> listRecords = db.getAllUnUploadedResults();
                int recordCount = db.getAllUnuploadedResultsCount(null);
                totalDataPointsBtn.setText("" + recordCount);
                if (appSettings.getConnectionId() == null) {
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
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);

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

    public void openDrawer() {
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    private void selectItem(int action) {
        mCurrentSelectedAction = action;
        log("action selected %d", action);

        switch (action) {
            case ACTION_PROFILE:
            case ACTION_PROJECTS:
            case ACTION_DISCOVER:
            case ACTION_QUICK_MEASUREMENT:
            case ACTION_SYNC_SETTINGS:
            case ACTION_ABOUT:
                if (mDrawerListView != null) {
                    mDrawerListView.setItemChecked(action - 1, true);
                    mNavigationAdapter.setItemSelected(action - 1);//this is a bit nasty, we probably should map actions to positions
                }
                break;
            case ACTION_SEND_DEBUG:
                PhotoSyncApplication.sApplication.log("debug button pressed", "this is buffer content", "ui-operations");
                PhotoSyncApplication.sApplication.uploadLog();
                break;
            case ACTION_SYNC_DATA:
            case ACTION_CACHED:
            case ACTION_DEVICE:
                break;
            default:
                break;
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(action);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
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
        outState.putInt(Constants.STATE_SELECTED_POSITION, mCurrentSelectedAction);
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
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    private class NavigationDrawerAdapter extends ArrayAdapter<String> {

        private Context mContext;
        private int id;
        private String[] items;
        private int mSelectedPosition;

        public NavigationDrawerAdapter(Context context, int resourceId, String[] list) {
            super(context, resourceId, list);
            mContext = context;
            id = resourceId;
            items = list;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            View mView = v;
            if (mView == null) {
                LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = vi.inflate(id, null);
            }

            TextView text = (TextView) mView.findViewById(R.id.tvNavigationItem);

            if (items[position] != null) {
                text.setTextColor(Color.WHITE);
                text.setText(items[position]);
                text.setTypeface(CommonUtils.getInstance(mContext).getFontRobotoLight());

                if (position == mSelectedPosition)
                    ((View) text.getParent()).setBackgroundColor(mContext.getResources().getColor(R.color.green));
                else
                    ((View) text.getParent()).setBackgroundColor(mContext.getResources().getColor(R.color.transparent));

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
//                                    final List<ProjectResult> listRecords = db.getAllUnUploadedResults();
//                                    String totalCachedDataPoints = PrefUtils.getFromPrefs(getActivity(), PrefUtils.PREFS_TOTAL_CACHED_DATA_POINTS, "0");
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
