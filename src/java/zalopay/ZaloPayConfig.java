/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zalopay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author TNO
 */
public class ZaloPayConfig {

    public static final String APP_ID = "2554";
    public static final String KEY1 = "sdngKKJmqEMzvh5QQcdD2A9XBSKUNaYn";
    public static final String KEY2 = "trMrHtvjo6myautxDUiAcYsVtaeQ8nhf";
    public static final String ENDPOINT = "https://sb-openapi.zalopay.vn/v2/create";
    public static final String CALLBACK_URL = "https://168c-1-55-69-119.ngrok-free.app/ShoesShopping/callback";

    public static String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }
}
