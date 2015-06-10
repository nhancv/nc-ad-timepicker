package cvnhan.android.calendarsample;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by cvnhan on 08-Jun-15.
 */
public class Utils {
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    private static float roundToOneSignificantFigure(double num) {
        final float d = (float) Math.ceil((float) Math.log10(num < 0 ? -num : num));
        final int power = 1 - (int) d;
        final float magnitude = (float) Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }
    private static final int POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000};


    /**
     * Formats a float value to the given number of decimals. Returns the length of the string.
     * The string begins at out.length - [return value].
     */
    public static int formatFloat(final char[] out, float val, int digits) {
        boolean negative = false;
        if (val == 0) {
            out[out.length - 1] = '0';
            return 1;
        }
        if (val < 0) {
            negative = true;
            val = -val;
        }
        if (digits > POW10.length) {
            digits = POW10.length - 1;
        }
        val *= POW10[digits];
        long lval = Math.round(val);
        int index = out.length - 1;
        int charCount = 0;
        while (lval != 0 || charCount < (digits + 1)) {
            int digit = (int) (lval % 10);
            lval = lval / 10;
            out[index--] = (char) (digit + '0');
            charCount++;
            if (charCount == digits) {
                out[index--] = '.';
                charCount++;
            }
        }
        if (negative) {
            out[index--] = '-';
            charCount++;
        }
        return charCount;
    }
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final int END_OF_STREAM = -1;

    /**
     * {@value}
     */
    public static final String TAG = "SunnyPoints";
    /**
     *
     * true - to enabled the log, else false
     */
    public static boolean LOGS = true;

    public static final int PULSE_ANIMATOR_DURATION = 544;

    /**
     *
     * @param msg
     *            - Verbose message you would like to log
     */
    public static void v(String msg) {
        if (LOGS) {
            Log.v(TAG, msg);
        }

    }

    public static void v(String tag, String msg) {
        if (LOGS) {
            Log.v(tag, msg);
        }

    }

    /**
     *
     * @param msg
     *            - Debug message you would like to log
     */
    public static void d(String msg) {
        if (LOGS) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LOGS) {
            Log.d(tag, msg);
        }
    }

    /**
     *
     * @param msg
     *            - Error message you would like to log
     */
    public static void e(String msg) {
        if (LOGS) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (LOGS) {
            Log.e(tag, msg);
        }
    }

    /**
     *
     * @param msg
     *            - Warning message you would like to log
     */
    public static void w(String msg) {
        if (LOGS) {
            Log.w(TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LOGS) {
            Log.w(tag, msg);
        }
    }

    /**
     *
     * @param msg
     *            - Info message you would like to log
     */
    public static void i(String msg) {
        if (LOGS) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LOGS) {
            Log.i(tag, msg);
        }
    }

    /**
     *
     * @param text
     *            - Text
     * @return Formatted string with decimal point
     */
    public static String getFormatedNumber(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        if (text.contains(".")) {
            String cleanString = text.replaceAll("[,.]", "");
            NumberFormat formatter = new DecimalFormat("#0.00");
            double number = Double.parseDouble(cleanString) / (long) Math.pow(10.0, 2);
            return formatter.format(number).toString();
        }
        String cleanString = text.replaceAll("[,.]", "");
        NumberFormat intFormatter = new DecimalFormat("#0");
        long number = Long.parseLong(cleanString);
        return intFormatter.format(number).toString();
    }

    /**
     *
     * @param text
     *            - keypad Text
     * @return - The string with commas and decimal
     */
    public static String getFormatedNumberWithComma(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        if (text.contains(".")) {
            String cleanString = text.replaceAll("[,.$�]", "");
            NumberFormat formatter = new DecimalFormat("#,##0.00");
            double number = Double.parseDouble(cleanString) / (long) Math.pow(10.0, 2);
            return formatter.format(number).toString();
        }
        String cleanString = text.replaceAll("[,.$�]", "");
        NumberFormat intFormatter = new DecimalFormat("#,##0");
        long number = Long.parseLong(cleanString);
        return intFormatter.format(number).toString();
    }

    /**
     * Checks whether the application is default home launcher app
     *
     * @return null if application is default home, else return the current home
     *         package name
     */
    public static String isMyLauncherDefault(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        if (context.getPackageName().equals(currentHomePackage)) {
            return null;
        }
        return currentHomePackage;
    }

    public static void showKeyboard(final View editText, final boolean force) {
        showKeyboard(editText, force, 50);
    }

    public static void showKeyboard(final View editText, final boolean force, final int delayTime) {
        if (editText != null) {
            // Delay some time to get focus(error occurs on HTC Android)
            editText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (force) {
                        imm.showSoftInput(editText, 0);
                    } else {
                        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
                    }
                }
            }, delayTime);
        }
    }

    public static void hideKeyboard(final View editText) {
        if (editText != null) {
            // Delay some time to get focus(error occurs on HTC Android)
            editText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }, 50);
        }
    }

    /**
     * Checking the status of network
     *
     * @param context
     * @return is true if the device is connected. Otherwise, is false
     */
    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check for connection
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getDeviceId(Context context) {
        String identifier = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            identifier = tm.getDeviceId();
            if (identifier != null) {
                // Ignore id 0000000000..
                int length = identifier.length();
                int i = 0;
                while (i < length && identifier.charAt(i) == '0') {
                    i++;
                }
                if (i == length) {
                    identifier = null;
                }
            }
            if (identifier != null) {
                // Ignore id *0123456789* (error from diginnos tablet)
                if (identifier.contains("0123456789")) {
                    identifier = null;
                }
            }
        }
        if (identifier == null || identifier.length() == 0) {
            identifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        return identifier;
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //
        }
        return "0.0";
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //
        }
        return 0;
    }

    public static String getDisplayDateString(int year, int month, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        return sdf.format(calendar.getTime());
    }

    /**
     *
     * @param hhmm
     *            0915
     * @return 2:15am
     */
    public static String getDisplayTimeString(String hhmm) {
        int[] hm = Utils.getHM(hhmm);
        return getDisplayTimeString(hm);
    }

    public static String getDisplayTimeString(int timeInMinutes) {
        int[] hm = new int[] { timeInMinutes / 60, timeInMinutes % 60 };
        return getDisplayTimeString(hm);
    }

    public static String getDisplayTimeString(int[] hm) {
        StringBuffer str = new StringBuffer();
        if (hm[0] < 12) {
            if (hm[0] == 0) {
                str.append(12);
            } else {
                str.append(hm[0]);
            }
            if (hm[1] > 0) {
                str.append(":").append(hm[1]);
            }
            str.append("am");
        } else {
            if (hm[0] == 12 && hm[1] == 0) {
                str.append("noon");
            } else {
                if (hm[0] == 12) {
                    str.append(12);
                } else {
                    str.append(hm[0] - 12);
                }
                if (hm[1] > 0) {
                    str.append(":").append(hm[1]);
                }
                str.append("pm");
            }
        }
        return str.toString();
    }

    /**
     *
     * @param calendar
     * @return 2013-07-25
     */
    public static String getYMD(Calendar calendar) {
        return Utils.getYMD(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     *
     * @param year
     *            from 1
     * @param month
     *            from 0
     * @param day
     *            from 1
     * @return 2013-07-25
     */
    public static String getYMD(int year, int month, int day) {
        StringBuffer str = new StringBuffer();
        str.append(year).append("-");
        month += 1;
        if (month < 10) {
            str.append("0");
        }
        str.append(month).append("-");
        if (day < 10) {
            str.append("0");
        }
        str.append(day);
        return str.toString();
    }

    public static String getYM(int year, int month) {
        StringBuffer str = new StringBuffer();
        str.append(year).append("-");
        month += 1;
        if (month < 10) {
            str.append("0");
        }
        return str.toString();
    }

    /**
     *
     * @param yyyymmdd
     *            2013-07-12
     * @return [2013, 7, 12]
     */
    public static int[] getYMD(String yyyymmdd) {
        int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(yyyymmdd.substring(0, 4));
        ymd[1] = Integer.parseInt(yyyymmdd.substring(5, 7)) - 1;
        ymd[2] = Integer.parseInt(yyyymmdd.substring(8, 10));
        return ymd;
    }

    /**
     *
     * @param yyyymmdd
     *            2013-07-18
     * @return 18/07/2013
     */
    public static String getYMDString(String yyyymmdd) {
        StringBuffer str = new StringBuffer();
        str.append(yyyymmdd.substring(8, 10)).append("/").append(yyyymmdd.substring(5, 7)).append("/").append(yyyymmdd.substring(0, 4));
        return str.toString();
    }

    /**
     * @param hhmm
     *            1745
     * @return 17:45
     */
    public static String getHHMM(String hhmm) {
        return hhmm.substring(0, 2) + ":" + hhmm.substring(2, 4);
    }

    public static String getShortHM(String fromTime) {
        String prefixTime = fromTime.substring(0, 1);
        String secondChar = fromTime.substring(1, 2);
        String slash = fromTime.substring(2, 3);
        String thirdChar = fromTime.substring(3, 4);
        String fourChar = fromTime.substring(4, 5);
        String result = "";
        int prefixNumber = Integer.parseInt(prefixTime);
        if (prefixNumber > 0) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(secondChar).append(slash).append(thirdChar).append(fourChar);
        } else {
            result = fromTime;
        }
        return result;
    }

    /**
     *
     * @param h
     *            hour
     * @param m
     *            minute
     * @return 07:45
     */
    public static String getHHMM(int h, int m) {
        StringBuffer str = new StringBuffer();
        if (h < 10) {
            str.append("0");
        }
        str.append(h).append(":");
        if (m < 10) {
            str.append("0");
        }
        str.append(m);
        return str.toString();
    }

    /**
     *
     * @param minutes
     * @return 07:30
     */
    public static String getHHMM(int minutes) {
        return getHHMM(minutes / 60, minutes % 60);
    }

    /**
     *
     * @param minutes
     * @return 7:30
     */
    public static String getHMM(int minutes) {
        StringBuffer str = new StringBuffer();
        str.append(minutes / 60).append(":");
        if (minutes % 60 < 10) {
            str.append("0");
        }
        str.append(minutes % 60);
        return str.toString();
    }

    /**
     *
     * @param hhmm
     *            0745 or 07:45 or 7:45
     * @return [7,45]
     */
    public static int[] getHM(String hhmm) {
        int[] hm = new int[2];
        int index = hhmm.indexOf(":");
        if (index < 0) {
            index = 2;
            hm[0] = Integer.parseInt(hhmm.substring(0, index));
        } else {
            hm[0] = Integer.parseInt(hhmm.substring(0, index));
            index++;
        }
        hm[1] = Integer.parseInt(hhmm.substring(index));
        return hm;
    }

    /**
     *
     * @param hhmm
     *            0745
     * @return 7 * 60 + 45
     */
    public static int getMinutes(String hhmm) {
        int[] hm = getHM(hhmm);
        return hm[0] * 60 + hm[1];
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static StringBuffer getHexString(byte[] data) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(0xFF & data[i]);
            if (hex.length() == 1) {
                // could use a for loop, but we're only dealing with a single
                // byte
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString;

    }

    public static String hashMessage(String source, String key) {
        try {
            String message = source;
            if (message == null || key == null)
                return null;

            char[] keys = key.toCharArray();
            char[] mesg = message.toCharArray();

            int ml = mesg.length;
            int kl = keys.length;
            char[] newmsg = new char[ml];

            for (int i = 0; i < ml; i++) {
                newmsg[i] = (char) (mesg[i] ^ keys[i % kl]);
            }
            mesg = null;
            keys = null;
            return new String(newmsg);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isSystemApp(Context context) {
        boolean result = (context.getApplicationInfo().flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
        return result;
    }

    public static boolean unzip(Context context, InputStream zipStream, File file) {
        boolean result = true;
        final int BUFFER_SIZE = IO_BUFFER_SIZE;
        BufferedOutputStream bufferedOutputStream = null;
        ZipInputStream zipInputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            zipInputStream = new ZipInputStream(new BufferedInputStream(zipStream));
            ZipEntry zipEntry;
            byte buffer[] = new byte[BUFFER_SIZE];

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String zipEntryName = zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    if (file.exists()) {
                        file.delete();
                    }
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    fileOutputStream = new FileOutputStream(file);
                    bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
                    int count;

                    while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                        bufferedOutputStream.write(buffer, 0, count);
                    }
                    bufferedOutputStream.flush();
                    Utils.d("Util.unzip() " + zipEntryName);
                }
            }
        } catch (FileNotFoundException e) {
            // Unexpected
            result = false;
        } catch (IOException e) {
            // Unexpected
            result = false;
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    // Unimportant exception
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    // Unimportant exception
                }
            }
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                    // Unimportant exception
                }
            }
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    // Unimportant exception
                }
            }
        }
        return result;
    }

    public static final <T> boolean equalsIgnoreSequence(ArrayList<T> array1, ArrayList<T> array2) {
        if (array1 == null && array2 == null) {
            return true;
        } else if (array1 == null || array2 == null) {
            return false;
        }
        if (array1.size() != array2.size()) {
            return false;
        }
        for (T t : array1) {
            if (!array2.contains(t)) {
                return false;
            }
        }
        return true;
    }

    public static String getTextFromNetwork(String urlLink) {
        HttpURLConnection connection = null;
        String str = "";

        try {

            URL url = new URL(urlLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Connection", "close");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputstream = connection.getInputStream();
                str = readFully(inputstream);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return str;
    }

    public static final String readFully(final InputStream pInputStream) throws IOException {
        final StringWriter writer = new StringWriter();
        final char[] buf = new char[IO_BUFFER_SIZE];
        try {
            final Reader reader = new BufferedReader(new InputStreamReader(pInputStream, "UTF-8"));
            int read;
            while ((read = reader.read(buf)) != END_OF_STREAM) {
                writer.write(buf, 0, read);
            }
        } finally {
            if (pInputStream != null) {
                try {
                    pInputStream.close();
                } catch (final IOException e) {
                    //
                }
            }
        }
        return writer.toString();
    }

    public static void setColorText(Context context, TextView tv, int stringResId, String msg, int colorResId) {
        String originalStr = context.getString(stringResId);
        int index = originalStr.indexOf("%1$s");
        String displayStr = String.format(originalStr, msg);
        Spannable spannable = new SpannableString(displayStr);
        if (index >= 0) {
            spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(colorResId)), index, index + msg.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(spannable);
    }

    /**
     * Render an animator to pulsate a view in place.
     *
     * @param labelToAnimate
     *            the view to pulsate.
     * @return The animator object. Use .start() to begin.
     */
    public static ObjectAnimator getPulseAnimator(View labelToAnimate, float decreaseRatio, float increaseRatio) {
        Keyframe k0 = Keyframe.ofFloat(0f, 1f);
        Keyframe k1 = Keyframe.ofFloat(0.275f, decreaseRatio);
        Keyframe k2 = Keyframe.ofFloat(0.69f, increaseRatio);
        Keyframe k3 = Keyframe.ofFloat(1f, 1f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1, k2, k3);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1, k2, k3);
        ObjectAnimator pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY);
        pulseAnimator.setDuration(PULSE_ANIMATOR_DURATION);
        return pulseAnimator;
    }

    public static String getDurationString(int minute) {
        if (minute == 0) {
            return "0'";
        }
        StringBuffer str = new StringBuffer();
        int h = minute / 60;
        int m = minute % 60;
        if (h > 0) {
            str.append(h).append("h ");
        }
        if (m > 0) {
            str.append(m).append("'");
        }
        return str.toString();
    }


    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isTextEquals(CharSequence a, CharSequence b) {
        boolean aEmpty = TextUtils.isEmpty(a);
        boolean bEmpty = TextUtils.isEmpty(b);
        if ((aEmpty && bEmpty) || TextUtils.equals(a, b)) {
            return true;
        }
        return false;
    }
}
