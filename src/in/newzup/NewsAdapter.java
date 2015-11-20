package in.newzup;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

@SuppressLint("ViewHolder")
public class NewsAdapter  extends ArrayAdapter<NewsModel> {
	private final Context context;
	private Activity parentActivity;
	private ArrayList<NewsModel> tempArrayList;
	private ImageLoader imageLoader;
	private DisplayImageOptions displayOptions;

	List<Integer> postLikes = new ArrayList<Integer>();
	List<Integer> postDislikes = new ArrayList<Integer>();

	TextView postLikeView;
	TextView postDislikeView;

	int contains_category = 0;

	int help_shown = 0;

	public NewsAdapter(Context context, Activity parentActivity, ArrayList<NewsModel> modelsArrayList) {

		super(context, R.layout.list_row, modelsArrayList);

		this.context = context;
		this.parentActivity = parentActivity;

		this.tempArrayList = modelsArrayList;
		for(int i=0; i<tempArrayList.size(); i++)
		{
			if(tempArrayList.get(i).getId() == -1)
				contains_category = 1;
		}

		displayOptions = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.build();

		ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(context)
		.threadPriority(Thread.MAX_PRIORITY)
		.memoryCache(new LruMemoryCache(4 * 1024 * 1024))
		.diskCacheSize(50 * 1024 * 1024)
		.diskCacheFileCount(100)
		.threadPoolSize(10)
		.build();

		imageLoader = ImageLoader.getInstance();
		imageLoader.init(imageLoaderConfiguration);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final NewsModel newsModel = tempArrayList.get(position);

		// 1. Create inflater 
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 2. Get rowView from inflater		
		View rowView = null;
		//rowView = inflater.inflate(R.layout.list_row, parent, false);
		rowView = inflater.inflate(R.layout.list_row, parent, false);

		// 3. Get icon,title & counter views from the rowView
		TextView titleView = (TextView) rowView.findViewById(R.id.title);
		TextView contentView = (TextView) rowView.findViewById(R.id.tags);
		TextView commentCountView = (TextView) rowView.findViewById(R.id.commentCount);
		postLikeView = (TextView) rowView.findViewById(R.id.postLike);
		postDislikeView = (TextView) rowView.findViewById(R.id.postDislike);
		TextView providerView = (TextView) rowView.findViewById(R.id.provider);

		TextView postContentView = (TextView) rowView.findViewById(R.id.news_content);
		postContentView.setText(Html.fromHtml(newsModel.getContent()));
		postContentView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(context, WebViewActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("position", position);
				intent.putExtra("url", newsModel.getLink());
				intent.putExtra("max_items", tempArrayList.size());
				intent.putExtra("urls", getUrlsFromNewsModel());
				intent.putExtra("ids", getPostIdsFromNewsModel());
				intent.putExtra("post_id", newsModel.getId());
				intent.putExtra("comment_count", newsModel.getComments());
				intent.putExtra("title", newsModel.getTitle());
				intent.putExtra("contains_category", contains_category);
				context.startActivity(intent);
			}
		});

		if(position != 0)
		{
			rowView.findViewById(R.id.contentLayout).setVisibility(View.GONE);
			rowView.findViewById(R.id.contentSeparator).setVisibility(View.GONE);
			rowView.findViewById(R.id.tagsSeparator).setVisibility(View.GONE);
			rowView.findViewById(R.id.tags).setVisibility(View.GONE);
		}
		else
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parentActivity);
			help_shown = preferences.getInt("FIRST_HELP_SHOWN", 0);
			if(help_shown != 0)
			{
				rowView.findViewById(R.id.contentLayout).setVisibility(View.GONE);
				rowView.findViewById(R.id.contentSeparator).setVisibility(View.GONE);
				rowView.findViewById(R.id.tagsSeparator).setVisibility(View.GONE);
				rowView.findViewById(R.id.tags).setVisibility(View.GONE);
			}
		}

		// 4. Set the text for textView
		makeTitleLink(newsModel.getId(), newsModel.getTitle(), titleView, postContentView, rowView.findViewById(R.id.contentLayout), rowView.findViewById(R.id.contentSeparator), rowView.findViewById(R.id.tags), rowView.findViewById(R.id.tagsSeparator));
		makeTagLinks(newsModel.getTags(), contentView);
		makeCommentLink(newsModel.getId(), newsModel.getTitle(), "      "+newsModel.getComments(), commentCountView);

		makePostLikeLink(newsModel.getId(), "    "+newsModel.getLikes(), postLikeView);
		makePostDislikeLink(newsModel.getId(), "    "+newsModel.getDislikes(), postDislikeView);

		providerView.setText(newsModel.getProvider());

		if(newsModel.getIcon().endsWith("http://newzup.in/"))
			((ImageView)rowView.findViewById(R.id.news_image)).setVisibility(View.GONE);
		else
			loadBitmap(R.id.news_image, (ImageView)rowView.findViewById(R.id.news_image), newsModel.getIcon().trim());

		// 5. return rowView
		return rowView;
	}

	public void loadBitmap(int resId, ImageView imageView, String imgURL) {
		//imageLoader.displayImage(imgURL, imageView);
		imageLoader.displayImage(imgURL, imageView, displayOptions);
	}

	@SuppressWarnings("deprecation")
	private void makeCommentLink(final int post_id, final String title, final String text, final TextView tv) {
		if (text == null || tv == null) {
			return;
		}

		final SpannableString ss = new SpannableString(text);
		int start = 0, end = text.length();

		Drawable d = context.getResources().getDrawable(R.drawable.ic_action_chat_dark); 
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
		ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
		ss.setSpan(span, 0, 5, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		start = 5;

		if (start < end){
			ss.setSpan(new MyCommentsClickableSpan(post_id, title, text), 0, end, 0);
			ss.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(ss, TextView.BufferType.SPANNABLE);
	}

	private class MyCommentsClickableSpan extends ClickableSpan {
		private final int post_id;
		final String title;
		private MyCommentsClickableSpan(final int post_id, final String title, final String text) {
			this.post_id = post_id;
			this.title = title;
		}
		@Override
		public void onClick(final View widget) {
			Intent intent = new Intent(context, CommentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("post_id", this.post_id);
			intent.putExtra("title", this.title);
			context.startActivity(intent);
		}
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false);
		}
	}

	@SuppressWarnings("deprecation")
	private void makePostLikeLink(final int post_id, final String likes, final TextView tv) {
		if (likes == null || tv == null) {
			return;
		}

		final SpannableString ss = new SpannableString(likes);
		int start = 0, end = likes.length();

		Drawable d = context.getResources().getDrawable(R.drawable.ic_action_good); 
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
		ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
		ss.setSpan(span, 0, 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		start = 4;

		if (start < end){
			ss.setSpan(new MyPostLikeClickableSpan(post_id, likes, tv), 0, end, 0);
			ss.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(ss, TextView.BufferType.SPANNABLE);
	}

	private class MyPostLikeClickableSpan extends ClickableSpan {
		private final int post_id;
		private final String likes;
		private final TextView tv;
		private MyPostLikeClickableSpan(final int post_id, final String likes, TextView tv) {
			this.post_id = post_id;
			this.likes = likes;
			this.tv = tv;
		}
		@Override
		public void onClick(final View widget) {
			if(UserDataHolder.getInstance().getUsername() == null || UserDataHolder.getInstance().getUsername() == "")
			{
				Toast.makeText(context, "You must signin to perform this activity...", Toast.LENGTH_SHORT).show();
				return;
			}

			if(!postLikes.contains(post_id) && !postDislikes.contains(post_id))
			{
				postLikes.add(post_id);
				makePostLikeLink(post_id, "    "+(Integer.parseInt(likes.trim())+1), tv);
				// call AsynTask to perform network operation on separate thread
				new HttpAsyncPostTask().execute("http://newzup.in/index.php/api/jsonpost/post/like", ""+post_id);
			}
		}
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false);
		}
	}

	@SuppressWarnings("deprecation")
	private void makePostDislikeLink(final int post_id, final String dislikes, final TextView tv) {
		if (dislikes == null || tv == null) {
			return;
		}

		final SpannableString ss = new SpannableString(dislikes);
		int start = 0, end = dislikes.length();

		Drawable d = context.getResources().getDrawable(R.drawable.ic_action_bad); 
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
		ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
		ss.setSpan(span, 0, 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		start = 4;

		if (start < end){
			ss.setSpan(new MyPostDislikeClickableSpan(post_id, dislikes, tv), 0, end, 0);
			ss.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(ss, TextView.BufferType.SPANNABLE);
	}

	private class MyPostDislikeClickableSpan extends ClickableSpan {
		private final int post_id;
		private final String dislikes;
		private final TextView tv;
		private MyPostDislikeClickableSpan(final int post_id, final String dislikes, TextView tv) {
			this.post_id = post_id;
			this.dislikes = dislikes;
			this.tv = tv;
		}
		@Override
		public void onClick(final View widget) {
			if(UserDataHolder.getInstance().getUsername() == null || UserDataHolder.getInstance().getUsername() == "")
			{
				Toast.makeText(context, "You must signin to perform this activity...", Toast.LENGTH_SHORT).show();
				return;
			}

			if(!postDislikes.contains(post_id) && !postLikes.contains(post_id))
			{
				postDislikes.add(post_id);
				makePostDislikeLink(post_id, "    "+(Integer.parseInt(dislikes.trim())+1), tv);
				// call AsynTask to perform network operation on separate thread
				new HttpAsyncPostTask().execute("http://newzup.in/index.php/api/jsonpost/post/dislike", ""+post_id);
			}
		}
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false);
		}
	}

	private class HttpAsyncPostTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.accumulate("post_id", urls[1]);
				jsonObject.accumulate("username", UserDataHolder.getInstance().getUsername());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return JsonUtil.POST(urls[0], jsonObject);
		}
		protected void onPostExecute(String result) {
		}
	}

	public TextView createTagTextView(String text){
		TextView tv = new TextView(parentActivity);
		tv.setText(text);
		return tv;
	}

	public Object convertViewToDrawable(View view) {
		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.measure(spec, spec);
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.translate(-view.getScrollX(), -view.getScrollY());
		view.draw(c);
		view.setDrawingCacheEnabled(true);
		Bitmap cacheBmp = view.getDrawingCache();
		Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
		view.destroyDrawingCache();
		return new BitmapDrawable(context.getResources(), viewBmp);
	}

	private void makeTagLinks(final String text, final TextView tv) {
		if (text == null || tv == null) {
			return;
		}

		final String[] items = text.split(", ");
		final SpannableString ss = new SpannableString(text);
		int start = 0, end;
		for (final String item : items) {
			end = start + item.length();
			if (start < end) {
				ss.setSpan(new MyClickableSpan(item), start, end, 0);
				ss.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			start += item.length() + 2;	//comma and space in the original text ;)
		}
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(ss, TextView.BufferType.SPANNABLE);

	}

	private class MyClickableSpan extends ClickableSpan {
		private final String mText;
		private MyClickableSpan(final String text) {
			mText = text.replace("#", "");
		}
		@Override
		public void onClick(final View widget) {
			Intent i = new Intent(context, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("tag", mText);
			context.startActivity(i);
		}
	}

	private void makeTitleLink(final int post_id, final String text, final TextView tv, TextView contentTV, View v, View v1, View tagView, View tagSeparator) {
		if (text == null || tv == null) {
			return;
		}

		final SpannableString ss = new SpannableString(Html.fromHtml(text));
		int start = 0, end = ss.length();//text.length();
		if (start < end){
			ss.setSpan(new TitleSpan(post_id, text, contentTV, v, v1, tagView, tagSeparator), start, end, 0);
			ss.setSpan(new ForegroundColorSpan(Color.BLACK), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(ss, TextView.BufferType.SPANNABLE);
	}

	private class TitleSpan extends ClickableSpan {
		private View v;
		private View v1;
		private View tagView;
		private View tagSeparator;
		private TitleSpan(final int post_id, final String text, TextView contentTV, View v, View v1, View tagView, View tagSeparator) {
			this.v = v;
			this.v1 = v1;
			this.tagView = tagView;
			this.tagSeparator = tagSeparator;
		}
		@Override
		public void onClick(final View widget) {
			if(v.getVisibility() == View.GONE)
			{
				v.setVisibility(View.VISIBLE);
				v1.setVisibility(View.VISIBLE);
				tagView.setVisibility(View.VISIBLE);
				tagSeparator.setVisibility(View.VISIBLE);
			}
			else
			{
				v.setVisibility(View.GONE);				
				v1.setVisibility(View.GONE);
				tagView.setVisibility(View.GONE);
				tagSeparator.setVisibility(View.GONE);
			}

		}

		public void updateDrawState(TextPaint ds) {// override updateDrawState
			ds.setUnderlineText(false); // set to false to remove underline
		}

	}

	protected String[] getUrlsFromNewsModel()
	{
		String[] urls = new String[tempArrayList.size()+1];
		int size = tempArrayList.size();
		for (int i=0; i<size; i++)
			urls[i] = tempArrayList.get(i).getLink();

		return urls;
	}

	protected int[] getPostIdsFromNewsModel()
	{
		int[] ids = new int[tempArrayList.size()+1];
		int size = tempArrayList.size();
		for (int i=0; i<size; i++)
			ids[i] = tempArrayList.get(i).getId();

		return ids;
	}

}