/*
 * Copyright (c) 2016 Vines High School Robotics Team
 *
 *                            Permission is hereby granted, free of charge, to any person obtaining a copy
 *                            of this software and associated documentation files (the "Software"), to deal
 *                            in the Software without restriction, including without limitation the rights
 *                            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *                            copies of the Software, and to permit persons to whom the Software is
 *                            furnished to do so, subject to the following conditions:
 *
 *                            The above copyright notice and this permission notice shall be included in all
 *                            copies or substantial portions of the Software.
 *
 *                            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *                            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *                            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *                            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *                            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *                            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *                            SOFTWARE.
 */

package org.firstinspires.ftc.robotcontroller.internal.testcode;

import com.qualcomm.hardware.matrix.MatrixDcMotorController;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple example of all motors and servos oscillating
 */
@Autonomous(name = "MatrixControllerDemo", group = "Examples")
@Disabled
public class MatrixControllerDemo extends OpMode {

    private ElapsedTime motorOscTimer = new ElapsedTime(0);
    private ElapsedTime servoOscTimer = new ElapsedTime(0);
    private ElapsedTime spamPrevention = new ElapsedTime(0);

    private DcMotor motor1;
    private DcMotor motor2;
    private DcMotor motor3;
    private DcMotor motor4;
    private Set<DcMotor> motorSet = new HashSet<DcMotor>();

    private Servo servo1;
    private Servo servo2;
    private Servo servo3;
    private Servo servo4;

    private MatrixDcMotorController mc;
    private ServoController sc;

    private boolean loopOnce = false;
    private boolean firstMotors = true;
    private boolean firstServos = true;
    private boolean firstBattery = true;
    private int battery;

    private final static double MOTOR_OSC_FREQ = 2.0;
    private final static double SERVO_OSC_FREQ = 1.0;
    private final static double SPAM_PREVENTION_FREQ = 1.0;

    private double motorPower = 1.0;
    private double servoPosition = 0.0;

    @Override
    public void init()
    {
        motor1 = hardwareMap.dcMotor.get("motor_1");
        motor2 = hardwareMap.dcMotor.get("motor_2");
        motor3 = hardwareMap.dcMotor.get("motor_3");
        motor4 = hardwareMap.dcMotor.get("motor_4");

        /*
         * A set of motors to use with the Matrix motor controller's
         * pending feature.  See example below.  Note that this is
         * completely optional.
         */
        motorSet.add(motor1);
        motorSet.add(motor2);
        motorSet.add(motor3);
        motorSet.add(motor4);

        servo1 = hardwareMap.servo.get("servo_1");
        servo2 = hardwareMap.servo.get("servo_2");
        servo3 = hardwareMap.servo.get("servo_3");
        servo4 = hardwareMap.servo.get("servo_4");

        /*
         * Matrix controllers are special.
         *
         * A Matrix controller is one controller with both motors and servos
         * but software wants to treat it as two distinct controllers, one
         * DcMotorController, and one ServoController.
         *
         * We accomplish this by appending Motor and Servo to the name
         * given in the configuration.  In the example below the name
         * of the controller is "MatrixController" so the motor controller
         * instance is "MatrixControllerMotor" and the servo controller
         * instance is "MatrixControllerServo".
         */
        mc = (MatrixDcMotorController)hardwareMap.dcMotorController.get("MatrixController");
        motor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor3.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor4.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        /*
         * Servos are not enabled by default.  Software must call pwmEnable()
         * for servos to function.
         */
        sc = hardwareMap.servoController.get("MatrixController");
        sc.pwmEnable();
    }

    @Override
    public void start()
    {
        motorOscTimer.reset();
        servoOscTimer.reset();
        spamPrevention.reset();
    }

    @Override
    public void stop()
    {
        /*
         * An example of setting power for individual motors as normal.
         *
         * For the Matrix controller, the methods take effect immediately
         * as each call to setPower(), or any other method that interacts
         * with the controller, is transformed into an i2c transaction and
         * queued.  A separate thread is processing the queue.
         *
         * In practice this means that the first call to setPower will
         * be applied 20 to 40 milliseconds before the last call as the
         * processing thread works through the queue.  Testing
         * has shown that this latency is not large enough to have any
         * real world negative impacts, however teams may choose to use
         * the controller's setMotorPower() method if they desire precise
         * simultaneous motor operations.  See example in handleMotors().
         */
        motor1.setPower(0.0);
        motor2.setPower(0.0);
        motor3.setPower(0.0);
        motor4.setPower(0.0);
        sc.pwmDisable();
    }

    /*
     * handleMotors
     *
     * Oscillate the motors.
     */
    protected void handleMotors()
    {
        if ((firstMotors) || (motorOscTimer.time() > MOTOR_OSC_FREQ)) {
            motorPower = -motorPower;

            /*
             * The MatrixDcMotorController's setMotorPower() method may take
             * a collection of motors.  If this is chosen, then the controller will
             * set a pending bit.  The pending bit tells the controller to
             * defer turning on, or changing the current set point, for a motor
             * until the pending bit is cleared.
             *
             * When the pending bit is cleared all motor power values are applied
             * simultaneously.  setMotorPower() handles the pending bit for you.
             */
            mc.setMotorPower(motorSet, motorPower);
            motorOscTimer.reset();
            firstMotors = false;
        }
    }

    /*
     * handleServos
     *
     * Oscillate the servos.
     */
    protected void handleServos()
    {
        if ((firstServos) || (servoOscTimer.time() > SERVO_OSC_FREQ)) {
            if (servoPosition == 0.0) {
                servoPosition = 1.0;
            } else {
                servoPosition = 0.0;
            }
            servo1.setPosition(servoPosition);
            servo2.setPosition(servoPosition);
            servo3.setPosition(servoPosition);
            servo4.setPosition(servoPosition);
            servoOscTimer.reset();
            firstServos = false;
        }
    }

    /*
     * handleBattery
     *
     * The Matrix controller has a separate battery whose voltage can be read.
     */
    protected void handleBattery()
    {
        if ((firstBattery) || (spamPrevention.time() > SPAM_PREVENTION_FREQ)) {
            battery = mc.getBattery();
            spamPrevention.reset();
            firstBattery = false;
        }
        telemetry.addData("Battery: ", ((float)battery/1000));
    }

    @Override
    public void loop()
    {
        handleMotors();
        handleServos();
        handleBattery();
    }
}
