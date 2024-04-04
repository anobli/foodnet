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

package ovh.bailon.foodnet2;

import android.content.Context;
import java.util.ArrayList;

import ovh.bailon.foodnet2.ui.SpinnerIconAdapter;
import ovh.bailon.foodnet2.ui.SpinnerItemIcon;

public class LocationAdapter extends SpinnerIconAdapter {
    /* Never change these ids! This is used by the database */
    public final static int FRIDGE_ID = 1;
    public final static int FREEZER_ID = 2;
    public final static int CUPBOARD_ID = 3;
    private ArrayList<SpinnerItemIcon> items;

    public LocationAdapter(Context context) {
        super(context);
        items = new ArrayList<>();
        items.add(new SpinnerItemIcon(FRIDGE_ID, R.string.fridge, R.drawable.ic_fridge_black_24dp));
        items.add(new SpinnerItemIcon(FREEZER_ID, R.string.freezer, R.drawable.ic_snowflake_black_24dp));
        items.add(new SpinnerItemIcon(CUPBOARD_ID, R.string.cupboard, R.drawable.ic_cupboard_black_24dp));

        addAll(items);
        notifyDataSetChanged();
    }
}
