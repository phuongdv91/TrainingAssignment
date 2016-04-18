package com.example.assignment;

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
import android.text.Html;
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

public class NewsActivity extends Activity implements IF_DataListener{

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
	public void onLoadDataCompleted(List<NewsItem> result) {
		if (result != null && result.size() > 0) {
			mIsLoaded = true;
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mNewsAdapter = new NewsAdapter(NewsActivity.this, result);
			mListViewNews.setAdapter(mNewsAdapter);
		} else {
			showToast(R.string.news_load_failed);
		}
	}
}
