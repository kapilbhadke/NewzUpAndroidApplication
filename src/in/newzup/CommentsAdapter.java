package in.newzup;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class CommentsAdapter  extends ArrayAdapter<CommentModel>{
	private final Context context;
	private final ArrayList<CommentModel> modelsArrayList;

	private ImageLoader imageLoader;
	private DisplayImageOptions displayOptions;

	TextView commentLikesView;
	TextView commentDislikesView; 

	List<Integer> commentLikes = new ArrayList<Integer>();
	List<Integer> commentDislikes = new ArrayList<Integer>();

	@SuppressWarnings("unused")
	private DisplayImageOptions options;

	public CommentsAdapter(Context context, ArrayList<CommentModel> modelsArrayList) {

		super(context, R.layout.comment_row, modelsArrayList);

		this.context = context;
		this.modelsArrayList = modelsArrayList;

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

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		CommentModel commentNewsModel = modelsArrayList.get(position);

		// 1. Create inflater 
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 2. Get rowView from inflater		
		View rowView = null;
		rowView = inflater.inflate(R.layout.comment_row, parent, false);

		// 3. Get icon,title & counter views from the rowView
		TextView titleView = (TextView) rowView.findViewById(R.id.user);
		TextView contentView = (TextView) rowView.findViewById(R.id.comment);
		commentLikesView = (TextView) rowView.findViewById(R.id.commentLike);
		commentDislikesView = (TextView) rowView.findViewById(R.id.commentDislike);

		// 4. Set the text for textView
		titleView.setText(commentNewsModel.getUsername());
		contentView.setText(commentNewsModel.getContent());

		if(commentNewsModel.getId() != -1)
		{
			makeCommentLikeLink(commentNewsModel.getId(), "    "+commentNewsModel.getLikes(), commentLikesView);
			makeCommentDislikeLink(commentNewsModel.getId(), "    "+commentNewsModel.getDislikes(), commentDislikesView);
			loadBitmap(R.id.user_image, (ImageView)rowView.findViewById(R.id.user_image), commentNewsModel.getImgurl());
		}

		// 5. retrn rowView
		return rowView;
	}

	public void loadBitmap(int resId, ImageView imageView, String imgURL) {
		imageLoader.displayImage(imgURL, imageView, displayOptions);
	}

	@SuppressWarnings("deprecation")
	private void makeCommentLikeLink(final int comment_id, final String likes, final TextView tv) {
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
			ss.setSpan(new MyCommentLikeClickableSpan(comment_id, likes, tv), 0, end, 0);
			ss.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(ss, TextView.BufferType.SPANNABLE);
	}

	private class MyCommentLikeClickableSpan extends ClickableSpan {
		private final int comment_id;
		private final String likes;
		private final TextView tv;
		private MyCommentLikeClickableSpan(final int comment_id, final String likes, TextView tv) {
			this.comment_id = comment_id;
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

			if(!commentLikes.contains(comment_id) && !commentDislikes.contains(comment_id))
			{
				commentLikes.add(comment_id);
				makeCommentLikeLink(comment_id, "    "+(Integer.parseInt(likes.trim())+1), tv);
				// call AsynTask to perform network operation on separate thread
				new HttpAsyncPostTask().execute("http://newzup.in/index.php/api/jsonpost/comment/like", ""+comment_id);
			}
		}
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false);
		}
	}

	@SuppressWarnings("deprecation")
	private void makeCommentDislikeLink(final int comment_id, final String dislikes, final TextView tv) {
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
			ss.setSpan(new MyCommentDislikeClickableSpan(comment_id, dislikes, tv), 0, end, 0);
			ss.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(ss, TextView.BufferType.SPANNABLE);
	}

	private class MyCommentDislikeClickableSpan extends ClickableSpan {
		private final int comment_id;
		private final String dislikes;
		private final TextView tv;
		private MyCommentDislikeClickableSpan(final int comment_id, final String dislikes, TextView tv) {
			this.comment_id = comment_id;
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

			if(!commentDislikes.contains(comment_id) && !commentLikes.contains(comment_id))
			{
				commentDislikes.add(comment_id);
				makeCommentDislikeLink(comment_id, "    "+(Integer.parseInt(dislikes.trim())+1), tv);
				// call AsynTask to perform network operation on separate thread
				new HttpAsyncPostTask().execute("http://newzup.in/index.php/api/jsonpost/comment/dislike", ""+comment_id);
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
				jsonObject.accumulate("comment_id", urls[1]);
				jsonObject.accumulate("username", UserDataHolder.getInstance().getUsername());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return JsonUtil.POST(urls[0], jsonObject);
		}
		protected void onPostExecute(String result) {
		}
	}

}
