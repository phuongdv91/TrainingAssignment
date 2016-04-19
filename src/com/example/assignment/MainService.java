package com.example.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class MainService extends Service{

	private MainService mMainService;
	private final IBinder mBinder = new ServiceBinder();
	
	private static final int sScheduleRequestCode = 1;
	private static final int sIntervalTime = 30000;
	private AlarmManager mAlarmManager;
	private PendingIntent mSchedulePendingIntent;
	private ResponseReceiver mResponseReceiver = new ResponseReceiver();
	
	private static final int sNotificationId = 123;
	private List<NewsItem> mNewsList;
	private Map<String, Integer> mNewsMap = new HashMap<String, Integer>();
	private boolean mHasNewPost = false;
	private IF_DataListener mDataListener;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class ServiceBinder extends Binder {
		public MainService getService() {
			mMainService = MainService.this;
			return mMainService;
		}
	}
	
	public void setDataListener(IF_DataListener listener) {
		mDataListener = listener;
		if (mDataListener != null && mNewsList != null && mNewsList.size() > 0) {
			mDataListener.onLoadDataCompleted(mNewsList, false);
		}
		NotificationManager notificationMng = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationMng.cancel(sNotificationId);
		processNetWorkState();
	}

	public void onNotified() {
		mHasNewPost = false;
	}

	public boolean hasNewPost() {
		return mHasNewPost;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(MainService.this, RssDataService.class);
		mSchedulePendingIntent = PendingIntent.getService(
				MainService.this, sScheduleRequestCode, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		registerReceiver(mResponseReceiver, new IntentFilter(
				ResponseReceiver.ACTION_RESP));
		registerReceiver(mNetworkChangeReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		processNetWorkState();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mResponseReceiver);
		unregisterReceiver(mNetworkChangeReceiver);
		super.onDestroy();
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
			if (result != null && result.size() > 0) {
				List<NewsItem> newPost = checkNewPost(result);
				boolean hasNewPost = (mNewsList != null && mNewsList.size() > 0
						&& newPost != null && newPost.size() > 0);
				mNewsList = result;
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
				if (mDataListener != null) {
					mDataListener.onLoadDataCompleted(mNewsList, hasNewPost);
				} else {
					createNotification(newPost);
				}
				mHasNewPost = mHasNewPost || hasNewPost;
				mNewsMap.clear();
				for (int i = 0; i < result.size(); i++) {
					mNewsMap.put(result.get(i).getLink(), i);
				}
			} else {
				if (mDataListener != null) {
					mDataListener.onLoadFailed();
				}
				processNetWorkState();
			}
		}

		private List<NewsItem> checkNewPost(List<NewsItem> result) {
			List<NewsItem> ret = new ArrayList<NewsItem>();
			for (int i = 0; i < result.size(); i++) {
				if (mNewsMap.containsKey(result.get(i).getLink()) == false) {
					ret.add(result.get(i));
				}
			}
			return ret;
		}
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
				if (mDataListener != null) {
					mDataListener.onLoading();
				}
				if (mNetWorkState != netInfo.getState()) {
					Intent intent = new Intent(MainService.this,
							RssDataService.class);
					startService(intent);
				}
			} else {
				boolean isNeedShowToast = netInfo.getState() == State.DISCONNECTED
						&& mNetWorkState != State.DISCONNECTED;
				if (mDataListener != null) {
					mDataListener.onNetWorkDisconnected(isNeedShowToast);
				}
				mAlarmManager.cancel(mSchedulePendingIntent);
			}
			mNetWorkState = netInfo.getState();
		} else {
			boolean isNeedShowToast = mNetWorkState != State.DISCONNECTED;
			if (mDataListener != null) {
				mDataListener.onNetWorkDisconnected(isNeedShowToast);
			}
			mAlarmManager.cancel(mSchedulePendingIntent);
		}
	}
	
	public void createNotification(List<NewsItem> newPost) {
		if (newPost == null || newPost.size() == 0) {
			return;
		}
		Intent intent = new Intent(MainService.this, NewsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		NotificationManager notificationMng = 
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationMng.cancel(sNotificationId);
		Builder builder = new Notification.Builder(MainService.this)
				.setSmallIcon(R.drawable.dantri_icon)
				.setDefaults(Notification.DEFAULT_ALL)
				.setOnlyAlertOnce(true)
				.setAutoCancel(true)
				.setWhen(System.currentTimeMillis());
		if (newPost.size() > 1) {
			builder.setContentTitle(getString(R.string.notification_title_multi))
					.setContentText(getString(R.string.notification_content_multi))
					.setTicker(getString(R.string.notification_sticker_multi));
			Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
			inboxStyle.setBigContentTitle(getString(R.string.notification_title_multi));
			for (int i = 0; i < newPost.size(); i++) {
				inboxStyle.addLine(newPost.get(i).getTitle());
			}
			builder.setStyle(inboxStyle);
		} else {
			builder.setContentTitle(newPost.get(0).getTitle())
					.setContentText(newPost.get(0).getDescription())
					.setTicker(newPost.get(0).getTitle());
		}

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(
                MainService.this, sNotificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingNotificationIntent);
        notificationMng.notify(sNotificationId, builder.build());
	}

	public interface IF_DataListener {
		public void onLoading();
		public void onLoadFailed();
		public void onNetWorkDisconnected(boolean isNeedShowToast);
		public void onLoadDataCompleted(List<NewsItem> result, boolean hasNewPost);
	}
}
