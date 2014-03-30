package com.ftd;

import com.ftd.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class FindView extends FrameLayout {

	private ImageView pictureView;
	private TextView nameInformationView;
	private FindViewListener listener;

	private PictureHolder pictureHolder = new PictureHolder();
	
	public FindView(Context context) {
		this(context, null, 0);
	}

	public FindView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setListener(FindViewListener findViewListener){
		this.listener = findViewListener;
	}

	public FindView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater.from(context).inflate(R.layout.find_view, this);
		this.pictureView = (ImageView) findViewById(R.id.picture);
		this.nameInformationView = (TextView) findViewById(R.id.name_information);
	}
	
	public PictureHolder getPictureHolder(){
		return pictureHolder;
	}
	
	public interface FindViewListener{		
		public void onChange();
	}
}
