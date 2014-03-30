package com.ftd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;

import retrofit.RestAdapter;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

import com.ftd.rest.DudeService;
import com.ftd.rest.model.DudeInformation;
import com.ftd.rest.model.DudeInformationWrapper;
import com.google.android.glass.media.CameraManager;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

public class MainActivity extends Activity {

	private static final int TAKE_PICTURE_REQUEST = 1;
	private static final String TAG = "FTDMenuActivity";
	private PictureHolder pictureHolder;
	private DudeService dudeService;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof FindService.FinderBinder) {
				pictureHolder = ((FindService.FinderBinder) service)
						.getPictureHolder();
			}
			// No need to keep the service bound.
			unbindService(this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// Nothing to do here.
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createRestAdapter();
		this.takePhoto();
	}

	private void createRestAdapter() {
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(
				"http://10.42.0.19:3000").build();
		this.dudeService = restAdapter.create(DudeService.class);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TAKE_PICTURE_REQUEST && 
				resultCode == RESULT_OK) {
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

				FileInputStream fis;
				fis = new FileInputStream(pictureFile);

				String names = "";
				List<DudeInformation> dudesList = new RetrieveDudeTask().
						execute("00000", Base64.encodeToString(IOUtils.toByteArray(fis), Base64.DEFAULT)).get();
				
				if(dudesList != null){
					for(DudeInformation dudeInformation : dudesList){
						names = names + dudeInformation.getFullName() + "\n";
					}
				}
				
				if(names == null || names.length() == 0){
					names = "No dudes found!";
				}
									
				TimelineManager timelineManager = TimelineManager.from(this);
				LiveCard liveCard = timelineManager.createLiveCard("LIVE_CARD");
				RemoteViews remoteViews = new RemoteViews(this.getBaseContext().getPackageName(), R.layout.find_view);
				remoteViews.setCharSequence(R.id.name_information, "setText", names);
				liveCard.setViews(remoteViews);

				fis = new FileInputStream(pictureFile);
				Bitmap bitmap2 = BitmapFactory.decodeStream(fis);
				Bitmap bitmap2r = Bitmap.createScaledBitmap(bitmap2, 320, 180,false);
				remoteViews.setImageViewBitmap(R.id.picture, bitmap2r);

				Intent menuIntent = new Intent(this, MenuActivity.class);
				menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				liveCard.setAction(PendingIntent.getActivity(this, 0,
						menuIntent, 0));
				liveCard.publish(LiveCard.PublishMode.REVEAL);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// The file does not exist yet. Before starting the file observer,
			// you
			// can update your UI to let the user know that the application is
			// waiting for the picture (for example, by displaying the thumbnail
			// image and a progress indicator).

			final File parentDirectory = pictureFile.getParentFile();
			FileObserver observer = new FileObserver(parentDirectory.getPath()) {
				// Protect against additional pending events after CLOSE_WRITE
				// is
				// handled.
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

	public static void copy(InputStream input, OutputStream output,int bufferSize) throws IOException {
		byte[] buf = new byte[bufferSize];
		int bytesRead = input.read(buf);
		while (bytesRead != -1) {
			output.write(buf, 0, bytesRead);
			bytesRead = input.read(buf);
		}
		output.flush();
	}
	
	private class RetrieveDudeTask extends AsyncTask<String, Void, List<DudeInformation>>{

		@Override
		protected List<DudeInformation> doInBackground(String... params) {
			return dudeService.findDudeInformation().getDudeInformation();
		}
		
	}
}