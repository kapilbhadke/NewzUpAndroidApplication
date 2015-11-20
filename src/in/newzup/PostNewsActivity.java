package in.newzup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PostNewsActivity extends Activity implements OnClickListener {

    TextView tvIsConnected;
    EditText etUrl, etTags;
    Spinner etCategory;
    Button btnPost;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        overridePendingTransition(R.anim.activity_open,R.anim.activity_close_scale);
        
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#253939")));
        actionBar.setDisplayHomeAsUpEnabled(true);

        // get reference to the views
        etUrl = (EditText) findViewById(R.id.etUrl);
        etCategory = (Spinner) findViewById(R.id.etCategory);
        etTags = (EditText) findViewById(R.id.etTags);
        btnPost = (Button) findViewById(R.id.btnPost);

        // add click listener to Button "POST"
        btnPost.setOnClickListener(this);
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
    
    @Override
    protected void onPause()
    {
      super.onPause();
      //closing transition animations
      overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close);
    }
    
    private class HttpAsyncPostTask extends AsyncTask<String, Void, String> {
    	private ProgressDialog dialog = new ProgressDialog(PostNewsActivity.this);
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(PostNewsActivity.this);
    	
    	int category, position;
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
            	// 1India-1, 2Economy-2, 3Politics-3,	4world-4, 5business-5, 6sports-6,	7Sc&T-7, 8miscellaneous-8
            	position = etCategory.getSelectedItemPosition();
        		category = position + 1;
				jsonObject.accumulate("link", etUrl.getText().toString());
				jsonObject.accumulate("cat", ""+category);
	            jsonObject.accumulate("tags", etTags.getText().toString());
	            jsonObject.accumulate("username", UserDataHolder.getInstance().getUsername());
			} catch (JSONException e) {
				e.printStackTrace();
			}

            return JsonUtil.POST(urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	try {
				JSONObject row = new JSONObject(result);
				String error = row.getString("error");
				if("0".equals(error))
					finish();
				else
				{
					dialog.dismiss();
					alertDialog.setMessage("This article has been posted already.");
					alertDialog.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							finish();
						}
					});
					alertDialog.show();
				}
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