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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ovh.bailon.foodnet.R;

public class ListItemTextValue extends ListItemBase {
    protected TextView valueTxt;

    public ListItemTextValue(Context context, int resourceId, int nameId) {
        super(context, resourceId, nameId);
        value = "";
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = super.getView(position, convertView, parent);
        }
        valueTxt = (TextView) convertView.findViewById(R.id.itemTextValue);
        if (value != null && value.length() > 0)
            valueTxt.setText(value);
        else
            valueTxt.setText("");

        return convertView;
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (valueTxt != null && value.length() > 0)
            valueTxt.setText(value);
    }

    @Override
    public String toString() {
        if (valueTxt != null)
            return valueTxt.getText().toString();
        return "";
    }
}
