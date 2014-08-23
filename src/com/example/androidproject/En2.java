package com.example.androidproject;

public class En2 extends GameObject{
	public En2(int appletWidth_, int appletHeight_){
		appletWidth = appletWidth_;
		appletHeight = appletHeight_;
		health = 5;
		ySpeed = 1.5;
		width = (int) (appletWidth * 0.15);
		height = (int) (width * 1.1);
		collisionHeight = height;
		collisionWidth = width;
		imageResource = R.drawable.enemy2;
		usingImageResource = true;
		collideable = true;
		destroyDuration = 50;
	}
}
