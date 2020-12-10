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

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class OpenDatingUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testOpenDatingConstructor() {
        OpenDating openDating;

        openDating = new OpenDating(1, "Socca", "Oct 4, 2020", "Oct 11, 2020",
                "Oct 4, 2020");

        assertEquals(1, openDating.getID());
        assertEquals("Socca", openDating.getFood());
        assertEquals("Oct 4, 2020", openDating.getProdDate());
        assertEquals("Oct 11, 2020", openDating.getExpDate());
        assertEquals("Oct 4, 2020", openDating.getOpeningDate());

        openDating = new OpenDating(2, "Cade", null, null, null);
        assertEquals(2, openDating.getID());
        assertEquals("Cade", openDating.getFood());
        assertEquals("", openDating.getProdDate());
        assertEquals("", openDating.getExpDate());
        assertEquals("", openDating.getOpeningDate());

        openDating = new OpenDating(3, "Pan Bagnat", "", null, null);
        assertEquals(3, openDating.getID());
        assertEquals("Pan Bagnat", openDating.getFood());
        assertEquals("", openDating.getProdDate());
        assertEquals("", openDating.getExpDate());
        assertEquals("", openDating.getOpeningDate());
    }

    @Test (expected = NullPointerException.class)
    public void testOpenDatingConstructorFoodNull() {
        OpenDating openDating = new OpenDating(4, null, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testOpenDatingConstructorFoodEmpty() {
        OpenDating openDating = new OpenDating(5, "", null, null, null);
    }

    @Test
    public void testOpenDatingConstructorBadDate() {
        OpenDating openDating;

        try {
            openDating = new OpenDating(6, "Ratatouille", "10", null, null);
            fail("IllegalArgumentException have not been throw");
        } catch(IllegalArgumentException ex) {
          assert true;
        }

        try {
            openDating = new OpenDating(6, "Ratatouille", "10", null, null);
            fail("IllegalArgumentException have not been throw");
        } catch (IllegalArgumentException ex) {
          assert true;
        }

        try {
            openDating = new OpenDating(6, "Ratatouille", "10", null, null);
            fail("IllegalArgumentException have not been throw");
        } catch (IllegalArgumentException ex) {
          assert true;
        }
    }
}
