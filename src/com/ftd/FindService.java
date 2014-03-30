package com.ftd;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class FindService extends Service{

	private static final String LIVE_CARD_TAG = "found_the_dude";
	private static final String TAG = "FindService";
	
    private TimelineManager timelineManager;
	private LiveCard liveCard;
	private FindDrawer findDrawer;
	
    /**
     * Binder giving access to the underlying {@code Timer}.
     */
    public class FinderBinder extends Binder {
        public PictureHolder getPictureHolder() {
            return findDrawer.getPictureHolder();
        }
    }

    private final FinderBinder finderBinder = new FinderBinder();
	
    @Override
	public void onCreate() {
		super.onCreate();
		timelineManager = TimelineManager.from(this);
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {	
		if(liveCard == null){
		
			Log.d(TAG, "Creating Live Card");
			liveCard = timelineManager.createLiveCard(LIVE_CARD_TAG);
			Log.d(TAG, "Live Card Created");
			
			Log.d(TAG, "Creating Find Drawer");
			findDrawer = new FindDrawer(this);
			Log.d(TAG, "Find Drawer Created");
			
			Log.d(TAG, "Setting callback");
			liveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(findDrawer);
			Log.d(TAG, "Callback set");
			
			Log.d(TAG, "Creating Menu Intent");
			Intent menuIntent = new Intent(this, MenuActivity.class);
			Log.d(TAG, "Menu intent created");
		    menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		    liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
		    
		    Log.d(TAG, "Publishing Live Car in REVEAL Mode");
		    liveCard.publish(LiveCard.PublishMode.REVEAL);
		    Log.d(TAG, "Live Card Publised in REVEAL Mode");
		}else{
			
		}      
        return START_STICKY;
    }
	
    @Override
    public void onDestroy() {
        if (liveCard != null && liveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");
            if (findDrawer != null) {
                liveCard.getSurfaceHolder().removeCallback(findDrawer);
            }
            liveCard.unpublish();
            liveCard = null;
        }
        super.onDestroy();
    }

	@Override
	public IBinder onBind(Intent intent) {
		return finderBinder;
	}
}
	