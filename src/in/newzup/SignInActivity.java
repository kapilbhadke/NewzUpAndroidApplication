package in.newzup;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;

public class SignInActivity extends Activity {
	Context mContext = SignInActivity.this;
	AccountManager mAccountManager;
	String token;
	int serverCode;
	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	private static final String SIGN_IN_URL = "http://www.newzup.in/index.php/api/signin";

	private String username = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_dialog);
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#253939")));

		overridePendingTransition(R.anim.activity_open,R.anim.activity_close_scale);
	}

	private String[] getAccountNames() {
		mAccountManager = AccountManager.get(this);
		Account[] accounts = mAccountManager
				.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		String[] names = new String[accounts.length];
		for (int i = 0; i < accounts.length; i++) {
			names[i] = accounts[i].name;
		}
		return names;
	}

	private AbstractGetNameTask getTask(SignInActivity activity, String email, String scope) {

		return new GetNameInForeground(activity, email, scope);
	}

	public void syncGoogleAccount(View v) {
		if (isNetworkAvailable() == true) {

			String[] accountarrs = getAccountNames();
			if (accountarrs.length > 0) {

				SignInGoogle(accountarrs);
				return;

			} else {
				Toast.makeText(SignInActivity.this, "No Google Account found for signing in !!", Toast.LENGTH_LONG).show();
				finish();
			}
		} else {
			Toast.makeText(SignInActivity.this, "No Network Service!", Toast.LENGTH_LONG).show();
		}
	}

	public boolean isNetworkAvailable() {

		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public void SignInGoogle(final String[] usernames)
	{
		username = usernames[0];
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignInActivity.this);

		alertDialog.setTitle("Sign In with");

		alertDialog.setSingleChoiceItems(usernames, 0, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				username = usernames[which];
			}
		});

		alertDialog.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				ProgressDialog dialog1 = new ProgressDialog(SignInActivity.this);
				dialog1.setTitle("Signing in !!!");
				dialog1.setMessage(" Please wait...");
				dialog1.show();

				getTask(SignInActivity.this, username, SCOPE).execute();
			}
		});

		// Setting Negative "NO" Button
		alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && 
						event.getAction() == KeyEvent.ACTION_UP && 
						!event.isCanceled()) {
					dialog.cancel();
					return true;
				}
				return false;
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	public void SignInNewzup(View v)
	{
		final EditText txtUsername = (EditText)findViewById(R.id.txtUsername);
		final EditText txtPassword = (EditText)findViewById(R.id.txtPassword);

		((TextView)findViewById(R.id.invalidUsername)).setVisibility(View.GONE);
		((TextView)findViewById(R.id.invalidPassword)).setVisibility(View.GONE);

		String username = txtUsername.getText().toString().trim();
		String password = txtPassword.getText().toString().trim();
		if(username.length() > 0 && password.length() > 0)
		{
			// Validate Your login credential here than display message
			new HttpAsyncPostTask().execute(SIGN_IN_URL);
		}
		else
			Toast.makeText(SignInActivity.this,	"Please enter Username and Password.", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close);
	}

	public void startNextActivity()
	{
		return;
	}

	public void finishActivity(View v)
	{
		startNextActivity();
		finish();
	}

	private class HttpAsyncPostTask extends AsyncTask<String, Void, String> {
		private ProgressDialog dialog = new ProgressDialog(SignInActivity.this);
		@Override
		protected void onPreExecute()
		{
			this.dialog.setTitle("Signing in !!!");
			this.dialog.setMessage("Please wait...");
			this.dialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {

			JSONObject jsonObject = new JSONObject();
			try {
				final EditText txtUsername = (EditText)findViewById(R.id.txtUsername);
				final EditText txtPassword = (EditText)findViewById(R.id.txtPassword);
				username = txtUsername.getText().toString().trim();
				String password = txtPassword.getText().toString().trim();
				jsonObject.accumulate("username", username);
				jsonObject.accumulate("password", password);
				jsonObject.accumulate("provider", "newzup");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return JsonUtil.POST(urls[0], jsonObject);
		}
		protected void onPostExecute(String result) {
			try {
				JSONObject row = new JSONObject(result);
				String error = row.getString("error");
				if("ERROR_NONE".equals(error))
				{
					// User authenticated... Set flag in SharedPreference
					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt("SIGN_IN", 1);
					editor.putString("USER_NAME", username);
					editor.putString("USER_EMAIL", username);
					editor.putString("USER_IMGURL", "");
					editor.apply();

					UserDataHolder userDataHolder = UserDataHolder.getInstance();
					userDataHolder.setUsername(username);
					userDataHolder.setImgurl("");
					dialog.dismiss();
					startNextActivity();
					finish();
				}
				else if("ERROR_USERNAME_INVALID".equals(error))
					((TextView)findViewById(R.id.invalidUsername)).setVisibility(View.VISIBLE);
				else if("ERROR_PASSWORD_INVALID".equals(error))
					((TextView)findViewById(R.id.invalidPassword)).setVisibility(View.VISIBLE);
				dialog.dismiss();
			} catch (Exception e) {
				Log.d("InputStream", "SignInActivity: "+e.getLocalizedMessage());
			}
		}
	}
}