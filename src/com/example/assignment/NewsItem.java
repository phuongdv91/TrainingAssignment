package com.example.assignment;

public class NewsItem {
	private String mImage;
	private String mTitle;
	private String mDateTime;
	private String mDescription;

	public NewsItem() {
	}

	public NewsItem(String image, String title, String dateTime,
			String description) {
		setImage(image);
		setTitle(title);
		setDateTime(dateTime);
		setDescription(description);
	}

	public String getImage() {
		return mImage;
	}

	public void setImage(String mImage) {
		this.mImage = mImage;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getDateTime() {
		return mDateTime;
	}

	public void setDateTime(String mDateTime) {
		this.mDateTime = mDateTime;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}
}
