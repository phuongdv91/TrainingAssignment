package com.example.assignment.ImageLoader;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.graphics.Bitmap;
import android.util.Log;

public class MemoryCache {

    private static final String TAG = MemoryCache.class.getSimpleName();
    private Map<String, Bitmap> mCache = Collections.synchronizedMap(
            new LinkedHashMap<String, Bitmap>(10,1.5f,true)); //Last argument true for LRU ordering
    private long mSize = 0; //current allocated size
    private long mLimit = 1000000; //max memory in bytes

    public MemoryCache(){
        //use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory()/4);
    }
    
    public void setLimit(long new_limit){
    	mLimit = new_limit;
        Log.i(TAG, "MemoryCache will use up to " + mLimit/1024./1024. + "MB");
    }

    public Bitmap get(String id){
        try {
            if (!mCache.containsKey(id)) {
                return null;
            }
            // NullPointerException sometimes happen here 
            // http://code.google.com/p/osmdroid/issues/detail?id=78 
            return mCache.get(id);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void put(String id, Bitmap bitmap){
        try {
            if (mCache.containsKey(id)) {
            	mSize -= getSizeInBytes(mCache.get(id));
            }
            mCache.put(id, bitmap);
            mSize += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
    
    private void checkSize() {
        Log.i(TAG, "cache size=" + mSize + " length=" + mCache.size());
        if (mSize > mLimit) {
            //least recently accessed item will be the first one iterated 
            Iterator<Entry<String, Bitmap>> iter = mCache.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, Bitmap> entry = iter.next();
                mSize -= getSizeInBytes(entry.getValue());
                iter.remove();
                if (mSize <= mLimit) {
                    break;
                }
            }
            Log.i(TAG, "Clean cache. New size " + mCache.size());
        }
    }

    public void clear() {
        try {
            // NullPointerException sometimes happen here 
        	// http://code.google.com/p/osmdroid/issues/detail?id=78 
        	mCache.clear();
        	mSize = 0;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private long getSizeInBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}