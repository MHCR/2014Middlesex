
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
        colorChooser.addDefault("Alliance",new Integer(0));
        colorChooser.addObject("Blue",new Integer(1));
        colorChooser.addObject("Red",new Integer(2));
        colorChooser.addObject("Green",new Integer(3));
        colorChooser.addObject("Purple",new Integer(4));
        colorChooser.addObject("Yellow",new Integer(5));
        colorChooser.addObject("Teal",new Integer(6));
        colorChooser.addObject("White",new Integer(7));
        colorChooser.addObject("Off",new Integer(8));
        SmartDashboard.putData("Team Color",colorChooser);
        autoChooser = new SendableChooser();
        autoChooser.addDefault("Autonomous",new DriveRoutine());
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
        lights.setColor(((Integer) colorChooser.getSelected()).intValue());
        lights.checkCountdown();
        lights.fire(Catapult.getInstance().isFiring());
        lights.searching(Lights.NO);
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
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        robotPeriodic();
        auto = (Runnable) autoChooser.getSelected();
        auto.run();
    }

    public void teleopInit() {
        
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        robotPeriodic();
        //System.out.println("Range: " + SmartDashboard.getNumber("Range"));
        //SmartDashboard.putNumber("Range", 47);
        //System.out.println(table.getBoolean("hot"));
        LCD.print(1, "Range");
        RobotDrive.getInstance().control();
        DidlerControl.getInstance().control();
        Catapult.getInstance().control();
    }

    public void testInit() {
        
    }

    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        Watchdog.getInstance().feed();
        lights.setColor(((Integer) colorChooser.getSelected()).intValue());
        lights.fire(Catapult.getInstance().isFiring());
        lights.countdown();
    }

}
