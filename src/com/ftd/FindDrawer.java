package com.ftd;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

public class FindDrawer implements SurfaceHolder.Callback{
	
	private final FindView findView;	
	private SurfaceHolder surfaceHolder;
	private boolean paused;
	
	private RenderThread renderThread;
	
	private static final long FRAME_TIME_MILLIS = 33;
	private static final String TAG = "FindDrawer";
	
	public FindDrawer(Context context) {		
		Log.i(TAG, "Creating FindDrawer");
		this.findView = new FindView(context);
		this.findView.setListener(new FindView.FindViewListener() {			
			@Override
			public void onChange() {
				draw(findView);
			}
		});
	}
	
	private void draw(View view) {
		Canvas canvas;
        try {
        	Log.i(TAG, "Locking Canvas");
            canvas = surfaceHolder.lockCanvas();
            Log.i(TAG, "Canvas Locked");
        } catch (Exception e) {
        	Log.e(TAG, "Couldn't lock canvas");
            return;
        }
        if(canvas != null){
        	Log.i(TAG, "Drawing find view on canvas");
        	view.draw(canvas);
        	Log.i(TAG, "Find view drew on canves");
        	this.surfaceHolder.unlockCanvasAndPost(canvas);
        	Log.i(TAG, "Canvas unlocked");
        }
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        surfaceHolder = holder;
        updateRendering();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
        Log.d(TAG, "Surface changed");
		surfaceHolder = null;
        updateRendering();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        updateRendering();
	}
	
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        this.paused = paused;
        updateRendering();
    }
	
    /**
     * Start or stop rendering according to the timeline state.
     */
    private synchronized void updateRendering() {
        
    	boolean shouldRender = (surfaceHolder != null) && !paused;
        boolean rendering = renderThread != null;

        Log.d(TAG, "Shoudl Render:" + shouldRender + " Rendering: " + rendering);
        
        if (shouldRender != rendering) {
            if (shouldRender) {
            	Log.d(TAG, "Render Thread started");
                renderThread = new RenderThread();
                Log.d(TAG, "Start Rendering");
                renderThread.start();
            } else {
            	Log.d(TAG, "Quiting Renderer");
                renderThread.quit();
                renderThread = null;
            }
        }
    }
    
    public PictureHolder getPictureHolder(){
    	return this.findView.getPictureHolder();
    }
    
    /**
     * Redraws in the background.
     */
    private class RenderThread extends Thread {
        private boolean mShouldRun;

        /**
         * Initializes the background rendering thread.
         */
        public RenderThread() {
            mShouldRun = true;
        }

        /**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        /**
         * Requests that the rendering thread exit at the next opportunity.
         */
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
            	Log.d(TAG, "Running so render!");
                draw(findView);
                SystemClock.sleep(FRAME_TIME_MILLIS);
            }
        }
    }
}
