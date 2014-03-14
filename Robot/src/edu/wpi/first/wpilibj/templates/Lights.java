/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Relay;

/**
 *
 * @author mechinn
 */
public class Lights {
    private static Lights instance;
    public static final int ALLIANCE = 0;
    public static final int BLUE = 1;
    public static final int RED = 2;
    public static final int GREEN = 3;
    public static final int PURPLE = 4;
    public static final int YELLOW = 5;
    public static final int TEAL = 6;
    public static final int WHITE = 7;
    public static final int OFF = 8;
    
    public static final int NO = 0;
    public static final int LOOKING = 1;
    public static final int FOUND = 2;
    
    private final Relay count;
    private final Relay pulse;
    private final Relay search;
    private final Relay found;
    private final Relay shoot;
    private final Relay color1;
    private final Relay color2;
    private final Relay color3;
    
    private Lights() {
        search = new Relay(1);
        found = new Relay(2);
        shoot = new Relay(3);
        color1 = new Relay(4);
        color2 = new Relay(5);
        color3 = new Relay(6);
        count = new Relay(7);
        pulse = new Relay(8);
        search.setDirection(Relay.Direction.kForward);
        found.setDirection(Relay.Direction.kForward);
        shoot.setDirection(Relay.Direction.kForward);
        color1.setDirection(Relay.Direction.kForward);
        color2.setDirection(Relay.Direction.kForward);
        color3.setDirection(Relay.Direction.kForward);
        count.setDirection(Relay.Direction.kForward);
        pulse.setDirection(Relay.Direction.kForward);
    }
    public static Lights getInstance() {
        if(null==instance) {
            instance = new Lights();
        }
        return instance;
    }
    public void searching(int looking) {
        switch(looking) {
            default:
                search.set(Relay.Value.kOff);
                found.set(Relay.Value.kOff);
                break;
            case LOOKING:
                search.set(Relay.Value.kOn);
                found.set(Relay.Value.kOff);
                break;
            case FOUND:
                found.set(Relay.Value.kOn);
                search.set(Relay.Value.kOff);
                break;
        }
    }
    public void fire(boolean shooting) {
        if(shooting) {
            shoot.set(Relay.Value.kOn);
        } else {
            shoot.set(Relay.Value.kOff);
        }
    }
    public void setColor(int color) {
        switch(color) {
            default:
                if(DriverStation.getInstance().getAlliance()==DriverStation.Alliance.kRed) {
                    color1.set(Relay.Value.kOff);
                    color2.set(Relay.Value.kOff);
                    color3.set(Relay.Value.kOff);
                } else {//if we are blue or not connected to an FMS make us blue
                    color1.set(Relay.Value.kOn);
                    color2.set(Relay.Value.kOff);
                    color3.set(Relay.Value.kOff);
                }
                break;
            case BLUE:
                color1.set(Relay.Value.kOff);
                color2.set(Relay.Value.kOff);
                color3.set(Relay.Value.kOff);
                break;
            case RED:
                color1.set(Relay.Value.kOn);
                color2.set(Relay.Value.kOff);
                color3.set(Relay.Value.kOff);
                break;
            case GREEN:
                color1.set(Relay.Value.kOff);
                color2.set(Relay.Value.kOn);
                color3.set(Relay.Value.kOff);
                break;
            case PURPLE:
                color1.set(Relay.Value.kOn);
                color2.set(Relay.Value.kOn);
                color3.set(Relay.Value.kOff);
                break;
            case YELLOW:
                color1.set(Relay.Value.kOff);
                color2.set(Relay.Value.kOff);
                color3.set(Relay.Value.kOn);
                break;
            case TEAL:
                color1.set(Relay.Value.kOn);
                color2.set(Relay.Value.kOff);
                color3.set(Relay.Value.kOn);
                break;
            case WHITE:
                color1.set(Relay.Value.kOff);
                color2.set(Relay.Value.kOn);
                color3.set(Relay.Value.kOn);
                break;
            case OFF:
                color1.set(Relay.Value.kOn);
                color2.set(Relay.Value.kOn);
                color3.set(Relay.Value.kOn);
                break;
        }
    }
    public void checkCountdown() {
        if(DriverStation.getInstance().getMatchTime() >= 145) {
            countdown();
        } else {
            count.set(Relay.Value.kOff);
        }
    }
    public void countdown() {
        count.set(Relay.Value.kOn);
        if((System.currentTimeMillis()/1000) % 2 == 0) {
            pulse.set(Relay.Value.kOff);
        } else {
            pulse.set(Relay.Value.kOn);
        }
    }
}
