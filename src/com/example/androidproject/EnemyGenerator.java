package com.example.androidproject;


/**
 * Class for enemies. The class itself acts as a generator, the class will return enemies whenever it's called.
 * @author Joel
 */
public class EnemyGenerator extends GameObject{
	//Placeholder for each enemy
	private GameObject enemy_;
	
	
	private class en1 extends GameObject{
		private en1(){
			shotReady = true;
			damage = 1;
			firing = true;
			currentAttackCycle = 0;
			attackCycle = 100;
			friction = 1;
			xSpeed = -3;
			health = 1;
			width = 70;
			height = 40;
		}
	}
	
	/**
	 * Method that will return a desired enemy at the given Y position.
	 * @param id_
	 * @param xPos_
	 * @return
	 */
	public GameObject getEnemy(enemyType id_, int xPos_){
		setInitValues(id_);
		enemy_.xPos = xPos_;
		
		return (GameObject) enemy_;
	}
	/**
	 * Method that will return a desired enemy at the given Y position. If the boolean is set to true,
	 * the enemy will instead spawn at the left side of the screen and go to the right.
	 * @param id_
	 * @param yPos_
	 * @param flipped_
	 * @return
	 */
	public GameObject getFlippedEnemy(enemyType id_, int yPos_){
		enemy_ = getEnemy (id_, yPos_);
		enemy_.ySpeed *= -1;
		enemy_.yPos = enemy_.height*-1;
		return (GameObject) enemy_;
	}
	/**
	 * Initiating method that chooses the type of enemy that will be generated based on the input.
	 * @param id_
	 */
	private void setInitValues(enemyType id_){
		/*
		 * This switch case will determine based on the id_ which type of enemy will be generated.
		 */
		switch (id_){
			case enemy1:
				enemy_ = new en1();
				break;
		default:
			break;
			
		}
		enemy_.type = "enemy";
		//Enemy will be collidable at start
		enemy_.collideable = true;
		//Enemy will start from the right
		enemy_.xPos = appletWidth;
	}
}
