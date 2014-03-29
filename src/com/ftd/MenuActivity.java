package com.ftd;

import java.io.File;

import com.ftf.R;
import com.google.android.glass.media.CameraManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuActivity extends Activity{
	
	private static final int TAKE_PICTURE_REQUEST = 1;
	
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
		openOptionsMenu();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ftf, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.take_picture:
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, TAKE_PICTURE_REQUEST);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
        	String path = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
        	processPictureWhenReady(path);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processPictureWhenReady(String path) {
    	File picture = new File(path);
    	if(picture.exists()) 
    		Log.i("FTF", "Picture was taken and ready to process");
    	else{
	    	Observer observer = new Observer(picture);
	    	observer.startWatching();
    	}
	}

	@Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the Activity.
        finish();
    }
	
	private class Observer extends FileObserver{

		private File picture;
		private boolean isFileWritten;
		
		public Observer(File picture) {
			super(picture.getParentFile().getPath());
			this.picture = picture;
		}

		@Override
		public void onEvent(int event, String path) {
		
			if (!isFileWritten) {
                // For safety, make sure that the file that was created in
                // the directory is actually the one that we're expecting.
                File affectedFile = new File(this.picture.getParentFile(), path);
                isFileWritten = (event == FileObserver.CLOSE_WRITE
                        && affectedFile.equals(picture));

                if (isFileWritten) {
                    stopWatching();

                    // Now that the file is ready, recursively call
                    // processPictureWhenReady again (on the UI thread).
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processPictureWhenReady(picture.getPath());
                        }
                    });
                }
            }			
		}
		
	}
}