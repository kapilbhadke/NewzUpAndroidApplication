package in.newzup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class HandleSendEventActivity extends Activity implements
ActionBar.OnNavigationListener, OnClickListener  {

	TextView tvIsConnected;
	EditText etUrl, etTags;
	Spinner etCategory;
	Button btnPost;

	int is_signed_in = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_external);

		etUrl = (EditText) findViewById(R.id.etUrl);
		etCategory = (Spinner) findViewById(R.id.etCategory);
		etTags = (EditText) findViewById(R.id.etTags);
		btnPost = (Button) findViewById(R.id.btnPost);

		// add click listener to Button "POST"
		btnPost.setOnClickListener(this);

		initSignin();

		handleSendEvent();
	}

	protected void initSignin()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		is_signed_in = preferences.getInt("SIGN_IN", 0);
		String username = preferences.getString("USER_EMAIL", "");

		if(is_signed_in == 1)
		{
			UserDataHolder userDataHolder = UserDataHolder.getInstance();
			userDataHolder.setUsername(username);
		}
		else
		{
			Toast.makeText(getBaseContext(), "You must SignUp with NewzUp to post News.", Toast.LENGTH_LONG).show();
			Handler handler = new Handler(); 
			handler.postDelayed(new Runnable() { 
				public void run() { 
					finish(); 
				} 
			}, 3000);
		}
	}

	@Override
	public void onClick(View view) {

		switch(view.getId()){
		case R.id.btnPost:
			if(!validate())
				Toast.makeText(getBaseContext(), "Enter valid data!", Toast.LENGTH_LONG).show();
			else
				// call AsynTask to perform network operation on separate thread
				new HttpAsyncPostTask().execute("http://newzup.in/index.php/api/jsonpost/news");
			break;
		}
	}

	private class HttpAsyncPostTask extends AsyncTask<String, Void, String> {
		int category;
		int position;
		@Override
		protected String doInBackground(String... urls) {

			JSONObject jsonObject = new JSONObject();
			try {
				// 1India-1, 2Economy-2, 3Politics-3,	4world-4, 5business-5, 6Sc&T-6,	7sports-7, 8miscellaneous-8
				position = etCategory.getSelectedItemPosition();
				category = position + 1;	//position starts from 0
				jsonObject.accumulate("link", etUrl.getText().toString());
				jsonObject.accumulate("cat", ""+category);
				jsonObject.accumulate("tags", etTags.getText().toString());
				jsonObject.accumulate("username", UserDataHolder.getInstance().getUsername());
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return JsonUtil.POST(urls[0], jsonObject);
		}
		@Override
		protected void onPostExecute(String result) {
			try {
				JSONObject row = new JSONObject(result);
				String error = row.getString("error");
				if("0".equals(error))
					finish();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean validate(){
		if(etUrl.getText().toString().trim().equals(""))
			return false;
		else
		{
			String url = extractUrlFromText(etUrl.getText().toString().trim());
			if(url != "")
				return true;
			else
				return false;
		}
	}

	protected void handleSendEvent()
	{
		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {

				// Handle text being sent
				String url = extractUrlFromText(intent.getStringExtra(Intent.EXTRA_TEXT));
				if(url == null || url == "")
					finish();

				etUrl.setText(url);

			} else if (type.startsWith("image/")) {
				; // Handle single image being sent
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				; // Handle multiple images being sent
			}
		} else {
			// Handle other intents, such as being started from the home screen
		}
	}

	protected String extractUrlFromText(String value)
	{
		String result = "";
		String urlPattern = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(value);
		if(m.find()) {
			result = value.substring(m.start(0),m.end(0));
		}
		return result;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		return false;
	}
}
