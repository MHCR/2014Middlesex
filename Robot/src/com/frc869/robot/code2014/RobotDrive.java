/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frc869.robot.code2014;


import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;

/**
 *
 * @author Kevin
 */
public class RobotDrive { //Human interface devices
    public static final int LEFT_STICK = 1;
    public static final int RIGHT_STICK = 2;
    
    //PWMs
    public static final int LEFT_MOTOR_1 = 2;
    public static final int LEFT_MOTOR_2 = 4;
    public static final int RIGHT_MOTOR_1 = 1;
    public static final int RIGHT_MOTOR_2 = 3;
    private static RobotDrive instance;

    private Talon tal1_left, tal2_left, tal1_right, tal2_right;
    private Joystick leftJoyStick, rightJoyStick;
    private static RobotDrive drive;
    private double precRight = 1;
    private double leftSpeed = 0;
    private double rightSpeed = 0;
    private double precLeft = 1;
    private boolean safeDrive;
    
    private RobotDrive() {
        leftJoyStick = new Joystick(LEFT_STICK);
        rightJoyStick = new Joystick(RIGHT_STICK);
        tal1_left = new Talon(LEFT_MOTOR_1);
        tal2_left = new Talon(LEFT_MOTOR_2);
        tal1_right = new Talon(RIGHT_MOTOR_1);
        tal2_right = new Talon(RIGHT_MOTOR_2);
    }

    public static RobotDrive getInstance() {
        if(drive == null){
            drive = new RobotDrive();
        }
        return drive;
    }
    
    public double getRightSpeed(){
        if(Math.abs(rightJoyStick.getY()) > .15f){
            return -rightJoyStick.getY();
        }
        return 0;
    }
    
    public void setSafeDrive(boolean safe){
        safeDrive = safe;
    }
    
     public double getLeftSpeed(){
         if(Math.abs(leftJoyStick.getY()) > .15f ){
             return -leftJoyStick.getY();
         }
         
        return 0;
    }
     
    public void control() {       
        leftSpeed = getLeftSpeed();
        rightSpeed = getRightSpeed();
        if(Logitech.getInstance().getL2()){
           stop();
        }else if(rightJoyStick.getTrigger()){
            tankDrive(rightSpeed, rightSpeed);
        }else if(leftJoyStick.getRawButton(3) 
                || leftJoyStick.getRawButton(4) 
                || leftJoyStick.getRawButton(5) 
                || leftJoyStick.getRawButton(6) 
                || rightJoyStick.getRawButton(3) 
                || rightJoyStick.getRawButton(4) 
                || rightJoyStick.getRawButton(5) 
                || rightJoyStick.getRawButton(6)){
        tankDrive(leftSpeed / 2, rightSpeed / 2);
        }else {
            tankDrive(leftSpeed, rightSpeed);
        }
    }

    public void tankDrive(double right, double left) {
        setRightMotors(right * precRight);
        setLeftMotors(left * precLeft);
    }

    public void setPrecisionValues(double right, double left) {
        precRight = right;
        precLeft = left;
    }
    
    public void setRightMotors(double intensity){
        tal1_right.set(intensity);
        tal2_right.set(intensity);
    }
    
     public void setLeftMotors(double intensity){
        tal1_left.set(-intensity);
        tal2_left.set(-intensity);

    }


    public void stop(){
        tal1_left.set(0);
        tal2_left.set(0);
        tal1_right.set(0);
        tal2_right.set(0);
    } 
       
}
