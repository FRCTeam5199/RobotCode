package org.usfirst.frc.team5199.robot;

import org.opencv.core.Mat;

import com.ctre.CANTalon;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * *****************************************************************************
 * ***************************************************** Robot Dolphins from
 * Outer Space should use the extension SampleRobot due to its efficiency in
 * looping quickly through the tele-op periodic IterativeRobot extension should
 * be avoided because a fast iteration rate is absolutely necessary (1000+
 * loops/sec vs 20 loops/sec)
 * 
 * 
 */
public class Robot extends SampleRobot {
	public int counter = 0;
	double current;
	Relay relay;
	Pixy pixyGear, pixyShooter;
	PixyProcess pixyGearProc;
	PixyProcess pixyShootProc;
	DualPixyProcess DualPixyProc;
	PixyFunctions pixyFunctionsGear, pixyFunctionsShooter;
	DualPixyFunctions DualPixyFunc;
	CANTalon shooter;
	Spark leftMotor, rightMotor, climber, transport, sweeper;
	DoubleSolenoid solenoid;
	CANTalon turret;
	UltrasonicData ultraData;
	UltrasonicFunctions ultraFunctions;
	EncoderDriveFunctions driveEncoders;
	EncoderShooterFunctions shootEncFunc;
	Encoder shooterEncoder;
	RobotDrive robotDriver;
	Joystick xBox, stick;
	GyroFunctions gyro;
	Servo servo, servo2;
	ServoDude shooterServo, servoBoy;
	double strokePos;
	double currAngle;
	String autoMode;
	Compressor compressor;
	int autoStep;
	int gearLoadStep;
	boolean flipped = false;
	CameraServer server;
	boolean first = true;
	double startTime = 0;
	PowerDistributionPanel pdp;
	public static int step = 0;
	int stepShooter = 0;
	int shooterSpeed = 2000;
	public static double position = 0.0, increment = 0.001;
	public static double straightMod = 1, turnMod = .6, driveMod = 1;
	public static int shiftStep = 0;
	public static int shifter = 0;
	public static double shootPow = 0;
	boolean left;
	public static final int encoderAVGArraySize = 85;
	public static boolean firstBufferRight = true;
	public static double sumBufferRight = 0;
	public static int counterRight = 0;
	public static Double[] distanceArrayRight;
	public static boolean lightOn = false;
	public static int shooterMode = 0;
	public static boolean servoSet = false;
	public static boolean firstRun = true;
	public static int lightStep = 0;
	public static boolean spinUp = false;
	public static double AVG = 0;

	public Robot() {
		firstRun = true;
		spinUp = false;
	}

	@Override
	public void robotInit() {
		distanceArrayRight = new Double[encoderAVGArraySize];
		for (int i = 0; i < encoderAVGArraySize; i++) {
			distanceArrayRight[i] = 0.0;
		}
		counter = 0;
		pdp = new PowerDistributionPanel();
		rightMotor = new Spark(RobotMap.rightMotor);
		leftMotor = new Spark(RobotMap.leftMotor);
		shooter = new CANTalon(RobotMap.shooter);
		sweeper = new Spark(RobotMap.intake);
		transport = new Spark(RobotMap.transport);
		climber = new Spark(RobotMap.climber);
		turret = new CANTalon(RobotMap.turret);
		driveEncoders = new EncoderDriveFunctions(rightMotor, leftMotor, shooter);
		driveEncoders.resetDrive();
		// shootEncFunc = new EncoderShooterFunctions(shooter, turret);
		shooterEncoder = new Encoder(RobotMap.encoderShooterDIOA, RobotMap.encoderShooterDIOB, false,
				Encoder.EncodingType.k4X);
		ultraData = new UltrasonicData(RobotMap.ultraRightEcho, RobotMap.ultraRightPing, RobotMap.ultraLeftEcho,
				RobotMap.ultraLeftPing);
		ultraFunctions = new UltrasonicFunctions(ultraData, rightMotor, leftMotor);
		robotDriver = new RobotDrive(rightMotor, leftMotor);
		stick = new Joystick(RobotMap.joyStickPort);
		xBox = new Joystick(RobotMap.xBoxPort);
		gyro = new GyroFunctions(rightMotor, leftMotor);
		SmartDashboard.putNumber("Gyro Adjust", 8.0);
		pixyGear = new Pixy(RobotMap.pixyGear);
		pixyGearProc = new PixyProcess(pixyGear);
		pixyShooter = new Pixy(RobotMap.pixyShoot);
		pixyShootProc = new PixyProcess(pixyShooter);
		pixyFunctionsGear = new PixyFunctions(pixyGear, ultraFunctions, driveEncoders, robotDriver);
		pixyFunctionsShooter = new PixyFunctions(pixyShooter, turret);
		DualPixyFunc = new DualPixyFunctions(ultraFunctions, driveEncoders, robotDriver);
		servo = new Servo(RobotMap.shooterServo);
		DualPixyProc = new DualPixyProcess();
		shooterServo = new ServoDude(servo);
		servo2 = new Servo(RobotMap.shooterServo2);
		servoBoy = new ServoDude(servo2);
		strokePos = 0.0;
		shootPow = 0;
		solenoid = new DoubleSolenoid(4, 5);
		gyro.gyro.calibrate();
		shooterEncoder.reset();
		shooterEncoder.setDistancePerPulse(RobotMap.inchesPerRotation / 2);
		spinUp = false;
		relay = new Relay(0);
		server = CameraServer.getInstance();
		server.startAutomaticCapture(0);
		SmartDashboard.putString("Autonomous Mode", "Enter Value");

		// TODO change the parameters of the pixyFunctions to give it the
		// controllers necessary to shoot
		// Thread t = new Thread(() -> {
		//
		// boolean allowCam1 = false;
		//
		// UsbCamera camera1 =
		// CameraServer.getInstance().startAutomaticCapture(0);
		// camera1.setResolution(320, 240);
		// camera1.setFPS(30);
		// UsbCamera camera2 =
		// CameraServer.getInstance().startAutomaticCapture(1);
		// camera2.setResolution(320, 240);
		// camera2.setFPS(30);
		//
		// CvSink cvSink1 = CameraServer.getInstance().getVideo(camera1);
		// CvSink cvSink2 = CameraServer.getInstance().getVideo(camera2);
		// CvSource outputStream =
		// CameraServer.getInstance().putVideo("Switcher", 320, 240);
		//
		// Mat image = new Mat();
		//
		// while (!Thread.interrupted()) {
		//
		// if (xBox.getRawButton(5)) {
		// allowCam1 = !allowCam1;
		// }
		// // if (flipped) {
		// // cvSink1.setEnabled(false);
		// // cvSink2.setEnabled(true);
		// // cvSink2.grabFrame(image);
		// // } else
		// if (allowCam1) {
		// cvSink2.setEnabled(false);
		// cvSink1.setEnabled(true);
		// cvSink1.grabFrame(image);
		// } else {
		// cvSink1.setEnabled(false);
		// cvSink2.setEnabled(true);
		// cvSink2.grabFrame(image);
		// }
		//
		// outputStream.putFrame(image);
		// }
		//
		// });
		// t.start();

		autoMode = SmartDashboard.getString("Autonomous Mode");
		autoStep = 0;
		driveEncoders.initEncoders();
		startTime = 0;
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * if-else structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomous() {
		autoMode = SmartDashboard.getString("Autonomous Mode");
		SmartDashboard.putNumber("Auto Step", autoStep);
		SmartDashboard.putNumber("Right Ultra", ultraData.distanceRight());
		SmartDashboard.putNumber("left Ultra", ultraData.distanceLeft());
		relay.set(Relay.Value.kForward);
		spinUp = false;
		AVG = 0;
		autoStep = 0;
		startTime = System.currentTimeMillis();
		driveEncoders.initEncoders();

		while (isAutonomous() && isEnabled()) {
			if (autoMode.equals("L") || autoMode.equals("l")) {
				if (autoStep == 0) {
					driveEncoders.initEncoders();
					solenoid.set(DoubleSolenoid.Value.kForward);
					startTime = System.currentTimeMillis();
					autoStep = 1;
				}

				if (autoStep == 1) {
					// TODO: Set time in robot map
					if (System.currentTimeMillis() - startTime > 1000) {
						solenoid.set(DoubleSolenoid.Value.kOff);
					}
					if (driveEncoders.driveStraightAuton(57)) {
						autoStep = 2;
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
					}
				}
				if (autoStep == 2) {
					if (driveEncoders.autonSixtyDegreeTurn(currAngle, false)) {
						autoStep = 3;
					}
				}
				if (autoStep == 3) {
					driveEncoders.initEncoders();
					autoStep = 4;
				}
				if (autoStep == 4) {
					if (driveEncoders.driveStraightAuton(21)) {
						autoStep = 5;
					}
				}
				if (autoStep == 5) {
					if (DualPixyFunc.turnAndGoStraightAuton()) {
						autoStep = 6;
					}
					if (ultraData.distanceRight() < 10 || ultraData.distanceLeft() < 10) {
						autoStep = 6;
					}
				}

				if (autoStep == 6) {
					if (ultraFunctions.driveFowardAuton(4)) {
						autoStep = 7;
					}
				}
				if (autoStep == 7) {
					robotDriver.stop();
				}
			} else if (autoMode.equals("C") || autoMode.equals("c")) {

				AVG = this.EncoderAVG();
				SmartDashboard.putNumber("AVG Auton RPM", AVG);
				SmartDashboard.putBoolean("Spin up", spinUp);
				SmartDashboard.putNumber("Auto Step", autoStep);
				if (spinUp) {
					// For the center gear position
					if (AVG < 2000) {
						shooter.set(1);
					} else if (AVG < 2500) {
						shooter.set(.9);
					} else if (AVG < 3000) {
						shooter.set(.8);
					} else if (AVG < 3300) {
						shooter.set(.6);
					} else if (AVG < 3445) {
						shootPow += 0.0004;
						shooter.set(shootPow + .6);
					} else {
						shooter.set(.6);
						shootPow -= 0.0001;
					}
				}
				if (autoStep == 0) {
					solenoid.set(DoubleSolenoid.Value.kForward);
					autoStep = 1;
				}

				if (autoStep == 1) {
					spinUp = true;
					// TODO: Set time in robot map
					if (System.currentTimeMillis() - startTime > 1000) {
						solenoid.set(DoubleSolenoid.Value.kOff);
					}
					// if (driveEncoders.driveStraightAuton(38)) {
					if (driveEncoders.driveStraightAuton(50)) {
						autoStep = 2;
					}
				}
				if (autoStep == 2) {
					// if (DualPixyFunc.turnAndGoStraightAuton()) {
					autoStep = 3;
					// }
					// if (ultraData.distanceRight() < 10 ||
					// ultraData.distanceLeft() < 10) {
					// autoStep = 3;
					// }
				}

				if (autoStep == 3) {
					if (ultraFunctions.driveFowardAutonSDR(3)) {
						autoStep = 4;
					}
				}
				if (autoStep == 4) {
					robotDriver.stop();
					transport.set(-1);
					sweeper.set(-1);
				}
			} else if (autoMode.equals("A") || autoMode.equals("a")) {
				if (autoStep == 0) {
					driveEncoders.initEncoders();
					solenoid.set(DoubleSolenoid.Value.kForward);
					startTime = System.currentTimeMillis();
					autoStep = 1;
				}

				if (autoStep == 1) {
					// TODO: Set time in robot map
					if (System.currentTimeMillis() - startTime > 1000) {
						solenoid.set(DoubleSolenoid.Value.kOff);
					}
					if (driveEncoders.driveStraightAuton(132)) {
						autoStep = 2;
					}
				}
				if (autoStep == 2) {
					robotDriver.stop();
					startTime = System.currentTimeMillis();
				}
			} else if (autoMode.equals("R") || autoMode.equals("r")) {
				if (autoStep == 0) {
					driveEncoders.initEncoders();
					solenoid.set(DoubleSolenoid.Value.kForward);
					startTime = System.currentTimeMillis();
					autoStep = 1;
				}

				if (autoStep == 1) {
					// TODO: Set time in robot map
					if (System.currentTimeMillis() - startTime > 1000) {
						solenoid.set(DoubleSolenoid.Value.kOff);
					}
					if (driveEncoders.driveStraightAuton(57)) {
						autoStep = 2;
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
					}
				}
				if (autoStep == 2) {
					if (driveEncoders.autonSixtyDegreeTurn(currAngle, true)) {
						autoStep = 3;
					}
				}
				if (autoStep == 3) {
					driveEncoders.initEncoders();
					autoStep = 4;
				}
				if (autoStep == 4) {
					if (driveEncoders.driveStraightAuton(21)) {
						autoStep = 5;
					}
				}
				if (autoStep == 5) {
					if (DualPixyFunc.turnAndGoStraightAuton()) {
						autoStep = 6;
					}
					if (ultraData.distanceRight() < 10 || ultraData.distanceLeft() < 10) {
						autoStep = 6;
					}
				}

				if (autoStep == 6) {
					if (ultraFunctions.driveFowardAuton(4)) {
						autoStep = 7;
					}
				}
				if (autoStep == 7) {
					robotDriver.stop();
				}
			} else if (autoMode.equals("2GB") || autoMode.equals("2gb")) {
				if (autoStep == 0) {
					driveEncoders.initEncoders();
					solenoid.set(DoubleSolenoid.Value.kForward);
					startTime = System.currentTimeMillis();
					autoStep = 1;
				}

				if (autoStep == 1) {
					// TODO: Set time in robot map
					if (System.currentTimeMillis() - startTime > 1000) {
						solenoid.set(DoubleSolenoid.Value.kOff);
					}
					if (driveEncoders.driveStraightAuton(38)) {
						autoStep = 2;
					}
				}
				if (autoStep == 2) {
					if (DualPixyFunc.turnAndGoStraightAuton()) {
						autoStep = 3;
					}
					if (ultraData.distanceRight() < 10 || ultraData.distanceLeft() < 10) {
						autoStep = 3;
					}
				}

				if (autoStep == 3) {
					if (ultraFunctions.driveFowardAuton(4)) {
						autoStep = 4;
					}
				}
				if (autoStep == 4) {
					robotDriver.stop();
					startTime = System.currentTimeMillis();
					Timer.delay(3);
					autoStep = 5;
				}
				if (autoStep == 5) {
					if (driveEncoders.driveStraightAuton(-24)) {
						autoStep = 6;
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
					}
				}
				if (autoStep == 6) {
					if (driveEncoders.autonNinetyDegreeTurn(currAngle, false)) {
						driveEncoders.initEncoders();
						autoStep = 7;
					}
				}
				if (autoStep == 7) {
					// TODO change number 34, this is experimental
					if (driveEncoders.driveStraightAuton(34)) {
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
						autoStep = 8;
					}
				}
				if (autoStep == 8) {
					if (driveEncoders.autonNinetyDegreeTurn(currAngle, false)) {
						driveEncoders.initEncoders();
						autoStep = 9;
					}
				}
				if (autoStep == 9) {
					if (driveEncoders.driveStraightAuton(10)) {
						autoStep = 10;
					}
				}
				if (autoStep == 10) {
					if (ultraFunctions.selfStraight()) {
						autoStep = 11;
					}
				}
				if (autoStep == 11) {
					if (ultraFunctions.driveFowardAuton(4)) {
						autoStep = 12;
					}
				}
				if (autoStep == 12) {
					robotDriver.stop();
					Timer.delay(3);
					driveEncoders.initEncoders();
					autoStep = 13;
				}
				if (autoStep == 13) {
					if (driveEncoders.driveStraightAuton(-24)) {
						autoStep = 14;
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
					}
				}
				if (autoStep == 14) {
					if (driveEncoders.autonNinetyDegreeTurn(currAngle, false)) {
						driveEncoders.initEncoders();
						autoStep = 15;
					}
				}
				if (autoStep == 15) {
					// TODO change number 34, this is a number derived from
					// using a
					// caliper to calculate pixels.
					if (driveEncoders.driveStraightAuton(34)) {
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
						autoStep = 16;
					}
				}
				if (autoStep == 16) {
					if (driveEncoders.autonNinetyDegreeTurn(currAngle, false)) {
						driveEncoders.initEncoders();
						autoStep = 17;
					}
				}
				if (autoStep == 17) {
					if (driveEncoders.driveStraightAuton(14)) {
						autoStep = 18;
					}
				}
				if (autoStep == 18) {
					if (DualPixyFunc.turnAndGoStraightAuton()) {
						autoStep = 19;
					}
					if (ultraData.distanceRight() < 10 || ultraData.distanceLeft() < 10) {
						autoStep = 19;
					}
				}

				if (autoStep == 19) {
					if (ultraFunctions.driveFowardAuton(4)) {
						autoStep = 20;
					}
				}
				if (autoStep == 20) {
					robotDriver.stop();
				}
			} else if (autoMode.equals("2GR") || autoMode.equals("2gr")) {
				if (autoStep == 0) {
					driveEncoders.initEncoders();
					solenoid.set(DoubleSolenoid.Value.kForward);
					startTime = System.currentTimeMillis();
					autoStep = 1;
				}

				if (autoStep == 1) {
					// TODO: Set time in robot map
					if (System.currentTimeMillis() - startTime > 1000) {
						solenoid.set(DoubleSolenoid.Value.kOff);
					}
					if (driveEncoders.driveStraightAuton(38)) {
						autoStep = 2;
					}
				}
				if (autoStep == 2) {
					if (DualPixyFunc.turnAndGoStraightAuton()) {
						autoStep = 3;
					}
					if (ultraData.distanceRight() < 10 || ultraData.distanceLeft() < 10) {
						autoStep = 3;
					}
				}

				if (autoStep == 3) {
					if (ultraFunctions.driveFowardAuton(4)) {
						autoStep = 4;
					}
				}
				if (autoStep == 4) {
					robotDriver.stop();
					startTime = System.currentTimeMillis();
					Timer.delay(3);
					autoStep = 5;
				}
				if (autoStep == 5) {
					if (driveEncoders.driveStraightAuton(-24)) {
						autoStep = 6;
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
					}
				}
				if (autoStep == 6) {
					if (driveEncoders.autonNinetyDegreeTurn(currAngle, true)) {
						driveEncoders.initEncoders();
						autoStep = 7;
					}
				}
				if (autoStep == 7) {
					// TODO change number 34, this is experimental
					if (driveEncoders.driveStraightAuton(34)) {
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
						autoStep = 8;
					}
				}
				if (autoStep == 8) {
					if (driveEncoders.autonNinetyDegreeTurn(currAngle, true)) {
						driveEncoders.initEncoders();
						autoStep = 9;
					}
				}
				if (autoStep == 9) {
					if (driveEncoders.driveStraightAuton(10)) {
						autoStep = 10;
					}
				}
				if (autoStep == 10) {
					if (ultraFunctions.selfStraight()) {
						autoStep = 11;
					}
				}
				if (autoStep == 11) {
					if (ultraFunctions.driveFowardAuton(4)) {
						autoStep = 12;
					}
				}
				if (autoStep == 12) {
					robotDriver.stop();
					Timer.delay(3);
					driveEncoders.initEncoders();
					autoStep = 13;
				}
				if (autoStep == 13) {
					if (driveEncoders.driveStraightAuton(-24)) {
						autoStep = 14;
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
					}
				}
				if (autoStep == 14) {
					if (driveEncoders.autonNinetyDegreeTurn(currAngle, true)) {
						driveEncoders.initEncoders();
						autoStep = 15;
					}
				}
				if (autoStep == 15) {
					// TODO change number 34, this is a number derived from
					// using a
					// caliper to calculate pixels.
					if (driveEncoders.driveStraightAuton(34)) {
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
						autoStep = 16;
					}
				}
				if (autoStep == 16) {
					if (driveEncoders.autonNinetyDegreeTurn(currAngle, true)) {
						driveEncoders.initEncoders();
						autoStep = 17;
					}
				}
				if (autoStep == 17) {
					if (driveEncoders.driveStraightAuton(14)) {
						autoStep = 18;
					}
				}
				if (autoStep == 18) {
					if (DualPixyFunc.turnAndGoStraightAuton()) {
						autoStep = 19;
					}
					if (ultraData.distanceRight() < 10 || ultraData.distanceLeft() < 10) {
						autoStep = 19;
					}
				}

				if (autoStep == 19) {
					if (ultraFunctions.driveFowardAuton(4)) {
						autoStep = 20;
					}
				}
				if (autoStep == 20) {
					robotDriver.stop();
				}
			}
		}
	}

	/**
	 * Runs the motors with arcade steering.
	 */
	@Override
	public void operatorControl() {
		// encoder.initEncoders();
		pixyGearProc.pixyTestReset();
		pixyShootProc.pixyTestReset();
		gyro.resetGyro();
		// shooterServo.set(0.0);
		// servoBoy.set(0.0);
		driveEncoders.initEncoders();
		lightOn = false;
		// SmartDashboard.putNumber("Gyro Adjust", 0);
		// SmartDashboard.putNumber("Override Angle", 0);
		// SmartDashboard.putNumber("Turn Speed", 0);
		counter = 0;
		while (isOperatorControl() && isEnabled()) {
			SmartDashboard.putNumber("Gyro Current Angle2", gyro.getAngle());
			pixyGearProc.pixyGearI2CTest();
			pixyGearProc.pixyGearTest();
			pixyShootProc.pixyShooterI2CTest();
			pixyShootProc.pixyShooterTest();
//			pixyShootProc.pixyI2CTest();
			SmartDashboard.putNumber("shooter data 47", pixyShootProc.shooterData()[0]);
			SmartDashboard.putNumber("Right Ultra", ultraData.distanceRight());
			SmartDashboard.putNumber("Left Ultra", ultraData.distanceLeft());
			SmartDashboard.putNumber("Ultra Average", ultraData.ultraAverage());

			AVG = this.EncoderAVG();

			if (stick.getRawButton(8) || xBox.getRawButton(10)) {
				// && (System.currentTimeMillis() - time) > 250) {
				if (lightStep == 0) {
					lightOn = !lightOn;
					lightStep = 1;
				}
			} else {
				lightStep = 0;
			}
			if (lightOn) {
				relay.set(Relay.Value.kForward);
			} else {
				relay.set(Relay.Value.kOff);
			}
			SmartDashboard.putBoolean("Light On", lightOn);

			// Assorted data for testing, to see if sensors are working etc.
			SmartDashboard.putNumber("Ultra Right", ultraData.distanceRight());
			SmartDashboard.putNumber("Ultra Left", ultraData.distanceLeft());

			if (stick.getRawButton(7)) {
				shooterMode = 1;
			} else if (stick.getRawButton(9)) {
				shooterMode = 2;
			} else if (stick.getRawButton(11)) {
				shooterMode = 3;
			}
			SmartDashboard.putNumber("Shooter Mode", shooterMode);

			// Flywheel activated when thumb button is pressed
			// if (stick.getRawButton(2) || stick.getRawButton(1)) {
			// if (shooterMode == 2) {
			if (stick.getRawButton(9)) {
				// For the close gear peg position
				if (AVG < 1000) {
					shooter.set(1);
				} else if (AVG < 2000) {
					shooter.set(.85);
				} else if (AVG < 2500) {
					shooter.set(.7);
				} else if (AVG < 2700) {
					shooter.set(.57);
				} else if (AVG < 3225 - (50 * stick.getRawAxis(3))) {
					shootPow += 0.0003;
					shooter.set(shootPow + .57);
				} else {
					shooter.set(.57);
					shootPow -= 0.0001;
				}
				// } else if (shooterMode == 1) {
			} else if (stick.getRawButton(7)) {
				// For the boiler hopper position
				if (AVG < 1000) {
					shooter.set(1);
				} else if (AVG < 1500) {
					shooter.set(.85);
				} else if (AVG < 2000) {
					shooter.set(.7);
				} else if (AVG < 2500) {
					shooter.set(.5);
				} else if (AVG < 2925 - (50 * stick.getRawAxis(3))) {
					shootPow += 0.0003;
					shooter.set(shootPow + .5);
				} else {
					shooter.set(.5);
					shootPow -= 0.0001;
				}
				// } else if (shooterMode == 3) {
			} else if (stick.getRawButton(11)) {
				// For the center gear position
				if (AVG < 2000) {
					shooter.set(1);
				} else if (AVG < 2500) {
					shooter.set(.9);
				} else if (AVG < 3000) {
					shooter.set(.8);
				} else if (AVG < 3300) {
					shooter.set(.6);
				} else if (AVG < 3445 - (50 * stick.getRawAxis(3))) {
					shootPow += 0.0004;
					shooter.set(shootPow + .6);
				} else {
					shooter.set(.6);
					shootPow -= 0.0001;
				}
			} else {
				shooter.set(0);
				shootPow = 0;
			}

			SmartDashboard.putNumber("Encoder Rate", AVG);
			SmartDashboard.putNumber("Motor", shooter.get());
			// Activate shooter for set positions with buttons 7,9,11

			// Stick button to enable the sweeper.
			// Also triggers with XBox right trigger
			if (stick.getRawButton(1) || xBox.getRawAxis(3) != 0 || stick.getRawButton(3)) {
				sweeper.set(-1);
			} else {
				sweeper.set(0);
			}

			// Ball transport only active when trigger is held
			if (stick.getRawButton(1)) {
				transport.set(-1);
			} else {
				transport.set(0);
			}

			// Reads "twist" of the joystick to set the Azimuth of shooter
			// if (stick.getRawButton(2) || stick.getRawButton(1)) {
			// pixyFunctionsShooter.alignShooterX();
			// } else
			// if (Math.abs(stick.getRawAxis(1)) > .15) {
			// turret.set(stick.getRawAxis(1) / 6);
			// }
			// else {
			// turret.set(0);
			// }
			if (stick.getRawButton(4)) {
				pixyFunctionsShooter.alignShooterX();
			} else {
				turret.set(stick.getRawAxis(2) / 6);
			}
			SmartDashboard.putNumber("Stick Twist", stick.getRawAxis(2) / 6);
			// TODO: We've been having problems where we can only move one servo
			// at a time
			// All servo inputs maniplate just one servo
			// Need to get to the bottom of that and find out how we can move
			// two servos separately

			// Manipulates the servo up or down
			if (stick.getPOV(0) == 0) {
				shooterServo.increment(-1);
			} else if (stick.getPOV(0) == 180) {
				shooterServo.increment(1);
			}

			// if (servoSet) {
			// if (shooterServo.position < RobotMap.servoLowPos) {
			// }
			// } else {
			// }

			// ----------------------------------------------------------------------
			// // TODO do not delete this code use for calibration of gyro at
			// // competition
			// // TODO
			// // TODO do not delete this code use for calibration of gyro at
			// // competition
			// // TODO
			// // TODO do not delete this code use for calibration of gyro at
			// // competition
			// // TODO
			// //
			// ------------------------------------------------------------------------
			// // if(xBox.getRawButton(1)){
			// // if(step ==0){
			// // SmartDashboard.putNumber("Init Angle", currAngle);
			// // encoder.initEncoders();
			// // step =1;
			// // }
			// // if(step==1){
			// // if(encoder.calibrateTurnWithGyrosAndEncoders()){
			// // step =2;
			// // }
			// // if(step==2){
			// // SmartDashboard.putNumber("Change in angle",
			// // gyro.getAngle()-currAngle);
			// // robotDriver.stop();
			// // }
			// // }
			// // }else{
			// // currAngle = gyro.getAngle();
			// // step =0;
			// // robotDriver.stop();
			// // }
			// //
			// ----------------------------------------------------------------------
			// // TODO do not delete this code use for calibration of gyro at
			// // competition
			// // TODO
			// // TODO do not delete this code use for calibration of gyro at
			// // competition
			// // TODO
			// // TODO do not delete this code use for calibration of gyro at
			// // competition
			// // TODO
			// //
			// ------------------------------------------------------------------------
//			SmartDashboard.putNumber("Pixy Cam X value gear", pixyGearProc.averageData(0, false, pixyGear)[0]);
			if (xBox.getRawButton(1)) {
				// THIS CODE FUCKIN WORKS FOR THE PIXY CENTERING
				// HOLY SHIT WE DID IT BOYS WE REALLY FUCKIN DID IT

				pixyFunctionsGear.turnAndGoStraightAuton();
			}

			// Pressing the X Button and moving the left stick will activate
			// a 110 degree turn
			else if (xBox.getRawButton(RobotMap.loaderTurnButton)) {
				if (Math.abs(xBox.getRawAxis(RobotMap.loaderTurnAxis)) >= .25) {
					// by reaching this point, driver pressed button and pulled
					// stick.
					// this is for quick turn on robot.
					// buttons must be released for another quick turn
					if (step == 0) {
						currAngle = gyro.getAngle();
						driveEncoders.initEncoders();
						step = 1;
					}
					if (step == 1) {
						if (xBox.getRawAxis(RobotMap.loaderTurnAxis) > .25) {
							left = true;
						} else if (xBox.getRawAxis(RobotMap.loaderTurnAxis) < -.25) {
							left = false;
						}
						// if (driveEncoders.loaderTurn(currAngle, left)){
						if (driveEncoders.autonSixtyDegreeTurn(currAngle, !left)) {
							step = 2;
						}
						if (step == 2) {
							robotDriver.stop();
						}
					}
				} else {
					robotDriver.stop();
				}

				// Pressing Y will invert the robot controls
				// for easier reverse driving
			} else if (xBox.getRawButton(RobotMap.flipperButt)) {
				if (step == 0) {
					step = 1;
					if (driveMod == 1) {
						driveMod = -1;
					} else {
						driveMod = 1;
					}
				}
			}

			// Defaults to manual driving. Also resets all steps and gyro
			else {
				SmartDashboard.putString("Encoder Test", "unused");
				gyro.resetGyro();
				step = 0;
				stepShooter = 0;
				gearLoadStep = 0;
				robotDriver.drive(xBox.getRawAxis(1) * straightMod, xBox.getRawAxis(4) * turnMod, driveMod);
			}

			// Watkins conditioning apparatus
			if (stick.getRawButton(12) && stick.getRawButton(10)) {
				xBox.setRumble(RumbleType.kRightRumble, 1);
				xBox.setRumble(RumbleType.kLeftRumble, 1);
			} else {
				xBox.setRumble(RumbleType.kRightRumble, 0);
				xBox.setRumble(RumbleType.kLeftRumble, 0);
			}

			// Activates the climber, giving the Operator stick priority
			if (stick.getRawButton(6) || stick.getRawButton(5)) {
				climber.set(1);

			} else if (xBox.getRawAxis(2) != 0) {
				climber.set(xBox.getRawAxis(2));
			} else {
				climber.set(0);
			}
			current = pdp.getCurrent(RobotMap.climberChannel);
			SmartDashboard.putNumber("Current", current);

			if (xBox.getRawButton(8)) {
				solenoid.set(DoubleSolenoid.Value.kForward);
			} else {
				solenoid.set(DoubleSolenoid.Value.kOff);
			}
		}

	}

	/**
	 * Runs during test mode
	 */
	@Override
	public void test() {

		relay.set(Relay.Value.kForward);

	}

	public double EncoderAVG() {

		double range = shooterEncoder.getRate();
		double result;

		sumBufferRight += range - distanceArrayRight[counterRight];

		distanceArrayRight[counterRight++] = range;

		if (counterRight == encoderAVGArraySize) {
			firstBufferRight = false;
			counterRight = 0;
		}

		if (firstBufferRight) {
			result = sumBufferRight / counterRight;

		} else {
			result = sumBufferRight / encoderAVGArraySize;
		}

		return (result);
	}
}
