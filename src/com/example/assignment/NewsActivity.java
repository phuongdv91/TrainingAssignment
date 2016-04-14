package com.example.assignment;

import java.util.List;

import com.example.assignment.ImageLoader.ImageLoader;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
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

public class NewsActivity extends Activity {

	private static final int sScheduleRequestCode = 1;
	private static final int sIntervalTime = 10000;
	private AlarmManager mAlarmManager;
	private PendingIntent mSchedulePendingIntent;
	private ResponseReceiver mResponseReceiver = new ResponseReceiver();
	private boolean mIsLoaded = false;

	private ListView mListViewNews;
	private NewsAdapter mNewsAdapter;
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		mListViewNews = (ListView) findViewById(R.id.listviewNews);
		mListViewNews.setOnItemClickListener(mOnItemClickListener);
		mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(NewsActivity.this, RssDataService.class);
		mSchedulePendingIntent = PendingIntent.getService(
				NewsActivity.this, sScheduleRequestCode, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		registerReceiver(mResponseReceiver, new IntentFilter(
				ResponseReceiver.ACTION_RESP));
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mResponseReceiver);
		super.onDestroy();
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
		registerReceiver(mNetworkChangeReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		processNetWorkState();
	};
	
	@Override
	protected void onPause() {
		mAlarmManager.cancel(mSchedulePendingIntent);
		mNetWorkState = null;
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
			Log.d(NewsActivity.class.getSimpleName(), netInfo.isConnected()
					+ " " + netInfo.getState());
			if (netInfo.isConnected()) {
				if (mNetWorkState != netInfo.getState()) {
					if (mIsLoaded == false) {
						showLoadingDialog();
					}
					Intent intent = new Intent(NewsActivity.this, RssDataService.class);
					startService(intent);
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
				mAlarmManager.cancel(mSchedulePendingIntent);
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
			mAlarmManager.cancel(mSchedulePendingIntent);
		}
	}

	public class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_RESP = "com.example.assignment.intent.action.MESSAGE_PROCESSED";
		public static final String RESULT_LIST = "rss_data_result";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null
					|| ACTION_RESP.equals(intent.getAction()) == false) {
				return;
			}
			List<NewsItem> result = intent.getParcelableArrayListExtra(RESULT_LIST);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			if (result != null && result.size() > 0) {
				mIsLoaded = true;
				mAlarmManager.cancel(mSchedulePendingIntent);
		        final int SDK_INT = Build.VERSION.SDK_INT;
				if (SDK_INT < Build.VERSION_CODES.KITKAT) {
					mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
							SystemClock.elapsedRealtime() + sIntervalTime,
							mSchedulePendingIntent);
				} else if (Build.VERSION_CODES.KITKAT <= SDK_INT) {
					mAlarmManager.setExact(
							AlarmManager.ELAPSED_REALTIME_WAKEUP,
							SystemClock.elapsedRealtime() + sIntervalTime,
							mSchedulePendingIntent);
				}
				mNewsAdapter = new NewsAdapter(NewsActivity.this, result);
				mListViewNews.setAdapter(mNewsAdapter);
			} else {
				showToast(R.string.news_load_fail);
			}
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
