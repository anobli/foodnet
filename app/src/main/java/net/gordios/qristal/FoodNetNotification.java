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

package net.gordios.qristal;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class FoodNetNotification {
    public static final String CHANNEL_ID = FoodNetReceiver.NOTIFICATION_CHANNEL_ID;

    private static Notification getNotification(Context context, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_ALARM);
        builder.setAutoCancel(true);
        return builder.build();
    }

    public static void scheduleNotification(Context context, String food, long timestamp) {
        String title = context.getResources().getString(R.string.notification_title);
        String format = context.getResources().getString(R.string.notification_content);
        String content = String.format(format, food);
        
        Intent notificationIntent = new Intent(context, FoodNetReceiver.class);
        notificationIntent.putExtra(FoodNetReceiver.NOTIFICATION_ID, food.hashCode());
        notificationIntent.putExtra(FoodNetReceiver.NOTIFICATION,
                getNotification(context, title, content));
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, food.hashCode(), notificationIntent, flags);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
            }
        }
    }

    public static void cancelNotification(Context context, String food) {
        Intent notificationIntent = new Intent(context, FoodNetReceiver.class);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, food.hashCode(), notificationIntent, flags);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
