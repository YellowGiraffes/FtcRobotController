package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 1) Axial:    Driving forward and backward               Left-joystick Forward/Backward
 2) Lateral:  Strafing right and left                     Left-joystick Right and Left
 3) Yaw:      Rotating Clockwise and counter clockwise    Right-joystick Right and Left
 */

@TeleOp(name="code for remote", group="Linear OpMode")
public class remote extends LinearOpMode {

    // Declare OpMode members for each of the 4 motors.
    private final ElapsedTime runtime = new ElapsedTime();
    private double MaxSpeed = 0.5;
    Servo frontled;
    CRServo trigger1;
    CRServo trigger2;
    private double ShooterSpeed = 0.5;

    @Override
    public void runOpMode() {

        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        DcMotor frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        DcMotor backLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        DcMotor frontRightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        DcMotor backRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        DcMotor Shooter = hardwareMap.get(DcMotor.class, "Shooter");

        frontled = hardwareMap.get(Servo.class, "frontled");
        trigger1 = hardwareMap.get(CRServo.class, "trigger1");
        trigger2 = hardwareMap.get(CRServo.class, "trigger2");

        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);

        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Wait for the game to start (driver presses START)
        telemetry.addData("Status", "Initialized. Updated 2/15/26 at 2:16pm");
        telemetry.update();

        waitForStart();
        runtime.reset();

        Shooter.setPower(ShooterSpeed);
        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            if(gamepad1.right_trigger>0) {
                gamepad1.rumble(500);
                if(gamepad1.left_trigger>0) {
                    gamepad1.setLedColor(255, 0, 0, 500);
                }
            }

            if(gamepad1.dpad_down) {
                ShooterSpeed = ShooterSpeed - 0.001;
                Shooter.setPower(ShooterSpeed);
                frontled.setPosition(0.0);
            }

            if(gamepad1.dpad_up) {
                ShooterSpeed = ShooterSpeed + 0.001;
                Shooter.setPower(ShooterSpeed);
                frontled.setPosition(1.0);
            }

            if(gamepad1.dpad_left) {
                MaxSpeed = 0.25;
            }

            if(gamepad1.dpad_right) {
                MaxSpeed = 0.75;
            }

            if(gamepad1.right_trigger > 0) {
                trigger1.setPower(1);
                trigger2.setPower(-1);
            }
            else {
                trigger1.setPower(0);
                trigger2.setPower(0);
            }

            frontled.setPosition(0.0);
            frontled.getPosition();

            double max;

            // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
            double axial   = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
            double lateral = gamepad1.left_stick_x;
            double yaw     = gamepad1.right_stick_x;

            // Combine the joystick requests for each axis-motion to determine each wheel's power.
            // Set up a variable for each drive wheel to save the power level for telemetry.
            double frontLeftPower  = axial - lateral - yaw;
            double frontRightPower = axial + lateral + yaw;
            double backLeftPower   = axial + lateral - yaw;
            double backRightPower  = axial - lateral + yaw;

            // Normalize the values so no wheel power exceeds 100%
            // This ensures that the robot maintains the desired motion.
            max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
            max = Math.max(max, Math.abs(backLeftPower));
            max = Math.max(max, Math.abs(backRightPower));

            if (max > 1.0) {
                frontLeftPower  /= max;
                frontRightPower /= max;
                backLeftPower   /= max;
                backRightPower  /= max;
            }

            frontLeftPower *= MaxSpeed;
            frontRightPower *= MaxSpeed;
            backLeftPower *= MaxSpeed;
            backRightPower *= MaxSpeed;

            // Send calculated power to wheels
            frontLeftDrive.setPower(frontLeftPower);
            frontRightDrive.setPower(frontRightPower);
            backLeftDrive.setPower(backLeftPower);
            backRightDrive.setPower(backRightPower);

            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: " + runtime);
            telemetry.addData("Wheel max power", "%4.2f", MaxSpeed);
            telemetry.addData("Front left/Right", "%4.2f, %4.2f", frontLeftPower, frontRightPower);
            telemetry.addData("Back  left/Right", "%4.2f, %4.2f", backLeftPower, backRightPower);
            telemetry.addData("Front LED", "%4.2f", frontled.getPosition());
            telemetry.addData("Shooter speed", "%4.2f", Shooter.getPower());
            telemetry.update();
        }
    }}
