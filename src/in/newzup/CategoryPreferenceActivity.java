package in.newzup;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InflateParams")
public class CategoryPreferenceActivity extends Activity {
	private Bitmap[] thumbnails;
	private Bitmap[] thumbnails_checked;
	private boolean[] thumbnailsselection;
	private String[] arrPath;
	private ImageAdapter imageAdapter;
	private static final String[] categories = {"All", "India", "Economy", "Politics", "World", "Business", "Sports", "Sc & T", "Misc"};
	private int[] cat_drawable_ids = { R.drawable.world, R.drawable.i, R.drawable.e, R.drawable.p, R.drawable.w, R.drawable.m, R.drawable.s, R.drawable.b, R.drawable.t};
	private int count = categories.length;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_grid);
		
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#253939")));
		
		this.thumbnails = new Bitmap[this.count];
		this.thumbnails_checked = new Bitmap[this.count];
		this.arrPath = new String[this.count];
		this.thumbnailsselection = new boolean[this.count];
		
		for (int i = 0; i < this.count; i++) {
			thumbnails[i] = BitmapFactory.decodeResource(getResources(), cat_drawable_ids[i]);
			thumbnails_checked[i] = BitmapFactory.decodeResource(getResources(), R.drawable.tick);
			arrPath[i]= categories[i];
		}
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String arr = sharedPrefs.getString("PREF_CATEGORIES", "0 1 2 3 4 5 6 7 8");	//Default all categories
		String[] items = arr.split(" ");
		int[] results = new int[items.length];
		for (int i = 0; i < items.length; i++) {
		    try {
		        results[i] = Integer.parseInt(items[i]);
		        thumbnailsselection[results[i]] = true;
		    } catch (NumberFormatException nfe) {};
		}
		
		GridView imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
		
		imageAdapter = new ImageAdapter();
		imagegrid.setAdapter(imageAdapter);

		final Button selectBtn = (Button) findViewById(R.id.selectBtn);
		selectBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				final int len = thumbnailsselection.length;
				int cnt = 0;
				StringBuilder selectedCategories = new StringBuilder();
				for (int i=1; i<len; i++)
				{
					if (thumbnailsselection[i]){
						cnt++;
						selectedCategories.append(i + " ");
					}
				}
				if (cnt == 0){
					Toast.makeText(getApplicationContext(),	"Please select at least one category.", Toast.LENGTH_LONG).show();
				} else {
					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CategoryPreferenceActivity.this);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString("PREF_CATEGORIES", selectedCategories.toString());
					editor.apply();
					finish();
				}
			}
		});
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
    public void onBackPressed() {
			finish();
    }
	
	public class ImageAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		ViewHolder holder;
		private final String[] categories = {"All", "India", "Economy", "Politics", "World", "Business", "Sports", "Sc & T", "Misc"};
		private int count = categories.length;
		ImageView iv[] = new ImageView[count];

		public ImageAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return count;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(
						R.layout.gallery_item, null);
				holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
				holder.imageview_checked = (ImageView) convertView.findViewById(R.id.thumbImageChecked);
				holder.textview = (TextView) convertView.findViewById(R.id.category);
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.imageview.setId(position);
			holder.imageview_checked.setId(position);
			iv[position] = holder.imageview_checked;
			
			holder.imageview.setImageBitmap(thumbnails[position]);
			holder.imageview_checked.setImageBitmap(thumbnails_checked[position]);
			holder.textview.setText(categories[position]);
			if(!thumbnailsselection[position] || position == 0)
				holder.imageview_checked.setVisibility(View.GONE);
			holder.id = position;

			holder.imageview.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					ImageView cb = (ImageView) v;
					int id = cb.getId();

					if(id == 0)
					{
						if(thumbnailsselection[id] == false)
						{
							for(int i=1; i<9; i++)
							{
								iv[i].setVisibility(View.VISIBLE);
								thumbnailsselection[i] = true;
							}
							thumbnailsselection[id] = true;
						}
						else
						{
							for(int i=1; i<9; i++)
							{
								iv[i].setVisibility(View.GONE);
								thumbnailsselection[i] = false;
							}
							thumbnailsselection[id] = false;
						}
					}
					else if (thumbnailsselection[id])
					{
						thumbnailsselection[id] = false;
						iv[id].setVisibility(View.GONE);
					}
					else 
					{
						thumbnailsselection[id] = true;
						iv[id].setVisibility(View.VISIBLE);
					}
				}
			});
			return convertView;
		}
	}
	class ViewHolder {
		ImageView imageview;
		ImageView imageview_checked;
		TextView textview;
		int id;
	}
}