/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frc869.robot.code2014.autonomous;

import com.frc869.robot.code2014.EncoderControl;
import com.frc869.robot.code2014.lib.CheesyVisionServer;
import edu.wpi.first.wpilibj.DriverStation;

/**
 *
 * @author Kevvers
 */
public class CheesyVisionAuto extends Autonomous {
    private static final double CHEESYDISTANCE = DISTANCE+(12*EncoderControl.CLICKS_PER_INCH);
    private static final double didlerDropSpeed = .5;
    private double cheesyTimer;
    private static final int CENTER = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private int facing = CENTER;

    public void routine() {
        switch (getMode()) {
            default:
                increaseMode();
                break;
            case 0:
                if (!getDidlers().moveDidlers(didlerDropSpeed)) {
                    increaseMode();
                }
                break;
            case 1:
                getDidlers().moveDidlers(didlerDropSpeed);
                if (drive(CHEESYDISTANCE, .5)) {
                    increaseMode();
                }
                break;
            case 2:
                System.out.println(facing);
                if(turn()) {
                    if(CheesyVisionServer.getInstance().getUpperRightStatus()) {
                        increaseMode();
                    } else if((DriverStation.getInstance().getMatchTime() - cheesyTimer) > .5) {
                        if(LEFT!=facing && CheesyVisionServer.getInstance().getLeftStatus()){
                            turn(30, false);
                            cheesyTimer = DriverStation.getInstance().getMatchTime();
                            if(CENTER==facing) {
                                facing = LEFT;
                            } else if(RIGHT==facing) {
                                facing = CENTER;
                            }
                        }else if(RIGHT!=facing && CheesyVisionServer.getInstance().getRightStatus()){
                            turn(30, true);
                            cheesyTimer = DriverStation.getInstance().getMatchTime();
                            if(CENTER==facing) {
                                facing = RIGHT;
                            } else if(LEFT==facing) {
                                facing = CENTER;
                            }
                        }
                    }
                }
                break;
            case 3:
                if (getCatapult().fire()) {
                    increaseMode();
                }
                break;
        }
    }
}
