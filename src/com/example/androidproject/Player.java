package com.example.androidproject;



/**
 * Class containing all variables and controls for the player.
 * @author Joel
 *
 */
public class Player extends GameObject{
	/**
	 * TODO:
	 * - XP curve
	 * - Damage done cooldown
	 */
	//Boolean that controls whether or not the player is controllable
	protected boolean controllable = true;
	//Timer for respawn function
	private int respawnTimer = -1;
	/**
	 * Constructor
	 */
	public Player(int appletWidth_, int appletHeight_){
		screenWidth = appletWidth_;
		screenHeight = appletHeight_;
		//Test
		type = "ally";
		damage = 1;
		collideable = false;
		blinking = true;
		maxHealth = 3;
		health = maxHealth;
		lives = 2;
		width = 40;
		height = 40;
		attackFrequency = 15;
		currentAttackCycle = 0;
		yPos = screenHeight-height;
		xPos = screenWidth/2;
		xSpeed = 0;
		ySpeed = 0;
		xSpeedMax = 7;
		ySpeedMax = 7;
		maxAcc = 0.5;
		friction = 0.95;
		collisionCooldown = 180;
		collisionHeight = 20;
		collisionWidth = 30;
		tempColCooldown = 0;
		imageResource = R.drawable.test_image;
		usingImageResource = true;
	}
	@Override
	protected void action(){
		updateCollisionCooldown();
		checkWait();
		checkRespawn();
	}
	@Override
	protected void move(){
		//Decrease speed based on friction
		xSpeed *= friction;
		ySpeed *= friction;
		//Remove minimal speeds
		if (xSpeed < 0.2 && xSpeed > -0.2){
			xSpeed = 0;
		}
		if (ySpeed < 0.2 && ySpeed > -0.2){
			ySpeed = 0;
		}
		//Move player (only inside window)
		if ((xSpeed < 0 && (xPos + xSpeed) > 0)){
			xPos += xSpeed;
		}
		else if ((xSpeed < 0 && (xPos + xSpeed) <= 0)){
			xPos = 0;
			xSpeed = 0;
		}
		if (xSpeed > 0 && (xSpeed + xPos + width) < screenWidth){
			xPos += xSpeed;
		}
		else if(xSpeed > 0 && (xSpeed + xPos + width) >= screenWidth){
			xPos = screenWidth-width;
			xSpeed = 0;
		}
		if ((ySpeed < 0 && (yPos + ySpeed) > 0)){
			yPos += ySpeed;
		}
		else if ((ySpeed < 0 && (yPos + ySpeed) <= 0)){
			yPos = 0;
			ySpeed = 0;
		}
		if (ySpeed > 0 && (ySpeed + yPos + height) < screenHeight){
			yPos += ySpeed;
		}
		else if(ySpeed > 0 && (ySpeed + yPos + height) >= screenHeight){
			yPos = screenHeight-height;
			ySpeed = 0;
		}
	}
	/**
	 * Check if the wait-timer has expired
	 */
	private void checkWait(){
		if (wait == 0){
			//Make player able to control plane again
			controllable = true;
			//Player's plane is visible again
			visible = true;
			//Reset collision cooldown
			tempColCooldown = 0;
			wait = -1;
		}
		else if (wait > 0){
			wait--;
		}
		else{
			
		}
	}
	
	/**
	 * Overriden method for losing a life. The player's plane is "destroyed" and respawns at a certain location.
	 */
	@Override
	protected void loseLife(){
		//Time unit used for making the player wait as well as setting respawn timer
		int time_ = 240;
		wait(time_);
		lives--;
		health = maxHealth;
		//Remove player's control of the plane
		controllable = false;
		stopMoving();
		//Test
		visible = false;
		blinking = false;
		
		//Respawn player after a certain time
		respawnTimer = time_;
		
		//Set player y-position to middle of window
		ySpeed = 0;
		xSpeed = 0;
		yPos = screenHeight*0.9;
		xPos = screenWidth/2-width/2;
		System.out.println("Player life: " + lives);
	}
	
	/**
	 * Timer that handles the respawn function for the player.
	 * -1: Respawn function does nothing
	 *  0: Respawn player
	 * >1: Count down
	 */
	private void checkRespawn(){
		//If timer expires
		if (respawnTimer == 0){
			//Make player visible
			visible = true;
			//Make player invincible for a while after spawn
			collideable = false;
			tempColCooldown = 0;
			blinking = true;
			//Make player able to control plane again
			controllable = true;
			//Set timer to "pause-mode"
			respawnTimer = -1;

		}
		//Count down timer
		else if (respawnTimer > 0){
			visible = false;
			respawnTimer--;
		}
	}
}