
package org.usfirst.frc.team5334.robot;


import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class.
 * The SampleRobot class is the base of a robot application that will automatically call your
 * Autonomous and OperatorControl methods at the right time as controlled by the switches on
 * the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're inexperienced,
 * don't. Unless you know what you are doing, complex code will be much more difficult under
 * this system. Use IterativeRobot or Command-Based instead if you're new.
 */
public class Robot extends SampleRobot {
    RobotDrive myRobot;  // class that handles basic drive operations
    RobotDrive revRobot;
    
    Joystick leftStick;  // set to ID 1 in DriverStation
    Joystick rightStick; // set to ID 2 in DriverStation
    
    SpeedController leftFront;
    SpeedController leftRear;
    SpeedController rightFront;
    SpeedController rightRear;
    
    SpeedController roller;
    
    CANTalon FourBar;
    CANTalon leftShooter;
    CANTalon rightShooter;
    
    boolean stickDrive = true;
    
    boolean FourBarStop = true;
    boolean FourBarUp = true;
    
    boolean rollerInClick = false;
    boolean rollerOutClick = false;
    boolean rollerIn = false;
    boolean rollerOut = false;
    
    boolean shooterInClick = false;
    boolean shooterOutClick = false;
    boolean shooterIn = false;
    boolean shooterOut = false;
    
    DigitalInput forwardLimit;
    DigitalInput rearLimit;
    
    final String defaultAuto = "Default";
    final String customAuto = "My Auto";
    SendableChooser chooser;

    public Robot() {
        //myRobot = new RobotDrive(0, 1);
        

        leftStick = new Joystick(0);
        rightStick = new Joystick(1);
        
        // substitute for Talons (SR) for competition robot
   
        leftFront  = new Talon(0);
        leftRear   = new Talon(1);
        rightFront = new Talon(2);
        rightRear  = new Talon(3);
        
        roller = new Talon(6);
        
        myRobot = new RobotDrive(leftFront,leftRear,rightFront,rightRear);
        revRobot = new RobotDrive(rightFront,rightRear,leftFront,leftRear);
        
        myRobot.setExpiration(0.1);
        
        // 4-bar linkage assembly up-down
        FourBar = new CANTalon(1);
        leftShooter = new CANTalon(2);
        rightShooter = new CANTalon(3);

        
        forwardLimit = new DigitalInput(0);
        rearLimit = new DigitalInput(1);
    }
    
    public void robotInit() {
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("My Auto", customAuto);
        SmartDashboard.putData("Auto modes", chooser);
        
        // switch the PID control mode for the FourBar Talon SRX to position seeking
   
        FourBar.configMaxOutputVoltage(.5);
        FourBar.configPeakOutputVoltage(.5, -.5);
        FourBar.configNominalOutputVoltage(0, 0);
        FourBar.setPosition(0);
        //FourBar.setAllowableClosedLoopErr(5);
        leftShooter.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        leftShooter.set(0);
		rightShooter.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
		rightShooter.enable();
		
    }

	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString line to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the if-else structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
    public void autonomous() {
    	
    	String autoSelected = (String) chooser.getSelected();
//		String autoSelected = SmartDashboard.getString("Auto Selector", defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
    	
    	switch(autoSelected) {
    	case customAuto:
            myRobot.setSafetyEnabled(false);
            myRobot.drive(-0.5, 1.0);	// spin at half speed
            Timer.delay(2.0);		//    for 2 seconds
            myRobot.drive(0.0, 0.0);	// stop robot
            break;
    	case defaultAuto:
    	default:
            myRobot.setSafetyEnabled(false);
            myRobot.drive(-0.5, 0.0);	// drive forwards half speed
            Timer.delay(2.0);		//    for 2 seconds
            myRobot.drive(0.0, 0.0);	// stop robot
            break;
    	}
    }

    /**
     * Runs the motors with arcade steering.
     */
    public void operatorControl() {
        myRobot.setSafetyEnabled(true);
        while (isOperatorControl() && isEnabled()) {
            //myRobot.arcadeDrive(stick); // drive with arcade style (use right stick)
            
            if (leftStick.getRawButton(1)) {  // trigger
            	revRobot.arcadeDrive(leftStick);
            } else {
            	myRobot.arcadeDrive(leftStick);
            }
            
            // run Four-Bar Linkage
        	if (stickDrive) {
        		FourBar.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        		FourBar.enable();
        		
        		if ((rightStick.getY() > -0.2) && (rightStick.getY() < 0.2)) {
        			FourBar.set(0);
        		}
        		if (!rearLimit.get() && (rightStick.getY() > 0.2)) {
        			FourBar.set(-0.4);
        		} else if (rearLimit.get() && (rightStick.getY() > 0.2)) {
        			FourBar.set(0);
        		}
        		if (!forwardLimit.get() && (rightStick.getY() < -0.2)) {
        			FourBar.set(0.4);
        		} else if (forwardLimit.get() && (rightStick.getY() < -0.2)) {
        			FourBar.set(0);
        		}
        		// pull back on rightstick SRX turns green
        		
        		double input = rightStick.getY();
        		
        	}
        	
        	// button 6 runs roller out
        	if (rightStick.getRawButton(6)) {
        		rollerOutClick = true;       		
        	} else if (rollerOutClick) {
        		rollerOutClick = false;
        		if (rollerOut) {
        			// stop roller
        			rollerOut = false;
        			rollerIn = false;
        			roller.set(0);
        		} else {
        			// start roller
        			rollerOut = true;
        			rollerIn = false;
        		}
        	}
        	
        	if (rollerOut) {
        		roller.set(1.0);
        		rollerIn = false;
        	}
        	
        	// button 7 runs roller in
        	if (rightStick.getRawButton(7)) {
        		rollerInClick = true;       		
        	} else if (rollerInClick) {
        		rollerInClick = false;
        		if (rollerIn) {
        			// stop roller
        			rollerIn = false;
        			rollerOut = false;
        			shooterIn = false;
        			roller.set(0);
        		} else {
        			// start roller
        			rollerIn = true;
        			shooterIn = true;
        			rollerOut = false;
        		}
        	}
        	
        	if (rollerIn) {
        		roller.set(-1.0);
        		rollerOut = false;
        	}
        	
        	// shooter in
        	if (rightStick.getRawButton(10)) {
        		shooterInClick = true;       		
        	} else if (shooterInClick) {
        		shooterInClick = false;
        		if (shooterIn) {
        			// stop shooter
        			shooterIn = false;
        			shooterOut = false;
        			rightShooter.set(0);
        			leftShooter.set(0);;
        		} else {
        			// start shooter
        			shooterIn = true;
        			shooterOut = false;
        		}
        	}
        	
        	if (shooterIn) {
        		rightShooter.set(0.5225);
        		leftShooter.set(-0.5225);
        		shooterOut = false;
        	}

        	// shooter out
        	if (rightStick.getRawButton(11)) {
        		shooterOutClick = true;       		
        	} else if (shooterOutClick) {
        		shooterOutClick = false;
        		if (shooterOut) {
        			// stop shooter
        			shooterOut = false;
        			shooterIn = false;
        			rightShooter.set(0);
        			leftShooter.set(0);;
        		} else {
        			// start shooter
        			shooterOut = true;
        			shooterIn = false;
        		}
        	}
        	
        	if (shooterOut) {
        		rightShooter.set(-1.0);
        		leftShooter.set(1.0);
        		shooterIn = false;
        	}
        	
            Timer.delay(0.005);		// wait for a motor update time
        }
    }

    /**
     * Runs during test mode
     */
    public void test() {
    }
}
