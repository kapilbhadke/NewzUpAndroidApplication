package in.newzup;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	private final String problem = "<br><br>" +
			"<b>What is NewzUp?</b><br>" +
			"<p>NewzUp is a social driven platform which collects unbiased, neutral, fair and honest face of news " +
			"and lets people discuss and decide what they want to read. NewzUp offers news from every spectrum of life " +
			"including those who don't even see a light in main stream media.</p><br>" +
			"<b>Why is it becoming important now?</b><br>" +
			"<p>News, in these days are biased, intentionally or unintentionally and so is the absorption. " +
			"The boundary between journalists and readers is getting blurred. Journalists shows only half side of the coin, " +
			"mostly biased, which unknowingly gets absorbed by the reader. In order to get unbiased news, " +
			"a broad input must be collected from different sources. </p>" +
			"<p>The Internet is flooded with poor quality news. Almost nobody has the time to filter out the highest quality " +
			"news articles and columns. Selection and presentation of News shapes ideas in society. People absorb what is " +
			"fed to them and not what they actually want to read. </p><br>" +
			"<b>How NewzUp solves these problems?</b><br>" +
			"<p>NewzUp filters news articles based on Quality of writing, accuracy and truth, evaluation and analysis, fairness and balance. NewzUp provides a central location where you can get the highest quality news which are Important and Useful and thus it saves users valuable time. </p><br>" +
			"<b>How can people use it?</b><br>" +
			"<p>Connect with us, read and spread the news, know and let others know the world around you.</p><br>";

	private final String solution = "";
	private final String vision = "<p><b>Vision</b>: Provide unbiased news for every citizen.</p>";
	private final String mission = "<p><b>Mission</b>: Connect world\'s best news articles to make them reach everyone.</p>";
	private final String intro = "";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_open, R.anim.activity_close_scale);
        setContentView(R.layout.about);
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#253939")));
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        ((TextView)findViewById(R.id.ProblemTV)).setText(Html.fromHtml(problem));
        ((TextView)findViewById(R.id.IntroTV)).setText(Html.fromHtml(solution));
        ((TextView)findViewById(R.id.VisionTV)).setText(Html.fromHtml(vision));
        ((TextView)findViewById(R.id.MissionTV)).setText(Html.fromHtml(mission));
        ((TextView)findViewById(R.id.DescrTV)).setText(Html.fromHtml(intro));
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