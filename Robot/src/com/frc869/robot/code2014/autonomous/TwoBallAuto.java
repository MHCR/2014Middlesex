/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frc869.robot.code2014.autonomous;

import edu.wpi.first.wpilibj.DriverStation;

/**
 *
 * @author Kevvers
 */
public class TwoBallAuto extends Autonomous {
    private static final double didlerDropSpeed = .5;
    private double didlerDragSpeed;
    private double didlerIntakeSpeed;
    private double didlerSettleSpeed;
    private double driveSpeed;
    public TwoBallAuto() {
        super();
        didlerDragSpeed = .4;
        didlerIntakeSpeed = .2;
        didlerSettleSpeed = -.35;
        driveSpeed = .5;
    }
    public void init() {
        didlerSettleSpeed = -1.0 * DriverStation.getInstance().getAnalogIn(1);
        didlerDragSpeed = DriverStation.getInstance().getAnalogIn(2);
        driveSpeed = DriverStation.getInstance().getAnalogIn(3);
        didlerIntakeSpeed = DriverStation.getInstance().getAnalogIn(4);
    }
    public void routine() {
        switch(getMode()) {
            default:
                increaseMode();
                break;
            case 0:
                getDidlers().moveDidlers(didlerDropSpeed);
                getDidlers().spinDidlers(didlerDragSpeed);
                if(getModeTime() > .5) {
                    increaseMode();
                }
                break;
            case 1:
                getDidlers().moveDidlers(didlerDropSpeed);
                getDidlers().spinDidlers(didlerDragSpeed);
                if(drive(DISTANCE,driveSpeed)) {
                    increaseMode();
                }
                break;
            case 2:
                if (getCatapult().fire() || getModeTime() > .5) {
                    increaseMode();
                }
                break;
            case 3:
                if(didlerIntakeSpeed < .55) {
                    didlerIntakeSpeed += .01;
                }
                getDidlers().moveDidlers(didlerDropSpeed);
                getDidlers().spinDidlers(didlerIntakeSpeed);
                if (getCatapult().fire()) {
                    getCatapult().resetAuto();
                    increaseMode();
                }
                break;
            case 4:
                if(!getDidlers().moveDidlers(didlerSettleSpeed)){
                    increaseMode();
                }
                break;
            case 5:
                if(!getDidlers().moveDidlers(.15)){
                    increaseMode();
                }
                break;
            case 6:
                if (getCatapult().fire()) {
                    increaseMode();
                }
                break;
        }
    }
}
