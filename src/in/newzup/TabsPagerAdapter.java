package in.newzup;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	private NewsFragment topNewsFragment;
	private NewsFragment indiaNewsFragment;
	private NewsFragment economyNewsFragment;
	private NewsFragment politicsNewsFragment;
	private NewsFragment worldNewsFragment;
	private NewsFragment businessNewsFragment;
	private NewsFragment technologyNewsFragment;
	private NewsFragment sportsNewsFragment;

	private NewsFragment tagNewsFragment;

	private int count = 0;

	public TabsPagerAdapter(FragmentManager fm, int count) {
		super(fm);
		
		topNewsFragment = new NewsFragment(0);
		indiaNewsFragment = new NewsFragment(1);
		economyNewsFragment = new NewsFragment(2);
		politicsNewsFragment = new NewsFragment(3);
		worldNewsFragment = new NewsFragment(4);
		businessNewsFragment = new NewsFragment(5);
		technologyNewsFragment = new NewsFragment(6);
		sportsNewsFragment = new NewsFragment(7);

		tagNewsFragment = new NewsFragment(0);
		this.count = count;
	}

	@Override
	public Fragment getItem(int index) {

		if(this.count == 1)	// News with specific tag
		{
			return tagNewsFragment;
		}
		else
		{
			switch (index) {
			case 0:
				return topNewsFragment;
			case 1:
				return indiaNewsFragment;
			case 2:
				return economyNewsFragment;
			case 3:
				return politicsNewsFragment;
			case 4:
				return worldNewsFragment;
			case 5:
				return businessNewsFragment;
			case 6:
				return sportsNewsFragment;
			case 7:
				return technologyNewsFragment;
			}
		}
		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return this.count;
	}

}