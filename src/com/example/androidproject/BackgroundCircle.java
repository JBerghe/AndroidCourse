package com.example.androidproject;


public class BackgroundCircle extends GameObject{
	private int alpha;
	public BackgroundCircle(int xPos_, int width_, double ySpeed_, int alpha_){
		type = "background";
		xPos = xPos_;
		width = width_;
		height = width;
		ySpeed = ySpeed_;
		yPos = height*-1;
		visible = true;
		usingImageResource = false;
		alpha = alpha_;
		checkAlpha();
	}
	public int getAlpha(){
		return alpha;
	}
	public void setAlpha(int alpha_){
		alpha = alpha_;
		checkAlpha();
	}
	public void setYPos(int yPos_){
		yPos = yPos_;
	}
	private void checkAlpha(){
		if (alpha <= 0){
			alpha = 100;
		}else if(alpha > 255){
			alpha = 255;
		}
	}
}
