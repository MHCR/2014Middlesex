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
public class TwoBallAuto implements Runnable {

    public double startTime;
    private RobotDrive drive;
    private EncoderControl encoders;
    private DidlerControl didlers;
    private Catapult catapult;
    private double DISTANCE = EncoderControl.CLICKS_PER_INCH * 72;
    private int numTargets = 0;
    private double didlerDropSpeed = .2;
    private double didlerDragSpeed = .2;
    private double didlerIntakeSpeed = .8;
    private double driveSpeed = .5;
    private boolean firedOnce = false;
    private double fireTime = 0;
    private double intakeDelay = 3.0;

    public TwoBallAuto() {
        catapult = Catapult.getInstance();
        didlers = DidlerControl.getInstance();
        startTime = System.currentTimeMillis();
        drive = RobotDrive.getInstance();
        encoders = EncoderControl.getInstance();
        encoders.reset();
    }

    public void run() {
        didlerDropSpeed = DriverStation.getInstance().getAnalogIn(1) / 5.0;
        didlerDragSpeed = DriverStation.getInstance().getAnalogIn(2) / 5.0;
        driveSpeed = DriverStation.getInstance().getAnalogIn(3) / 5.0;
        intakeDelay = DriverStation.getInstance().getAnalogIn(4) / 5.0;
        if (drive(DISTANCE)) {
            drive.stop();
            if (firedOnce) {
                if (fireTime == 0) {
                    fireTime = DriverStation.getInstance().getMatchTime();
                }

                if (catapult.isLimitHit() && (DriverStation.getInstance().getMatchTime() - fireTime) < intakeDelay && !catapult.isFiring()) {
                    didlers.moveDidlers(didlerDropSpeed);
                    didlers.spinDidlers(didlerIntakeSpeed);

                } else if ((DriverStation.getInstance().getMatchTime() - fireTime) > intakeDelay) {
                    didlers.moveDidlers(0);
                    didlers.spinDidlers(0);
                    catapult.fire();
                }

            } else {
                didlers.moveDidlers(0);
                didlers.spinDidlers(0);

                //TODO: this might be the issue with moving the didlers late we probably hit this first cycle
                if (DriverStation.getInstance().getMatchTime() >= 3) {
                    if (!didlers.isSpinning() && !didlers.isDropping() && !firedOnce) {

                        if (catapult.fire()) {
                            if (!firedOnce) {
                                catapult.resetAuto();
                                fireTime = DriverStation.getInstance().getMatchTime();
                                firedOnce = true;
                            }
                        }
                    }

                }
            }
        } else {
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
                drive.setLeftMotors(-1.0 * driveSpeed);
                drive.setRightMotors((-1.0 * driveSpeed) +.1);
            } else if (offset < -40) {
                drive.setRightMotors(-1.0 * driveSpeed);
                drive.setLeftMotors((-1.0 * driveSpeed) +.1);
            } else {
                drive.setRightMotors(-1.0 * driveSpeed);
                drive.setLeftMotors(-1.0 * driveSpeed);
            }
        } else {
            drive.setLeftMotors(0);
            drive.setRightMotors(0);
            return true;

        }
        return false;

    }

}
