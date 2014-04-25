
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package com.frc869.robot.code2014;

import com.frc869.robot.code2014.autonomous.Autonomous;
import com.frc869.robot.code2014.autonomous.CheesyVisionAuto;
import com.frc869.robot.code2014.autonomous.OneAndAHalfAuto;
import com.frc869.robot.code2014.autonomous.OneBallAuto;
import com.frc869.robot.code2014.autonomous.TwoBallAuto;
import com.frc869.robot.code2014.autonomous.TwoBallAutoHotHot;
import com.frc869.robot.code2014.lib.CheesyVisionServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotMain extends IterativeRobot {

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    private CheesyVisionServer server;
    private MaxbotixUltrasonic ultraSonic;
    private Autonomous auto;
    private SendableChooser colorChooser;
    private SendableChooser autoChooser;
    private Lights lights;
    private static final int DEFAULT = 0;
    private static final int ONEBALL = 1;
    private static final int ONEANDAHALF = 2;
    private static final int TWOBALL = 3;
    private static final int TWOHOT = 4;
    private static final int CHEEZYONEBALL = 5;

    public void robotInit() {
        //   ultraSonic = MaxbotixUltrasonic.getInstance();
        lights = Lights.getInstance();
        colorChooser = new SendableChooser();
        colorChooser.addDefault("Alliance",new Integer(Lights.ALLIANCE));
        colorChooser.addObject("Blue",new Integer(Lights.BLUE));
        colorChooser.addObject("Red",new Integer(Lights.RED));
        colorChooser.addObject("Green",new Integer(Lights.GREEN));
        colorChooser.addObject("Purple",new Integer(Lights.PURPLE));
        colorChooser.addObject("Yellow",new Integer(Lights.YELLOW));
        colorChooser.addObject("Teal",new Integer(Lights.TEAL));
        colorChooser.addObject("White",new Integer(Lights.WHITE));
        colorChooser.addObject("Off",new Integer(Lights.OFF));
        SmartDashboard.putData("Team Color",colorChooser);
        autoChooser = new SendableChooser();
        autoChooser.addDefault("One Ball",new Integer(ONEBALL));
        autoChooser.addObject("One and a half ball", new Integer(ONEANDAHALF));
        autoChooser.addObject("Two Ball", new Integer(TWOBALL));
        autoChooser.addObject("Two Hot", new Integer(TWOHOT));
        autoChooser.addObject("Cheesy Vision One Ball", new Integer(CHEEZYONEBALL));
        SmartDashboard.putData("Autonomous Mode",autoChooser);
        server = CheesyVisionServer.getInstance();
        server.start();
        
        //if we go a second and seem to loop infinitely kill the robot
        Watchdog.getInstance().setExpiration(1);
        Watchdog.getInstance().setEnabled(true);
    }
    
    /**
     * Common periodic functions that the robot should do in every mode
     */
    public void robotPeriodic() {
        Watchdog.getInstance().feed();
        DriverStation.getInstance().setDigitalOut(8, !Catapult.getInstance().isSafetyIn());
        DriverStation.getInstance().setDigitalOut(7, !Catapult.getInstance().showSafetyValue());
        System.out.println("right: " + EncoderControl.getInstance().getRightDistance());
        System.out.println("left: " + EncoderControl.getInstance().getLeftDistance());
        //Timer.delay(.01);
    }

    public void disabledInit() {
        server.stopSamplingCounts();
    }

    public void disabledPeriodic() {
        robotPeriodic();
        Catapult.getInstance().setFiredAuto(false);
        EncoderControl.getInstance().getLeft().reset();
        EncoderControl.getInstance().getRight().reset();
    }

    public void autonomousInit() {
        server.reset();
        server.startSamplingCounts();
        
        Integer valueObject = (Integer) autoChooser.getSelected();
        int value = DEFAULT;
        if(null!=valueObject) {
            value = valueObject.intValue();
        }
        switch (value) {
            case ONEBALL:
                auto = new OneBallAuto();
                break;
            case ONEANDAHALF:
            default:
                auto = new OneAndAHalfAuto();
                break;
            case TWOBALL:
                auto = new TwoBallAuto();
                break;
            case TWOHOT:
                auto = new TwoBallAutoHotHot();
                break;
            case CHEEZYONEBALL:
                auto = new CheesyVisionAuto();
                break;
        }
        auto.init();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
//        System.out.println("Current upper left: " + server.getUpperLeftStatus() + ", current upper right: " + server.getUpperRightStatus()+"Current left: " + server.getLeftStatus() + ", current right: " + server.getRightStatus());
//        System.out.println("Upper Left count: " + server.getUpperLeftCount() + ", upper right count: " + server.getUpperRightCount() +"Left count: " + server.getLeftCount() + ", right count: " + server.getRightCount() + ", total: " + server.getTotalCount() + "\n");
        robotPeriodic();
        lights.safety();
        lights.fire();
        auto.run();
    }

    public void teleopInit() {
        DidlerControl.getInstance().setDidlerSpeed(.70);
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        robotPeriodic();
        lights.setColor(((Integer) colorChooser.getSelected()).intValue());
        lights.checkCountdown();
        lights.fire();
        lights.safety();
        lights.victory();
        //lights.searching(Lights.NO);
        RobotDrive.getInstance().control();
        DidlerControl.getInstance().control();
        Catapult.getInstance().control();
    }

    public void testInit() {
        DidlerControl.getInstance().setDidlerSpeed(1);
    }

    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        robotPeriodic();
        lights.countdown();
        RobotDrive.getInstance().control();
        DidlerControl.getInstance().control();
//        Catapult.getInstance().control();
        if(Logitech.getInstance().getAbutton()) {
            RobotDrive.getInstance().tankDrive(.1, .1);
        } else {
            RobotDrive.getInstance().tankDrive(0, 0);
        }
    }
}
