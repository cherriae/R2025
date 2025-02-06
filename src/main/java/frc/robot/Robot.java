// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;
import static edu.wpi.first.wpilibj2.command.Commands.*;
import static edu.wpi.first.wpilibj2.command.button.RobotModeTriggers.*;
import static frc.robot.subsystems.Wristevator.WristevatorSetpoint.*;

import choreo.auto.AutoChooser;
import com.ctre.phoenix6.SignalLogger;
import dev.doglog.DogLog;
import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.Logged.Strategy;
import edu.wpi.first.epilogue.logging.EpilogueBackend;
import edu.wpi.first.epilogue.logging.FileBackend;
import edu.wpi.first.epilogue.logging.NTEpilogueBackend;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.lib.FaultLogger;
import frc.lib.InputStream;
import frc.robot.Constants.FieldConstants;
import frc.robot.Constants.ManipulatorConstants;
import frc.robot.Constants.Ports;
import frc.robot.Constants.SwerveConstants;
import frc.robot.Constants.WristevatorConstants;
import frc.robot.commands.Autos;
import frc.robot.commands.Superstructure;
import frc.robot.commands.WheelRadiusCharacterization;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Manipulator;
import frc.robot.subsystems.Manipulator.Piece;
import frc.robot.subsystems.Serializer;
import frc.robot.subsystems.Swerve;
import frc.robot.subsystems.Wristevator;
import frc.robot.utils.AlignPoses;
import frc.robot.utils.AlignPoses.AlignSide;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the Main.java file in the project.
 */
@Logged(strategy = Strategy.OPT_IN)
public class Robot extends TimedRobot {
  // controllers
  private final CommandXboxController _driverController =
      new CommandXboxController(Ports.driverController);

  private final CommandXboxController _operatorController =
      new CommandXboxController(Ports.operatorController);

  @Logged(name = "Swerve")
  private final Swerve _swerve = TunerConstants.createDrivetrain();

  @Logged(name = "Intake")
  private final Intake _intake = new Intake();

  @Logged(name = "Serializer")
  private final Serializer _serializer = new Serializer();

  @Logged(name = "Manipulator")
  private final Manipulator _manipulator = new Manipulator((Piece piece) -> _currentPiece = piece);

  @Logged(name = "Wristevator")
  private final Wristevator _wristevator = new Wristevator();

  private final Autos _autos = new Autos(_swerve);
  private final AutoChooser _autoChooser = new AutoChooser();

  private final NetworkTableInstance _ntInst;

  private boolean _fileOnlySet = false;

  // global state variables
  private static Piece _currentPiece = Piece.NONE;

  /** The current piece in the manipulator. */
  public static Piece getCurrentPiece() {
    return _currentPiece;
  }

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    this(NetworkTableInstance.getDefault());
  }

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot(NetworkTableInstance ntInst) {
    _ntInst = ntInst;

    // set up loggers
    DogLog.setOptions(DogLog.getOptions().withCaptureDs(true));

    setFileOnly(false); // file-only once connected to fms

    Epilogue.bind(this);
    SignalLogger.start();

    DriverStation.silenceJoystickConnectionWarning(isSimulation());

    FaultLogger.setup(_ntInst);

    configureDefaultCommands();
    configureDriverBindings();
    configureOperatorBindings();

    // piece comes into the robot from ground intake
    new Trigger(_serializer::getBackBeam)
        .onTrue(
            either(
                runOnce(() -> _currentPiece = Piece.NONE),
                rumbleControllers(1, 1),
                () -> getCurrentPiece() == Piece.CORAL));

    // piece in manipulator changes
    new Trigger(() -> getCurrentPiece() == Piece.NONE).onChange(rumbleControllers(1, 1));

    // a coral was passoff'ed
    _manipulator
        .getBeamNoPiece()
        .and(_wristevator::homeSwitch)
        .and(() -> _manipulator.getFeedDirection() == -1)
        .onTrue(runOnce(() -> _currentPiece = Piece.CORAL));

    SmartDashboard.putData(
        "Robot Self Check",
        sequence(
                runOnce(() -> DataLogManager.log("Robot Self Check Started!")),
                _swerve.fullSelfCheck(),
                runOnce(() -> DataLogManager.log("Robot Self Check Successful!")))
            .withName("Robot Self Check"));

    SmartDashboard.putData(new WheelRadiusCharacterization(_swerve));
    SmartDashboard.putData(runOnce(FaultLogger::clear).withName("Clear Faults"));

    // set up auto chooser
    _autoChooser.addRoutine("Simple Trajectory", _autos::simpleTrajectory);

    SmartDashboard.putData("Auto Chooser", _autoChooser);

    autonomous().whileTrue(_autoChooser.selectedCommandScheduler());

    addPeriodic(FaultLogger::update, 1);
  }

  // set logging to be file only or not
  private void setFileOnly(boolean fileOnly) {
    DogLog.setOptions(DogLog.getOptions().withNtPublish(!fileOnly));

    if (fileOnly) {
      Epilogue.getConfig().backend = new FileBackend(DataLogManager.getLog());
      return;
    }

    // if doing both file and nt logging, use the datalogger multilogger setup
    Epilogue.getConfig().backend =
        EpilogueBackend.multi(
            new NTEpilogueBackend(_ntInst), new FileBackend(DataLogManager.getLog()));
  }

  private void configureDefaultCommands() {
    _swerve.setDefaultCommand(
        _swerve.drive(
            InputStream.of(_driverController::getLeftY)
                .negate()
                .signedPow(2)
                .scale(SwerveConstants.maxTranslationalSpeed.in(MetersPerSecond)),
            InputStream.of(_driverController::getLeftX)
                .negate()
                .signedPow(2)
                .scale(SwerveConstants.maxTranslationalSpeed.in(MetersPerSecond)),
            InputStream.of(_driverController::getRightX)
                .negate()
                .signedPow(2)
                .scale(SwerveConstants.maxAngularSpeed.in(RadiansPerSecond))));

    _wristevator.setDefaultCommand(
        _wristevator.setSpeeds(
            InputStream.of(_operatorController::getRightY)
                .deadband(0.05, 1)
                .negate()
                .scale(WristevatorConstants.maxElevatorSpeed.in(RadiansPerSecond)),
            InputStream.of(_operatorController::getLeftY)
                .deadband(0.05, 1)
                .negate()
                .scale(WristevatorConstants.maxWristSpeed.in(RadiansPerSecond))));
  }

  private void alignmentTriggers(Trigger button, AlignPoses poses) {
    button
        .and(_driverController.leftTrigger().and(_driverController.rightTrigger().negate()))
        .whileTrue(_swerve.alignTo(poses, AlignSide.LEFT));

    button
        .and(
            _driverController.leftTrigger().negate().and(_driverController.rightTrigger().negate()))
        .whileTrue(_swerve.alignTo(poses, AlignSide.CENTER));

    button
        .and(_driverController.rightTrigger().and(_driverController.leftTrigger().negate()))
        .whileTrue(_swerve.alignTo(poses, AlignSide.RIGHT));
  }

  private void configureDriverBindings() {
    _driverController.a().whileTrue(_swerve.brake());
    _driverController.x().onTrue(_swerve.toggleFieldOriented());
    _driverController.y().onTrue(_swerve.resetHeading());

    alignmentTriggers(_driverController.povUp(), FieldConstants.reef);
    alignmentTriggers(_driverController.povLeft(), FieldConstants.human);
  }

  private void configureOperatorBindings() {
    _operatorController.back().whileTrue(_wristevator.setSetpoint(PROCESSOR));
    _operatorController.start().whileTrue(_wristevator.setSetpoint(HUMAN));
    _operatorController.rightStick().whileTrue(_wristevator.setSetpoint(HOME));

    _operatorController.a().whileTrue(_wristevator.setSetpoint(L1));

    _operatorController
        .b()
        .whileTrue(
            either(
                _wristevator.setSetpoint(L2),
                _wristevator.setSetpoint(LOWER_ALGAE),
                () -> getCurrentPiece() == Piece.CORAL));

    _operatorController
        .y()
        .whileTrue(
            either(
                _wristevator.setSetpoint(L3),
                _wristevator.setSetpoint(UPPER_ALGAE),
                () -> getCurrentPiece() == Piece.CORAL));

    _operatorController.x().whileTrue(_wristevator.setSetpoint(L4));

    _operatorController
        .rightBumper()
        .and(() -> _wristevator.homeSwitch())
        .whileTrue(Superstructure.passoff(_intake, _serializer, _manipulator));

    _operatorController
        .rightBumper()
        .and(() -> !_wristevator.homeSwitch())
        .whileTrue(Superstructure.groundIntake(_intake, _serializer));

    _operatorController.leftBumper().whileTrue(Superstructure.groundOuttake(_intake, _serializer));

    _operatorController
        .rightTrigger()
        .whileTrue(
            either(
                Superstructure.inversePassoff(_serializer, _manipulator),
                _manipulator.setSpeed(ManipulatorConstants.feedSpeed.in(RadiansPerSecond)),
                _wristevator::homeSwitch));

    _operatorController
        .leftTrigger()
        .whileTrue(_manipulator.setSpeed(-ManipulatorConstants.feedSpeed.in(RadiansPerSecond)));
  }

  /** Rumble the driver and operator controllers for some amount of seconds. */
  private Command rumbleControllers(double rumble, double seconds) {
    return run(() -> {
          _driverController.getHID().setRumble(RumbleType.kBothRumble, rumble);
          _operatorController.getHID().setRumble(RumbleType.kBothRumble, rumble);
        })
        .finallyDo(
            () -> {
              _driverController.getHID().setRumble(RumbleType.kBothRumble, 0);
              _operatorController.getHID().setRumble(RumbleType.kBothRumble, 0);
            })
        .withTimeout(seconds);
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();

    if (DriverStation.isFMSAttached() && !_fileOnlySet) {
      setFileOnly(true);

      _fileOnlySet = true;
    }

    DogLog.log("Manipulator Current Piece", _currentPiece);
  }

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }

  @Override
  public void close() {
    super.close();

    _swerve.close();
    _wristevator.close();
    _manipulator.close();
    _intake.close();
    _serializer.close();
  }
}
