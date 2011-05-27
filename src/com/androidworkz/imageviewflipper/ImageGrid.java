/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidworkz.imageviewflipper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.androidworkz.imageviewflipper.R;
import com.buuuk.android.util.FileUtils;


public class ImageGrid extends Activity implements OnScrollListener{

    GridView mGrid;
    private static final String DIRECTORY = "/sdcard/";
	private static final String DATA_DIRECTORY = "/sdcard/.ImageViewFlipper/";
	private static final String DATA_FILE = "/sdcard/.ImageViewFlipper/imagelist.dat";
	List<String> ImageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadApps(); // do this in onresume?

        setContentView(R.layout.grid);
        mGrid = (GridView) findViewById(R.id.myGrid);
        mGrid.setOnScrollListener(this);
       
        
        File data_directory = new File(DATA_DIRECTORY);
		if (!data_directory.exists()) {
			if (data_directory.mkdir()) {
				FileUtils savedata = new FileUtils();
				Toast toast = Toast.makeText(ImageGrid.this,
						"Please wait while we search your SD Card for images...", Toast.LENGTH_SHORT);
				toast.show();
				SystemClock.sleep(100);
				ImageList = FindFiles();
				savedata.saveArray(DATA_FILE, ImageList);
				
			} else {
				ImageList = FindFiles();
			}

		}
		else {
			File data_file= new File(DATA_FILE);
			if (!data_file.exists()) {
				FileUtils savedata = new FileUtils();
				Toast toast = Toast.makeText(ImageGrid.this,
						"Please wait while we search your SD Card for images...", Toast.LENGTH_SHORT);
				toast.show();
				SystemClock.sleep(100);
				ImageList = FindFiles();
				savedata.saveArray(DATA_FILE, ImageList);
			} else {
				FileUtils readdata = new FileUtils();
				ImageList = readdata.loadArray(DATA_FILE);
			}
		}
		mAdapter = new AppsAdapter();
		 mGrid.setAdapter(mAdapter);
		 mThumbnails = new HashMap<Integer,SoftReference<ImageView>>();
         mThumbnailImages = new HashMap<Integer,SoftReference<Bitmap>>();
    }
    
    private List<String> FindFiles() {
		final List<String> tFileList = new ArrayList<String>();
		Resources resources = getResources();
		// array of valid image file extensions
		String[] imageTypes = resources.getStringArray(R.array.image);
		FilenameFilter[] filter = new FilenameFilter[imageTypes.length];

		int i = 0;
		for (final String type : imageTypes) {
			filter[i] = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith("." + type);
				}
			};
			i++;
		}

		FileUtils fileUtils = new FileUtils();
		File[] allMatchingFiles = fileUtils.listFilesAsArray(
				new File(DIRECTORY), filter, -1);
		for (File f : allMatchingFiles) {
			tFileList.add(f.getAbsolutePath());
		}
		return tFileList;
	}

    private List<ResolveInfo> mApps;

    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    public AppsAdapter mAdapter;
    public class AppsAdapter extends BaseAdapter {
        public AppsAdapter() {
        	map = new HashMap();
        }
        
        public Map<Integer,SoftReference<Bitmap>> map;
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView i;

            if (convertView == null) {
                i = new ImageView(ImageGrid.this);
                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
                i.setLayoutParams(new GridView.LayoutParams(80, 80));
            } else {
                i = (ImageView) convertView;
            }

            
            //else
           // 	i.setImageDrawable(Drawable.createFromPath(ImageList
			//	.get(position)));
            
            if(!mBusy && mThumbnailImages.containsKey(position) 
        			&& mThumbnailImages.get(position).get()!=null) {
        		i.setImageBitmap(mThumbnailImages.get(position).get());
        	}
        	else  {
        		i.setImageBitmap(null);
        		if(!mBusy)loadThumbnail(i,position);
        	}
            
            i.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Toast.makeText(ImageGrid.this, "Opening Image...", Toast.LENGTH_LONG).show();

					// TODO Auto-generated method stub
					SharedPreferences indexPrefs = getSharedPreferences("currentIndex",
							MODE_PRIVATE);
					
					SharedPreferences.Editor indexEditor = indexPrefs.edit();
					indexEditor.putInt("currentIndex", position);
					indexEditor.commit();
					final Intent intent = new Intent(ImageGrid.this, ImageViewFlipper.class);
		            startActivity(intent);
		           
				}
			});
            
            
            return i;
        }


        public final int getCount() {
            return ImageList.size();
        }

        public final Object getItem(int position) {
            return ImageList.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }
    public void onScroll(AbsListView view, int firstVisibleItem,
    	    int visibleItemCount, int totalItemCount) {
    	
    }
    
    public boolean mBusy = false;
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
        case OnScrollListener.SCROLL_STATE_IDLE:
            mBusy = false;
            mAdapter.notifyDataSetChanged();
            break;
        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
            mBusy = true;
            // mStatus.setText("Touch scroll");
            break;
        case OnScrollListener.SCROLL_STATE_FLING:
            mBusy = true;
            // mStatus.setText("Fling");
            break;
        }
    }
    
    private Map<Integer,SoftReference<ImageView>> mThumbnails;
    private Map<Integer,SoftReference<Bitmap>> mThumbnailImages;

    private void loadThumbnail( ImageView iv, int position ){
    	mThumbnails.put(position,new SoftReference<ImageView>(iv));
    	try{new LoadThumbnailTask().execute(position);}catch(Exception e){}
    }
    public void onThumbnailLoaded( int position, Bitmap bm, LoadThumbnailTask t ){
    	Bitmap tn = bm;
    	if( mThumbnails.get(position).get() != null && tn!=null)
    		mThumbnails.get(position).get().setImageBitmap(tn);
    	
    	t.cancel(true);
    }
    
    public class LoadThumbnailTask extends AsyncTask<Integer, Void, Bitmap>{
    	private int position;
		@Override
		protected Bitmap doInBackground(Integer... params) {
        	try{
				position = params[0];
				Bitmap bitmapOrg = BitmapFactory.decodeFile(ImageList.get(position));
	        
	        	int width = bitmapOrg.getWidth();
	        	int height = bitmapOrg.getHeight();
	     
	        	//new width / height
	        	int newWidth = 80;
	        	int newHeight = 80;
	
	        	// calculate the scale
	        	float scaleWidth = (float) newWidth / width;
	        	float scaleHeight = (float) newHeight/ (height * scaleWidth) ;
	        	// create a matrix for the manipulation
	        	Matrix matrix = new Matrix();
	
	        	// resize the bit map
	        	matrix.postScale(scaleWidth, scaleWidth);
	        	matrix.postScale(scaleHeight, scaleHeight);
	
	        	// recreate the new Bitmap and set it back
	        	Bitmap bm = Bitmap.createBitmap(bitmapOrg, 0, 0,width, height, matrix, true);
	            
	            mThumbnailImages.put(position, new SoftReference<Bitmap>(bm));
	            System.gc();
	            return bm;
        	}catch(Exception e){
        		
        	}
            
            
			return null;
		}
		protected void onPostExecute(Bitmap bm) {
	         
	         onThumbnailLoaded(position, bm, this);
	     }
    	
    }

}
