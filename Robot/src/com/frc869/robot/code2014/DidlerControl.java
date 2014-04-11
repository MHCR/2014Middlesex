/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frc869.robot.code2014;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Talon;

/**
 *
 * @author Kevvers
 */
public class DidlerControl {

    private static DidlerControl instance;
    private static final int RIGHT_TALON_PWM = 6;
    private static final int LEFT_TALON_PWM = 5;
    
    private static final int DIDLER_TALON_PWM = 7;
    
    private static final int FORWARD_DIO = 4;
    private static final int BACKWARD_DIO = 3;
    
    private final Talon rightDidler;
    private final Talon leftDidler;
    private final Talon didlerRotator;
    private final DigitalInput forwardD;
    private final DigitalInput backwardD;
    private final Logitech controller;
    private double didlerSpeed = 0;
    private boolean forward = false;
    private double acceleration = 0;
    
    private DidlerControl() {
        controller = Logitech.getInstance();
        didlerRotator = new Talon(DIDLER_TALON_PWM);
        forwardD = new DigitalInput(FORWARD_DIO);
        backwardD = new DigitalInput(BACKWARD_DIO);
        rightDidler = new Talon(RIGHT_TALON_PWM);
        leftDidler = new Talon(LEFT_TALON_PWM);
    }
    
    public static DidlerControl getInstance() {
        if (instance == null) {
            instance = new DidlerControl();
        }
        return instance;
    }
    
    public void control() {
        if (Math.abs(controller.getLeftStickY()) > .1){
            moveDidlers(controller.getLeftStickY() / 2.0,false);
        } else {
            moveDidlers(controller.getRightStickY() / 2.0,true);
        }
        if (controller.getL1()) {            
            rightDidler.set(didlerSpeed * -1.0F);
            leftDidler.set(didlerSpeed * 1.0F);
        } else if (controller.getR1()) {
            rightDidler.set(didlerSpeed * 1.0F);
            leftDidler.set(didlerSpeed * -1.0F);
        } else {
            leftDidler.set(0);
            rightDidler.set(0);
        }
    }
    
    public boolean moveDidlers(double speed) {
        return moveDidlers(speed,true);
    }
    
    public boolean moveDidlers(double speed, boolean enableAccel) {
        if (!forwardD.get()) {
            forward = true;
            acceleration = 0;
            if (speed > .1) {
                didlerRotator.set(speed);
                return true;
            } else {
                didlerRotator.set(0);
                return false;
            }
        } else if (!backwardD.get()) {
            forward = false;
            acceleration = 0;
            if (speed < -.1) {
                didlerRotator.set(speed);
                return true;
            } else {
                didlerRotator.set(0);
                return false;
            }            
        } else if(enableAccel) {
            if(speed > 0 && !forward && acceleration < speed) {
                acceleration += .005;
            } else if(speed < 0 && forward && acceleration > speed) {
                acceleration -= .005;
            } else {
                acceleration = speed;
            }
            didlerRotator.set(acceleration);
            return true;
        }else{
            didlerRotator.set(speed);
            return true;
        }
    }
    
    public void spinDidlers(double didlerSpeed){
         rightDidler.set(didlerSpeed * -1.0);            
         leftDidler.set(didlerSpeed * 1.0F);
    }
    
    public void setDidlerSpeed(double value) {
        didlerSpeed = value;
    }
  
    public boolean isSpinning(){
        return (Math.abs((leftDidler.get() + rightDidler.get()) / 2.0) > .1);
    }
    
    public boolean isDropping(){
        return (Math.abs(didlerRotator.get()) > .1);
    }
    
    public boolean didlersRotating(){
        return Math.abs(didlerRotator.get()) < .1;
    }
    
}
