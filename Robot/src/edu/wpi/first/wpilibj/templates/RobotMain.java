
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.templates.autonomous.DriveRoutine;
import edu.wpi.first.wpilibj.templates.autonomous.OneAndAHalfRoutineFuckinJeffAndHisNames;
import edu.wpi.first.wpilibj.templates.autonomous.TwoBallAuto;

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
    private MaxbotixUltrasonic ultraSonic;
    private Runnable auto;
    private SendableChooser colorChooser;
    private SendableChooser autoChooser;
    private Lights lights;

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
        autoChooser.addObject("Autonomous",new DriveRoutine());
        autoChooser.addDefault("One and a half ball, jeff and his fuckin names", new OneAndAHalfRoutineFuckinJeffAndHisNames());
        autoChooser.addObject("Two Ball", TwoBallAuto.getInstance());
        SmartDashboard.putData("Autonomous Mode",autoChooser);
        //if we go a second and seem to loop infinitely kill the robot
        Watchdog.getInstance().setExpiration(1);
        Watchdog.getInstance().setEnabled(true);
    }
    
    /**
     * Common periodic functions that the robot should do in every mode
     */
    public void robotPeriodic() {
        Watchdog.getInstance().feed();
    }

    public void disabledInit() {
        
    }

    public void disabledPeriodic() {
        robotPeriodic();
        Catapult.getInstance().setFiredAuto(false);
        EncoderControl.getInstance().getLeft().reset();
        EncoderControl.getInstance().getRight().reset();
    }

    public void autonomousInit() {
        TwoBallAuto.getInstance().resetRoutine();
        Catapult.getInstance().resetAuto();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        robotPeriodic();
        lights.fire();
        auto = (Runnable) autoChooser.getSelected();
        auto.run();
    }

    public void teleopInit() {
        DidlerControl.getInstance().setDidlerSpeed(.75);
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        robotPeriodic();
        lights.setColor(((Integer) colorChooser.getSelected()).intValue());
        lights.checkCountdown();
        lights.fire();
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
        Catapult.getInstance().control();
        if(Logitech.getInstance().getAbutton()) {
            RobotDrive.getInstance().tankDrive(.1, .1);
        } else {
            RobotDrive.getInstance().tankDrive(0, 0);
        }
    }

}
