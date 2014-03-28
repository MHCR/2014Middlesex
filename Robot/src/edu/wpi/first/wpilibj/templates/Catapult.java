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
public class Catapult {

    private static final int TALON_PWM = 8;
    private static final int CATAPULT_LIMIT_DIO = 1;
    private Talon catapultMotor;
    private DigitalInput catapultSwitch;

    private boolean firing;

    private static Catapult instance;
    private boolean firedAuto;

    public boolean isFiredAuto() {
        return firedAuto;
    }

    public void setFiredAuto(boolean firedAuto) {
        this.firedAuto = firedAuto;
    }

    private Catapult() {
        firing = false;
        firedAuto = false;
        catapultSwitch = new DigitalInput(CATAPULT_LIMIT_DIO);
        catapultMotor = new Talon(TALON_PWM);
    }

    public static Catapult getInstance() {
        if (instance == null) {
            instance = new Catapult();
        }
        return instance;
    }

    public boolean isFiring() {
        return firing;
    }
    
    public void control() {
        firedAuto = false;
        if(Logitech.getInstance().getR2()) {
            firing = true;
            catapultMotor.set(-1.00);
        } else if(!catapultSwitch.get() || !firing) {
            catapultMotor.set(0);
            firing = false;
        } else {
            catapultMotor.set(-1.00);
        }
    }
     double timeT;
    public boolean fire() {
        if(!firedAuto) {
            timeT = System.currentTimeMillis();
            firedAuto = true;
            firing = true;
            catapultMotor.set(-1.00);
            return false;
        } else if((!catapultSwitch.get() || !firing) && (System.currentTimeMillis() - timeT) >1000 ) {
            catapultMotor.set(0);
            firing = false;
            return true;
        } else {
            catapultMotor.set(-1.00);
            return false;
        }
         
    }
    
    public void resetAuto(){
        firedAuto = false;
        timeT = 0;
        firing = false;
    }
    
    public boolean isLimitHit(){
        return !(catapultSwitch.get());
    }
    
    

}
