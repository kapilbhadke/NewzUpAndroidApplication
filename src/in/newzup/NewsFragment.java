package in.newzup;

import in.newzup.showcase.ShowcaseManager;
import in.newzup.showcaseview.OnShowcaseEventListener;
import in.newzup.showcaseview.ShowcaseView;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class NewsFragment extends Fragment  implements OnShowcaseEventListener {

	private static final String API_URL_PREFIX = "http://www.newzup.in/index.php/api/";
	private static final int DEFAULT_NUM_POSTS = 10;
	private static final String DEFAULT_CATEGORIES = "1 2 3";
	int num_posts = 10;
	int current_page = 0;
	int current_category = 0;	
	String fetch_categories = "";
	String current_url = "";
	String content_url = "";
	String current_tag;
	String intent_category;

	Button btnLoadMore;

	int help_shown = 0;
	ShowcaseView sv;

	final ArrayList<NewsModel> models = new ArrayList<NewsModel>();
	NewsAdapter adapter;
	ListView listView;
	HttpAsyncTask getNewsTask;

	public NewsFragment(int category)
	{
		this.current_category = category;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Retain this fragment across configuration changes.
		setRetainInstance(true);

		current_tag = getActivity().getIntent().getStringExtra("tag");
		if(current_tag != null)
			current_page = 0;

		// Fetch News data from server
		refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_news, container, false);
		return rootView;
	}

	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		initNewsList();
	}

	protected void initNewsList() {
		// 1. pass context and data to the custom adapter        			
		adapter = new NewsAdapter(getActivity(), getActivity(), models);
		// 2. Get ListView from activity_main.xml
		listView = new ListView(getActivity());
		listView = (ListView) getView().findViewById(R.id.news_list);

		listView.setTextFilterEnabled(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long id) {

				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "@NewzUp\n" + models.get(position).getTitle() + "\n" + models.get(position).getLink());
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
				return true;
			}
		});

		// LoadMore button
		btnLoadMore = (Button) getView().findViewById(R.id.btnLoadMore);
		btnLoadMore.setOnClickListener(new View.OnClickListener() {        	 
			@Override
			public void onClick(View arg0) {
				loadMore();
			}
		});

		// 3. setListAdapter
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	public String getFetchUrl(int current_category, int current_page)
	{
		if(current_tag != null)
			current_url = API_URL_PREFIX + "posts/tag/" + current_tag + "/" + current_page;
		else
			current_url = API_URL_PREFIX + "posts/cat/" + current_category + "/" + current_page;

		current_url = API_URL_PREFIX + "posts";
		return current_url;
	}

	public void loadMore()
	{
		if(getNewsTask != null && getNewsTask.getStatus() == AsyncTask.Status.RUNNING)
			getNewsTask.cancel(true);
		getNewsTask = new HttpAsyncTask();

		current_page++;
		getNewsTask.execute(getFetchUrl(current_category, current_page));
	}

	public void refresh()
	{
		// call AsynTask to perform network operation on separate thread
		if(getNewsTask != null && getNewsTask.getStatus() == AsyncTask.Status.RUNNING)
			getNewsTask.cancel(true);
		getNewsTask = new HttpAsyncTask();
		models.clear();
		getNewsTask.execute(getFetchUrl(current_category, 0));
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute()
		{
			this.dialog.setTitle("Loading !!!");
			this.dialog.setMessage("Please wait...");
			this.dialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			if (isCancelled())
			{
				this.dialog.dismiss();
				return null;
			}

			JSONObject jsonObject = new JSONObject();
			try {
				if(current_tag != null)
					jsonObject.accumulate("tag", current_tag);
				if(current_category != 0) {	//Fetch all categories
					fetch_categories = "" + current_category;
					num_posts = DEFAULT_NUM_POSTS;
				}
				else {
					SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
					fetch_categories = sharedPrefs.getString("PREF_CATEGORIES", DEFAULT_CATEGORIES);
				}
				jsonObject.accumulate("current_page", "" + current_page);
				jsonObject.accumulate("num_posts", "" + num_posts);
				jsonObject.accumulate("category", fetch_categories);
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

			Log.e("JSON REPLY", result);
			
			if(adapter == null)
				initNewsList();

			try {
				String title;
				String imgurl;
				String link;
				String tags;
				String category;
				String provider = "";
				String content;
				int comments;
				int likes, dislikes;
				int id;
				JSONArray array = new JSONArray(result);
				for (int i = 0; i < array.length(); i++) {
					JSONObject row = array.getJSONObject(i);

					id = Integer.parseInt(row.getString("id"));
					imgurl = row.getString("imgurl");
					tags = row.getString("tags");
					category = row.getString("category");
					comments = Integer.parseInt(row.getString("comment_count"));
					likes = Integer.parseInt(row.getString("likes"));
					dislikes = Integer.parseInt(row.getString("dislikes"));
					link = row.getString("link");
					content = row.getString("content").trim();
					String temp = row.getString("title").trim();
					if(temp.lastIndexOf("-") != -1)
					{
						provider = temp.substring(temp.lastIndexOf("-")+2, temp.length());
						if(provider.lastIndexOf("|") != -1)
						{
							provider = temp.substring(temp.lastIndexOf("|")+2, temp.length());
							title = temp.substring(0, temp.lastIndexOf("|"));
						}
						else
							title = temp.substring(0, temp.lastIndexOf("-"));

						if(provider.split(" ").length >5)
						{
							provider = "";
							title = temp;
						}
					}						
					else if(temp.lastIndexOf("|") != -1)
					{
						title = temp.substring(0, temp.lastIndexOf("|"));
						provider = temp.substring(temp.lastIndexOf("|")+2, temp.length());

						if(provider.split(" ").length >5)
						{
							provider = "";
							title = temp;
						}
					}
					else
					{
						title = temp;
						provider = "";
					}

					models.add(new NewsModel(id, imgurl, title, tags, category, link, comments, likes, dislikes, provider, content));
					if(adapter != null)
						adapter.notifyDataSetChanged();
				}

				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				help_shown = preferences.getInt("HELP_SHOWN", 0);
				if(help_shown == 0)
				{
					final Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							listView.getChildAt(0).findViewById(R.id.news_content).setVisibility(View.VISIBLE);
							listView.getChildAt(0).findViewById(R.id.tags).setVisibility(View.VISIBLE);
							new ShowcaseManager(getActivity()).showcaseMainActivity();
						}
					}, 1000);
					
					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt("HELP_SHOWN", 1);
					editor.apply();
				}

			} catch (JSONException e) {
				e.printStackTrace();
				if(result.contains("No items where found for model"))
				{
					Toast.makeText(getActivity(), "No more items...", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {	}

	@Override
	public void onShowcaseViewDidHide(ShowcaseView showcaseView) {	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {	}
}