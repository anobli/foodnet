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

import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class OpenDating {
    private static final long NOTIFICATION_LEAD_TIME_MS = 2L * 24 * 60 * 60 * 1000;

    private DateFormat df;
    private HashMap<String, String> hashMap;

    public static final String ID = "Id";
    public static final String FOOD = "Food";
    public static final String PROD_DATE = "ProdDate";
    public static final String EXP_DATE = "ExpDate";
    public static final String OPENING_DATE = "OpeningDate";
    public static final String LOCATION = "Location";
    public static final String LABEL = "Label";
    public static final String KCAL = "Kcal";
    public static final String COOKING_TEMP = "CookingTemp";
    public static final String COOKING_TIME = "CookingTime";
    public static final String PORTIONS = "Portions";

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

    /** Optional field: valid if unset/empty, or a non-negative integer. */
    protected boolean checkNonNegativeInt(String key) {
        String value = hashMap.get(key);
        if (value != null && !value.isEmpty()) {
            try {
                if (Integer.parseInt(value) < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
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

        for (String key : new String[]{KCAL, COOKING_TEMP, COOKING_TIME, PORTIONS}) {
            if (!checkNonNegativeInt(key)) {
                return false;
            }
        }

        return true;
    }

    public OpenDating(Map<String, Object> hashMap) {
        this.hashMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : hashMap.entrySet()) {
            Object obj = entry.getValue();
            if (obj == null) continue;
            if (obj instanceof String) {
                this.hashMap.put(entry.getKey(), (String) obj);
            } else if (obj instanceof Long) {
                this.hashMap.put(entry.getKey(), Long.toString((long) obj));
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
        if (hashMap.get(EXP_DATE) == null || hashMap.get(EXP_DATE).isEmpty())
            return;

        try {
            Date expDate = df.parse(hashMap.get(EXP_DATE));
            if (expDate != null) {
                long id = hashMap.get(ID) != null ? Long.parseLong(hashMap.get(ID)) : -1;
                String food = hashMap.get(FOOD);
                long notifyTime = expDate.getTime() - NOTIFICATION_LEAD_TIME_MS;
                FoodNetNotification.scheduleNotification(context, id, food, notifyTime);
            }
        } catch (ParseException ex) {
            // ignore
        }
    }

    public void cancelNotifications(Context context) {
        FoodNetNotification.cancelNotification(context, hashMap.get(FOOD));
    }

    public String getLocation() {
        return hashMap.get(LOCATION);
    }

    public String getLabel() {
        return hashMap.get(LABEL);
    }

    /**
     * Return the nutritional energy of the food.
     * @return The energy, in kilocalories (kcal), or an empty string if unset
     */
    public String getKcal() {
        if (hashMap.get(KCAL) == null)
            return "";
        return hashMap.get(KCAL);
    }

    public void setKcal(String kcal) {
        hashMap.put(KCAL, kcal);
    }

    /**
     * Return the cooking temperature (e.g. oven).
     * @return The temperature, in degrees Celsius, or an empty string if unset
     */
    public String getCookingTemp() {
        return getOrEmpty(COOKING_TEMP);
    }

    public void setCookingTemp(String cookingTemp) {
        hashMap.put(COOKING_TEMP, cookingTemp);
    }

    /**
     * Return the cooking time.
     * @return The time, in minutes, or an empty string if unset
     */
    public String getCookingTime() {
        return getOrEmpty(COOKING_TIME);
    }

    public void setCookingTime(String cookingTime) {
        hashMap.put(COOKING_TIME, cookingTime);
    }

    /**
     * Return the number of portions.
     * @return The number of portions, or an empty string if unset
     */
    public String getPortions() {
        return getOrEmpty(PORTIONS);
    }

    public void setPortions(String portions) {
        hashMap.put(PORTIONS, portions);
    }

    private String getOrEmpty(String key) {
        String value = hashMap.get(key);
        return value != null ? value : "";
    }

    public HashMap<String, String> getHashMap() {
        return hashMap;
    }

    public void setId(String id) {
        hashMap.put(ID, id);
    }
}
