package com.example.androidproject;

import java.util.Timer;

import android.graphics.Bitmap;

/**
 * Class for each object visible 
 * @author Joel
 *
 */
public class GameObject {
	public String type;
	protected int wait = -1;
	protected int damage = 1;
	//Temporary
	protected int appletWidth;
	protected int appletHeight;
	protected int screenWidth;
	protected int screenHeight;
	protected int maxHealth;
	protected double xPos;
	protected double yPos;
	protected int lives = 1;
	protected int health = 1;
	protected int width;
	protected int height;
	protected int collisionWidth;
	protected int collisionHeight;
	protected int attackFrequency;
	protected int currentAttackCycle = 0;
	protected int collisionCooldown = 5;
	protected int tempColCooldown;
	protected int destroyDuration = 1;
	protected int blinkRefreshRate = 2;
	protected int currentDestruction = 0;
	private int blinkCycle = 0;
	protected boolean up, down, left, right;
	protected boolean collideable;
	protected boolean firing = false;
	protected boolean shotReady = false;
	protected boolean reloaded = false;
	protected boolean blinking;
	protected boolean visible = true;
	protected boolean destroyed = false;
	protected boolean usingImageResource = false;
	protected double acc;
	protected double xSpeed;
	protected double ySpeed;
	protected double xSpeedMax;
	protected double ySpeedMax;
	protected double friction = 1;
	protected double maxAcc;
	protected int imageResource;
	protected Bitmap objectImage;
	protected Timer tm;
	protected GameObject bullet;
	/**
	 * Constructor
	 * @param xPos_
	 * @param yPos_
	 * @param width_
	 * @param height_
	 */
	public GameObject(int xPos_, 
			int yPos_, 
			int width_, 
			int height_){
		xPos = xPos_;
		yPos = yPos_;
		width = width_;
		height = height_;
	}
	protected void init(){
		
	}
	/**
	 * Constructor
	 */
	public GameObject(){
		
	}
	/**
	 * Function for updating position and fire rate.
	 * This function also runs all other functions that are set individually for each object.
	 */
	public void refresh(){
		//Update fire cycle
		if (currentAttackCycle < attackFrequency){
			currentAttackCycle++;
			reloaded = false;
		}
		else if (currentAttackCycle >= attackFrequency){
			reloaded = true;
		}
		move();
		action();
		toggleBlinking();
		shoot();
		updateCollisionCooldown();
		checkDestruction();
	}
	/**
	 * Update speed and position
	 */
	protected void move(){
		xPos += xSpeed;
		yPos += ySpeed;
		xSpeed *= friction;
		ySpeed *= friction;
	}
	protected void wait(int time_){
		wait += time_;	
	}
	/**
	 * Function for individual actions for each object
	 */
	protected void action(){
	}
	/**
	 * Collision detection
	 * @param e
	 * @param b
	 * @return
	 */
	protected static boolean collide(GameObject e, GameObject b){
		if ((b.xPos + b.width/2+b.collisionWidth/2) < (e.xPos+e.width/2-e.collisionWidth/2)) {
			return false;
		}
		if (b.xPos+b.width/2-b.collisionWidth/2 > (e.xPos + e.width/2+e.collisionWidth/2)){
			return false;
		}
		if ((b.yPos + b.height/2+b.collisionHeight/2) < (e.yPos+e.height/2-e.collisionHeight/2)) {
			return false;
		}
		if (b.yPos + b.height/2-b.collisionHeight/2 > (e.yPos + e.height/2+e.collisionHeight/2)){
			return false;
		}
		return true; 
	}
	/**
	 * Method for handling the objects blinking (e.g. when taking damage)
	 */
	private void toggleBlinking(){
		//If blinking has been turned on and the unit is not collidable
		if (blinking && collideable == false){
			//Blinking rate
			if (blinkCycle <= blinkRefreshRate){
				blinkCycle++;
			}
			//Every time the blinking cycle resets
			else{
				//Reset blink cycle
				blinkCycle = 0;
				//Alternate between visible and not visible
				if (visible){
					visible = false;
				}
				else{
					visible = true;
				}
			}
		}
		//Fail safe method to make the unit visible
		else if(blinking && collideable == true){
			visible = true;
		}
	}
	/**
	 * Makes the object take damage
	 */
	protected void takeDamage(int damage_){
		if (health > 1){
			health -= damage_;
			collideable = false;
			blinking = true;
			//Reset the temporary collision cooldown
			tempColCooldown = 0;
			//Temp
			/*System.out.println("DAMAGE: " + getClass() 
					+ "\nDamage: " + damage 
					+ "\nHealth: " + health 
					+ "\nMax Health: " + maxHealth
					+ "\nLives: " + lives);
					*/
			
		}
		else{
			loseLife();
		}
	}
	/**
	 * Check if player is collidable yet.
	 */
	protected void updateCollisionCooldown(){
		if (tempColCooldown < collisionCooldown){
			tempColCooldown++;
		}
		else{
			collideable = true;
		}
	}
	/**
	 * Method for forcing the object to stop moving.
	 */
	protected void stopMoving(){
		up = false;
		down = false;
		left = false;
		right = false;
		shotReady = false;
	}
	/**
	 * Method for handling shooting
	 */
	protected void shoot(){
		if (shotReady && reloaded){
			firing = true;
			currentAttackCycle = 0;
			//System.out.println("FIRE: " + getClass());
		}
		else{
		}
	}
	/**
	 * Method for making the object lose a life and reset its health to max
	 */
	protected void loseLife(){
		//System.out.println(this.type + " LOST life");
		lives--;
		health = maxHealth;
	}
	/**
	 * Increment speed in X axis
	 * @param d
	 */
	protected void incrementXSpeed(double d){
		if(d > 0){
			if((xSpeed + maxAcc) <= xSpeedMax){
				if(d <= maxAcc){
					xSpeed += d;
				}
				else{
					xSpeed += maxAcc;
				}
			}
			else{
				xSpeed = xSpeedMax;
			}
		}
		else if(d < 0){
			if((xSpeed + maxAcc*1) >= xSpeedMax*-1){
				if(d >= maxAcc*-1){
					xSpeed += d;
				}
				else{
					xSpeed += maxAcc*-1;
				}
			}
			else{
				xSpeed = xSpeedMax*-1;
			}
		}
	}
	protected void incrementYSpeed(double d){
		if(d > 0){
			if((ySpeed + maxAcc) <= ySpeedMax){
				if(d <= maxAcc){
					ySpeed += d;
				}
				else{
					ySpeed +=maxAcc;
				}
			}
			else{
				ySpeed = ySpeedMax;
			}
		}
		else if(d < 0){
			if((ySpeed + maxAcc*1) >= ySpeedMax*-1){
				if(d >= maxAcc*-1){
					ySpeed += d;
				}
				else{
					ySpeed += maxAcc*-1;
				}
			}
			else{
				ySpeed = ySpeedMax*-1;
			}
		}
	}
	protected void checkDestruction(){
		if (destroyed){
			blinking = true;
			collideable = false;
			currentDestruction++;
			blinkRefreshRate = 1;
		}
	}
}
