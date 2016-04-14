package com.example.assignment;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsItem implements Parcelable{
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

	public NewsItem(Parcel in) {
		mTitle = in.readString();
		mDateTime = in.readString();
		mDescription = in.readString();
		mImage = in.readString();
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
	}
}
