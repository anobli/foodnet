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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import ovh.bailon.foodnet2.R;

public class ListItemSpinner<T> extends ListItemBase implements AdapterView.OnItemSelectedListener {
    private Spinner spinner;
    private ArrayAdapter<T> adapter;

    public ListItemSpinner(Context context, ArrayAdapter<T> adapter) {
        super(context, R.layout.item_content_simple_spinner, 0);
        this.adapter = adapter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(resourceId, parent, false);

        spinner = convertView.findViewById(R.id.itemSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        setSelectedItem(value);

        return convertView;
    }

    @Override
    public String toString() {
        if (spinner != null) {
            return Long.toString(spinner.getAdapter().getItemId(spinner.getSelectedItemPosition()));
        }
        return "0";
    }

    private void setSelectedItem(String value) {
        if (value == null)
            return;

        for(int i=0; i < spinner.getAdapter().getCount(); i++) {
            if(spinner.getAdapter().getItemId(i) == Long.parseLong(value)) {
                spinner.setSelection(i);
                break;
            }
        };
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (spinner == null || spinner.getAdapter() == null)
            return;

        setSelectedItem(value);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setValue(Long.toString(spinner.getAdapter().getItemId(position)));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
