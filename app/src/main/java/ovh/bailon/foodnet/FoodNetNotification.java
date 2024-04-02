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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class FoodNetNotification {
    public static String CHANNEL_ID = "0";

    private static Notification getNotification(Context context, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setChannelId(CHANNEL_ID);
        return builder.build();
    }

    public static void scheduleNotification(Context context, String food, long delay) {
        String title = context.getResources().getString(R.string.notification_title);
        String format = context.getResources().getString(R.string.notification_content);
        String content = String.format(format, food);
        Intent notificationIntent = new Intent(context, FoodNetReceiver.class);
        notificationIntent.putExtra(FoodNetReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(FoodNetReceiver.NOTIFICATION,
                getNotification(context, title, content));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC, delay, pendingIntent);
    }

    public  static void cancelNotification(Context context, String food) {
        String title = context.getResources().getString(R.string.notification_title);
        String format = context.getResources().getString(R.string.notification_content);
        String content = String.format(format, food);
        Intent notificationIntent = new Intent(context, FoodNetReceiver.class);
        notificationIntent.putExtra(FoodNetReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(FoodNetReceiver.NOTIFICATION,
                getNotification(context, title, content));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
