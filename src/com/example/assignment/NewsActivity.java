package com.example.assignment;

import java.util.ArrayList;
import java.util.List;

import com.example.assignment.RssDataController.IF_TaskListener;
import com.example.assignment.ImageLoader.ImageLoader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NewsActivity extends Activity implements IF_TaskListener {

	private static final String URL = "http://vietnamnet.vn/rss/home.rss";
	private boolean mIsLoaded = false;

	private IntentFilter mFilter = new IntentFilter(
			ConnectivityManager.CONNECTIVITY_ACTION);
	private ListView mListViewNews;
	private NewsAdapter mNewsAdapter;
	private ProgressDialog mProgressDialog;
	private RssDataController mTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		mListViewNews = (ListView) findViewById(R.id.listviewNews);
		mListViewNews.setOnItemClickListener(mOnItemClickListener);
		mTask = new RssDataController();
		mTask.setListenter(this);
		processNetWorkState();
	}

	private void showLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = new ProgressDialog(NewsActivity.this);
		mProgressDialog.setMessage(getString(R.string.news_loading_message));
		mProgressDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mNetworkChangeReceiver, mFilter);
	};
	
	@Override
	protected void onPause() {
		unregisterReceiver(mNetworkChangeReceiver);
		super.onPause();
	}
	
	private State mNetWorkState;
	private BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				processNetWorkState();
			}
		}
	};
	
	private void processNetWorkState() {
		Log.d(NewsActivity.class.getSimpleName(), "processNetWorkState");
		ConnectivityManager conMan = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMan.getActiveNetworkInfo();
		if (netInfo != null) {
			Log.d(NewsActivity.class.getSimpleName(), netInfo.isConnected() + " " + netInfo.getState());
			if (netInfo.isConnected()) {
				Log.d(NewsActivity.class.getSimpleName(), mTask.getStatus() + " ");
				if (mTask.getStatus() != Status.RUNNING) {
					if (mIsLoaded == false) {
						showLoadingDialog();
					}
					mTask = new RssDataController();
					mTask.setListenter(this);
					mTask.execute(URL);
				}
			} else {
				if (mProgressDialog != null
						&& mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				mProgressDialog = null;
				if (netInfo.getState() == State.DISCONNECTED
						&& mNetWorkState != State.DISCONNECTED) {
					showToast(R.string.news_network_disconnected);
				}
			}
			mNetWorkState = netInfo.getState();
		} else {
			if (mProgressDialog != null
					&& mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			mProgressDialog = null;
			if (mNetWorkState != State.DISCONNECTED) {
				showToast(R.string.news_network_disconnected);
			}
		}
	}
	
	@Override
	public void onLoadRssComplete(ArrayList<NewsItem> result) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		if (result != null && result.size() > 0) {
			mIsLoaded = true;
			mNewsAdapter = new NewsAdapter(this, result);
			mListViewNews.setAdapter(mNewsAdapter);
		} else {
			showToast(R.string.news_load_fail);
		}
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

		}
	};

	private List<NewsItem> mNews;
	public class NewsAdapter extends BaseAdapter {
		private LayoutInflater mInflator;
		private ImageLoader mImageLoader;

		public NewsAdapter(Activity activity, List<NewsItem> news) {
			super();
			mNews = news;
			mInflator = activity.getLayoutInflater();
			mImageLoader = new ImageLoader(activity.getApplicationContext());
		}

		@Override
		public View getView(int position, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			if (view == null) {
				view = mInflator.inflate(R.layout.news_list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.image = (ImageView) view
						.findViewById(R.id.news_item_image);
				viewHolder.title = (TextView) view
						.findViewById(R.id.news_item_title);
				viewHolder.dateTime = (TextView) view
						.findViewById(R.id.news_item_date);
				viewHolder.description = (TextView) view
						.findViewById(R.id.news_item_description);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}
			NewsItem item = mNews.get(position);
			viewHolder.title.setText(item.getTitle());
			viewHolder.dateTime.setText(item.getDateTime());
			viewHolder.description.setText(Html.fromHtml(item.getDescription()));
			mImageLoader.displayImage(item.getImage(), viewHolder.image);
			return view;
		}

		private class ViewHolder {
			ImageView image;
			TextView title;
			TextView dateTime;
			TextView description;
		}

		@Override
		public int getCount() {
			return mNews.size();
		}

		@Override
		public Object getItem(int position) {
			return mNews.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	private Toast mToast;
    public void showToast(int resId) {
        showToast(getString(resId));
    }
    
    public void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }
}
