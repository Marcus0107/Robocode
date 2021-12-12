package aaaa_marcus;

import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.awt.Color;
//import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;
import static robocode.util.Utils.normalRelativeAngleDegrees;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * MyFirstNormalOne - a robot by (your name here)
 */
public class StroboCopV2 extends AdvancedRobot
{
	//Ivos Tracker
	int moveDirection=1;//which way to move
	String trackName;

	int others;
	static int corner = 0;
	boolean stopWhenSeeRobot = false;

	//Marcus Strobo
	public double oldEnemyHeading = 0.0;
	public ScannedRobotEvent currentTarget = null;
	public ArrayList<ScannedRobotEvent> scannedRobots = new ArrayList<ScannedRobotEvent>();

	public void run() {
		if(chooseStrategyForManyPeople()){
			runStrobo();

		} else {
			runTracker();
		}
	}

	public boolean chooseStrategyForManyPeople(){
		return true;
	}

	public void runTracker(){
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		turnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	public void runStrobo(){
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		do360Scan();
		while (chooseStrategyForManyPeople()) {

			do360Scan();
			setTurnRight(300);
			setMaxVelocity(5);
			ahead(300);
		}

		//Switch strategy to tracker
		run();
	}

	public boolean robotIsClose(ScannedRobotEvent e){
		return e.getDistance() < 50;
	}

	public boolean foundPotentialTarget(String name){
		return name != currentTarget.getName();
	}

	public boolean potentialTargetIsNotCloser(double distance){
		return distance > currentTarget.getDistance();
	}

	public void stroboScannedRobot(ScannedRobotEvent e){

		scannedRobots.add(e);

		if(currentTarget == null){
			currentTarget = e;
		}

		if(gunIsTurning() && robotIsClose(e)){
			fire(1);
		}

		do360Scan();

		if(foundPotentialTarget(e.getName()) && potentialTargetIsNotCloser(e.getDistance()))
		{
			return;
		}

		CircularTargeting(
			e,
			calculateBulletPower(
				e.getDistance()
			)
		);
	}

	public void turnGun(double amount){
		setTurnGunRightRadians(
			robocode.util.Utils.normalRelativeAngle(amount)
		);

	}
	public void trackerScannedRobot(ScannedRobotEvent e){
		double absBearing=e.getBearingRadians()+getHeadingRadians();//enemies absolute bearing
		double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);//enemies later velocity

		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
		if (Math.random()>.9){
			setMaxVelocity((12*Math.random())+12);//randomly change speed
		}
		if (e.getDistance() > 150) {
			turnGun(absBearing - getGunHeadingRadians() + latVel/22);
			setTurnRightRadians(
				robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity())
			);//drive towards the enemies predicted future location
		}
		else {
			turnGun(absBearing- getGunHeadingRadians()+latVel/15);
			setTurnLeft(-90-e.getBearing()); //turn perpendicular to the enemy
		}

		setAhead((e.getDistance() - 140)*moveDirection);
		setFire(3);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		setColors(
		   	nextRandomColor(),
		   	nextRandomColor(),
		    nextRandomColor(),
			brightGreen(),
			brightGreen()
		);

		if(chooseStrategyForManyPeople()){
			stroboScannedRobot(e);
		} else {
			trackerScannedRobot(e);
		}
	}

	public void stroboGotHit(HitByBulletEvent e){
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 80));
			setAhead(140*1);
	}

	public void trackerGotHit(HitByBulletEvent e){
		setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 80));
		setAhead(140*moveDirection);
	}

	public void onHitByBullet(HitByBulletEvent e){
		if(chooseStrategyForManyPeople()){
			stroboGotHit(e);
		} else {
			trackerGotHit(e);
		}
	}

	public void trackerCrashedIntoARobot(HitRobotEvent e){
		// Set the target
		trackName = e.getName();
		back(80);
	}

	public void onHitRobot(HitRobotEvent e) {
		if(chooseStrategyForManyPeople()){
			do360Scan();
			back(80);
		} else {
			trackerCrashedIntoARobot(e);
		}
	}

	public void onHitWall(HitWallEvent e){
		moveDirection=-moveDirection;//reverse direction upon hitting a wall
	}

	public boolean gunIsTurning(){
		return getGunTurnRemaining() != 0;
	}

	public int nextRandomInt(){
		int min;
		int max;

		if(chooseStrategyForManyPeople()){
			min = 125;
			max= 255;
		} else {
			min = 0;
			max = 124;
		}
		return ThreadLocalRandom.current().nextInt(0, 255 + 1);
	}

	public Color nextRandomColor(){
		return new Color(nextRandomInt(), nextRandomInt(), nextRandomInt());
	}

	public Color brightGreen() {
		return  new Color(0, 247, 0);
	}

	public double calculateBulletPower(double distance){
		double bulletPower = 2;
		if(distance >150){
			bulletPower = 1;
		}
		if(distance <50){
			bulletPower = 3;
		}
		return bulletPower;
	}

	public void debug(String m){
		System.out.println(m);
	}

	public void CircularTargeting(ScannedRobotEvent e, double bulletPower){

		double myX = getX();
		double myY = getY();

		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);

		double enemyHeading = e.getHeadingRadians();
		double enemyHeadingChange = enemyHeading - oldEnemyHeading;
		double enemyVelocity = e.getVelocity();
		oldEnemyHeading = enemyHeading;

		double 	deltaTime = 0;
		double 	battleFieldHeight = getBattleFieldHeight(),
		       	battleFieldWidth = getBattleFieldWidth();

		double 	predictedX = enemyX,
				predictedY = enemyY;

		while(
			(++deltaTime) * (20.0 - 3.0 * bulletPower)
			< Point2D.Double.distance(myX, myY, predictedX, predictedY)
		)
		{
			predictedX += Math.sin(enemyHeading) * enemyVelocity;
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			enemyHeading += enemyHeadingChange;

			if(	predictedX < 18.0
				|| predictedY < 18.0
				|| predictedX > battleFieldWidth - 18.0
				|| predictedY > battleFieldHeight - 18.0
			){
				predictedX = Math.min(Math.max(18.0, predictedX),
				    battleFieldWidth - 18.0);
				predictedY = Math.min(Math.max(18.0, predictedY),
				    battleFieldHeight - 18.0);
				break;
			}
		}

		double theta = Utils.normalAbsoluteAngle(
			Math.atan2(
		    	predictedX - getX(),
				predictedY - getY()
			)
		);

		setTurnRadarRightRadians(
			Utils.normalRelativeAngle(
		    	absoluteBearing - getRadarHeadingRadians()
			)
		);

		setTurnGunRightRadians(
			Utils.normalRelativeAngle(
		   		theta - getGunHeadingRadians()
			)
		);
		fire(2);
		//addCustomEvent(
	//		new GunTurnCompleteCondition(this)
	//	);
	}

	public void onCustomEvent(CustomEvent e) {
		if (e.getCondition().getName().equals("robocode.GunTurnCompleteCondition")) {
			fire(3);
			removeCustomEvent(e.getCondition());
		}

		if(e.getCondition().getName().equals("turnRadar")){
			removeCustomEvent(e.getCondition());
			findClosestEnemy();
		}
	}

	public void findClosestEnemy(){
		double minmalDistance = 10000;
		for (int i = 0; i < scannedRobots.size(); i++) {
			ScannedRobotEvent event = scannedRobots.get(i);
			if(event.getDistance() < minmalDistance)
			{
				currentTarget = event;
			}
		}
		scannedRobots.clear();
	}

	public void onRobotDeath(RobotDeathEvent e){
		if(currentTarget.getName() == e.getName()){
			currentTarget = null;
		}
	}


	public void do360Scan(){
		setTurnRadarRight(360);
		addCustomEvent(new Condition("turnRadar", 99) {
			public boolean test() {
				return getRadarTurnRemaining() == 0;
			}
		});

	}

	public void onDeath(DeathEvent e) {
		// Well, others should never be 0, but better safe than sorry.
		if (others == 0) {
			return;
		}

		// If 75% of the robots are still alive when we die, we'll switch corners.
		if ((others - getOthers()) / (double) others < .75) {
			corner += 90;
			if (corner == 270) {
				corner = -90;
			}
			out.println("I died and did poorly... switching corner to " + corner);
		} else {
			out.println("I died but did well.  I will still use corner " + corner);
		}
	}
}
