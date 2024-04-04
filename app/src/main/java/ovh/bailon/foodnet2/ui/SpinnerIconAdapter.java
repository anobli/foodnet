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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ovh.bailon.foodnet2.R;


public class SpinnerIconAdapter extends ArrayAdapter<SpinnerItemIcon> {
    public SpinnerIconAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SpinnerItemIcon item = (SpinnerItemIcon) getItem(position);
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
        return getItem(position).getId();
    }
}
