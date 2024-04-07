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

package ovh.bailon.foodnet2.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ArrayItems {

    private LinkedHashMap<String, ListItemBase> uiMap;
    private HashMap<String, String> dbMap;

    public ArrayItems() {
        this.uiMap = new LinkedHashMap<>();
        this.dbMap = new HashMap<>();
    }

    public void add(String key, ListItemBase item) {
        uiMap.put(key, item);
        dbMap.put(key, item.toString());
    }

    public HashMap<String, String> getHashMap() {
        for(Map.Entry<String, ListItemBase> entry : uiMap.entrySet()) {
            dbMap.put(entry.getKey(), entry.getValue().toString());
        }

        return dbMap;
    }

    public ArrayList<ListItemBase> getUiList() {
        return new ArrayList<>(uiMap.values());
    }

    public void updateUi(HashMap<String, String> hashMap) {
        for(Map.Entry<String, String> entry : hashMap.entrySet()) {
            ListItemBase item = uiMap.get(entry.getKey());
            if (item != null)
                item.setValue(entry.getValue());
        }
    }
}
