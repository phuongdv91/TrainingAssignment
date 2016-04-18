package com.example.assignment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.example.assignment.MainService.ResponseReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.net.ParseException;
import android.util.Log;

public class RssDataService extends IntentService{

	private static final String URL = "http://vietnamnet.vn/rss/home.rss";

	public RssDataService() {
		super(RssDataService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		InputStream is = null;
		RSSXMLTag currentTag = null;
		ArrayList<NewsItem> postDataList = new ArrayList<NewsItem>();
		try {
			URL url = new URL(URL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setReadTimeout(10 * 1000);
			connection.setConnectTimeout(10 * 1000);
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.connect();
			int response = connection.getResponseCode();
			Log.d("debug", "The response is: " + response);
			is = connection.getInputStream();

			// parse xml after getting the data
			XmlPullParserFactory factory = XmlPullParserFactory
					.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, null);

			int eventType = xpp.getEventType();
			NewsItem pdData = null;
			Stack<RSSXMLTag> stackRSSXMLTag = new Stack<RSSXMLTag>();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {

				} else if (eventType == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("item")) {
						pdData = new NewsItem();
						stackRSSXMLTag.clear();
					} else if (xpp.getName().equals("title")) {
						currentTag = RSSXMLTag.TITLE;
					} else if (xpp.getName().equals("link")) {
						currentTag = RSSXMLTag.LINK;
					} else if (xpp.getName().equals("pubDate")) {
						currentTag = RSSXMLTag.DATE;
					} else if (xpp.getName().equals("description")) {
						currentTag = RSSXMLTag.DESCRIPTION;
					} else if (xpp.getName().equals("image")) {
						currentTag = RSSXMLTag.IMAGE;
					} else {
						currentTag = RSSXMLTag.IGNORETAG;
					}
					stackRSSXMLTag.add(currentTag);
				} else if (eventType == XmlPullParser.END_TAG) {
					if (xpp.getName().equals("item")) {
						checkPostData(pdData);
						postDataList.add(pdData);
						stackRSSXMLTag.clear();
					} else {
						if (stackRSSXMLTag.size() < 2) {
							currentTag = RSSXMLTag.IGNORETAG;
							stackRSSXMLTag.clear();
						} else {
							stackRSSXMLTag.pop();
							currentTag = stackRSSXMLTag.peek();
						}
					}
				} else if (eventType == XmlPullParser.TEXT) {
					String content = xpp.getText();
					content = content.trim();
					// Log.d("debug", currentTag + content);
					if (pdData != null) {
						switch (currentTag) {
						case TITLE:
							if (content.length() != 0) {
								if (pdData.getTitle() != null) {
									pdData.setTitle(pdData.getTitle() + content);
								} else {
									pdData.setTitle(content);
								}
							}
							break;
						case IMAGE:
							if (content.length() != 0) {
								if (pdData.getImage() != null) {
									pdData.setImage(pdData.getImage() + content);
								} else {
									pdData.setImage(content);
								}
							}
							break;
						case DATE:
							if (content.length() != 0) {
								if (pdData.getDateTime() != null) {
									pdData.setDateTime(pdData.getDateTime() + content);
								} else {
									pdData.setDateTime(content);
								}
							}
							break;
						case DESCRIPTION:
							if (content.length() != 0) {
								if (pdData.getDescription() != null) {
									pdData.setDescription(pdData.getDescription() + content);
								} else {
									pdData.setDescription(content);
								}
							}
							break;
						default:
							break;
						}
					}
				}

				eventType = xpp.next();
			}
			Log.v("tst", String.valueOf((postDataList.size())));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		Intent broadcastIntent = new Intent(ResponseReceiver.ACTION_RESP);
		broadcastIntent.putParcelableArrayListExtra(
				ResponseReceiver.RESULT_LIST, postDataList);
		sendBroadcast(broadcastIntent);
	}

	private void checkPostData(NewsItem pdData) {
		String description = pdData.getDescription();
		int imgTagIndex = description.indexOf("<img");
		if (imgTagIndex > 0) {
			String content = description.substring(0, imgTagIndex);
			int brTagIndex = content.indexOf("<br");
			if (brTagIndex > 0) {
				pdData.setDescription(content.substring(0, brTagIndex));
			} else {
				pdData.setDescription(content);
			}
			String imgTag = description.substring(imgTagIndex, description.length());
			int imgSrcStartIndex = imgTag.indexOf("http://");
			if (imgSrcStartIndex > 0) {
				int imgSrcEndIndex = imgTag.indexOf("/>");
				if (imgSrcEndIndex > 0) {
					pdData.setImage(imgTag.substring(imgSrcStartIndex, imgSrcEndIndex));
				}
			}
		}
	}

	private enum RSSXMLTag {
		TITLE, DATE, DESCRIPTION, IMAGE, LINK, IGNORETAG
	}
}
