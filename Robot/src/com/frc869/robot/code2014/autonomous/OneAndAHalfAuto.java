/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frc869.robot.code2014.autonomous;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 * @author Kevvers
 */
public class OneAndAHalfAuto extends Autonomous {
    private static final double didlerDropSpeed = .5;
    private static final double didlerDragSpeed = .45;
    public void routine() {
        switch(getMode()) {
            default:
                increaseMode();
                break;
            case 0:
                getDidlers().moveDidlers(didlerDropSpeed);
                getDidlers().spinDidlers(didlerDragSpeed);
                if(getModeTime() > 1) {
                    increaseMode();
                }
                break;
            case 1:
                getDidlers().moveDidlers(didlerDropSpeed);
                getDidlers().spinDidlers(didlerDragSpeed);
                if(drive(DISTANCE,.5)) {
                    increaseMode();
                }
                break;
            case 2:
                if (getModeTime() > 1) {
                    increaseMode();
                }
                break;
            case 3:
                if(SmartDashboard.getBoolean("hot",false) || DriverStation.getInstance().getMatchTime() >= 5){
                    increaseMode();
                }
                break;
            case 4:
                if(getCatapult().fire()) {
                    increaseMode();
                }
                break;
        }
    }
}