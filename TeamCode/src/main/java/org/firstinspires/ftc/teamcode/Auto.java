package org.firstinspires.ftc.teamcode;

import android.util.Log;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Autonomous(name="auto mateo", group="Robot")
public class Auto extends LinearOpMode {

    private final ElapsedTime runtime = new ElapsedTime();
    private final Yoshi robot         = new Yoshi();

    private double routeStartX = 0;
    private double routeStartY = 0;

    @Override
    public void runOpMode() {
        robot.init(hardwareMap);

        telemetry.addData("Status", "Ready to run");
        telemetry.update();

        waitForStart();
        runtime.reset();

        robot.updateOdometry();
        routeStartX = robot.getXMM();
        routeStartY = robot.getYMM();

        driveToPosition(500, 200, 180, 0.6);

        telemetry.addData("Path", "Complete");
        telemetry.addData("Total X traveled (mm)", robot.getXMM() - routeStartX);
        telemetry.addData("Total Y traveled (mm)", robot.getYMM() - routeStartY);
        telemetry.update();
        sleep(1000);
    }

    // Drives forward (positive mm) or backward (negative mm) the given distance.
    private void moveForwardAndBackward(double distanceMM, double speed) {
        robot.updateOdometry();
        double startX = robot.getXMM();
        double direction = distanceMM > 0 ? 1.0 : -1.0;
        double timeout = (Math.abs(distanceMM) / 200.0) + 3.0;
        double moveStart = runtime.seconds();
        robot.drive(direction * Math.abs(speed), 0, 0, 1.0);
        while (opModeIsActive() && runtime.seconds() - moveStart < timeout) {
            robot.updateOdometry();
            telemetry.addData("X traveled (mm)", robot.getXMM() - routeStartX);
            telemetry.addData("Y traveled (mm)", robot.getYMM() - routeStartY);
            telemetry.update();
            if (Math.abs(robot.getXMM() - startX) >= Math.abs(distanceMM)) {
                break;
            }
        }
        robot.stopDrive();
    }

    // Strafes left (positive mm) or right (negative mm) the given distance.
    private void moveLeftAndRight(double distanceMM, double speed) {
        robot.updateOdometry();
        double startY = robot.getYMM();
        double direction = distanceMM > 0 ? 1.0 : -1.0;
        double timeout = (Math.abs(distanceMM) / 200.0) + 3.0;
        double moveStart = runtime.seconds();
        robot.drive(0, direction * Math.abs(speed), 0, 1.0);
        while (opModeIsActive() && runtime.seconds() - moveStart < timeout) {
            robot.updateOdometry();
            telemetry.addData("X traveled (mm)", robot.getXMM() - routeStartX);
            telemetry.addData("Y traveled (mm)", robot.getYMM() - routeStartY);
            telemetry.update();
            if (Math.abs(robot.getYMM() - startY) >= Math.abs(distanceMM)) {
                break;
            }
        }
        robot.stopDrive();
    }

    // Turns clockwise (positive degrees) or counter-clockwise (negative degrees).
    private void turn(double degrees, double speed) {
        robot.updateOdometry();
        double startHeading = robot.getHeadingDegrees();
        double direction = degrees > 0 ? 1.0 : -1.0;
        double timeout = (Math.abs(degrees) / 90.0) * 3.0 + 2.0;
        double moveStart = runtime.seconds();
        robot.drive(0, 0, direction * Math.abs(speed), 1.0);
        while (opModeIsActive() && runtime.seconds() - moveStart < timeout) {
            robot.updateOdometry();
            double turned = robot.getHeadingDegrees() - startHeading;
            telemetry.addData("Target (deg)", degrees);
            telemetry.addData("Turned (deg)", turned);
            telemetry.update();
            if (Math.abs(turned) >= Math.abs(degrees)) {
                break;
            }
        }
        robot.stopDrive();
    }

    private void shoot(double shooterSpeed, double duration) {
        robot.setShooterPower(shooterSpeed);
        robot.fireTrigger();
        double shootStart = runtime.seconds();
        while (opModeIsActive() && runtime.seconds() - shootStart < duration) {
            sleep(10);
        }
        robot.stopTrigger();
        robot.setShooterPower(0);
    }

    // Drives to a specific field position and heading.
    // targetX and targetY are in mm from where the robot started the OpMode.
    // targetHeading is in degrees (0 = same direction as start, positive = clockwise).
    // speed controls how fast the robot drives (0.0 to 1.0).
    private void driveToPosition(double targetX, double targetY, double targetHeading, double speed) {
        final double POSITION_TOLERANCE_MM = 15.0;
        final double HEADING_TOLERANCE_DEG = 3.0;

        // Phase 1: drive to XY position (no rotation)
        double timeout   = 10.0;
        double moveStart = runtime.seconds();
        while (opModeIsActive() && runtime.seconds() - moveStart < timeout) {
            robot.updateOdometry();
            double currentX       = robot.getXMM();
            double currentY       = robot.getYMM();
            double currentHeading = robot.getHeadingDegrees();

            double dx       = targetX - currentX;
            double dy       = targetY - currentY;
            double posError = Math.sqrt(dx * dx + dy * dy);

            telemetry.addData("Phase", "1 - driving to position");
            telemetry.addData("X traveled (mm)", currentX - routeStartX);
            telemetry.addData("Y traveled (mm)", currentY - routeStartY);
            telemetry.addData("posError (mm)", posError);
            telemetry.update();

            Log.d("DriveToPos", String.format(
                    "[Phase1] pos=(%.1f, %.1f) heading=%.1f dx=%.1f dy=%.1f posError=%.1f",
                    currentX, currentY, currentHeading, dx, dy, posError));

            if (posError < POSITION_TOLERANCE_MM) {
                break;
            }

            double headingRad   = Math.toRadians(currentHeading);
            double robotAxial   = dx * Math.cos(headingRad) + dy * Math.sin(headingRad);
            double robotLateral = -dx * Math.sin(headingRad) + dy * Math.cos(headingRad);
            double posScale     = Math.min(1.0, posError / 150.0);
            double axialPower   = (robotAxial / posError) * speed * posScale;
            double lateralPower = (robotLateral / posError) * speed * posScale;

            Log.d("DriveToPos", String.format(
                    "[Phase1] axial=%.3f lateral=%.3f posScale=%.2f",
                    axialPower, lateralPower, posScale));

            robot.drive(axialPower, lateralPower, 0, 1.0);
        }
        robot.stopDrive();

        // Phase 2: rotate to target heading
        moveStart = runtime.seconds();
        while (opModeIsActive() && runtime.seconds() - moveStart < 5.0) {
            robot.updateOdometry();
            double currentHeading = robot.getHeadingDegrees();
            double dHeading       = targetHeading - currentHeading;

            // Normalize to [-180, 180]
            dHeading = ((dHeading + 180) % 360 + 360) % 360 - 180;

            telemetry.addData("Phase", "2 - rotating to heading");
            telemetry.addData("Current heading (deg)", currentHeading);
            telemetry.addData("dHeading (deg)", dHeading);
            telemetry.update();

            Log.d("DriveToPos", String.format(
                    "[Phase2] heading=%.1f dHeading=%.1f", currentHeading, dHeading));

            if (Math.abs(dHeading) < HEADING_TOLERANCE_DEG) {
                break;
            }

            double yawPower = Math.signum(dHeading) * speed * 0.5;
            robot.drive(0, 0, yawPower, 1.0);
        }
        robot.stopDrive();
    }
}
