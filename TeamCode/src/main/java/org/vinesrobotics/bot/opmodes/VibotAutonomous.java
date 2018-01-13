/*
 * Copyright (c) 2017 Vines High School Robotics Team
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

package org.vinesrobotics.bot.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.vinesrobotics.bot.utils.Range;
import org.vinesrobotics.bot.utils.opencv.ColorBlobDetector;
import org.vinesrobotics.bot.utils.opencv.OpenCvManager;
import org.vinesrobotics.bot.utils.vuforia.VuforiaManager;

/**
 * Created by ViBots on 11/30/2017.
 */

public class VibotAutonomous extends VibotControlled {

    public enum AutoPosition {
        RedBack, BlueBack, RedFront, BlueFront, None
    }

    private OpenCvManager cvmanager = new OpenCvManager();
    private ColorBlobDetector redBlobDet = new ColorBlobDetector();
    private ColorBlobDetector blueBlobDet = new ColorBlobDetector();

    private AutoPosition Position = AutoPosition.None;

    public VibotAutonomous(AutoPosition pos) {
        Position = pos;
    }

    @Override
    public void init_spec() {
        clawServos.setPosition(clawServoMax);
        jewelArmServos.setPosition(1);
        switch (Position) {
            case BlueBack: {
                leftMotors.reverseDirection();
                rightMotors.reverseDirection();
            }
            break;
            case RedBack: {
            }
            break;

            case BlueFront: {
                leftMotors.reverseDirection();
                rightMotors.reverseDirection();
            }
            break;
            case RedFront: {
            }
            break;
        }

        cvmanager.initCV();
        redBlobDet.setHsvColor(new Scalar(251,255,255));
        redBlobDet.setColorRadius(new Scalar(12,96, 127));
        blueBlobDet.setHsvColor(new Scalar(150, 255, 255));
        blueBlobDet.setColorRadius(new Scalar(25 ,255, 200));
        cvmanager.registerBlobDetector(redBlobDet);
        cvmanager.registerBlobDetector(blueBlobDet);

        /*
       VuforiaManager.init();

       telemetry.addLine("Vu inited");
       telemetry.update();*/
    }

    private enum AutoState {
        AUTO_START(0,.001),
        ADJUST_SLIDE(.001, .2),
        MOVE_JEWEL(.2,3),
        RESET_JEWEL(3,3.1),
        CRYPTO_SAFEZONE(3.1,Double.POSITIVE_INFINITY),;

        public Range timeRange;

        AutoState(double minTime, double maxTime) {
            timeRange = new Range(minTime, maxTime);
        }
    }

    private AutoState currentState = AutoState.AUTO_START;
    private double stateOffset = 0;

    private int realTurnDir = 0;

    @Override
    public void loop_m(double delta) {
        if (!currentState.timeRange.inRange(ctime))
            // update state based on time
            for (AutoState states : AutoState.values())
                if (states.timeRange.inRange(ctime)) {
                    currentState = states;
                    stateOffset = 0;
                }
        stateOffset += delta;

        switch (currentState) {
            case AUTO_START:
                jewelArmServos.setPosition(-.2);
                clawServos.setPosition(clawServoMin);
                break;
            case ADJUST_SLIDE:
                slidePosition = .3;
                break;
            case MOVE_JEWEL:
                double directionPow = .25;
                int turnDir = 0; // 1 == right, -1 == left

                if (realTurnDir == 0) {

                    double redx = redBlobDet.centerOfAll.x;
                    double blux = blueBlobDet.centerOfAll.x;

                    double wrongcol = Double.NaN;
                    double rightcol = Double.NaN;
                    switch (Position) {
                        case RedBack:
                        case RedFront:
                            wrongcol = blux;
                            rightcol = redx;
                            break;
                        case BlueBack:
                        case BlueFront:
                            wrongcol = redx;
                            rightcol = blux;
                            break;
                    }

                    int split = 375;

                    if (wrongcol < split || rightcol > split)
                        turnDir = -1;
                    if (wrongcol > split || rightcol < split)
                        turnDir = 1;

                    realTurnDir = turnDir;
                }

                // figure out which way to turn and turn

                telemetry.addData("realTurnDir", realTurnDir);

                double half_point = currentState.timeRange.size() / 2d;

                if (stateOffset < half_point) {
                    leftMotors.setPower(-realTurnDir * directionPow);
                    rightMotors.setPower(-realTurnDir * directionPow);
                } else if (stateOffset >= half_point) {
                    leftMotors.setPower(realTurnDir * directionPow);
                    rightMotors.setPower(realTurnDir * directionPow);
                }

                break;
            case RESET_JEWEL:
                jewelArmServos.setPosition(1);
                break;
            case CRYPTO_SAFEZONE:
                leftMotors.setPower(0);
                rightMotors.setPower(0);

                double timingConstant = 1.;
                double smallOffset = .5;
                switch (Position) {
                    case BlueBack:
                    {
                        if (stateOffset < timingConstant) {
                            leftMotors.setPower(1d);
                            rightMotors.setPower(1d - smallOffset);
                        }
                    } break;
                    case BlueFront: {
                        if (stateOffset < timingConstant) {
                            leftMotors.setPower(1d- smallOffset);
                            rightMotors.setPower(1d);
                        }
                    } break;

                    case RedBack: {
                        if (stateOffset < timingConstant) {
                            leftMotors.setPower(1d- smallOffset);
                            rightMotors.setPower(1d);
                        }
                    } break;
                    case RedFront: {
                        if (stateOffset < timingConstant) {
                            leftMotors.setPower(1d);
                            rightMotors.setPower(1d- smallOffset);
                        }
                    } break;
                }

                double turnDuration = 1;
                switch (Position) {
                    case RedBack:
                    case RedFront:
                        if (stateOffset > timingConstant && ctime < timingConstant + turnDuration) {
                            rightMotors.setPower(1);
                            leftMotors.setPower(-1);
                        }
                }

                // temporayre code location
                clawServos.setPosition(clawServoMax);

                double finalMoveTime = .2;
                if (stateOffset > timingConstant + turnDuration && stateOffset < timingConstant + turnDuration + finalMoveTime) {
                    leftMotors.setPower(1);
                    rightMotors.setPower(1);
                }

                break;
        }

        int calcPos = (int)Math.round(slidePosition * linSlideUnitMultiplier);
        if (linSlide.getTargetPosition() != calcPos) linSlide.setTargetPosition(calcPos);

        telemetry.addLine("Blob centers");
        telemetry.addData("  Center of all reds", redBlobDet.centerOfAll);
        telemetry.addData("  Center of all blues", blueBlobDet.centerOfAll);
        for (Point point : redBlobDet.colorCenterPoints)
            telemetry.addData("    Red blob center", point.toString());
        for (Point point : blueBlobDet.colorCenterPoints)
            telemetry.addData("    Blue blob center", point.toString());

    }

    @Override
    public void stop() {
        cvmanager.stopCV();
    }

}
