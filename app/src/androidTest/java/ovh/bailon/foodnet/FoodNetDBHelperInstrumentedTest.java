/*
 * Copyright (C) 2020 Alexandre Bailon
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * If not, see <https://www.gnu.org/licenses/>.
 */

package ovh.bailon.foodnet;

import static org.junit.Assert.*;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.IOException;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ovh.bailon.foodnet.FoodnetDBHelper;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FoodNetDBHelperInstrumentedTest {
    private FoodnetDBHelper db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = new FoodnetDBHelper(context, Locale.US);
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void testGetAndAdd() {
        OpenDating openDating;

        openDating = db.get(123456);
        assertEquals(null, openDating);

        openDating = new OpenDating(123456, "Socca", null, null, null);
        db.add(openDating);

        openDating = db.get(123456);
        assertEquals(123456, openDating.getID());
        assertEquals("Socca", openDating.getFood());
    }

    @Test
    public void testDelete() {
        OpenDating openDating;

        openDating = new OpenDating(123457, "Cade", null, null, null);
        db.add(openDating);

        openDating = db.get(123457);
        assertEquals("Cade", openDating.getFood());

        db.delete(openDating);
        openDating = db.get(123457);
        assertEquals(null, openDating);
    }

    @Test
    public void testDeleteInvalid() {
        OpenDating openDating;

        openDating = new OpenDating(123460, "Chiken", null, null, null);
        db.delete(openDating);
    }

    @Test
    public void testDeleteNull() {
        db.delete(null);
    }

    @Test
    public void testUpdate() {
        OpenDating openDating;

        openDating = new OpenDating(123458, "Ratatouille", null, null, null);
        db.add(openDating);

        openDating = db.get(123458);
        assertEquals("Ratatouille", openDating.getFood());
        assertEquals("", openDating.getExpDate());

        openDating.setExpDate("Oct 21, 2020");
        db.update(openDating);

        openDating = db.get(123458);
        assertEquals("Ratatouille", openDating.getFood());
        assertEquals("Oct 21, 2020", openDating.getExpDate());
    }

    @Test
    public void testUpdateNull() {
        db.update(null);
    }

    @Test
    public void testUpdateInvalid() {
        OpenDating openDating;

        openDating = new OpenDating(123459, "Cassoulet", null, null, null);
        db.update(openDating);

        openDating = db.get(123459);
        assertEquals(null, openDating);
    }
}