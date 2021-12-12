package md;
import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * MyFirstNormalOne - a robot by (your name here)
 */
public class MyFirstNormalOne extends AdvancedRobot
{
	/**
	 * run: MyFirstNormalOne's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar
		//setAdjustRadarForGunTurn(true);
		//setAdjustGunForRobotTurn(true);
		//setAdjustRadarForRobotTurn(true);
		
		//setTurnGunRightRadians(Double.POSITIVE_INFINITY);
		//setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		// Robot main loop
		while(true) {
		setAhead(Double.POSITIVE_INFINITY);
		setTurnRight(100);
		setTurnLeft(100);
		execute();
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public double oldEnemyHeading = 0.0;
	public void onScannedRobot(ScannedRobotEvent e) {	
	
		System.out.println(e.getName());
		double bulletPower = Math.min(3.0,getEnergy());
		double myX = getX();
		double myY = getY();
		
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
		
		var kartDistance = Math.pow(myX - enemyX ,2)+ Math.pow( myY-enemyY,2);

		double distance = Math.sqrt(kartDistance);
		
		if(distance < 30){
			bulletPower=	bulletPower*2;
		}
	
		
		

		//CircularTargeting(e,bulletPower );
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
		
		double deltaTime = 0;
		double battleFieldHeight = getBattleFieldHeight(), 
		       battleFieldWidth = getBattleFieldWidth();
		double predictedX = enemyX, predictedY = enemyY;
		while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
		      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
			predictedX += Math.sin(enemyHeading) * enemyVelocity;
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			enemyHeading += enemyHeadingChange;
			if(	predictedX < 18.0 
				|| predictedY < 18.0
				|| predictedX > battleFieldWidth - 18.0
				|| predictedY > battleFieldHeight - 18.0){
		
				predictedX = Math.min(Math.max(18.0, predictedX), 
				    battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), 
				    battleFieldHeight - 18.0);
				break;
			}
		}
		double theta = Utils.normalAbsoluteAngle(Math.atan2(
		    predictedX - getX(), predictedY - getY()));
		
		setTurnRadarRightRadians(Utils.normalRelativeAngle(
		    absoluteBearing - getRadarHeadingRadians()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(
		    theta - getGunHeadingRadians()));
		fire(1);

	}
	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		//back(10);
		System.out.println(e.toString());
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		//back(20);
	}	
}
