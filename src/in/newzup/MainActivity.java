package in.newzup;


import in.newzup.showcase.ShowcaseManager;
import in.newzup.showcaseview.OnShowcaseEventListener;
import in.newzup.showcaseview.ShowcaseView;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity implements
ActionBar.TabListener, OnShowcaseEventListener {
	
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "manifest-audio-93110";
    static final String TAG = "GCM NewzUp";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regid;

	private static final int RESULT_SETTINGS = 1;
	private static final int RESULT_SIGNIN = 2;
	private static final int RESULT_SIGNOUT = 3;
	private static final int RESULT_POST_NEWS = 4;
	int is_signed_in = 0;
	int is_first_time = 1;
	int never_ask_sign_in = 0;

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;

	private String[] mNewsCategories;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        /*if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }*/

		mNewsCategories = getResources().getStringArray(R.array.category_arrays);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		is_signed_in = preferences.getInt("SIGN_IN", 0);
		String username = preferences.getString("USER_EMAIL", "");
		String userImgurl = preferences.getString("USER_IMGURL", "");
		if(is_signed_in == 1)
		{
			UserDataHolder userDataHolder = UserDataHolder.getInstance();
			userDataHolder.setUsername(username);
			userDataHolder.setImgurl(userImgurl);
		}

		//Action Bar
		actionBar = getActionBar();

		// Adding Tabs
		String current_tag = getIntent().getStringExtra("tag");
		if(current_tag != null)
		{
			mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), 1);
			actionBar.addTab(actionBar.newTab().setText(current_tag).setTabListener(this));
		}			
		else
		{
			mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), 8);
			for (String tab_name : mNewsCategories) {
				actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
			}
		}

		// viewpager Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);		
		viewPager.setAdapter(mAdapter);

		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});

	}

	@Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }
	
	/**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    
    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }
        }.execute(null, null, null);
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
      // Your implementation here.
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		is_signed_in = preferences.getInt("SIGN_IN", 0);
		if(is_signed_in == 1)
		{
			MenuItem item = menu.findItem(R.id.action_signout);
			item.setVisible(true);
		}
		else
		{
			MenuItem item = menu.findItem(R.id.action_signin);
			item.setVisible(true);
		}

		return true;
	}

	public void refresh()
	{
		int cur_item = viewPager.getCurrentItem();
		NewsFragment newsFragment = (NewsFragment) mAdapter.getItem(cur_item);
		newsFragment.refresh();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case R.id.action_refresh:
			refresh();
			return true;

		case R.id.action_new:
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			is_signed_in = preferences.getInt("SIGN_IN", 0);
			if(is_signed_in == 1)
			{
				intent = new Intent(getApplicationContext(), PostNewsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
			else
			{
				intent = new Intent(getApplicationContext(), SignInActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivityForResult(intent, RESULT_POST_NEWS);
			}
			return true;
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_about:
			intent = new Intent();
			intent.setClass(MainActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_settings:
			intent = new Intent(this, CategoryPreferenceActivity.class);
			startActivityForResult(intent, RESULT_SETTINGS);
			return true;
		case R.id.action_signin:
			intent = new Intent(getApplicationContext(), SignInActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivityForResult(intent, RESULT_SIGNIN);
			return true;
		case R.id.action_signout:
			signOut();
			intent  = getIntent();
			finish();
			startActivity(intent);
			return true;
		case R.id.action_help:
			new ShowcaseManager(this).showcaseMainActivity();
			//showHelp();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void restart()
	{
		Intent intent  = getIntent();
		finish();
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			refresh();
			break;
		case RESULT_SIGNIN:
			restart();
			break;
		case RESULT_SIGNOUT:
			refresh();
			break;
		case RESULT_POST_NEWS:
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			is_signed_in = preferences.getInt("SIGN_IN", 0);
			if(is_signed_in == 1)
			{
				Intent intent = new Intent(getApplicationContext(), PostNewsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
			break;
		}
	}

	public void signOut()
	{
		// Set flag in SharedPreference
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("SIGN_IN", 0);
		editor.putString("USER_NAME", "");
		editor.putString("USER_EMAIL", "");
		editor.putString("USER_IMGURL", "");
		editor.apply();

		UserDataHolder userDataHolder = UserDataHolder.getInstance();
		userDataHolder.setUsername("");
		userDataHolder.setImgurl("");
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
	}

	@Override
	public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
	}

}