package in.newzup;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CommentActivity extends Activity {

	HttpAsyncGetTask getCommentsTask;
	int post_id;
	String title;
	int num_comments = 10;

	int is_signed_in = 0;

	ListView listView;
	Button btnSignIn;

	EditText etComment;
	CommentModel commentNewsModel;

	SharedPreferences preferences;
	int current_page = 0;
	Button btnLoadMore;
	CommentsAdapter adapter;
	final ArrayList<CommentModel> models = new ArrayList<CommentModel>();
	
	private static final int RESULT_SETTINGS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		overridePendingTransition(R.anim.activity_open,R.anim.activity_close_scale);

		// Get post id of whose comments needs to be fetched.
		Intent i = getIntent();
		this.post_id = i.getIntExtra("post_id", 1);
		this.title = i.getStringExtra("title");

		setupActionBar();

		TextView innerTitleTV = (TextView)findViewById(R.id.title);
		innerTitleTV.setText(this.title);
		
		etComment = (EditText)findViewById(R.id.commentText);

		// call AsynTask to perform network operation on separate thread
		getCommentsTask = new HttpAsyncGetTask();
		getCommentsTask.execute("http://www.newzup.in/index.php/api/comments/");
		//getCommentsTask.execute("http://www.newzup.in/index.php/api/comments/" + this.post_id + "/offset/" + this.current_page);
	}

	@SuppressLint("InflateParams")
	private void setupActionBar() {
		ActionBar ab = getActionBar();
		ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#253939")));
		ab.setDisplayShowCustomEnabled(true);
		ab.setDisplayShowTitleEnabled(false);

		ab.setIcon(R.drawable.ic_launcher);

		LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.comment_view_action_bar, null);

		TextView titleTV = (TextView) v.findViewById(R.id.comment_action_bar_title);
		titleTV.setText(this.title);

		ab.setCustomView(v);
		ab.setDisplayHomeAsUpEnabled(true);
	}

	protected void initCommentsList()
	{
		findViewById(R.id.InnerRelativeLayout).setVisibility(View.GONE);
		
		btnSignIn = new Button(getApplicationContext());
		btnSignIn.setText("Sign In to post comment !");        	 
		((ListView)findViewById(R.id.comments_list)).addHeaderView(btnSignIn);

		btnSignIn.setOnClickListener(new View.OnClickListener() {        	 
			@Override
			public void onClick(View arg0) {
				startSignInActivity();
			}
		});
	}

	protected void startSignInActivity()
	{
		Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivityForResult(intent, RESULT_SETTINGS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			Intent intent = getIntent();
			finish();
			startActivity(intent);
			break;
		}
	}

	private class HttpAsyncGetTask extends AsyncTask<String, Void, String> {
		private ProgressDialog dialog = new ProgressDialog(CommentActivity.this);

		@Override
		protected void onPreExecute()
		{
			this.dialog.setTitle("Loading !!!");
			this.dialog.setMessage("Please wait...");
			this.dialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			if (isCancelled())  return null;
			
			JSONObject jsonObject = new JSONObject();
            try {
				jsonObject.accumulate("post_id", "" + post_id);
				jsonObject.accumulate("current_page", "" + current_page);
				jsonObject.accumulate("num_comments", "" + num_comments);
			} catch (JSONException e) {
				e.printStackTrace();
			}

            return JsonUtil.POST(urls[0], jsonObject);
		}

		@Override
		protected void onPostExecute(String result) {
			JSONArray array = null;

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			try {
				int id;
				String content;
				String username;
				String imgurl;
				int likes, dislikes;
				array = new JSONArray(result);
				for (int i = 0; i <  array.length(); i++) {
					JSONObject row = array.getJSONObject(i);
					id = Integer.parseInt(row.getString("id"));
					content = row.getString("content");
					username = row.getString("username");
					imgurl = row.getString("imgurl");
					likes = Integer.parseInt(row.getString("likes"));
					dislikes = Integer.parseInt(row.getString("dislikes"));

					models.add(0, new CommentModel(id, content, username, imgurl, likes, dislikes));
				}

			} catch (JSONException e) {
				models.add(new CommentModel(-1, "Be the first one to comment", "NewzUp", "", 0, 0));
				e.printStackTrace();
			}
			finally {
				is_signed_in = preferences.getInt("SIGN_IN", 0);
				if(is_signed_in == 0)
					initCommentsList();

				// 1. pass context and data to the custom adapter        			
				adapter = new CommentsAdapter(getApplicationContext(), models);

				// 2. Get ListView from activity_main.xml
				ListView listView = (ListView) findViewById(R.id.comments_list);

				// LoadMore button
				if(btnLoadMore == null)
				{
					btnLoadMore = new Button(getApplicationContext());
					btnLoadMore.setText("Load more...");        	 
					listView.addHeaderView(btnLoadMore);
				}

				btnLoadMore.setOnClickListener(new View.OnClickListener() {        	 
					@Override
					public void onClick(View arg0) {
						loadMore();
					}
				});

				if(array == null || array.length() < 10)
					btnLoadMore.setVisibility(View.GONE);
				else
					btnLoadMore.setVisibility(View.VISIBLE);

				// 3. setListAdapter
				listView.setAdapter(adapter);

				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, final View view,
							int position, long id) {        	            	
						return;
					}
				});
			}
		}
	}

	public void loadMore()
	{
		if(getCommentsTask != null && getCommentsTask.getStatus() == AsyncTask.Status.RUNNING)
			getCommentsTask.cancel(true);
		getCommentsTask = new HttpAsyncGetTask();

		this.current_page++;
		getCommentsTask.execute("http://www.newzup.in/index.php/api/comments/" + this.post_id + "/offset/" + this.current_page);
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.send_button:
			if(!validate())
				Toast.makeText(getBaseContext(), "Enter valid data!", Toast.LENGTH_LONG).show();
			else
			{
				new HttpAsyncPostTask().execute("http://newzup.in/index.php/api/jsonpost/comment", etComment.getText().toString(), ""+this.post_id);

				// Add comment in current window
				models.add(new CommentModel(0, etComment.getText().toString(), UserDataHolder.getInstance().getUsername(), UserDataHolder.getInstance().getImgurl(), 0, 0));
				adapter.notifyDataSetChanged();

				// Hide keyboard
				etComment.setText("");
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
			}
			break;
		}
		//Toast.makeText(getApplicationContext(), "Sending...", Toast.LENGTH_SHORT).show();
	}

	private class HttpAsyncPostTask extends AsyncTask<String, Void, String> {
		private ProgressDialog dialog = new ProgressDialog(CommentActivity.this);
		@Override
		protected void onPreExecute()
		{
			this.dialog.setTitle("Sending !!!");
			this.dialog.setMessage("Please wait...");
			this.dialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {

			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.accumulate("comment", urls[1]);
				jsonObject.accumulate("post_id", urls[2]);
				jsonObject.accumulate("username", UserDataHolder.getInstance().getUsername());
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return JsonUtil.POST(urls[0], jsonObject);
		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	}

	private boolean validate(){
		if(etComment.getText().toString().trim().equals(""))
			return false;
		else
			return true;    
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}