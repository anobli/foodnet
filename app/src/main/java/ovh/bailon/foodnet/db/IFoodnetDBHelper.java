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

package ovh.bailon.foodnet.db;

import ovh.bailon.foodnet.OnDataEventListener;
import ovh.bailon.foodnet.OpenDating;

public interface IFoodnetDBHelper {
    void add(OpenDating openDating);

    void requestGet(long id);

    void requestGetAll();

    void requestGetAll(int location);

    int update(OpenDating openDating);

    void delete(OpenDating openDating);

    void registerOnDataChange(OnDataEventListener listener);
}
