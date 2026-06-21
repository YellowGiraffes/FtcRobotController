package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name="auto mateo", group="Robot")
public class Auto extends LinearOpMode {

    private final ElapsedTime runtime      = new ElapsedTime();
    private final ElapsedTime functionTime = new ElapsedTime();
    private final Yoshi robot              = new Yoshi();

    @Override
    public void runOpMode() {
        robot.init(hardwareMap);

        telemetry.addData("Status", "Ready to run");
        telemetry.update();

        waitForStart();
        runtime.reset();

        // Tweak this sequence each season to match the new autonomous routine.
        moveForwardAndBackward(0.5, 2);
        moveForwardAndBackward(-0.7, 1);
        moveLeftAndRight(0.4, 1);
        moveLeftAndRight(-0.4, 1);

        telemetry.addData("Path", "Complete");
        telemetry.update();
        sleep(1000);
    }

    private void moveForwardAndBackward(double speed, double maxDuration) {
        functionTime.reset();
        // axial = forward/back, no lateral or yaw
        robot.drive(speed, 0, 0, 1.0);
        while (opModeIsActive() && functionTime.seconds() < maxDuration) {
            sleep(10);
        }
        robot.stopDrive();
    }

    private void moveLeftAndRight(double speed, double maxDuration) {
        functionTime.reset();
        // lateral = strafe, no axial or yaw
        robot.drive(0, speed, 0, 1.0);
        while (opModeIsActive() && functionTime.seconds() < maxDuration) {
            sleep(10);
        }
        robot.stopDrive();
    }

    private void turn(double speed, double maxDuration) {
        // Positive speed = clockwise, negative = counter-clockwise.
        functionTime.reset();
        robot.drive(0, 0, speed, 1.0);
        while (opModeIsActive() && functionTime.seconds() < maxDuration) {
            sleep(10);
        }
        robot.stopDrive();
    }

    private void shoot(double shooterSpeed, double duration) {
        robot.setShooterPower(shooterSpeed);
        robot.fireTrigger();
        functionTime.reset();
        while (opModeIsActive() && functionTime.seconds() < duration) {
            sleep(10);
        }
        robot.stopTrigger();
        robot.setShooterPower(0);
    }
}
