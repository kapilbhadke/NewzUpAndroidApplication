package in.newzup;

import in.newzup.SimpleGestureFilter.SimpleGestureListener;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewActivity extends Activity  implements SimpleGestureListener{

	private SimpleGestureFilter gestureDetector;

	private WebView webView;
	private int post_id;
	private String url;
	private String title;
	private String urls[];
	private int ids[];
	private int position;
	private int max_items;
	private int comment_count;
	
	static final String FILE_PATH_PREFIX = Environment.getExternalStorageDirectory().getPath() + "/newzup/news/";
	
	int animationDirection = -1;	// -1 = right to left, 1 = left to right

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		
		Intent i = getIntent();
        animationDirection = i.getIntExtra("animationDirection", -1);
        if(animationDirection == 1)  // -1 = Right to left, 1 = left to right      
        	overridePendingTransition(R.anim.activity_open,R.anim.activity_close_scale);
        else
        	overridePendingTransition(R.anim.activity_open_from_left,R.anim.activity_close_scale);

		showSwipeHelp();

		float density = getResources().getDisplayMetrics().density;
		gestureDetector = new SimpleGestureFilter(this, this, density);

		this.url = i.getStringExtra("url");
		this.post_id = i.getIntExtra("post_id", 1);
		this.position = i.getIntExtra("position", 1);
		this.max_items = i.getIntExtra("max_items", 1);
		this.urls = new String[this.max_items];
		this.urls = i.getStringArrayExtra("urls");
		this.ids = new int[this.max_items];
		this.ids = i.getIntArrayExtra("ids");
		this.comment_count = i.getIntExtra("comment_count", 0);
		this.title = i.getStringExtra("title");

		setupActionBar();

		final ProgressDialog dialog = new ProgressDialog(WebViewActivity.this);

		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);
		
		// Settings for faster loading
		webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		webView.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE );

		webView.setWebViewClient(new WebViewClient(){

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				dialog.setTitle("Loading !!!");
				dialog.setMessage("Please wait...");
				dialog.show();
			}

			public void onPageFinished(WebView view, String url) {
				dialog.dismiss();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String lurl){
				return false;
			}
		});

		webView.loadUrl(this.url);
	}
	
	@Override
    protected void onPause()
    {
      super.onPause();
      overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close);
    }

	public void onClick(View v) {
		Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// parameters
		intent.putExtra("post_id", this.post_id);
		intent.putExtra("title", this.title);
		startActivity(intent);
	}
	
	public void onShare(View v) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl() + "\nvia @NewzUp");
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me){
		// Call onTouchEvent of SimpleGestureFilter class
		this.gestureDetector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}

	@Override
	public void onSwipe(int direction)
	{
		switch (direction) {
		case SimpleGestureFilter.SWIPE_RIGHT : loadNextNews(this.position, this.max_items, -1); break;
		case SimpleGestureFilter.SWIPE_LEFT :  loadNextNews(this.position, this.max_items, 1);  break;
		}
	}

	public void loadNextNews(int position, int max_items, int direction)
	{
		int new_position = position + direction;

		if(new_position >= max_items || new_position < 0)
			return;

		Intent i = getIntent();
		finish();
		i.putExtra("post_id", (int)this.ids[new_position]);
		i.putExtra("position", new_position);
		i.putExtra("url", this.urls[new_position]);
		i.putExtra("max_items", this.max_items);
		i.putExtra("urls", this.urls);
		i.putExtra("animationDirection", direction);
		startActivity(i);
		return;
	}

	@SuppressLint("InflateParams")
	private void setupActionBar() {
		ActionBar ab = getActionBar();
		ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#253939")));
		ab.setDisplayShowCustomEnabled(true);
		ab.setDisplayShowTitleEnabled(false);

		LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.web_view_action_bar, null);

		TextView position = (TextView) v.findViewById(R.id.action_bar_position);
		position.setText("["+(this.position+1)+"/"+(this.max_items)+"]");

		TextView comments_cnt = (TextView) v.findViewById(R.id.action_bar_comments);
		comments_cnt.setText(" "+comment_count);

		ab.setCustomView(v);
		ab.setDisplayHomeAsUpEnabled(true);
	}

	@SuppressLint("InflateParams")
	public void showSwipeHelp()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		int help_shown = preferences.getInt("SHOW_HELP", 0);
		if(help_shown != 0)
			return;
		else
		{
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("SHOW_HELP", 1);
			editor.apply();
		}

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(WebViewActivity.this);
		alertDialog.setTitle("Swipe left or right to read next News");

		LayoutInflater factory = LayoutInflater.from(WebViewActivity.this);
		final View view = factory.inflate(R.layout.swipe_help, null);
		alertDialog.setView(view);
		alertDialog.setNeutralButton("Got it!", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int sumthin) {
			}
		});
		alertDialog.show();
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

	@Override
	public void onDoubleTap() {
	}
}