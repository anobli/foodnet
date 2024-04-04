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

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

import ovh.bailon.foodnet2.R;

public class ListItemDate extends ListItemBase
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private final DateFormat df;
    private TextView valueTxt;
    final private Calendar calendar = Calendar.getInstance();

    public ListItemDate(Context context, int resourceId, int nameId) {
        super(context, resourceId, nameId);
        df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        valueTxt = (TextView) convertView.findViewById(R.id.itemTextValue);
        valueTxt.setText(value);
        valueTxt.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        new DatePickerDialog(context, this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        valueTxt.setText(df.format(calendar.getTime()));
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (valueTxt != null)
            valueTxt.setText(value);
    }

    @Override
    public String toString() {
        if (valueTxt != null)
            return valueTxt.getText().toString();
        return "";
    }
}
