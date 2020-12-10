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

package ovh.bailon.foodnet.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ovh.bailon.foodnet.R;

public class ListItemBase {
    protected int resourceId;
    protected int nameId;
    protected Context context;
    protected String value;

    public ListItemBase(Context context, int resourceId, int nameId) {
        this.context = context;
        this.resourceId = resourceId;
        this.nameId = nameId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context)
                .inflate(resourceId, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.itemTextName);
        name.setText(nameId);

        return convertView;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
