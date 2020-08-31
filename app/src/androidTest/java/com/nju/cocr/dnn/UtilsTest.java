package com.nju.cocr.dnn;

import android.graphics.PointF;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.nju.cocr.dnn.Config.SIZE_FIXED_INPUT;
import static org.junit.Assert.assertTrue;

public class UtilsTest {
    List<List<PointF>> nHeptane;// 正庚烷的笔迹

    @Before
    public void setUp() throws Exception {
        nHeptane = new ArrayList<List<PointF>>() {{
            add(new ArrayList<PointF>() {{
                add(new PointF(0, 1));
                add(new PointF(1.73f, 0));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(1.73f, 0));
                add(new PointF(3.46f, 1));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(3.46f, 1));
                add(new PointF(5.91f, 0));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(0, 1));
                add(new PointF(-1.73f, 0));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(-1.73f, 0));
                add(new PointF(-3.46f, 1));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(-3.46f, 1));
                add(new PointF(-5.91f, 0));
            }});
        }};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void normalizeScript() {
        List<List<PointF>> result = Utils.normalizeScript(nHeptane);
        for (List<PointF> script : result) {
            for (PointF pts : script) {
                assertTrue(pts.x < SIZE_FIXED_INPUT - 5);
                assertTrue(pts.y < SIZE_FIXED_INPUT - 5);
                assertTrue(pts.x > 5);
                assertTrue(pts.y > 5);
            }
        }
        System.out.println(result.toString());
    }
}