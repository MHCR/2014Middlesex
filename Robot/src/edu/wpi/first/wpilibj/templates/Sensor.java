/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.Ultrasonic;

/**
 *
 * @author Kevvers
 */
public class Sensor {
   private AnalogChannel ultraSonicSensor;
    private static final double TARGET_DISTANCE = 0;
    private static int ULTRA_SONIC_ANALOG = 1;
    private static Sensor instance;
    
    private Sensor(){
          ultraSonicSensor = new AnalogChannel(ULTRA_SONIC_ANALOG);
    }
    
    public static Sensor getInstance(){
        if(instance == null){
            instance = new Sensor();
        }
        return instance;
    }
    
    public AnalogChannel getUltraSonicSensor(){      
       return ultraSonicSensor;
    }
    
    public double getRange(){
        return   ultraSonicSensor.getVoltage()/ .0049;
    }
    
    public double getDistanceToTargetSpot(){
        return getRange() - TARGET_DISTANCE;
    }
}
