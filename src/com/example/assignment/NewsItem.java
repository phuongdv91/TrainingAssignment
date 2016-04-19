package com.example.assignment;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsItem implements Parcelable{
	private String mImage;
	private String mTitle;
	private String mDateTime;
	private String mDescription;
	private String mLink;

	public NewsItem() {
	}

	public NewsItem(String image, String title, String dateTime,
			String description, String link) {
		setImage(image);
		setTitle(title);
		setDateTime(dateTime);
		setDescription(description);
		setLink(link);
	}

	public NewsItem(Parcel in) {
		mTitle = in.readString();
		mDateTime = in.readString();
		mDescription = in.readString();
		mImage = in.readString();
		mLink = in.readString();
	}

	public String getImage() {
		return mImage;
	}

	public void setImage(String image) {
		mImage = image;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getDateTime() {
		return mDateTime;
	}

	public void setDateTime(String dateTime) {
		mDateTime = dateTime;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getLink() {
		return mLink;
	}

	public void setLink(String link) {
		mLink = link;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<NewsItem> CREATOR = new Parcelable.Creator<NewsItem>() {
        public NewsItem createFromParcel(Parcel in) {
            return new NewsItem(in);
        }

        public NewsItem[] newArray(int size) {
            return new NewsItem[size];
        }
    };
    
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mTitle);
		dest.writeString(mDateTime);
		dest.writeString(mDescription);
		dest.writeString(mImage);
		dest.writeString(mLink);
	}
}
