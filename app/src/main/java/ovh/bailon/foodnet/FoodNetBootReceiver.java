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

public class FoodNetBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        FoodnetDBHelper db = new FoodnetDBHelper(context);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            for (OpenDating openDating : db.getAll()) {
                openDating.scheduleNotifications(context);
            }
        }
    }
}
