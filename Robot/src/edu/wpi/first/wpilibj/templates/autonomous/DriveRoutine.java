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
public class DriveRoutine implements Runnable {

    public double startTime;
    private RobotDrive drive;
    private EncoderControl encoders;
    private DidlerControl didlers;
    private Catapult catapult;
    private double DISTANCE = EncoderControl.CLICKS_PER_INCH * 72;
    private int numTargets = 0;

    public DriveRoutine() {
        catapult = Catapult.getInstance();
        didlers = DidlerControl.getInstance();
        startTime = System.currentTimeMillis();
        drive = RobotDrive.getInstance();
        encoders = EncoderControl.getInstance();
        encoders.reset();
    }

    public void run() {
        if (drive(DISTANCE)) {
            drive.stop();
            didlers.moveDidlers(.5);
            
            //TODO: this might be the issue with moving the didlers late we probably hit this first cycle
            if (DriverStation.getInstance().getMatchTime() >= 3) {
                didlers.moveDidlers(0);
                if(SmartDashboard.getBoolean("hot") || DriverStation.getInstance().getMatchTime() >= 9){ //if we finally see hot
                    catapult.fire();
                } else if (catapult.isFiring()) { //if we are already firing finish up
                    catapult.fire();
                }
            }
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
