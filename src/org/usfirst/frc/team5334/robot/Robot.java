
package org.usfirst.frc.team5334.robot;


import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
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
    
    SpeedController tiltTable;
    SpeedController roller;
    SpeedController boulderHolder;
    
    CANTalon FourBar;
    CANTalon leftShooter;
    CANTalon rightShooter;
    
    boolean stickDrive = true;
    
    boolean FourBarStop = true;
    boolean FourBarUp = false;
    boolean FourBarDown = false;
    
    boolean rollerInClick = false;
    boolean rollerOutClick = false;
    boolean rollerIn = false;
    boolean rollerOut = false;
    
    boolean tiltTableUp = false;
    boolean tiltTableDown = false;
    
    boolean shooterInClick = false;
    boolean shooterOutClick = false;
    boolean shooterIn = false;
    boolean shooterOut = false;
    
    boolean boulderIn = false;
    boolean boulderOut = false;
    
    DigitalInput fourBarForwardLimit;
    DigitalInput fourBarRearLimit;
    
    DigitalInput tiltTableForwardLimit;
    DigitalInput tiltTableRearLimit;
    
    DigitalInput boulderHolderLimit;
    
    final int LOW_GOAL_MODE = 0;
    final int HIGH_GOAL_MODE = 1;
    final int PICKUP_MODE = 2;
    final int START_MODE = 3;
    
    int currentMode = -1;
    int pastMode = -1;
    int newMode = -1;
    
    int boulderOutCount = 0;
    
    final String defaultAuto = "Default";
    final String customAuto = "My Auto";
    SendableChooser chooser;

    public Robot() {
        //myRobot = new RobotDrive(0, 1);
        

        leftStick = new Joystick(0);
        rightStick = new Joystick(1);
        
        // substitute for Talons (SR) for competition robot

        /*
        // Competition Bot
        leftFront  = new Talon(0);
        leftRear   = new Talon(1);
        rightFront = new Talon(2);
        rightRear  = new Talon(3);
        
        tiltTable = new Talon(5);        
        roller = new Talon(6);
        
        boulderHolder = new Talon(7);
        */
        
        // practice Bot
        leftFront  = new Victor(0);
        leftRear   = new Victor(1);
        rightFront = new Victor(2);
        rightRear  = new Victor(3);
        
        tiltTable     = new Victor(5);
        roller        = new Victor(6);
        
        boulderHolder = new Victor(7);
        
        myRobot = new RobotDrive(leftFront,leftRear,rightFront,rightRear);
        revRobot = new RobotDrive(rightFront,rightRear,leftFront,leftRear);
        
        myRobot.setExpiration(0.1);
        
        // 4-bar linkage assembly up-down
        FourBar = new CANTalon(1);
        leftShooter = new CANTalon(2);
        rightShooter = new CANTalon(3);

        
        fourBarForwardLimit = new DigitalInput(0);
        fourBarRearLimit = new DigitalInput(1);
        
        tiltTableForwardLimit = new DigitalInput(2);
        tiltTableRearLimit = new DigitalInput(3);
        
        boulderHolderLimit = new DigitalInput(4);
        
        currentMode = START_MODE;
        pastMode = -1;
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
        leftShooter.enableBrakeMode(false);
        leftShooter.set(0);
        leftShooter.disable();
		rightShooter.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
		rightShooter.enableBrakeMode(false);
		rightShooter.set(0);
		rightShooter.disable();
		
		rightShooter.changeControlMode(CANTalon.TalonControlMode.Follower);
		rightShooter.set(leftShooter.getDeviceID());
		rightShooter.reverseOutput(true);
		
		currentMode = START_MODE;
		pastMode = -1;
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
            
            // Operator Switch Mode
            double input = rightStick.getY();
    		boolean trigger = rightStick.getRawButton(1);
            boolean b3 = rightStick.getRawButton(3);
            boolean b2 = rightStick.getRawButton(2);
            
            if (input < -0.3) {
            	// push joystick forward
            	newMode = LOW_GOAL_MODE;
            } else if (input > 0.3) {
            	// pull joystick back
            	newMode = HIGH_GOAL_MODE;
            } else if (b3) {
            	newMode = PICKUP_MODE;
            } else if (b2) {
            	newMode = START_MODE;
            } else {
            	// no control change, no mode change
            	newMode = currentMode;
            }
            
            if (newMode != currentMode) {
            	// mode change
            	pastMode = currentMode;
            	currentMode = newMode;
            	// stop all movement
            	// this might be problematic if boulder is mid-travel
            	tiltTable.set(0);
            	roller.set(0);
            	boulderHolder.set(0);
        		FourBar.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        		FourBar.enable();
        		FourBar.set(0);
        		FourBar.disable();
        		leftShooter.set(0);
        		leftShooter.disable();
        		rightShooter.set(0);
        		rightShooter.disable();
        		
        		// reset all counters
        		boulderOutCount = 0;
            	
            }
            
            // Operator Dispatch
            if (currentMode == START_MODE) {
            	runStartMode();
            }
            if (currentMode == PICKUP_MODE) {
            	runPickupMode();
            }
            if (currentMode == LOW_GOAL_MODE) {
            	runLowGoalMode();
            }
            if (currentMode == HIGH_GOAL_MODE) {
            	runHighGoalMode();
            }
            
            //return;
/*            
            // run Four-Bar Linkage
        	if (stickDrive) {
        		FourBar.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        		FourBar.enable();
        		
        		
        		if (!trigger) {
	        		// joystick pullback = postive values
	        		// joystick pull back = FourBar up = rearlimit controlled
	        		// -/neg motor turn -> FourBar up
	        		// +/pos motor turn -> FourBar down
	        		
	        		if ((input > -0.2) && (input < 0.2)) {
	        			FourBar.set(0);
	        		}
	        		// FourBar up  = pull joystick back
	        		if (!fourBarRearLimit.get() && (input > 0.2)) {
	        			FourBar.set(-0.5);
	        		} else if (fourBarRearLimit.get() && (input > 0.2)) {
	        			FourBar.set(0);
	        		}
	        		
	        		// FourBar down = push joystick forward
	        		if (!fourBarForwardLimit.get() && (input < -0.2)) {
	        			FourBar.set(0.5);
	        		} else if (fourBarForwardLimit.get() && (input < -0.2)) {
	        			FourBar.set(0);
	        		}
	        		// pull back on rightstick SRX turns green
        		
        		} else {
        			
        			// run tiltTable
        			// +/pos motor turn -> tiltTable up
        			
	        		if ((input > -0.2) && (input < 0.2)) {
	        			tiltTable.set(0);
	        		}
	        		// tiltTable up  = pull joystick back
	        		if (!tiltTableRearLimit.get() && (input > 0.2)) {
	        			tiltTable.set(1.0);
	        		} else if (tiltTableRearLimit.get() && (input > 0.2)) {
	        			tiltTable.set(0);
	        		}
	        		
	        		// tiltTable down = push joystick forward
	        		if (!tiltTableForwardLimit.get() && (input < -0.2)) {
	        			tiltTable.set(-1.0);
	        		} else if (tiltTableForwardLimit.get() && (input < -0.2)) {
	        			tiltTable.set(0);
	        		}
        			
        		
        		}
        		
        		
        		
        	}
        	
        	// tileTable up and down
        	if (rightStick.getRawButton(3)) {
        		// tiltTable down
        		tiltTableDown = true;
        		tiltTableUp = false;
        	}
        	
        	if (rightStick.getRawButton(2)) {
        		tiltTableUp = true;
        		tiltTableDown = false;
        	}
        	
        	if (!rightStick.getRawButton(2) && !rightStick.getRawButton(3)) {
        		tiltTableUp = false;
        		tiltTableDown = false;
        	}
        	
        	if (tiltTableUp) {
        		if (!tiltTableRearLimit.get()) {
        			tiltTable.set(1.0);
        		} else {
        			tiltTable.set(0);
        		}
        	} else if (tiltTableDown) {
        		if (!tiltTableForwardLimit.get()) {
        			tiltTable.set(-1.0);
        		} else {
        			tiltTable.set(0);
        		}
        	} else {
        		tiltTable.set(0);
        	}
        	
        	// button 6 runs roller out
        	// should only run roller out if tiltTable is down
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
        	//  run roller in only if tiltTable is down
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
        			leftShooter.set(0);
        			leftShooter.disable();
        			rightShooter.disable();
        		} else {
        			// start shooter
        			shooterIn = true;
        			shooterOut = false;
        			leftShooter.disable();
        			rightShooter.disable();
        		}
        	}
        	
        	if (shooterIn) {
        		
        		if (leftShooter.getOutputVoltage() > -0.1) {
        			leftShooter.changeControlMode(CANTalon.TalonControlMode.Speed);
        		
        			//100% throttle = 11.64V  velocity = 40064        		
        			//leftShooter.setPID(.21, 0.0, 0.0);
        			//leftShooter.setF(1.5);

        			leftShooter.setPID(0.03, 0, 0);
        			leftShooter.setF(0.8);
        			
        			rightShooter.changeControlMode(CANTalon.TalonControlMode.Follower);
        			rightShooter.set(leftShooter.getDeviceID());
        			rightShooter.reverseOutput(true);
        			
        			leftShooter.set(2000);
        			leftShooter.enable();
        			rightShooter.enable();
        		}
        		
        		
        		//rightShooter.set(0.5225);
        		//leftShooter.set(-0.5225);
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
        			leftShooter.set(0);
        			leftShooter.disable();
        			rightShooter.disable();
        		} else {
        			// start shooter
        			shooterOut = true;
        			shooterIn = false;
        			leftShooter.disable();
        			rightShooter.disable();
        		}
        	}
        	
        	if (shooterOut) {
        		
        		
        		
        		if (leftShooter.getOutputVoltage() < 0.1) {
        			leftShooter.changeControlMode(CANTalon.TalonControlMode.Speed);
        		
        			//100% throttle = 11.64V  velocity = 40064        		
        			//leftShooter.setPID(.21, 0.0, 0.0);
        			//leftShooter.setF(1.5);

        			leftShooter.setPID(0.03, 0, 0);
        			leftShooter.setF(0.8);
        			
        			rightShooter.changeControlMode(CANTalon.TalonControlMode.Follower);
        			rightShooter.set(leftShooter.getDeviceID());
        			rightShooter.reverseOutput(true);
        			
        			leftShooter.set(-2250);
        			leftShooter.enable();
        			rightShooter.enable();
        		}
        		
        		//rightShooter.set(-1.0);
        		//leftShooter.set(1.0);
        		shooterIn = false;
        	}
        	
        	if (!shooterIn && !shooterOut) {
        		leftShooter.set(0);
        		rightShooter.set(0);
        		leftShooter.disable();
        		rightShooter.disable();
        	}
        	
*/        	
            Timer.delay(0.005);		// wait for a motor update time
        }
    }
    
    public void runStartMode () {
    	// FourBarUp
    	// tiltTable Up
    	FourBarUp = true;
    	FourBarDown = false;
    	runFourBarUpDown();
    	
    	tiltTableUp = true;
    	tiltTableDown = false;
    	runTiltTableUpDown();
    	// trigger non-functional
    	
    }
    
    public void runPickupMode () {
    	// FourBar Down
    	// tiltTable Down
    	FourBarUp = false;
    	FourBarDown = true;
    	runFourBarUpDown();
    	
    	tiltTableUp = false;
    	tiltTableDown = true;
    	runTiltTableUpDown();	
    	
    	runPickupSequence();
    }
    
    public void runLowGoalMode () {
    	// FourBar Up
    	// tiltTable Down
    	FourBarUp = true;
    	FourBarDown = false;
    	runFourBarUpDown();
    	
    	tiltTableUp = false;
    	tiltTableDown = true;
    	runTiltTableUpDown();
    	// trigger shoots boulder if boulder is armed
    	runShooterOutSequence();
    }

    public void runHighGoalMode () {
    	// FourBar Down
    	// tiltTable Up
    	FourBarUp = false;
    	FourBarDown = true;
    	runFourBarUpDown();

    	tiltTableUp = true;
    	tiltTableDown = false;
    	runTiltTableUpDown();
    	// trigger shoots boulder if boulder is armed
    	
    	runShooterOutSequence();
    }
    
    public void runPickupSequence() {
    	boolean trigger = rightStick.getRawButton(1);
    	boolean isBoulderIn = boulderHolderLimit.get();
    	
    	if (!isBoulderIn) {
    		// run pickup only if no boulder in
    		
    		if (trigger) {
    			rollerIn = true;
    			rollerOut = false;
    			runRoller();
    			
    			shooterIn = true;
    			shooterOut = false;
    			runShooter();
    			
    			boulderIn = true;
    			boulderOut = false;
    			runBoulder();
    		} else {
    			
        		// if boulder is not in, then stop all
        		rollerIn = false;
        		rollerOut = false;
        		runRoller();
        		
        		shooterIn = false;
        		shooterOut = false;
        		runShooter();
        		
        		boulderIn = false;
        		boulderOut = false;
        		runBoulder();
    		}
    	} else {
    		
    		// if boulder is in, then stop all
    		rollerIn = false;
    		rollerOut = false;
    		runRoller();
    		
    		shooterIn = false;
    		shooterOut = false;
    		runShooter();
    		
    		boulderIn = false;
    		boulderOut = false;
    		runBoulder();
    	}
    }
    
    public void runShooterOutSequence () {
    	boolean trigger = rightStick.getRawButton(1);
    	boolean isBoulderIn = boulderHolderLimit.get();
    	
    	if (isBoulderIn) {
    	
        	if (trigger) {
        		// trigger in, start shooter motors, reset boulderOutCount
        		shooterOutClick = true;
        		
        		shooterOut = true;
        		shooterIn = false;
        		runShooter();
        		boulderOutCount = 0;
        		
        	} else if (shooterOutClick) {
        		// trigger release, start boulder motor
        		
        		shooterOutClick = false;
        		
        		boulderOut = true;        		
        		boulderIn = false;
        		runBoulder();

        	}
    		
    	
    	} else {
    		boulderOutCount++;
    		if (boulderOutCount > 33) {
    			boulderOutCount = 0;
    			// stop shooter motors
    			shooterOut = false;
    			shooterIn = false;
    			runShooter();
    			// stop boulder motor
    			boulderOut = false;
    			boulderIn = false;
    			runBoulder();
    		}
    		
    	}
    }
    
    public void runRoller() {
    	if (rollerOut) {
    		roller.set(-1.0);
    		rollerIn = false;
    	}
    	if (rollerIn) {
    		roller.set(1.0);
    		rollerOut = false;
    	}
    	if (!rollerOut && !rollerIn) {
    		roller.set(0);
    	}
    }
    
    public void runBoulder() {
    	if (boulderOut) {
    		boulderHolder.set(0.5);
    		boulderIn = false;
    	}
    	if (boulderIn) {
    		boulderHolder.set(-0.5);
    		boulderOut = false;
    	}
    	if (!boulderOut && !boulderIn) {
    		boulderHolder.set(0);
    	}
    }
    
    public void runShooter () {
    	if (shooterOut) {
    		    		
    		if (leftShooter.getOutputVoltage() < 0.1) {
    			leftShooter.changeControlMode(CANTalon.TalonControlMode.Speed);
    		
    			//100% throttle = 11.64V  velocity = 40064        		
    			//leftShooter.setPID(.21, 0.0, 0.0);
    			//leftShooter.setF(1.5);

    			leftShooter.setPID(0.03, 0, 0);
    			leftShooter.setF(0.8);
    			
    			rightShooter.changeControlMode(CANTalon.TalonControlMode.Follower);
    			rightShooter.set(leftShooter.getDeviceID());
    			rightShooter.reverseOutput(true);
    			
    			leftShooter.set(-2250);
    			leftShooter.enable();
    			rightShooter.enable();
    		}
    		
    		//rightShooter.set(-1.0);
    		//leftShooter.set(1.0);
    		shooterIn = false;
    	}
    	
    	if (shooterIn) {
    		
    		if (leftShooter.getOutputVoltage() > -0.1) {
    			leftShooter.changeControlMode(CANTalon.TalonControlMode.Speed);
    		
    			//100% throttle = 11.64V  velocity = 40064        		
    			//leftShooter.setPID(.21, 0.0, 0.0);
    			//leftShooter.setF(1.5);

    			leftShooter.setPID(0.03, 0, 0);
    			leftShooter.setF(0.8);
    			
    			rightShooter.changeControlMode(CANTalon.TalonControlMode.Follower);
    			rightShooter.set(leftShooter.getDeviceID());
    			rightShooter.reverseOutput(true);
    			
    			leftShooter.set(2000);
    			leftShooter.enable();
    			rightShooter.enable();
    		}
    	    		
    		//rightShooter.set(0.5225);
    		//leftShooter.set(-0.5225);
    		shooterOut = false;
    	}
    	
    	if (!shooterIn && !shooterOut) {
    		leftShooter.set(0);
    		rightShooter.set(0);
    		leftShooter.disable();
    		rightShooter.disable();
    	}
    }
    
    public void runFourBarUpDown () {
		// FourBar up  = pull joystick back
    	
    	if (FourBarUp) {
    		if (!fourBarRearLimit.get()) {
        		FourBar.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        		FourBar.enable();
    			FourBar.set(-0.5);
    		} else {
    			FourBar.set(0);
    			FourBar.disable();
    		}
    	} else if (FourBarDown) {
    		if (!fourBarForwardLimit.get()) {
        		FourBar.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        		FourBar.enable();
    			FourBar.set(0.5);
    		} else {
    			FourBar.set(0);
    			FourBar.disable();
    		}
    	} else {
    		FourBar.set(0);
    		FourBar.disable();
    	}
/*    	
		if (!fourBarRearLimit.get() && (input > 0.2)) {
			FourBar.set(-0.5);
		} else if (fourBarRearLimit.get() && (input > 0.2)) {
			FourBar.set(0);
		}
		
		// FourBar down = push joystick forward
		if (!fourBarForwardLimit.get() && (input < -0.2)) {
			FourBar.set(0.5);
		} else if (fourBarForwardLimit.get() && (input < -0.2)) {
			FourBar.set(0);
		}
		*/
    }
    
    public void runTiltTableUpDown () {
       	// tileTable up and down
    	
/*    	
    	if (rightStick.getRawButton(3)) {
    		// tiltTable down
    		tiltTableDown = true;
    		tiltTableUp = false;
    	}
    	
    	if (rightStick.getRawButton(2)) {
    		tiltTableUp = true;
    		tiltTableDown = false;
    	}
    	
    	if (!rightStick.getRawButton(2) && !rightStick.getRawButton(3)) {
    		tiltTableUp = false;
    		tiltTableDown = false;
    	}
*/    	
    	if (tiltTableUp) {
    		if (!tiltTableRearLimit.get()) {
    			tiltTable.set(1.0);
    		} else {
    			tiltTable.set(0);
    		}
    	} else if (tiltTableDown) {
    		if (!tiltTableForwardLimit.get()) {
    			tiltTable.set(-1.0);
    		} else {
    			tiltTable.set(0);
    		}
    	} else {
    		tiltTable.set(0);
    	}
    }
    
    /**
     * Runs during test mode
     */
    public void test() {
    }
}
