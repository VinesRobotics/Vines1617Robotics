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

package org.vinesrobotics.bot.utils.curves;

import org.firstinspires.ftc.robotcore.internal.android.dex.util.Unsigned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ViBots on 11/3/2017.
 */

public abstract class CurveBase implements Curve {

    private Map<Double,Double> cache = new HashMap<>();
    private Map<Double,Long> cachect = new HashMap<>();

    private boolean useCache = true;
    public void enableCache() {useCache = true;}
    public void disableCache() {useCache = false;}

    private int cacheSize = 512;
    public void setCacheSize(int caches){cacheSize = caches;}

    public CurveBase() {
        if (cacheCleanThread == null) cacheCleanThread = new CacheCleanThread();
        if (!cacheCleanThread.isAlive()) cacheCleanThread.start();

        cacheCleanThread.curves.add(this);
    }

    public abstract double getValue(double x);

    private static CacheCleanThread cacheCleanThread;
    private class CacheCleanThread extends Thread {
        public List<CurveBase> curves;

        public CacheCleanThread() {
            curves = Collections.synchronizedList(new ArrayList<CurveBase>());

            this.setDaemon(true);
            this.setName("Curve Cache Cleaner");
            this.setPriority(Thread.MIN_PRIORITY);
        }

        public void run() {

        }
    }

    @Override
    public double getValueFor(double x) {

        if (useCache) {
            if (cache.containsKey(x)) {
                cachect.put(x, cachect.get(x)+1);
                return cache.get(x);
            } else {
                double v = getValue(x);
                cache.put(x, v);
                cachect.put(x, 1l);
                return v;
            }
        } else {
            return getValue(x);
        }

    }

}
