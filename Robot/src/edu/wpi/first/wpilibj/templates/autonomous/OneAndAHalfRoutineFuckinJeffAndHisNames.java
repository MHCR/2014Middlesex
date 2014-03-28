/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.autonomous;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.templates.Catapult;
import edu.wpi.first.wpilibj.templates.DidlerControl;
import edu.wpi.first.wpilibj.templates.EncoderControl;
import edu.wpi.first.wpilibj.templates.RobotDrive;

/**
 *
 * @author Kevvers
 */
public class OneAndAHalfRoutineFuckinJeffAndHisNames implements Runnable {

    public double startTime;
    private RobotDrive drive;
    private EncoderControl encoders;
    private DidlerControl didlers;
    private Catapult catapult;
    private double DISTANCE = EncoderControl.CLICKS_PER_INCH * 72;
    private int numTargets = 0;
    private double didlerDropSpeed = .5;
    private double didlerDragSpeed = .4;

    public OneAndAHalfRoutineFuckinJeffAndHisNames() {
        catapult = Catapult.getInstance();
        didlers = DidlerControl.getInstance();
        startTime = System.currentTimeMillis();
        drive = RobotDrive.getInstance();
        encoders = EncoderControl.getInstance();
        encoders.reset();
    }

    public void run() {
        if (DriverStation.getInstance().getMatchTime() > 1.0 && drive(DISTANCE)) {
            drive.stop();
            didlers.moveDidlers(.5);
            didlers.spinDidlers(0);
            
            //TODO: this might be the issue with moving the didlers late we probably hit this first cycle
            if (DriverStation.getInstance().getMatchTime() >= 3) {
                didlers.moveDidlers(0);
                if(SmartDashboard.getBoolean("hot") || DriverStation.getInstance().getMatchTime() >= 8.5){ //if we finally see hot
                    catapult.fire();
                } else if (catapult.isFiring()) { //if we are already firing finish up
                    catapult.fire();
                }
            }
        }else{
            didlers.moveDidlers(didlerDropSpeed);
            didlers.spinDidlers(didlerDragSpeed);
        }
    }

    public double getEncoderOffset() {
        return encoders.getLeftDistance() - encoders.getRightDistance();
    }

    public double getDistanceTraveled() {
        return Math.abs(EncoderControl.getInstance().getLeftDistance() + EncoderControl.getInstance().getRightDistance()) / 2;
    }

    protected boolean drive(double distance) {
        System.out.println("Distance: " + getDistanceTraveled());

        double offset = getEncoderOffset();
        if (getDistanceTraveled() < distance) {
            if (offset > 40) {
                drive.setLeftMotors(-.5);
                drive.setRightMotors(-.4);
            } else if (offset < -40) {
                drive.setRightMotors(-.5);
                drive.setLeftMotors(-.4);
            } else {
                drive.setRightMotors(-.5);
                drive.setLeftMotors(-.5);
            }
        } else {
            drive.setLeftMotors(0);
            drive.setRightMotors(0);
            return true;

        }
        return false;

    }

}
