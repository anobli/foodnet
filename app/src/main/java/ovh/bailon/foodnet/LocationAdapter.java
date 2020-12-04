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

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.HashMap;

class LocationItem {
    int id;
    int icon;
    int text;

    public LocationItem(int id, int text, int icon) {
        this.id = id;
        this.text = text;
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public int getText() {
        return text;
    }

    public int getId() {
        return id;
    }
}

public class LocationAdapter extends ArrayAdapter<LocationItem> {
    /* Never change these ids! This is used by the database */
    public final static int UNKNOWN_LOCATION = 0;
    public final static int FRIDGE_ID = 1;
    public final static int FREEZER_ID = 2;
    public final static int CUPBOARD_ID = 3;
    private ArrayList<LocationItem> list;

    public LocationAdapter(Context context) {
        super(context, 0);
        list = new ArrayList<>();
        list.add(new LocationItem(FRIDGE_ID, R.string.fridge, R.drawable.ic_fridge_black_24dp));
        list.add(new LocationItem(FREEZER_ID, R.string.freezer, R.drawable.ic_snowflake_black_24dp));
        list.add(new LocationItem(CUPBOARD_ID, R.string.cupboard, R.drawable.ic_cupboard_black_24dp));
        addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LocationItem item = (LocationItem) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_food_location, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.locationName);
        ImageView icon = (ImageView) convertView.findViewById(R.id.locationIcon);

        name.setText(item.getText());
        icon.setImageResource(item.getIcon());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public long getItemId(int position) {
        Log.d("DTC", "position: " + position + ", id: " + getItem(position).getId());
        return getItem(position).getId();
    }
}
