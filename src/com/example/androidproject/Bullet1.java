package com.example.androidproject;

public class Bullet1 extends GameObject{
	
	public Bullet1(String type_, int appletWidth_, int appletHeight_) {
		damage = 1;
		friction = 1;
		health = 1;
		lives = 1;
		screenWidth = appletWidth_;
		screenHeight = appletHeight_;
		width = (int) (screenWidth * 0.03);
		height = width;
		ySpeed = -10;
		collisionHeight = height;
		collisionWidth = width;
		collideable = true;
		imageResource = R.drawable.bullet;
		usingImageResource = true;
		type = type_;
		
	}
	
}
