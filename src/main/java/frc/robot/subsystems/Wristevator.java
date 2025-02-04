package frc.robot.subsystems;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj2.command.Command;
import frc.lib.AdvancedSubsystem;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public class Wristevator extends AdvancedSubsystem {
  /** A setpoint for the wristevator. */
  public static enum WristevatorSetpoint {
    HOME(Radians.of(0), Meters.of(0)),
    HUMAN(Radians.of(0), Meters.of(0)),
    PROCESSOR(Radians.of(0), Meters.of(0)),

    L1(Radians.of(0), Meters.of(0)),
    L2(Radians.of(0), Meters.of(0)),
    L3(Radians.of(0), Meters.of(0)),
    L4(Radians.of(0), Meters.of(0)),

    LOWER_ALGAE(Radians.of(0), Meters.of(0)),
    UPPER_ALGAE(Radians.of(0), Meters.of(0));

    private final Angle _angle;
    private final Distance _height;

    private WristevatorSetpoint(Angle angle, Distance height) {
      _angle = angle;
      _height = height;
    }

    public Angle getAngle() {
      return _angle;
    }

    public Distance getHeight() {
      return _height;
    }
  }

  private final Consumer<WristevatorSetpoint> _wristevatorSetpointSetter;

  public Wristevator(Consumer<WristevatorSetpoint> wristevatorSetpointSetter) {
    _wristevatorSetpointSetter = wristevatorSetpointSetter;
  }

  @Logged(name = "Height")
  public double getHeight() {
    return 0;
  }

  @Logged(name = "Angle")
  public double getAngle() {
    return 0;
  }

  /** Set the wristevator to a setpoint. */
  public Command setSetpoint(WristevatorSetpoint setpoint) {
    return run(() -> {})
        .beforeStarting(() -> _wristevatorSetpointSetter.accept(setpoint))
        .withName("Set Setpoint: " + setpoint.toString());
  }

  /** Control the elevator and wrist speeds individually. */
  public Command setSpeeds(DoubleSupplier elevatorSpeed, DoubleSupplier wristSpeed) {
    return run(() -> {}).withName("Set Speeds");
  }

  @Override
  public void periodic() {
    super.periodic();
  }

  @Override
  public void close() {}
}
