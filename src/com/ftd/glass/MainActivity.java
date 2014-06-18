package com.ftd.glass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit.RestAdapter;
import retrofit.mime.TypedFile;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

import com.ftd.rest.DudeService;
import com.ftd.rest.model.DudeInformation;
import com.google.android.glass.media.CameraManager;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

public class MainActivity extends Activity {

	private static final int TAKE_PICTURE_REQUEST = 1;
	private static final String TAG = "MenuActivity";
	private DudeService dudeService;
	private LiveCard liveCard;
	private TimelineManager timelineManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		takePhoto();
	}

	private void init() {
		createRestAdapter();
		prepareTimlineManager();
	}

	private void prepareTimlineManager() {
		timelineManager = TimelineManager.from(this);
	}

	private void createRestAdapter() {
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://10.42.0.29:3000").build();
		this.dudeService = restAdapter.create(DudeService.class);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TAKE_PICTURE_REQUEST &&  resultCode == RESULT_OK) {
			String path = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
			processPictureWhenReady(path);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void processPictureWhenReady(final String picturePath) {
		final File pictureFile = new File(picturePath);
		if (pictureFile.exists()) {
			
			Log.i(TAG, "Picture was taken and ready to process");
			try {				
				List<DudeInformation> dudeInformationList = retrieveDudeInformationList(pictureFile);
				String names = concatanateNamesFromList(dudeInformationList);
				publishCard(pictureFile, dudeInformationList);				
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			final File parentDirectory = pictureFile.getParentFile();
			FileObserver observer = new FileObserver(parentDirectory.getPath()) {

				private boolean isFileWritten;

				@Override
				public void onEvent(int event, String path) {
					if (!isFileWritten) {
						// For safety, make sure that the file that was created
						// in
						// the directory is actually the one that we're
						// expecting.
						File affectedFile = new File(parentDirectory, path);
						isFileWritten = (event == FileObserver.CLOSE_WRITE && affectedFile
								.equals(pictureFile));

						if (isFileWritten) {
							stopWatching();

							// Now that the file is ready, recursively call
							// processPictureWhenReady again (on the UI thread).
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									processPictureWhenReady(picturePath);
								}
							});
						}
					}
				}
			};
			observer.startWatching();
		}
	}

	private void publishCard(File pictureFile, List<DudeInformation> dudeInformationList) throws FileNotFoundException {
		
		if(liveCard == null){
			timelineManager = TimelineManager.from(this);
			liveCard = timelineManager.createLiveCard("LIVE_CARD");
		}
		
		RemoteViews remoteViews = new RemoteViews(this.getBaseContext().getPackageName(), R.layout.find_view);
		remoteViews.setCharSequence(R.id.name_information, "setText", concatanateNamesFromList(dudeInformationList));
		liveCard.setViews(remoteViews);
		
		Bitmap image = preparePicture(pictureFile);
		remoteViews.setImageViewBitmap(R.id.picture, image);
		
		Intent menuIntent = new Intent(this, MenuActivity.class);
		menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
		liveCard.publish(LiveCard.PublishMode.REVEAL);
	}

	private Bitmap preparePicture(File pictureFile) throws FileNotFoundException {			
		FileInputStream fis = new FileInputStream(pictureFile);
		Bitmap bitmap2 = BitmapFactory.decodeStream(fis);
		return Bitmap.createScaledBitmap(bitmap2, 320, 180,false);
	}

	private String concatanateNamesFromList(List<DudeInformation> dudeInformationList) {		
		if(dudeInformationList == null || dudeInformationList.isEmpty()) 
			return "Dude not found!";
		StringBuilder names = new StringBuilder();
		for(DudeInformation dudeInformation : dudeInformationList){
			names = names.append(dudeInformation.getName()).append("\n");
		}		
		return names.toString();
	}

	private List<DudeInformation> retrieveDudeInformationList(File pictureFile) 
			throws IOException, InterruptedException, ExecutionException { 
		TypedFile photo = new TypedFile("image/jpeg", pictureFile);
		return new RetrieveDudeTask().execute("788781095", photo).get();

	}
	
	private class RetrieveDudeTask extends AsyncTask<Object, Void, List<DudeInformation>>{
		@Override
		protected List<DudeInformation> doInBackground(Object... params) {		
			return dudeService.findDudeInformation((String)params[0], (TypedFile)params[1]).getDudeInformation();
		}	
	}
}