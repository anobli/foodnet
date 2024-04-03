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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ovh.bailon.foodnet.db.FoodnetDBHelper;
import static ovh.bailon.foodnet.LocationAdapter.FRIDGE_ID;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FoodNetDBHelperInstrumentedTest implements OnDataEventListener {
    private FoodnetDBHelper db;
    private OpenDating openDating = null;
    protected Semaphore mutex = new Semaphore(1);

    @Override
    public void onGetAllReady(ArrayList<OpenDating> list) {

    }

    @Override
    public void onGetReady(OpenDating openDating) {
        this.openDating = openDating;
        mutex.release();
    }

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
    public void testGetAndAdd() throws InterruptedException {
        openDating = null;
        db.requestGet(123456);
        mutex.acquire();
        assertEquals(null, openDating);

        openDating = new OpenDating(123456, "Socca", null, null, null, FRIDGE_ID);
        db.add(openDating);

        openDating = null;
        db.requestGet(123456);
        mutex.acquire();
        assertEquals(123456, openDating.getID());
        assertEquals("Socca", openDating.getFood());
    }

    @Test
    public void testDelete() throws InterruptedException {
        openDating = new OpenDating(123457, "Cade", null, null, null, FRIDGE_ID);
        db.add(openDating);

        openDating = null;
        db.requestGet(123457);
        mutex.acquire();
        assertEquals("Cade", openDating.getFood());

        db.delete(openDating);
        openDating = null;
        db.requestGet(123457);
        mutex.acquire();
        assertEquals(null, openDating);
    }

    @Test
    public void testDeleteInvalid() {
        OpenDating openDating;

        openDating = new OpenDating(123460, "Chiken", null, null, null, FRIDGE_ID);
        db.delete(openDating);
    }

    @Test
    public void testDeleteNull() {
        db.delete(null);
    }

    @Test
    public void testUpdate() throws InterruptedException {

        openDating = new OpenDating(123458, "Ratatouille", null, null, null, FRIDGE_ID);
        db.add(openDating);

        openDating = null;
        db.requestGet(123458);
        mutex.acquire();
        assertEquals("Ratatouille", openDating.getFood());
        assertEquals("", openDating.getExpDate());

        openDating.setExpDate("Oct 21, 2020");
        db.update(openDating);

        openDating = null;
        db.requestGet(123458);
        mutex.acquire();
        assertEquals("Ratatouille", openDating.getFood());
        assertEquals("Oct 21, 2020", openDating.getExpDate());
    }

    @Test
    public void testUpdateNull() {
        db.update(null);
    }

    @Test
    public void testUpdateInvalid() throws InterruptedException {
        openDating = new OpenDating(123459, "Cassoulet", null, null, null, FRIDGE_ID);
        db.update(openDating);

        openDating = null;
        db.requestGet(123459);
        mutex.acquire();
        assertEquals(null, openDating);
    }
}