package in.newzup;

public class CommentModel {
	private int id;
	private String content;
	private String username;
	private String imgurl;
	private int create_time;
	private int author;
	private int likes;
	private int dislikes;
	private int post_id;
	
	public CommentModel(int id, String content, int create_time, int author, int likes, int dislikes, int post_id) {
        super();
        this.setId(id);
        this.content = content;
        this.setCreateTime(create_time);
        this.setAuthor(author);
        this.setLikes(likes);
        this.setDislikes(dislikes);
        this.setPostId(post_id);
    }
	
	public CommentModel(int id, String content, String username, String imgurl, int likes, int dislikes) {
        super();
        this.id = id;
        this.content = content;
        this.setUsername(username);
        this.imgurl = imgurl;
        this.likes = likes;
        this.dislikes = dislikes;
    }
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCreateTime() {
		return create_time;
	}

	public void setCreateTime(int create_time) {
		this.create_time = create_time;
	}

	public int getAuthor() {
		return author;
	}

	public void setAuthor(int author) {
		this.author = author;
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

	public int getPostId() {
		return post_id;
	}

	public void setPostId(int post_id) {
		this.post_id = post_id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}
}
