/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.first.wpilibj.templates;

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
    
    private Talon rightDidler;
    private Talon leftDidler;
    private Talon didlerRotator;
    private DigitalInput forwardD;
    private DigitalInput backwardD;
    private Logitech controller;
    private double didlerSpeed = 0;
    
    public void setDidlerSpeed(double value) {
        didlerSpeed = value;
    }
    
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
        //System.out.println("forward: " + forwardD.get() + " back: " + backwardD.get());
        if (!forwardD.get()) {
            if (controller.getLeftStickY() > .2) {
                didlerRotator.set(controller.getLeftStickY() / 2.0);
            } else {
                didlerRotator.set(0);
                
            }
        } else if (!backwardD.get()) {
            if (controller.getLeftStickY() < -.2) {
                didlerRotator.set(controller.getLeftStickY() / 2.0);
            } else {
                didlerRotator.set(0);
            }            
        } else {
               // System.out.println("LEFTG STICKSAKJHDKJASHDKJASHDKJASHDKJAH: " + controller.getLeftStickY() / 2.0)   ;         
                didlerRotator.set(controller.getLeftStickY() / 2.0);
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
        
      //  System.out.println("left: " + leftDidler.get() + "right: " + rightDidler.get());
        
    }
    
    public void moveDidlers(double speed) {
        if (!backwardD.get()) {
            didlerRotator.set(0);
        } else {
            didlerRotator.set(speed);
        }
    }
    
    public void spinDidlers(double didlerSpeed){
         rightDidler.set(didlerSpeed * didlerSpeed * -1.0);            
         leftDidler.set(didlerSpeed * 1.0F);
    }
    
  
    public boolean isSpinning(){
        return (Math.abs((leftDidler.get() + rightDidler.get()) / 2.0) > .1);
    }
    
    public boolean isDropping(){
        return (Math.abs(didlerRotator.get()) > .1);
    }
    
}
