package aaaa_marcus;
import java.util.Dictionary;
import java.util.HashMap;

import javax.swing.text.Utilities;

import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Newton - a robot by (your name here)
 */
class Location{
	public double X;
	public double Y;

	public Location(double x, double y){
		X = x;
		Y = y;
	}

	public String toString(){
		return "x: " + X + " Y: " + Y;
	}
}

class Enemy {
	public double Bearing;
	public double BearingInRadians;
	public double Distance;
	public double Energy;
	public double Heading;
	public double HeadingInRadians;
	public String Name;
	public double Velocity;
	public Point2D.Double Location;

	public Enemy(ScannedRobotEvent s, AdvancedRobot r){
		Bearing = s.getBearing();
		BearingInRadians = s.getBearingRadians();
		Distance = s.getDistance();
		Energy = s.getEnergy();
		Heading = s.getHeading();
		HeadingInRadians = s.getHeadingRadians();
		Name = s.getName();
		Velocity = s.getVelocity();
		Location = getEnemyLocation(s,r); 
	}

	public String toString(){
		return "Name: " + this.Name + " X: "+ this.Location.x + " Y " + this.Location.y;
	}

		
	private  Point2D.Double getEnemyLocation(ScannedRobotEvent e, AdvancedRobot r){

        // Calculate the angle to the scanned robot
        double absBearing = e.getBearingRadians() + r.getHeadingRadians();

        // Calculate the coordinates of the robot
        double enemyX = (r.getX() + Math.sin(absBearing) * e.getDistance());
        double enemyY = (r.getY() + Math.cos(absBearing) * e.getDistance());

		return new Point2D.Double(enemyX,enemyY);
	}
}

public class Newton extends AdvancedRobot
{


    private RobotStatus robotStatus;

	public void onStatus(StatusEvent e) {
        this.robotStatus = e.getStatus();
    }    
	/**
	 * run: Newton's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Robot main loop
		goTo(200,200);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	public double gForce(Enemy e){
		return 1;
	}

	public double gravitationalPushToX(Enemy e){		
		return Math.sin(enemyAbsoluteBearing(e)) / Math.pow(e.Location.distance(getX(), getY()), 2) ;
	}

	public double gravitationalPushToY(Enemy e){
		return Math.cos(enemyAbsoluteBearing(e)) / Math.pow(e.Location.distance(getX(), getY()), 2) ;
	}

	public double enemyAbsoluteBearing(Enemy e){
		double xVector = e.Location.x - getX();
		double yVector =  e.Location.y- getY();

		return Utils.normalAbsoluteAngle(
			Math.atan2(xVector , yVector)
		);
	}

	private boolean reachedDestination = false;
	/**
	 * onScannedRobot: What to do when you see another robot
	 */

	HashMap<String, Enemy> map = new HashMap<String, Enemy>();
	public void onScannedRobot(ScannedRobotEvent e) {
		map.put(
			e.getName(),
			new Enemy(e,this)
		);

		var angle = getAverageBearing();
		debug("Angle of enemy "+ e.getName() + " is " + angle);

		reverseGravityMovement(angle);
	}



	public void reverseGravityMovement(double angle){
		if (fx == 0 && fy == 0) {
			// If no force, do nothing
		} else if(Math.abs(angle-getHeadingRadians())<Math.PI/2){
			setTurnRightRadians(Utils.normalRelativeAngle(angle-getHeadingRadians()));
			setAhead(Double.POSITIVE_INFINITY);
		} else {
			setTurnRightRadians(Utils.normalRelativeAngle(angle+Math.PI-getHeadingRadians()));
			setAhead(Double.NEGATIVE_INFINITY);
		}
	}

	double fx = 0;
	double fy = 0;
	public double getAverageBearing(){
		map.forEach(
			(k,v)-> {
				fx -= gravitationalPushToX(v);
				fy -= gravitationalPushToY(v);
			}
		);
 
        return Math.atan2(fx, fy);
	}




	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		//	back(10);
	}

	public void debug(String s)	{
		System.out.println(s);
	}	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		//back(20);
	}	


	public void goTo(double x, double y) {
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
