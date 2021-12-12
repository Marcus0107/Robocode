package aaaa_marcus;
import robocode.util.*;
import robocode.*;
import java.util.concurrent.ThreadLocalRandom;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Newton - a robot by (your name here)
 */
public class Target extends AdvancedRobot
{
	/**
	 * run: Newton's default behavior
	 */
	public static int x =  ThreadLocalRandom.current().nextInt(100, 800);
	public static int y =  ThreadLocalRandom.current().nextInt(100, 800);
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Robot main loop
		goTo(300,300);
		//setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if(getX() != x){
			goTo(200,200);
			setTurnRadarRightRadians(0);
			return;
		}
		// Replace the next line with any behavior you would like
		//fire(1);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
	//	back(10);
	}

	public void debug(String s)
	{
		System.out.println(s);
	}	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		//back(20);
	}	

	private void goTo(double x, double y) {
		/* Calculate the difference bettwen the current position and the target position. */
		x = x - getX();
		y = y - getY();
		
		/* Calculate the angle relative to the current heading. */
		double goAngle = Utils.normalRelativeAngle(Math.atan2(x, y) - getHeadingRadians());
		
		/*
		 * Apply a tangent to the turn this is a cheap way of achieving back to front turn angle as tangents period is PI.
		 * The output is very close to doing it correctly under most inputs. Applying the arctan will reverse the function
		 * back into a normal value, correcting the value. The arctan is not needed if code size is required, the error from
		 * tangent evening out over multiple turns.
		 */
		setTurnRightRadians(Math.atan(Math.tan(goAngle)));
		
		/* 
		 * The cosine call reduces the amount moved more the more perpendicular it is to the desired angle of travel. The
		 * hypot is a quick way of calculating the distance to move as it calculates the length of the given coordinates
		 * from 0.
		 */
		setAhead(Math.cos(goAngle) * Math.hypot(x, y));
	}
}
