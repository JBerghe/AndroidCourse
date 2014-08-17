package com.example.androidproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ObjectImage{
	private Bitmap bitmap;
	private int resource;
	public ObjectImage(Bitmap bitmap_, int resource_){
		bitmap = bitmap_;
		resource = resource_;
	}
	public void setBitmap(Bitmap bitmap_){
		bitmap = bitmap_;
	}
	public void setResource(int resource_){
		resource = resource_;
	}
	public Bitmap getBitmap(){
		return bitmap;
	}
	public int getResource(){
		return resource;
	}
}
