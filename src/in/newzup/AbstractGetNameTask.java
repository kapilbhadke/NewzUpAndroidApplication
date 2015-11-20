package in.newzup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;

/**
 * Display personalized greeting. This class contains boilerplate code to
 * consume the token but isn't integral to getting the tokens.
 */
public abstract class AbstractGetNameTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "TokenInfoTask";
	protected Activity mActivity;
    public static String GOOGLE_USER_DATA="No_data";
	protected String mScope;
	protected String mEmail;
	protected int mRequestCode;
	protected String launch_this_activity;
	protected int post_id;
	protected String username = "";
	protected String imgurl = "";

	AbstractGetNameTask(Activity activity, String email, String scope) {
		this.mActivity = activity;
		this.mScope = scope;
		this.mEmail = email;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			fetchNameFromProfileServer();
			
		} catch (IOException ex) {
			onError("Following Error occured, please try again. "
					+ ex.getMessage(), ex);
		} catch (JSONException e) {
			onError("Bad response: " + e.getMessage(), e);
		}
		return null;
	}

	protected void onPostExecute(String result) {
		try {

		} catch (Exception e) {
			Log.d("InputStream", "AbstractGetNameTask: "+e.getLocalizedMessage());
		}
	}
	
	protected void onError(String msg, Exception e) {
		if (e != null) {
			Log.e(TAG, "Exception: ", e);
		}
	}

	/**
	 * Get a authentication token if one is not available. If the error is not
	 * recoverable then it displays the error message on parent activity.
	 */
	protected abstract String fetchToken() throws IOException;
	
	/**
	 * Contacts the user info server to get the profile of the user and extracts
	 * the first name of the user from the profile. In order to authenticate
	 * with the user info server the method first fetches an access token from
	 * Google Play services.
	 * @return 
	 * @return 
	 * 
	 * @throws IOException
	 *             if communication with user info server failed.
	 * @throws JSONException
	 *             if the response from the server could not be parsed.
	 */
	private void fetchNameFromProfileServer() throws IOException, JSONException {
		String token = fetchToken();
		URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token="+ token);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		int sc = con.getResponseCode();
		if (sc == 200) {
			InputStream is = con.getInputStream();
			GOOGLE_USER_DATA = readResponse(is);
			is.close();
			
			JSONObject profileData = new JSONObject(GOOGLE_USER_DATA);

			if (profileData.has("picture"))
				imgurl = profileData.getString("picture");
			if (profileData.has("name"))
				username = profileData.getString("name");


			// User authenticated... Set flag in SharedPreference
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("SIGN_IN", 1);
			editor.putString("USER_NAME", username);
			editor.putString("USER_EMAIL", mEmail);
			editor.putString("USER_IMGURL", imgurl);
			editor.apply();
			
			UserDataHolder userDataHolder = UserDataHolder.getInstance();
			userDataHolder.setUsername(mEmail);
			userDataHolder.setImgurl(imgurl);
			
			new HttpAsyncPostTask().execute("http://newzup.in/index.php/api/signin", mEmail, imgurl);
			
			// Start MainActivity
			startNewActivity();
			
			return;
		} else if (sc == 401) {
			GoogleAuthUtil.invalidateToken(mActivity, token);
			onError("Server auth error, please try again.", null);
			// Start MainActivity
			startNewActivity();
			return;
		} else {
			onError("Server returned the following error code: " + sc, null);
			// Start MainActivity
			startNewActivity();
			return;
		}
	}
	
	private class HttpAsyncPostTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.accumulate("username", urls[1]);
				jsonObject.accumulate("imgurl", urls[2]);
				jsonObject.accumulate("provider", "google");
				jsonObject.accumulate("password", "");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return JsonUtil.POST(urls[0], jsonObject);
		}
	}
	
	protected void startNewActivity()
	{
		mActivity.finish();
	}

	/**
	 * Reads the response from the input stream and returns it as a string.
	 */
	private static String readResponse(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] data = new byte[2048];
		int len = 0;
		while ((len = is.read(data, 0, data.length)) >= 0) {
			bos.write(data, 0, len);
		}
		return new String(bos.toByteArray(), "UTF-8");
	}
}
