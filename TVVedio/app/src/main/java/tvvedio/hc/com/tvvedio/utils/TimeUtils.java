package tvvedio.hc.com.tvvedio.utils;

/**
 * Created by ly on 2019/6/14.
 */

public class TimeUtils {

    public static  String calculateTime(int time) {
        int minute;
        int second;

        String strMinute;

        if (time >= 60) {
            minute = time / 60;
            second = time % 60;

            //分钟在0-9
            if (minute >= 0 && minute < 10) {
                //判断秒
                if (second >= 0 && second < 10) {
                    return "0" + minute + ":" + "0" + second;
                } else {
                    return "0" + minute + ":" + second;
                }

            } else
            //分钟在10以上
            {
                //判断秒
                if (second >= 0 && second < 10) {
                    return minute + ":" + "0" + second;
                } else {
                    return minute + ":" + second;
                }
            }

        } else if (time < 60) {
            second = time;
            if (second >= 0 && second < 10) {
                return "00:" + "0" + second;
            } else {
                return "00:" + second;
            }

        }
        return null;
    }
}
