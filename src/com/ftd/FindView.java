package com.ftd;

import com.ftf.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class FindView extends FrameLayout{

	private ImageView pictureView;
	private TextView nameInformationView;
	
	public FindView(Context context) {
		this(context, null, 0);
	}


	public FindView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public FindView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater.from(context).inflate(R.layout.find_view, this);
		
		this.pictureView = (ImageView) findViewById(R.id.picture);
		this.nameInformationView = (TextView) findViewById(R.id.name_information);			
	}	

}
