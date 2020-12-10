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
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

import static ovh.bailon.foodnet.LocationAdapter.CUPBOARD_ID;
import static ovh.bailon.foodnet.LocationAdapter.FREEZER_ID;
import static ovh.bailon.foodnet.LocationAdapter.FRIDGE_ID;

public class FoodNetAdapter extends ArrayAdapter<OpenDating> {
    private IFoodnetDBHelper db;
    private Context context;
    private ArrayList<OpenDating> list;

    public FoodNetAdapter(Context context, ArrayList<OpenDating> list, IFoodnetDBHelper db) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
        this.db = db;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OpenDating openDating = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_food_net, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.food);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        ImageView icon = (ImageView) convertView.findViewById(R.id.locationIcon);
        ConstraintLayout layout = (ConstraintLayout) convertView.findViewById(R.id.layout);

        name.setText(openDating.getFood());
        date.setText(openDating.getExpDate());
        switch ((int)openDating.getLocationLong()) {
            case FREEZER_ID:
                icon.setImageResource(R.drawable.ic_snowflake_black_24dp);
                break;
            case FRIDGE_ID:
                icon.setImageResource(R.drawable.ic_fridge_black_24dp);
                break;
            case CUPBOARD_ID:
                icon.setImageResource(R.drawable.ic_cupboard_black_24dp);
                break;
        }

        layout.setTag(position);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();
                OpenDating openDating = getItem(position);
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("foodnet")
                    .authority("foodnet.bailon.ovh")
                    .appendQueryParameter("id", "" + openDating.getID());

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(builder.build());
                context.startActivity(i);
            }
        });

        ImageButton del = (ImageButton) convertView.findViewById(R.id.delete);
        del.setTag(position);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();

                OpenDating openDating = getItem(position);
                openDating.cancelNotifications(context);
                db.delete(openDating);
            }
        });

        return convertView;
    }
}
