// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.AngularVelocityUnit;
import edu.wpi.first.units.LinearAccelerationUnit;
import edu.wpi.first.units.LinearVelocityUnit;
import edu.wpi.first.units.VoltageUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.units.measure.LinearAcceleration;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.units.measure.Per;
import frc.robot.generated.TunerConstants;
import frc.robot.utils.VisionPoseEstimator.VisionPoseEstimatorConstants;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static final Frequency simUpdateFrequency = Hertz.of(200);

  public static final String canivore = "Drivetrain";

  public static class Ports {
    public static final int driverController = 0;
    public static final int operatorController = 1;
  }

  public static class FieldConstants {
    public static final AprilTagFieldLayout fieldLayout =
        AprilTagFieldLayout.loadField(AprilTagFields.k2025Reefscape);
  }

  public static class VisionConstants {
    public static final String blueArducamName = "blue-arducam";

    public static final double[] singleTagBaseStdDevs = new double[] {5, 5, 5};
    public static final double[] multiTagBaseStdDevs = new double[] {1, 1, 1};

    public static final double xBoundMargin = 0.01;
    public static final double yBoundMargin = 0.01;
    public static final double zBoundMargin = 0.01;

    public static final VisionPoseEstimatorConstants blueArducam =
        new VisionPoseEstimatorConstants(
            blueArducamName,
            new Transform3d(new Translation3d(0, 0, 1), new Rotation3d()),
            0.2,
            0.0001,
            3,
            7);
  }

  public static class SwerveConstants {
    public static final Frequency odometryFrequency = Hertz.of(250);

    public static final Distance driveRadius =
        Meters.of(
            Math.sqrt(
                Math.pow(TunerConstants.FrontLeft.LocationX, 2)
                    + Math.pow(TunerConstants.FrontLeft.LocationY, 2)));

    public static final LinearVelocity maxTranslationalSpeed = MetersPerSecond.of(3.632);
    public static final AngularVelocity maxAngularSpeed = RadiansPerSecond.of(Math.PI);

    // respecting wheel COF and max motor torque (this can be obtained from choreo probably)
    public static final LinearAcceleration maxTranslationalAcceleration =
        MetersPerSecondPerSecond.of(14.715);
    public static final AngularAcceleration maxAngularAcceleration =
        RadiansPerSecondPerSecond.of(Math.PI * 3);

    public static final LinearVelocity translationalDeadband = maxTranslationalSpeed.times(0.1);
    public static final AngularVelocity rotationalDeadband = maxAngularSpeed.times(0.1);
  }

  public static class IntakeConstants {
    public static final int feedMotorId = 8;
    public static final int actuatorMotorId = 9;

    public static final Per<VoltageUnit, AngularVelocityUnit> actuatorkV =
        VoltsPerRadianPerSecond.ofNative(0.1);
    public static final Per<VoltageUnit, AngularAccelerationUnit> actuatorkA =
        VoltsPerRadianPerSecondSquared.ofNative(0.1);

    public static final AngularVelocity actuatorVelocity = RadiansPerSecond.of(1);
    public static final AngularAcceleration actuatorAcceleration = RadiansPerSecondPerSecond.of(2);

    public static final double feedGearRatio = 1;
    public static final double actuatorGearRatio = 1;
  }

  public static class WristevatorConstants {
    // temporary values
    public static final AngularVelocity maxWristSpeed = RadiansPerSecond.of(Math.PI);
    public static final LinearVelocity maxElevatorSpeed = MetersPerSecond.of(1);

    public static final int homeSwitch = 0;

    public static final int leftMotorId = 10;
    public static final int rightMotorId = 11;
    public static final int wristMotorId = 12;

    public static final Distance drumCircumference = Meters.of(1.504 * Math.PI);
    public static final double elevatorGearRatio = 1;
    public static final double wristGearRatio = 4;
    public static final Distance minHeight = Meters.of(0);
    public static final Distance maxHeight = Meters.of(2);
    public static final Angle minWristAngle = Radians.of(-Math.PI);
    public static final Angle maxWristAngle = Radians.of(Math.PI);
    public static final Distance manipulatorLength = Meters.of(1);

    public static final Per<VoltageUnit, LinearVelocityUnit> elevatorkV =
        VoltsPerMeterPerSecond.ofNative(0.1);
    public static final Per<VoltageUnit, LinearAccelerationUnit> elevatorkA =
        VoltsPerMeterPerSecondSquared.ofNative(0.1);
    public static final Per<VoltageUnit, AngularVelocityUnit> wristkV =
        VoltsPerRadianPerSecond.ofNative(0.1);
    public static final Per<VoltageUnit, AngularAccelerationUnit> wristkA =
        VoltsPerRadianPerSecondSquared.ofNative(0.1);
  }

  public static class SerializerConstants {
    public static final int frontBeamPort = 1; // change later
    public static final int backBeamPort = 2; // change later

    public static final int feedMotorId = 13;
  }

  public static class ManipulatorConstants {
    // temp values?
    public static final int leftMotorId = 14;
    public static final int rightMotorId = 15;

    public static final int beamPort = 3;
    public static final int switchPort = 4;

    public static final double holdAlgaeVoltage = 0; // need to find
  }
}
