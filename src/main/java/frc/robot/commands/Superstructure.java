// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import static edu.wpi.first.units.Units.*;
import static edu.wpi.first.wpilibj2.command.Commands.*;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.IntakeConstants;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Manipulator;
import frc.robot.subsystems.Serializer;

/**
 * Contains factory methods for commands that involve the whole superstructure of the robot (require
 * multiple subsystems).
 */
public class Superstructure {
  private Superstructure() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /** Passoff from intake (if needed) -> serializer -> manipulator. */
  public static Command passoff(Intake intake, Serializer serializer, Manipulator manipulator) {
    return parallel(
            intake.intake().unless(serializer::getBackBeam),
            serializer.passoff(),
            manipulator.passoff())
        .withName("Passoff");
  }

  /** Coral passoff from manipulator -> serializer. */
  public static Command inversePassoff(Serializer serializer, Manipulator manipulator) {
    return deadline(serializer.inversePassoff(), manipulator.inversePassoff())
        .withName("Inverse Passoff");
  }

  /** Outtake from serializer -> intake. */
  public static Command groundOuttake(Intake intake, Serializer serializer) {
    return sequence(
            intake
                .outtake()
                .until(
                    () ->
                        MathUtil.isNear(
                            IntakeConstants.actuatorOut.in(Radians), intake.getAngle(), 0.01)),
            serializer.outtake())
        .withName("Ground Outtake");
  }

  /** Intake from intake -> serializer. */
  public static Command groundIntake(Intake intake, Serializer serializer) {
    return deadline(serializer.intake(), intake.intake()).withName("Ground Intake");
  }
}
