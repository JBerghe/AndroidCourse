package com.example.androidproject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class GameActivity extends Activity {
	GameSession gs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Get display data
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		//Get width
		int appletWidth = size.x;
		int appletHeight = size.y;
		//Lock rotation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//Disable screen auto lock
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//Initate new game session
		gs = new GameSession(this, appletWidth, appletHeight);
		//Set content view to game session
		setContentView(gs);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		gs.resume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		gs.pause();
	}
	public class GameSession extends SurfaceView implements Runnable, SensorEventListener{
		/**
		 * TODO:
		 * [?] Separate thread for drawing images (Fixed?)
		 * [X] Remove auto-sleep 
		 * [ ] Controls
		 * [ ] Dynamic background
		 * [ ] Dynamic collision, player/bullet/power-ups
		 */
		private int appletWidth, appletHeight, appletPadding;
		private boolean running = false;
		private ArrayList<GameObject> allyList;
		private ArrayList<GameObject> enemyList;
		@SuppressWarnings("rawtypes")
		private ArrayList<ArrayList> listCollection;
		private Player player;
		private EnemyGenerator enemyGen;
		private Bitmap b;
		private Paint paint = new Paint();
		private long timeMs;
		private int sleepTime;
		private Thread gameThread = null;
		private SurfaceHolder surfaceHolder;
		private Sensor sensor;
		private SensorManager sensorManager;
		private float origX, origY, gyroX, gyroY;
		private double updateThreshold;
		private boolean gyroSet = false;
		
		@SuppressWarnings("rawtypes")
		public GameSession(Context context, int appletWidth_, int appletHeight_){
			super(context);
			//Set surface holder
			surfaceHolder = getHolder();
			//Set thread boolean to running
			running = true;
			//Set thread sleep time
			sleepTime = 5;
			//Set applet width
			appletWidth = appletWidth_;
			appletHeight = appletHeight_;
			appletPadding = 10;
			//Initialize sensor manager
			sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			//Initialze sensor
			sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			//Register listener for sensor
			sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
			//Initialize enemy generator
			enemyGen = new EnemyGenerator(appletWidth, appletHeight);
			//Initialize game object lists
			allyList = new ArrayList<GameObject>();
			enemyList = new ArrayList<GameObject>();
			listCollection = new ArrayList<ArrayList>();
			//Initialize time in ms
			timeMs = System.currentTimeMillis() % 1000;
			b = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
			listCollection.add(enemyList);
			listCollection.add(allyList);
			player = new Player(appletWidth, appletHeight);
			player.yPos = appletHeight-player.height;
			player.xPos = appletWidth/2;
			allyList.add(player);
			//Set threshold for when tilting the pad will update speed
			updateThreshold = 0.5;
			createEnemy(enemyType.enemy1, 100);
			
		}
		public void run(){
			//Run while boolean is true
			while(running){
				//Check validity of the holder
				if (!surfaceHolder.getSurface().isValid()){
					continue;
				}
				//Update objects
				update();
				//Lock the current canvas
				Canvas canvas = surfaceHolder.lockCanvas();
				//Paint background
				drawObjects(canvas);
				surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
		public void pause(){
			//Set boolean
			running = false;
			//Wait until the thread is closed
			while(true){
				try{
					//Block current thread until execution is finished
					gameThread.join();
				}catch (InterruptedException e){
					e.printStackTrace();
				}
				//Break loop
				break;
			}
			//Nullify thread
			gameThread = null;
		}
		public void resume(){
			running = true;
			//Reset thread
			gameThread = new Thread(this);
			//Start thread
			gameThread.start();
		}
		
		/**
		 * Method that updates all objects in the game and controls the refresh rate of the game.
		 */
		@SuppressWarnings("unchecked")
		public void update(){
			try {
				//Set new origin X and Y values for gyro steering
				if(!gyroSet){
					setNewOrigValues();
					gyroSet = true;
				}
				//Update player steering
				updatePlayerSteering();
				//Get current time in ms
				timeMs = System.currentTimeMillis() % 1000;
				//Iterate through all lists of objects
				for (ArrayList<GameObject> list: listCollection){
					//Iterate through each object in the list
					for (GameObject object: (ArrayList<GameObject>)list.clone()){
						//Check position of current object and delete if outside game area
						if (((object.yPos+object.height) < (-1*(appletPadding + object.height))) || 
								((object.yPos) > (appletHeight + appletPadding + object.height))){
							//Remove all life for object (thus destroying it)
							object.health = -1;
							object.lives = -1;
						}
						//Health handling
						if (object.health <= 0){
							if (object.type.equals(player)){
								System.out.println("REMOVED PLAYER");
								allyList.remove(player);
							}
							list.remove(object);
							//Test
							createEnemy(enemyType.enemy1, 400);
							System.out.println("DESTROYED: " + object.getClass().toString());
						}
						//Unit that are inside window do their actions
						else{
							//Refresh unit
							object.refresh();
						}
					}
				}
				//Collision detection (in ally list)
				for (GameObject object: (ArrayList<GameObject>)allyList.clone()){
					if (object.collideable){
						//System.out.println(object.type);
						collisionHandling(object);
					}
				}
				//Check player lives
				if(player.lives <= -1){
					allyList.remove(player);
				}
				//If player is dead, end game session
				else{
					
				}
				//Calculate work time	
				int timeDiff = (int) ((System.currentTimeMillis() % 1000) - timeMs);
				//System.out.println("Diff: " + timeDiff + " Sleep: " + (sleepTime-timeDiff));
				
				if (timeDiff > 0 && timeDiff <= sleepTime){
					//Refresh rate (sleep for X milliseconds)
					Thread.sleep(sleepTime - timeDiff);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		protected void drawObjects(Canvas canvas) {
			//Set background
			canvas.drawColor(Color.CYAN);
			for (ArrayList<GameObject> list: listCollection){
				if (list.size() > 0){
					for (GameObject object: list){
						if (object.visible){
							Bitmap scaledImage = Bitmap.createScaledBitmap(b, object.width, object.height, true);
							canvas.drawBitmap(scaledImage, (float) object.xPos, (float) object.yPos, paint);
						}
					}
				}
			}
		}
		/**
		 * Update the origin values for steering
		 */
		private void setNewOrigValues(){
			origY = gyroY;
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
		private void createEnemy(enemyType id_, int xPos_){
			enemyList.add(enemyGen.getEnemy(id_, xPos_));
		}
		@SuppressWarnings("unused")
		private void createFlippedEnemy(enemyType id_, int xPos_){
			enemyList.add(enemyGen.getFlippedEnemy(id_, xPos_));
		}
		/**
		 * Perform collision check between two objects
		 * @param ob1
		 * @param ob2
		 */
		private void collisionCheck(GameObject ob1, GameObject ob2){
			if (GameObject.collide(ob1, ob2)){
				//System.out.println(ob1.collideable + " " + ob2.tempColCooldown + ob2.collideable);
				if (ob1.collideable){
					ob1.takeDamage(ob2.damage);
				}
				if (ob2.collideable){
					ob2.takeDamage(ob1.damage);
				}
			}
		}
		/**
		 * Sets the steering based on the current gyroscope tilting
		 * 
		 */
		private void updatePlayerSteering(){
			if (player.controllable){
				if ((origX +updateThreshold < gyroX)
						|| (origX -updateThreshold > gyroX)){
					player.incrementXSpeed(gyroX*-0.4);
					
				}
				if ((origY +updateThreshold < gyroY)
						|| (origY -updateThreshold > gyroY)){
					player.incrementYSpeed((origY*0.4-gyroY*0.4)*-1);
					//System.out.println("OrigY: " + origY + "Gyro: " + gyroY);
				}
			}
			
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			gyroX = event.values[0];
			gyroY = event.values[1];
			//System.out.println("X: " + gyroX + " Y: " + gyroY + "OrigY: " + origY);
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	}
}
	
 