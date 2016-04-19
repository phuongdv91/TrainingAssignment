package com.example.assignment;

import java.util.ArrayList;
import java.util.List;

import com.example.assignment.MainService.IF_DataListener;
import com.example.assignment.ImageLoader.ImageLoader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NewsActivity extends Activity implements IF_DataListener{

	private boolean mIsLoaded = false;

	private TextView mTxtNewStories;
	private ListView mListViewNews;
	private NewsAdapter mNewsAdapter;
	private ProgressDialog mProgressDialog;
	private List<NewsItem> mNewsList = new ArrayList<NewsItem>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		mListViewNews = (ListView) findViewById(R.id.listviewNews);
		mTxtNewStories = (TextView) findViewById(R.id.news_txt_newStories);
		mTxtNewStories.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainService.onNotified();
				mListViewNews.smoothScrollToPosition(0);
				mTxtNewStories.setVisibility(View.GONE);
			}
		});
		mNewsAdapter = new NewsAdapter(NewsActivity.this, mNewsList);
		mListViewNews.setAdapter(mNewsAdapter);
		mListViewNews.setOnItemClickListener(mOnItemClickListener);
		mListViewNews.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				checkListAtTop();
			}
		});
	}

	private void showLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			return;
		}
		mProgressDialog = new ProgressDialog(NewsActivity.this);
		mProgressDialog.setMessage(getString(R.string.news_loading_message));
		mProgressDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent serviceIntent = new Intent(this, MainService.class);
		startService(serviceIntent);
		bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
	};

	@Override
	protected void onPause() {
		if (mMainService != null) {
			mMainService.setDataListener(null);
			mMainService = null;
			unbindService(mServiceConnection);
		}
		super.onPause();
	};

	private MainService mMainService;
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mMainService = (MainService) ((MainService.ServiceBinder) service).getService();
			mMainService.setDataListener(NewsActivity.this);
		}
	};
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

		}
	};

	public class NewsAdapter extends BaseAdapter {
		private LayoutInflater mInflator;
		private ImageLoader mImageLoader;

		public NewsAdapter(Activity activity, List<NewsItem> news) {
			super();
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
			NewsItem item = mNewsList.get(position);
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
			return mNewsList.size();
		}

		@Override
		public Object getItem(int position) {
			return mNewsList.get(position);
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

	@Override
	public void onLoading() {
		if (mIsLoaded == false) {
			showLoadingDialog();
		}
	}

	@Override
	public void onLoadFailed() {
		showToast(R.string.news_load_failed);
	}

	@Override
	public void onNetWorkDisconnected(boolean isNeedShowToast) {
		if (mProgressDialog != null
				&& mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
		if (isNeedShowToast) {
			showToast(R.string.news_network_disconnected);
		}
	}

	@Override
	public void onLoadDataCompleted(List<NewsItem> result, boolean hasNewPost) {
		if (result != null && result.size() > 0) {
			mIsLoaded = true;
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mNewsList = result;
			Log.d("TEST", "hasNewPost: " + hasNewPost);
			if (hasNewPost) {
				Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(500);
			}
			mNewsAdapter.notifyDataSetChanged();
			checkListAtTop();
		} else {
			showToast(R.string.news_load_failed);
		}
	}
	
	private void checkListAtTop() {
		boolean listIsAtTop = false;
		if (mListViewNews.getChildCount() == 0) {
			listIsAtTop = true;
		} else {
			listIsAtTop = mListViewNews.getChildAt(0).getTop() == 0;
		}
		if (listIsAtTop) {
			if (mMainService != null) {
				mMainService.onNotified();
			}
			mTxtNewStories.setVisibility(View.GONE);
		} else {
			if (mMainService != null && mMainService.hasNewPost()) {
				mTxtNewStories.setVisibility(View.VISIBLE);
			}
		}
	}
}
