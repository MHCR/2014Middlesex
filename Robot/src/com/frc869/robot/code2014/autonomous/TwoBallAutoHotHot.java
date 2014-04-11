/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frc869.robot.code2014.autonomous;

import com.frc869.robot.code2014.EncoderControl;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 * @author Kevvers
 */
public class TwoBallAutoHotHot extends Autonomous {

    private static final double didlerDropSpeed = .35;

    private boolean goalOnLeft = true;

    private double didlerDragSpeed;
    private double didlerIntakeSpeed;
    private double didlerSettleSpeed;
    private double driveSpeed;
    private boolean firstGoalHot;

    public TwoBallAutoHotHot() {
        super();
        didlerDragSpeed = .4;
        didlerIntakeSpeed = .2;
        didlerSettleSpeed = -.35;
        driveSpeed = .5;
    }

    public void init() {
        didlerDragSpeed = .4;
        didlerIntakeSpeed = .2;
        didlerSettleSpeed = -.35;
        driveSpeed = .5;
    }

    public void routine() {
        switch (getMode()) {
            default:
                increaseMode();
                break;
            case 0:
                getDidlers().moveDidlers(didlerDropSpeed);
                getDidlers().spinDidlers(didlerDragSpeed);
                if (getModeTime() > .5) {
                    increaseMode();
                }
                break;
            case 1:
                getDidlers().moveDidlers(didlerDropSpeed);
                getDidlers().spinDidlers(didlerDragSpeed);
                if (drive(DISTANCE, driveSpeed)) {
                    increaseMode();
                }
                break;
            case 2:
                if (getModeTime() > 1) {
                    increaseMode();
                }
                break;
            case 3:
                if ((SmartDashboard.getBoolean("hot", false)) || DriverStation.getInstance().getMatchTime() > 4.95) {
                    if(SmartDashboard.getBoolean("hot", false)){
                        firstGoalHot = true;                       
                    }
                    increaseMode();
                }
                break;
            case 4:
                if (getCatapult().fire() || getModeTime() > .5) {
                    increaseMode();
                }
                break;
            case 5:
                if (getModeTime() > .3) {
                    increaseMode();
                }
                break;
            case 6:
                if (didlerIntakeSpeed < .65) {
                    didlerIntakeSpeed += .01;
                }
                getDidlers().moveDidlers(didlerDropSpeed);
                getDidlers().spinDidlers(didlerIntakeSpeed);               
                if (getCatapult().fire()) {
                    getCatapult().resetAuto();
                    increaseMode();
                }
                break;
            case 7:
                if (!getDidlers().moveDidlers(didlerSettleSpeed) || getModeTime() > 1.0) {
                    if(firstGoalHot){
                      setMode(10);  
                    }else{
                    increaseMode();
                    }
                }
                break;
            case 8:
                if (!getDidlers().moveDidlers(.25) || getModeTime() > 1.0) {
                    increaseMode();
                }
                break;
            case 9:
                if (getCatapult().fire()) {
                    increaseMode();
                }
                break;
            case 10:
                if(turn(35, false)){
                    increaseMode();
                }
            break;
                case 11:
                if (!getDidlers().moveDidlers(.25) || getModeTime() > 1.0) {
                    increaseMode();
                }
                break;
            case 12:
                if(getModeTime() > .5){     //d   W   E     i     A   R   E     d     T   A   K   I   N   G     d     H   O   M   E     l     C   A   N   A   D   A    e     !
                    if(getCatapult().fire()){
                        increaseMode();
                    }
                }
            break;

        }

    }

   
}
