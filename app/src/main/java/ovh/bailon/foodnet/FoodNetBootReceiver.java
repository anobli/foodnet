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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

public class FoodNetBootReceiver extends BroadcastReceiver implements OnDataEventListener {
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            IFoodnetDBHelper db = new FoodnetDBHelper(context);
            db.registerOnDataChange(this);
        }
    }

    @Override
    public void onGetAllReady(ArrayList<OpenDating> list) {
        for (OpenDating openDating : list) {
            openDating.scheduleNotifications(context);
        }
    }

    @Override
    public void onGetReady(OpenDating openDating) {

    }
}
