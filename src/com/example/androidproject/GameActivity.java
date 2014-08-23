package com.example.androidproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class GameActivity extends Activity {
	GameSession gs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Get display data
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		//Get display size
		display.getSize(size);
		//Save size
		int screenWidth = size.x;
		int screenHeight = size.y;
		//Lock rotation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//Disable screen auto lock
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//Initate new game session
		gs = new GameSession(this, screenWidth, screenHeight);
		//Set content view to game session
		setContentView(gs);
		if (gs.finished){
			finish();
		}
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
		 * [X] Remove auto-sleep
		 * [X] Dynamic background
		 * [X] Dynamic object size
		 * [X] Add starting background clouds
		 * [X] Dynamic collision, player/bullet/power-ups
		 * [X] Implement collision size
		 * [O] Add death (blinking) animation
		 * [ ] Change image of flipped enemies
		 * [ ] Sort enemy list
		 * [ ] Only check first enemy in level list
		 * 
		 * FIX (REQ)
		 * [X] Change enemy generator to generate enemy classes
		 * [X] Fix background circle array list
		 * [X] Create all images at beginning of game
		 * 
		 */
		//Declare all variables
		private int appletWidth, appletHeight, appletPadding, timeCounter;
		private boolean running = false;
		private boolean finished = false;
		private ArrayList<GameObject> allyList;
		private ArrayList<GameObject> enemyList;
		private ArrayList<GameObject> backgroundObjects;
		private ArrayList<ObjectImage> objectImages;
		@SuppressWarnings("rawtypes")
		private ArrayList<ArrayList> listCollection;
		private ArrayList<EnemyInfo> levelEnemies;
		private Player player;
		private Bitmap bitmap;
		private Paint paint = new Paint();
		private long timeMs;
		private int sleepTime;
		private int backgroundAmount;
		private Thread gameThread = null;
		private SurfaceHolder surfaceHolder;
		private Sensor sensor;
		private SensorManager sensorManager;
		private float origX, origY, gyroX, gyroY;
		private double minimumSteeringThreshold;
		private boolean gyroSet = false;
		private boolean scoreUpdated = false;
		private int endingTimer = 0;
		private int endingDuration = 400;
		private double fadeOutAlpha = 0;
		private int score = 0;
		private int playerBulletCount = 0;

		@SuppressWarnings("rawtypes")
		public GameSession(Context context, int appletWidth_, int appletHeight_){
			super(context);
			//Set surface holder
			surfaceHolder = getHolder();
			//Set thread boolean to running
			running = true;
			//Set thread sleep time (60 FPS)
			sleepTime = 1000/60;
			//Set game counter
			timeCounter = 0;
			//Set on touch listener
			setOnTouchListener(new View.OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					//Get touch position
					float touchX = event.getX();
					float touchY = event.getY();
					//-->				Test (check touch within certain position)
					if ((touchY < 50 && touchX < 50)){

					}
					//Triggers when user touches screen
					if(event.getAction() == MotionEvent.ACTION_DOWN && player.controllable){
						player.shotReady = true;
						return true;
					}
					//Triggers when user stops touching screen
					if(event.getAction() == MotionEvent.ACTION_UP){
						player.shotReady = false;
						return true;
					}
					//No change
					return false;
				}

			});

			//Set applet width
			appletWidth = appletWidth_;
			appletHeight = appletHeight_;
			//Set applet padding
			appletPadding = 10;
			//Initialize sensor manager
			sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			//Initialze sensor
			sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			//Register listener for sensor
			sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
			//Initialize enemy generator
			//enemyGen = new EnemyGenerator(appletHeight);
			//Set amount of maximum background objects
			backgroundAmount = 200;
			//Initialize game object lists
			backgroundObjects = new ArrayList<GameObject>();
			allyList = new ArrayList<GameObject>();
			enemyList = new ArrayList<GameObject>();
			listCollection = new ArrayList<ArrayList>();
			levelEnemies = new ArrayList<EnemyInfo>();
			//Initialize time in ms
			timeMs = System.currentTimeMillis() % 1000;
			listCollection.add(backgroundObjects);


			//Add enemy list to list collection
			listCollection.add(enemyList);
			//Add ally list to list collection
			listCollection.add(allyList);
			player = new Player(appletWidth, appletHeight);
			//Add player to ally list
			allyList.add(player);
			//Set threshold for when tilting the pad will update speed
			minimumSteeringThreshold = 0.5;
			//TEST
			initLevel(R.raw.level1);
			//Initiate all game images
			initGameImages();
			//Initate background
			initBackground();

		}
		/**
		 * Looping function when thread is currently running
		 */
		public void run(){
			//Run while boolean is true
			while(running){
				//Get current time in ms
				timeMs = System.currentTimeMillis() % 1000;
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
				//Calculate work time	
				int timeDiff = (int) ((System.currentTimeMillis() % 1000) - timeMs);
				//System.out.print("Diff: " + timeDiff + " Sleep: " + (sleepTime-timeDiff));
				//Only sleep if work time is less than maximum sleep time
				try{
					if (timeDiff > 0 && timeDiff <= sleepTime){
						//Refresh rate (sleep for X milliseconds)
						Thread.sleep(sleepTime - timeDiff);
						//System.out.println(" TOT: " + (sleepTime-timeDiff+timeDiff));
					}
				}catch (InterruptedException e) {
					e.printStackTrace();
				}


				surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
		/**
		 * Pause thread
		 */
		public void pause(){
			//Set run boolean
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
		/**
		 * Resume thread
		 */
		public void resume(){
			//Set run boolean
			running = true;
			//Reset thread
			gameThread = new Thread(this);
			//Start thread
			gameThread.start();
		}

		/**
		 * Method that updates all objects in the game.
		 */
		@SuppressWarnings("unchecked")
		public void update(){
			//Set new origin X and Y values for gyro steering
			if(!gyroSet){
				setNewOrigValues();
				gyroSet = true;
			}
			//Update background objects
			updateBackground();
			//Update player steering
			updatePlayerSteering();
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
					if (object.currentDestruction > object.destroyDuration){
						
						
						//Remove object from list
						list.remove(object);
						if (object.getClass().equals(Player.class.getClass())){
							//Player is removed, exit game
							System.out.println("REMOVED PLAYER");
							//EXIT GAME
						
						}
					}
					//Unit that are inside window do their actions
					else{
						//Refresh unit
						object.refresh();
						//Check if unit is firing
						if(object.firing){
							if (object.getClass().equals(Player.class.getClass())){
								playerBulletCount++;
							}
							Bullet1 b = new Bullet1(object.type, appletWidth, appletHeight);
							b.xPos = object.xPos+object.width/2-b.width/2;
							b.yPos = object.yPos+object.height-b.height;
							switch (object.type){
							//Object is player
							case "ally":
								allyList.add(b);
								break;
								//Object is an enemy
							case "enemy":
								enemyList.add(b);
								break;
							}
							
							
							object.firing = false;
						}
						//Object has no more health, destroy
						if(object.health <= 0){
							//Object is blinking (indicating destruction)
							object.destroyed = true;
						}
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
			if(player.lives < 0){
				allyList.remove(player);
			}
			//If player is dead, end game session
			else{

			}
			//Check if level has ended
			if(levelEnemies.size() == 0 && enemyList.size() == 0){
				
				
			}
			//Update time counter
			timeCounter++;
			//Check whether or not to add new enemies to game
			checkEnemyAddition();
			//Check if enemies are left to be added
			endingSequence();
		}
		/**
		 * Draw all objects in the game on the canvas
		 * @param canvas
		 */
		protected void drawObjects(Canvas canvas) {
			//Set background
			canvas.drawColor(Color.BLACK);
			//Set color for background objects
			paint.setColor(Color.WHITE);

			//iterate through each object list
			for (ArrayList<GameObject> list: listCollection){
				//Iterate through each object in list
				for (GameObject object: (ArrayList<GameObject>)list){
					if (object.visible){
						//Draw object image (if object is using an image resource)
						if(!(object instanceof BackgroundCircle)){
							//Check if object has declared an image
							if(object.objectImage == null){
								//iterate through all image resource to find the right one
								for (ObjectImage bm: objectImages){
									if(object.imageResource == bm.getResource()){
										bitmap = bm.getBitmap();
										object.objectImage = 
												Bitmap.createScaledBitmap(
														bitmap, object.width, 
														object.height, true);
									}

								}
							}
							//Draw the image on the canvas
							canvas.drawBitmap(
									object.objectImage, (float) object.xPos, 
									(float) object.yPos, paint);
						}else{
							paint.setAlpha(((BackgroundCircle) object).getAlpha());
							canvas.drawCircle((float)object.xPos, 
									(float)object.yPos, object.width, paint);
							paint.setAlpha(255);
						}
						
					}
				}
			}
			if(fadeOutAlpha > 0){
				paint.setColor(Color.BLACK);
				if (fadeOutAlpha <= 255){
					paint.setAlpha((int) fadeOutAlpha);
					
				}else{
					paint.setAlpha(255);
				}
				canvas.drawRect(0, 0, appletWidth, appletHeight, paint);
				if (fadeOutAlpha > 200){
					paint.setColor(Color.WHITE);
					canvas.drawText("Score: " + score, appletWidth/2, appletHeight/2, paint);
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
		 * Function for checking collision with other objects. 
		 * The type of collision check depends on the object.
		 * @param object_
		 */
		@SuppressWarnings("unchecked")
		private void collisionHandling(GameObject object_){
			switch (object_.type){
			//Object is player
			case "ally":
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
		 * Perform collision check between two objects
		 * @param ob1
		 * @param ob2
		 */
		private void collisionCheck(GameObject ob1, GameObject ob2){
			if (GameObject.collide(ob1, ob2)){
				if (ob1.collideable && ob2.collideable){
					ob1.takeDamage(ob2.damage);
					ob2.takeDamage(ob1.damage);
					//Increase score
					score += 100;
				}
			}
		}
		/** 
		 * Sets the steering based on the current gyroscope tilting
		 */
		private void updatePlayerSteering(){
			if (player.controllable){
				//Only trigger movement after tilting a certain degree from original value
				if ((origX +minimumSteeringThreshold < gyroX)
						|| (origX -minimumSteeringThreshold > gyroX)){
					player.incrementXSpeed(gyroX*-0.4);

				}
				//Only trigger movement after tilting a certain degree from original value
				if ((origY +minimumSteeringThreshold < gyroY)
						|| (origY -minimumSteeringThreshold > gyroY)){
					player.incrementYSpeed((origY*0.4-gyroY*0.4)*-1);
				}
			} 
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			gyroX = event.values[0];
			gyroY = event.values[1];
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
		private void initGameImages(){
			objectImages = new ArrayList<ObjectImage>();
			Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
			ObjectImage objectImage = new ObjectImage(bm, R.drawable.bullet);
			objectImages.add(objectImage);
			bm = BitmapFactory.decodeResource(getResources(), R.drawable.player);
			objectImage = new ObjectImage(bm, R.drawable.player);
			objectImages.add(objectImage);
			bm = BitmapFactory.decodeResource(getResources(), R.drawable.enemy1);
			objectImage = new ObjectImage(bm, R.drawable.enemy1);
			objectImages.add(objectImage);
			bm = BitmapFactory.decodeResource(getResources(), R.drawable.enemy2);
			objectImage = new ObjectImage(bm, R.drawable.enemy2);
			objectImages.add(objectImage);
		}
		/**
		 * Initiate background objects
		 */
		private void initBackground(){
			int amount = backgroundAmount/2 + (int)Math.random()*backgroundAmount;
			for (int i = 0; i < amount; i++){
				BackgroundCircle bc = getRandomBackgroundCircle();
				bc.setYPos((int)((bc.height*-1)+ Math.random()*(appletHeight+bc.height)));
				backgroundObjects.add(bc);
			}
		}
		/**
		 * Returns a random background circle
		 * @return
		 */
		private BackgroundCircle getRandomBackgroundCircle(){
			int xPos;
			int width;
			double speed;
			int alpha;
			width = (int) (1+(Math.random()*Math.random()*Math.random()*appletWidth*0.01));
			xPos = (int) ((width*-0.5)+(Math.random()*(appletWidth+width*0.5)));
			speed = (Math.random()*width*0.2);
			alpha = (int) (100+(155/speed));
			BackgroundCircle bc = new BackgroundCircle(xPos, width, speed, alpha);
			return bc;
		}
		/**
		 * Check amount of background objects and add new ones if necessary
		 */
		private void updateBackground(){
			if(backgroundObjects.size() < backgroundAmount*0.5){
				BackgroundCircle bc = getRandomBackgroundCircle();
				backgroundObjects.add(bc);
			}
		}
		/**
		 * Loads all enemies used for the level from a file.
		 * @param resource the file resource used
		 */
		private void initLevel(int resource){
			//Initalize input stream to read enemy list file
			InputStream is = getResources().openRawResource(resource);
			//String to store each lines
			String line;
			//Class to store info of enemy
			EnemyInfo enemyInfo = new EnemyInfo();
			int enemyDistanceSpread = 0;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try{ 
				while((line = br.readLine()) != null){
					//Split the line to an array containing the enemy info
					String[] structuredInfo = line.split("#");
					double currentXPos = 0;
					int currentSpread = 0;
					//Check the amount of enemies added in the group
					int groupAmount = Integer.parseInt(structuredInfo[0]);
					//Set current x position
					currentXPos = (double)(appletWidth*((Double.parseDouble(structuredInfo[4])/100)));
					//Set spread between enemies
					enemyDistanceSpread = (int) (appletWidth*((Double.parseDouble(structuredInfo[5])/100)));
					//iterate and add enemies equal to the group amount
					for (int i = 0; i < groupAmount; i++){
						enemyInfo = new EnemyInfo();
						//Declare time
						enemyInfo.time = Integer.parseInt(structuredInfo[1]);
						//Declare enemy type
						switch (structuredInfo[2]){
							case "en1":
								enemyInfo.enemy = new En1(appletWidth, appletHeight);
								break;
							case "en2":
								enemyInfo.enemy = new En2(appletWidth, appletHeight);
								break;
						}
						//Check if enemy comes from top or bottom
						if(structuredInfo[3].equals("t")){
							enemyInfo.flipped = true;
						}else{
							enemyInfo.flipped = false;
						}
						//Fix position of first enemy in each group
						if (i == 0){
							currentXPos -= enemyInfo.enemy.width/2;
						}
						//Set x pos
						enemyInfo.enemy.xPos = currentXPos;
						//Increase spread
						currentSpread += enemyDistanceSpread;
						if (i%2 != 0){
							currentXPos -= currentSpread;
						}else{
							currentXPos += currentSpread;
						}
						//Set x speed
						enemyInfo.enemy.xSpeed = Double.parseDouble(structuredInfo[6]);
						//Add to enemy level list
						levelEnemies.add(enemyInfo);
					}
				}
				br.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		/**
		 * Check the list of enemies to be added.
		 */
		@SuppressWarnings("unchecked")
		private void checkEnemyAddition(){
			for (EnemyInfo enemyInfo: (ArrayList<EnemyInfo>) levelEnemies.clone()){
				if(enemyInfo.time == timeCounter){
					//enemyInfo.enemy.init();
					//Set enemy parameters
					if (enemyInfo.flipped){
						enemyInfo.enemy.ySpeed *= -1;
						enemyInfo.enemy.yPos = appletHeight+enemyInfo.enemy.height;
						enemyInfo.enemy.xSpeed *= -1;
					}else{
						enemyInfo.enemy.yPos = enemyInfo.enemy.height*-1;
					}
					//Add enemy to game
					enemyList.add(enemyInfo.enemy);
					enemyInfo.enemy.init();
					levelEnemies.remove(enemyInfo);
				}
			}
		}
		private void endingSequence(){
			if((levelEnemies.size() == 0 && enemyList.size() == 0) || (!allyList.contains(player))){
				endingTimer++;
				if (scoreUpdated = false){
					score = calculateScore();
					scoreUpdated = true;
				}
				if (endingTimer > 150 && fadeOutAlpha <= 255){
					fadeOutAlpha += 2;
				}
				if(endingTimer > endingDuration){
					//Exit game
					finish();
				}
				
			}
		}
		private int calculateScore(){
			int healthScore = player.health * 150;
			int lifeScore = player.lives * 200;
			int bulletScore = playerBulletCount * -2;
			int finalScore = score + healthScore + lifeScore + bulletScore;
			if(finalScore < 0){
				finalScore = 0;
			}
			return finalScore;
		}
	}
	/**
	 * Class containing an enemy and a variable for when 
	 * the enemy is to be added to the game session.
	 * @author Joel
	 *
	 */
	private class EnemyInfo{
		GameObject enemy;
		boolean flipped = false;
		int time;
		public EnemyInfo(){
		}
	}
	
}

