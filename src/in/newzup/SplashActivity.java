package in.newzup;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class SplashActivity extends Activity {
	/** Duration of wait **/
	private final int SPLASH_DISPLAY_LENGTH = 1000;

	/** Called when the activity is first created. */
	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.splash);

		TextView titleTV = (TextView) findViewById(R.id.splash_title);
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/wildarrows.ttf");
		titleTV.setTypeface(font);
		titleTV.setText("Newz Up !");
		
		final ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#253939")));

		/* New Handler to start the MainActivity 
		 * and close this Splash-Screen after some seconds.*/

		if(isConnected())
		{
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					/* Create an Intent that will start the Menu-Activity. */
					Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
					SplashActivity.this.startActivity(mainIntent);
					SplashActivity.this.finish();
				}
			}, SPLASH_DISPLAY_LENGTH);
		}
		else
		{
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			alertDialog.setTitle("Alert !!!");
			alertDialog.setMessage("No internet connection.\nPlease enable internet connection and try again.");
			alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int which) {
					finish();
				}
			});
			alertDialog.show();
		}
	}

	public boolean isConnected(){
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) 
			return true;
		else
			return false;	
	}
}