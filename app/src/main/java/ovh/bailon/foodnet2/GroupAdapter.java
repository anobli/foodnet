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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import ovh.bailon.foodnet2.db.FirestoreGroup;

public class GroupAdapter extends ArrayAdapter<String> {
    private FirestoreGroup db;
    FirebaseUser firebaseUser;

    public GroupAdapter(@NonNull Context context, ArrayList<String> list, FirestoreGroup db) {
        super(context, 0, list);
        this.db = db;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String member = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_group_member, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.member);
        name.setText(member);

        ImageButton del = (ImageButton) convertView.findViewById(R.id.delete);

        if (firebaseUser.getEmail().compareTo(member) == 0) {
            del.setEnabled(false);
            del.setVisibility(View.INVISIBLE);
        }
        del.setTag(position);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();

                String member = getItem(position);
                db.delete(member);
            }
        });

        return convertView;
    }
}
