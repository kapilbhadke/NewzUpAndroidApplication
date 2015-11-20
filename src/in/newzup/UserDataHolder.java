package in.newzup;

public class UserDataHolder {

	private static final UserDataHolder holder = new UserDataHolder();
	public static UserDataHolder getInstance() {return holder;}

	private static String username;
	private static String imgurl;

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		UserDataHolder.username = username;
	}

	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		UserDataHolder.imgurl = imgurl;
	}
}