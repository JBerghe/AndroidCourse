package com.example.androidproject;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.widget.TextView;

public class GameActivity extends Activity {
	private int appletWidth, appletHeight, appletPadding;
	private boolean run;
	private ArrayList<GameObject> allyList;
	private ArrayList<GameObject> enemyList;
	private ArrayList<ArrayList> listCollection;
	private Player player;
	private EnemyGenerator enemyGen;
	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		run = true;
		//Get display data
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		appletWidth = size.x;
		appletHeight = size.y;
		appletPadding = 10;
		//Initialize enemy generator
		enemyGen = new EnemyGenerator();
		//inititalize game object lists
		allyList = new ArrayList<GameObject>();
		enemyList = new ArrayList<GameObject>();
		listCollection = new ArrayList<ArrayList>();
		//Test
		tv = (TextView) findViewById(R.id.textView1);
		tv.setText("asdsa " + appletWidth + " " + appletHeight);
	}
	/**
	 * Method that updates all objects in the game and controls the refresh rate of the game.
	 */
	@SuppressWarnings("unchecked")
	public void update(){
		try {
			//Iterate through all lists of objects
			for (ArrayList<GameObject> list: listCollection){
				//Iterate through each object in the list
				for (GameObject object: (ArrayList<GameObject>)list.clone()){
					//Check position of current object and delete if outside game area
					if (((object.xPos+object.width) < (-1*(appletPadding + object.width))) || 
							((object.xPos) > (appletWidth + appletPadding + object.width))){
						//Remove all life for object (thus destroying it)
						object.health = 0;
						object.lives = 0;
					}
					//Health handling
					if (object.health <= 0){
						list.remove(object);
						//Test
						createFlippedEnemy(enemyType.enemy1, 200);
						System.out.println("DESTROYED: " + object.getClass().toString());
					}
					//Unit that are inside window do their actions
					else{
						//Check collision with other objects
						if (object.collideable){
							collisionHandling(object);
						}
						//Refresh unit
						object.refresh();
					}
				}
			}
			//Refresh rate (sleep for X milliseconds)
			Thread.sleep(15);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Function for checking collision with other objects. The type of collision check depends on the object.
	 * @param object_
	 */
	private void collisionHandling(GameObject object_){
		switch (object_.type){
		//Object is player
		
		case "player":
			for (GameObject enemy_: (ArrayList<GameObject>)enemyList.clone()){
				collisionCheck (enemy_, object_);
			}
			break;
		//Object is an enemy
		case "enemy":
			for (GameObject ally_: (ArrayList<GameObject>)allyList.clone()){
				collisionCheck (ally_, object_);
			}
			break;
		}
	}
	/**
	 * Method for adding a new enemy to the game.
	 * @param id_
	 * @param yPos_
	 */
	private void createEnemy(enemyType id_, int yPos_){
		enemyList.add(enemyGen.getEnemy(id_, yPos_));
	}
	@SuppressWarnings("unused")
	private void createFlippedEnemy(enemyType id_, int yPos_){
		enemyList.add(enemyGen.getFlippedEnemy(id_, yPos_));
	}
	/**
	 * Perform collision check between two objects
	 * @param ob1
	 * @param ob2
	 */
	private void collisionCheck(GameObject ob1, GameObject ob2){
		if (GameObject.collide(ob1, ob2)){
			System.out.println(ob1.collideable + " " + ob2.tempColCooldown + ob2.collideable);
			if (ob1.collideable == true && ob2.collideable == true){
				ob1.takeDamage(ob2.damage);
				ob2.takeDamage(ob1.damage);
			}
		}
	}
}
