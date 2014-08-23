package com.example.androidproject;


public class En1 extends GameObject{
	public En1(int appletWidth_, int appletHeight_){
		appletWidth = appletWidth_;
		appletHeight = appletHeight_;
		health = 2;
		ySpeed = 2.5;
		width = (int) (appletWidth * 0.07);
		height = width;
		collisionHeight = height;
		collisionWidth = width;
		imageResource = R.drawable.enemy1;
		usingImageResource = true;
		collideable = true;
		destroyDuration = 25;
	}
}
