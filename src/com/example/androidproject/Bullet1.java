package com.example.androidproject;

public class Bullet1 extends GameObject{
	
	public Bullet1(String type_) {
		init();
		type = type_;
	}
	protected void init(){
		damage = 1;
		friction = 1;
		health = 1;
		lives = 1;
		width = 15;
		height = 15;
		ySpeed = -10;
		collisionHeight = height;
		collisionWidth = width;
		collideable = true;
		imageResource = R.drawable.bullet;
		usingImageResource = true;
	}
	
}
