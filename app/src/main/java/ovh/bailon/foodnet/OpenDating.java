package ovh.bailon.foodnet;

import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class OpenDating {
    private int id;
    private String food;
    private Date prodDate;
    private Date expDate;
    private Date openingDate;

    private DateFormat df;

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
    public OpenDating(int id, String food, String prodDate, String expDate,
                      String openingDate, Locale locale) {
        this.id = id;
        this.food = food;

        if (food == null) {
            throw new NullPointerException();
        }

        if (food.compareTo("") == 0) {
            throw new IllegalArgumentException();
        }

        df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        try {
            this.prodDate = setDate(prodDate);
            this.expDate = setDate(expDate);
            this.openingDate = setDate(openingDate);
        } catch (ParseException e) {
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
    public OpenDating(int id, String food, String prodDate, String expDate,
                      String openingDate) {
        this(id, food, prodDate, expDate, openingDate, Locale.getDefault());
    }

    /**
     * Get the id.
     * @return The id of the object
     */
    public int getID() {
        return id;
    }

    /**
     * Get the food name.
     * @return The food name
     */
    public String getFood() {
        return food;
    }

    /**
     * Set the food name.
     * @param food The food name
     */
    public void setFood(String food) {
        this.food = food;
    }

    /**
     * Return the production date.
     * @return The production date, represented by a string formatted as define
     *         by DateFormat.MEDIUM
     */
    public String getProdDate() {
        if (prodDate == null) {
            return "";
        }

        return df.format(prodDate);
    }

    private Date setDate(String strDate) throws ParseException {
        Date date;

        if (strDate != null && strDate.compareTo("") != 0) {
            return df.parse(strDate);
        } else {
            return null;
        }
    }

    private Date setDateOrNull(String strDate) {
        try {
            return setDate(strDate);
        } catch (ParseException ex) {
            return null;
        }
    }

    /**
     * Set the production date.
     * @param prodDate The production date, represented by a string formatted as
     *                 define by DateFormat.MEDIUM
     */
    public void setProdDate(String prodDate) {
        this.prodDate = setDateOrNull(prodDate);
    }

    /**
     * Return the expiration date.
     * @return The expiration date, represented by a string formatted as define
     *         by DateFormat.MEDIUM
     */
    public String getExpDate() {
        if (expDate == null) {
            return "";
        }

        return df.format(expDate);
    }

    /**
     * Set the expiration name.
     * @param expDate The expiration date, represented by a string formatted as
     *                define by DateFormat.MEDIUM
     */
    public void setExpDate(String expDate) {
        this.expDate = setDateOrNull(expDate);
    }

    /**
     * Return the production date.
     * @return The production date, represented by a string formatted as define
     *         by DateFormat.MEDIUM
     */
    public String getOpeningDate() {
        if (openingDate == null) {
            return "";
        }

        return df.format(openingDate);
    }

    /**
     * Set the opening date.
     * @param openingDate The opening date, represented by a string formatted as
     *                    define by DateFormat.MEDIUM
     */
    public void setOpeningDate(String openingDate) {
        this.openingDate = setDateOrNull(openingDate);
    }
}
