/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frc869.robot.code2014.autonomous;

import com.frc869.robot.code2014.lib.CheesyVisionServer;
import edu.wpi.first.wpilibj.DriverStation;

/**
 *
 * @author Kevvers
 */
public class CheesyVisionAuto extends Autonomous {

    private static final double didlerDropSpeed = .5;
    private double cheesyTimer = 1;

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
                if (drive(DISTANCE, .5)) {
                    increaseMode();
                }
                break;
            case 2:
                if(turn() && (DriverStation.getInstance().getMatchTime() - cheesyTimer) > .5) {
                    if(CheesyVisionServer.getInstance().getUpperRightStatus()) {
                        increaseMode();
                    } else if(CheesyVisionServer.getInstance().getLeftStatus()){
                        turn(20, false);
                        cheesyTimer = DriverStation.getInstance().getMatchTime();
                    }else if(CheesyVisionServer.getInstance().getRightStatus()){
                        turn(20, true);
                        cheesyTimer = DriverStation.getInstance().getMatchTime();
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
