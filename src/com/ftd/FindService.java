package com.ftd;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class FindService extends Service{

	private static final String LIVE_CARD_TAG = "found_the_dude";
	private static final String TAG = "FindService";
	
    private TimelineManager timelineManager;
	private LiveCard liveCard;
	
	
    @Override
	public void onCreate() {
		super.onCreate();
		timelineManager = TimelineManager.from(this);
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	liveCard = timelineManager.createLiveCard(LIVE_CARD_TAG);
        Intent menuIntent = new Intent(this, MenuActivity.class);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
        liveCard.publish(PublishMode.REVEAL);
        return START_STICKY;
    }
	
    @Override
    public void onDestroy() {
        if (liveCard != null && liveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");
            liveCard.unpublish();
            liveCard = null;
        }
        super.onDestroy();
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
	