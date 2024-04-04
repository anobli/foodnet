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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class OpenDating {
    private DateFormat df;
    private HashMap<String, String> hashMap;

    public static final String ID = "Id";
    public static final String FOOD = "Food";
    public static final String PROD_DATE = "ProdDate";
    public static final String EXP_DATE = "ExpDate";
    public static final String OPENING_DATE = "OpeningDate";
    public static final String LOCATION = "Location";

    public OpenDating(HashMap<String, String> hashMap) {
        this.hashMap = hashMap;
        df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
    }

    protected boolean checkDate(String id) {
        if (hashMap.containsKey(id) && hashMap.get(id) != null) {
            if (!((String)hashMap.get(id)).isEmpty()) {
                try {
                    df.parse((String) hashMap.get(id));
                } catch (ParseException e) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean check() {
        if (hashMap == null) {
            return false;
        }

        if (hashMap.size() == 0) {
            return false;
        }

        if (!hashMap.containsKey(FOOD) || hashMap.get(FOOD) == null) {
            return false;
        }

        if (((String)hashMap.get(FOOD)).isEmpty()) {
            return false;
        }

        if (!checkDate(EXP_DATE)) {
            return false;
        }

        if (!checkDate(OPENING_DATE)) {
            return false;
        }

        if (!checkDate(PROD_DATE)) {
            return false;
        }

        return true;
    }

    public OpenDating(Map<String, Object> hashMap) {
        this.hashMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : hashMap.entrySet()) {
            Object obj = entry.getValue();
            if (obj.getClass() == String.class) {
                this.hashMap.put(entry.getKey(), (String) entry.getValue());
            } else if (obj.getClass() == Long.class) {
                this.hashMap.put(entry.getKey(), Long.toString((long) entry.getValue()));
            }
        }
        df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

        if (!check()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create an OpenDating object.
     * @param id The id of the OpenDating
     * @param food The name of the food
     * @param prodDate The production date of the food
     * @param expDate The expiration of the food
     * @param openingDate The opening date
     * @param locale For testing purpose only, the locale to use for date formating
     * @throws NullPointerException if food is null
     * @throws IllegalArgumentException if food is empty or date are not valid
     */
    public OpenDating(long id, String food, String prodDate, String expDate,
                      String openingDate, String location, Locale locale) {
        hashMap = new HashMap<>();
        hashMap.put(ID, Long.toString(id));
        hashMap.put(FOOD, food);
        hashMap.put(PROD_DATE, prodDate);
        hashMap.put(EXP_DATE, expDate);
        hashMap.put(OPENING_DATE, openingDate);
        hashMap.put(LOCATION, location);

        df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        if (!check()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create an OpenDating object.
     * @param id The id of the OpenDating
     * @param food The name of the food
     * @param prodDate The production date of the food
     * @param expDate The expiration of the food
     * @param openingDate The opening date
     * @throws NullPointerException if food is null
     * @throws IllegalArgumentException if food is empty or date are not valid
     */
    public OpenDating(long id, String food, String prodDate, String expDate,
                      String openingDate, String location) {
        this(id, food, prodDate, expDate, openingDate, location, Locale.getDefault());
    }

    /**
     * Create an OpenDating object.
     * @param id The id of the OpenDating
     * @param food The name of the food
     * @param prodDate The production date of the food
     * @param expDate The expiration of the food
     * @param openingDate The opening date
     * @throws NullPointerException if food is null
     * @throws IllegalArgumentException if food is empty or date are not valid
     */
    public OpenDating(long id, String food, String prodDate, String expDate,
                      String openingDate, long location) {
        this(id, food, prodDate, expDate, openingDate, Long.toString(location),
                Locale.getDefault());
    }

    /**
     * Get the id.
     * @return The id of the object
     */
    public long getID() {
        return Long.parseLong(hashMap.get(ID));
    }

    /**
     * Get the food name.
     * @return The food name
     */
    public String getFood() {
        return hashMap.get(FOOD);
    }

    /**
     * Return the production date.
     * @return The production date, represented by a string formatted as define
     *         by DateFormat.MEDIUM
     */
    public String getProdDate() {
        if (hashMap.get(PROD_DATE) == null)
            return "";
        return (String) hashMap.get(PROD_DATE);
    }

    /**
     * Return the expiration date.
     * @return The expiration date, represented by a string formatted as define
     *         by DateFormat.MEDIUM
     */
    public String getExpDate() {
        if (hashMap.get(EXP_DATE) == null)
            return "";
        return (String) hashMap.get(EXP_DATE);
    }

    /**
     * Set the expiration name.
     * @param expDate The expiration date, represented by a string formatted as
     *                define by DateFormat.MEDIUM
     */
    public void setExpDate(String expDate) {
        hashMap.put(EXP_DATE, expDate);
    }

    /**
     * Return the production date.
     * @return The production date, represented by a string formatted as define
     *         by DateFormat.MEDIUM
     */
    public String getOpeningDate() {
        if (hashMap.get(OPENING_DATE) == null)
            return "";
        return (String) hashMap.get(OPENING_DATE);
    }

    public void scheduleNotifications(Context context) {
        if (hashMap.get(EXP_DATE) == null || hashMap.get(EXP_DATE).length() > 0)
            return;

        try {
            Date expDate = df.parse(hashMap.get(EXP_DATE));
            String food = hashMap.get(FOOD);
            FoodNetNotification.scheduleNotification(context, food, expDate.getTime());
        } catch (ParseException ex) {
            return;
        }
    }

    public void cancelNotifications(Context context) {
        FoodNetNotification.cancelNotification(context, hashMap.get(FOOD));
    }

    public String getLocation() {
        return hashMap.get(LOCATION);
    }

    public HashMap<String, String> getHashMap() {
        return hashMap;
    }

    public void setId(String id) {
        hashMap.put(ID, id);
    }
}
