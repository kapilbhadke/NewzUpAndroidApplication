package in.newzup.showcase;

import in.newzup.R;
import android.app.Activity;
import android.view.View;
import android.widget.ListView;

// Encapsulate all the ShowCase behavior so we can change it all in one place
public class ShowcaseManager {

    private final Activity activity;
    private final ListView listView;

    public ShowcaseManager(Activity activity) {
        this.activity = activity;
		this.listView = (ListView) activity.findViewById(R.id.news_list);
    }

    // Example of how to do consecutive showcases
    public void showcaseMainActivity() {
    	
    	listView.getChildAt(0).findViewById(R.id.news_content).setVisibility(View.VISIBLE);
    	listView.getChildAt(0).findViewById(R.id.tags).setVisibility(View.VISIBLE);
    	
        ShowcaseViews views = new ShowcaseViews(activity, R.layout.view_showcase);
        views.addView(new ShowcaseViews.ViewProperties(R.id.action_new, R.string.help_post_news));
        views.addView(listView.getChildAt(0).findViewById(R.id.title), R.string.help_expand_title);
        views.addView(listView.getChildAt(0).findViewById(R.id.news_content), R.string.help_expand_news);
        views.addView(listView.getChildAt(0).findViewById(R.id.tags), R.string.help_tags);
      //views.addView(new ShowcaseViews.ViewProperties(R.id.pager, R.string.help_swipe_categories));
        views.show();
    }

}
