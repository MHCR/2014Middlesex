/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frc869.robot.code2014;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.AnalogModule;
import edu.wpi.first.wpilibj.SensorBase;

public class MaxbotixUltrasonic extends SensorBase {
    private static MaxbotixUltrasonic instance;
    private final double IN_TO_CM_CONVERSION = 2.54;
    private final int NUM_AVERAGE_BITS    = 3;
    private final double MODULE_SAMPLE_RATE  = 1000.0;
    
    private final double max_voltage;    // Maximum voltage the sensor can output
    private final double min_distance;   // Minimum distance the ultrasonic sensor can return in cm
    private final double max_distance;   // Maximum distance the ultrasonic sensor can return in cm
    private final double volts_per_cm;   // volts/cm based on datasheet, Maxbotix it's Voc/1024
    
    private AnalogChannel channel;
    private AnalogModule  module;
    
    //constructor
    private MaxbotixUltrasonic(int _channel) {
        // configure analog channel/module
        channel = new AnalogChannel(_channel);
        module  = AnalogModule.getInstance(0x1);
        
        channel.setAverageBits(NUM_AVERAGE_BITS);
        module.setSampleRate(MODULE_SAMPLE_RATE);

        // default parameters for MB1200 Maxbotix sensor
        max_voltage    = 5.0;
        volts_per_cm   = max_voltage/1024.0;
	min_distance   = 20.0;
        max_distance   = 700.0;
    }
    
    public static MaxbotixUltrasonic getInstance() {
        if(null==instance) {
            instance = new MaxbotixUltrasonic(1);
        }
        return instance;
    }
    
    //constructor
    public MaxbotixUltrasonic(int _channel, double _max_voltage, double _min_distance, double _max_distance) {
        channel = new AnalogChannel(_channel);
        module  = AnalogModule.getInstance(0x1);
        
        channel.setAverageBits(NUM_AVERAGE_BITS);
        module.setSampleRate(MODULE_SAMPLE_RATE);
        
        max_voltage    = _max_voltage;
        volts_per_cm   = _max_voltage/1024.0;
        min_distance   = _min_distance;
        max_distance   = _max_distance;
    }

    double
    GetVoltage() 
    {
        return channel.getAverageVoltage();
    }

    double
    GetRangeInInches() 
    {
       double range;
          
       range = (channel.getVoltage() / volts_per_cm);
       if(range < min_distance)
           range = min_distance;
       else if(range > max_distance) {
           range = max_distance;
       }
       
       range /= IN_TO_CM_CONVERSION;
       return range;
    }

    double
    GetRangeInCM()
    {
        double range;

        range = (channel.getVoltage() * volts_per_cm);
        if(range < min_distance)
            return min_distance;
        
        if(range > max_distance)
            return max_distance;
        
        return range;
    }
}
    
