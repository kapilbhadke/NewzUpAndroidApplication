package in.newzup;

public class NewsModel {
	private int id;
	private String title;
	private String imgurl;
	private String link;
	private String content;
	private String tags;
	private String category;
	private int likes;
	private int dislikes;
	private int comments;
	private String username;
	private String provider;
	
	public NewsModel(int id, String title, String imgurl, String link, String tags, String content, String category, int likes, int dislikes, int comments) {
        super();
        this.setId(id);
        this.title = title;
        this.imgurl = imgurl;
        this.setLink(link);
        this.setTags(tags);
        this.setCategory(category);
        this.setLikes(likes);
        this.setDislikes(dislikes);
        this.setComments(comments);
    }
	
	public NewsModel(String imgurl, String title, String tags, String link) {
        super();
        this.title = title;
        this.imgurl = imgurl;
        this.tags = tags;
        this.link = link;
    }
	
	public NewsModel(int id, String imgurl, String title, String tags, String category, String link, int comments, int likes, int dislikes, String provider, String content) {
        super();
        this.id = id;
        this.title = title;
        this.imgurl = imgurl;
        this.tags = tags;
        this.category = category;
        this.link = link;
        this.setContent(content);
        this.comments = comments;
        this.likes = likes;
        this.dislikes = dislikes;
        this.setProvider(provider);
    }
	
	public NewsModel(String link, String category, String tags) {
        super();
        this.tags = tags;
        this.link = link;
        this.category = category;
    }
	
	public String getIcon() {
		return imgurl;
	}
	public void setIcon(String imgurl) {
		this.imgurl = imgurl;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTags() {
		return "#" + tags.replaceAll(",", ", #");
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public int getDislikes() {
		return dislikes;
	}

	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}

	public int getComments() {
		return comments;
	}

	public void setComments(int comments) {
		this.comments = comments;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
