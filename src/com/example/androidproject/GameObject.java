package com.example.androidproject;

/**
 * Class for each object visible 
 * @author Joel
 *
 */
public class GameObject {
	public String type;
	protected int wait = -1;
	protected int damage;
	//Temporary
	protected int appletWidth = 1280;
	protected int appletHeight = 768;
	protected int maxHealth;
	protected double xPos;
	protected double yPos;
	protected int lives;
	protected int health;
	protected int width;
	protected int height;
	protected int attackCycle;
	protected int currentAttackCycle;
	protected int collisionCooldown;
	protected int tempColCooldown;
	private int blinkCycle = 0;
	protected boolean up, down, left, right;
	protected boolean collideable;
	protected boolean firing;
	protected boolean shotReady = false;
	protected boolean reloaded;
	protected boolean blinking;
	protected boolean visible = true;
	protected double acc;
	protected double xSpeed;
	protected double ySpeed;
	protected double xSpeedMax;
	protected double ySpeedMax;
	protected double friction;
	/**
	 * Constructor
	 * @param xPos_
	 * @param yPos_
	 * @param width_
	 * @param height_
	 */
	public GameObject(int xPos_, int yPos_, int width_, int height_){
		xPos = xPos_;
		yPos = yPos_;
		width = width_;
		height = height_;
	}
	public void init(){
		
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
		currentAttackCycle++;
		if (currentAttackCycle >= attackCycle){
			shotReady = true;
			reloaded = true;
			currentAttackCycle = 0;
		}
		else{
			shotReady = false;
		}
		move();
		action();
		toggleBlinking();
		shoot();
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
		if ((b.xPos + b.width) < (e.xPos)) 
			return false; 
		if (b.xPos > (e.xPos + e.width)) 
			return false; 
		if ((b.yPos + b.height) < (e.yPos)) 
			return false; 
		if (b.yPos > (e.yPos + e.height)) 
			return false;
		return true; 
	}
	/**
	 * Method for handling the objects blinking (when e.g. taking damage)
	 */
	private void toggleBlinking(){
		//If blinking has been turned on and the unit is not collidable
		if (blinking && collideable == false){
			//Blinking rate
			if (blinkCycle <= 5){
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
			System.out.println("DAMAGE: " + getClass() 
					+ "\nDamage: " + damage 
					+ "\nHealth: " + health 
					+ "\nMax Health: " + maxHealth
					+ "\nLives: " + lives);
		}
		else{
			loseLife();
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
	}
	/**
	 * Method for handling shooting
	 */
	protected void shoot(){
//		System.out.println(getClass().toString());
		if (shotReady == true && firing == true){
			//Temp
			System.out.println("FIRE: " + getClass());
		}
		else{
		}
	}
	/**
	 * Method for making the object lose a life and reset its health to max
	 */
	protected void loseLife(){
		lives--;
		health = maxHealth;
	}
}
