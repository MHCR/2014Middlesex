/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Watchdog;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class David extends IterativeRobot {
	RobotDrive m_robotDrive;
	int m_dsPackets;

        static final int RIGHTSTICK_USBPORT = 1;
        static final int LEFTSTICK_USBPORT  = 2;
	Joystick m_rightStick;
	Joystick m_leftStick;

	static final int NUM_JOYSTICK_BUTTONS = 16;
	boolean[] m_rightStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
	boolean[] m_leftStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
        
        static final int LEFT_FRONT_DRIVEMOTOR = 1;
        static final int RIGHT_FRONT_DRIVEMOTOR = 2;
        static final int LEFT_REAR_DRIVEMOTOR = 3;
        static final int RIGHT_REAR_DRIVEMOTOR = 4;
        
    public David() {
	// Create a robot using standard right/left robot drive on PWMS 1, 2, 3, and #4
        m_robotDrive = new RobotDrive(LEFT_FRONT_DRIVEMOTOR, LEFT_REAR_DRIVEMOTOR,
                RIGHT_FRONT_DRIVEMOTOR, RIGHT_REAR_DRIVEMOTOR);

        m_dsPackets = 0;

        // Define joysticks being used at USB port #1 and USB port #2 on the Drivers Station
        m_rightStick = new Joystick(RIGHTSTICK_USBPORT);
        m_leftStick = new Joystick(LEFTSTICK_USBPORT);

        // Iterate over all the buttons on each joystick, setting state to false for each						// start counting buttons at button 1
        for (int buttonNum = 1; buttonNum <= NUM_JOYSTICK_BUTTONS; buttonNum++) {
                m_rightStickButtonState[buttonNum] = false;
                m_leftStickButtonState[buttonNum] = false;
        }
        System.out.println("David() constructor completed.\n");
    }
    
    public void bookkeeping() {
        Watchdog.getInstance().feed();
        m_dsPackets++;
    }
    
    public void robotInit() {
        System.out.println("RobotInit() completed.\n");
    }

    public void diabledPeriodic() {
	bookkeeping();
    }
    
    public void autonomousPeriodic() {
	bookkeeping();
    }

    public void teleopPeriodic() {
        bookkeeping();
        m_robotDrive.tankDrive(m_leftStick, m_rightStick);
    }
    
    public void testPeriodic() {
        bookkeeping();
    }
    
}
