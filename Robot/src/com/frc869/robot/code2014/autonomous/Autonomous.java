/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frc869.robot.code2014.autonomous;

import com.frc869.robot.code2014.Catapult;
import com.frc869.robot.code2014.DidlerControl;
import com.frc869.robot.code2014.EncoderControl;
import com.frc869.robot.code2014.RobotDrive;
import edu.wpi.first.wpilibj.DriverStation;

/**
 *
 * @author Kevvers
 */
public abstract class Autonomous implements Runnable {

    protected static final double DISTANCE = EncoderControl.CLICKS_PER_INCH * 72;
    protected static final int drift = 40;
    private final RobotDrive drive;
    private final EncoderControl encoders;
    private final DidlerControl didlers;
    private final Catapult catapult;
    private int mode;
    private int resetMode;
    private double resetTime;    
    private static double WHEEL_BASE_LENGTH = 34.1;
    private static double DISTANCE_TO_SPIN = (Math.PI * WHEEL_BASE_LENGTH * EncoderControl.CLICKS_PER_INCH) ;

    public Autonomous() {
        catapult = Catapult.getInstance();
        didlers = DidlerControl.getInstance();
        drive = RobotDrive.getInstance();
        encoders = EncoderControl.getInstance();
        encoders.reset();
        mode = 0;
        resetMode = 0;
        resetTime = 0;
    }

    protected RobotDrive getDrive() {
        return drive;
    }

    protected boolean turn(float degrees, boolean right) {
//        if(degrees > 180){
//            degrees = 360 - degrees;
//        }
        double distanceTurned = Math.abs(getEncoders().getLeftDistance()) + Math.abs(getEncoders().getRightDistance());
        double change = (degrees / 360F) * DISTANCE_TO_SPIN;

        System.out.println("change: " + change + "turned: " + distanceTurned);
        if (distanceTurned < change && right) {
            drive.setLeftMotors(.5);
            drive.setRightMotors(-.5);
            return false;
        } else if (distanceTurned < change && !right) {
            drive.setLeftMotors(-.5);
            drive.setRightMotors(.5);
            return false;
        } else {
            drive.setLeftMotors(0);
            drive.setRightMotors(0);
            return true;
        }

    }

    protected EncoderControl getEncoders() {
        return encoders;
    }

    protected DidlerControl getDidlers() {
        return didlers;
    }

    protected Catapult getCatapult() {
        return catapult;
    }

    protected int getMode() {
        return mode;
    }

    protected void increaseMode() {
        ++mode;
    }

    protected double getEncoderOffset() {
        return encoders.getLeftDistance() - encoders.getRightDistance();
    }

    protected double getDistanceTraveled() {
        return Math.abs(EncoderControl.getInstance().getLeftDistance() + EncoderControl.getInstance().getRightDistance()) / 2;
    }

    protected double getModeTime() {
        return DriverStation.getInstance().getMatchTime() - resetTime;
    }

    protected boolean drive(double distance, double speed) {
        System.out.println("Distance: " + getDistanceTraveled());

        double offset = getEncoderOffset();
        if (getDistanceTraveled() < distance) {
            if (offset > drift) {
                drive.setLeftMotors(-speed);
                drive.setRightMotors(-speed * .8);
            } else if (offset < -drift) {
                drive.setLeftMotors(-speed * .8);
                drive.setRightMotors(-speed);
            } else {
                drive.setRightMotors(-speed);
                drive.setLeftMotors(-speed);
            }
            return false;
        } else {
            drive.setLeftMotors(0);
            drive.setRightMotors(0);
            return true;
        }
    }
    
    public void setMode(int mode){
        this.mode = mode;
    }

    public void init() {

    }

    public void run() {
        if (resetMode != mode) {
            drive.stop();
            didlers.moveDidlers(0);
            didlers.spinDidlers(0);

            resetTime = DriverStation.getInstance().getMatchTime();
        }
        resetMode = mode;
        routine();
    }

    public abstract void routine();
}
