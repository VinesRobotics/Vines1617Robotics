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

/**
 * Created by ViBots on 11/30/2017.
 */

public class VibotAutonomous extends VibotControlled {

    public enum AutoPosition {
        RedBack, BlueBack, RedFront, BlueFront, None
    }

    private AutoPosition Position = AutoPosition.None;

    public VibotAutonomous(AutoPosition pos) {
        Position = pos;
    }

    @Override
    public void init_spec() {
        clawServos.setPosition(1);

        switch (Position) {
            case BlueBack: {
                leftMotors.reverseDirection();
                rightMotors.reverseDirection();
            } break;
            case RedBack: {
            } break;

            case BlueFront: {
                leftMotors.reverseDirection();
                rightMotors.reverseDirection();
            } break;
            case RedFront: {
            } break;
        }
    }

    private static double timingConstant = 1.;
    private static double smallOffset = .5;

    @Override
    public void loop_m(double delta) {
        jewelArmServos.setPosition(-1);

        switch (Position) {
            case BlueBack: {
                if (ctime < timingConstant) {
                    leftMotors.setPower(1d);
                    rightMotors.setPower(1d-smallOffset);
                } else {
                    leftMotors.setPower(0);
                    rightMotors.setPower(0);
                }
            } break;
            case BlueFront: {
                if (ctime < timingConstant) {
                    leftMotors.setPower(1d-smallOffset);
                    rightMotors.setPower(1d);
                } else {
                    leftMotors.setPower(0);
                    rightMotors.setPower(0);
                }
            } break;

            case RedBack: {
                if (ctime < timingConstant) {
                    leftMotors.setPower(1d-smallOffset);
                    rightMotors.setPower(1d);
                } else {
                    leftMotors.setPower(0);
                    rightMotors.setPower(0);
                }
            } break;
            case RedFront: {
                if (ctime < timingConstant) {
                    leftMotors.setPower(1d);
                    rightMotors.setPower(1d-smallOffset);
                } else {
                    leftMotors.setPower(0);
                    rightMotors.setPower(0);
                }
            } break;
        }

        if (ctime < timingConstant) {
            jewelArmServos.setPosition(0);
        }

    }

    @Override
    public void stop() {

    }

}
