package tvvedio.hc.com.tvvedio.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ly on 2019/6/12.
 */

public class Utils {
    public static int PLAY_COMPLETE = 0x00001;

    public static void showToast(Context context, String s) {
        Toast.makeText(context,s,Toast.LENGTH_LONG).show();
    }

    public class NetWorkUtil {

//                public final static String BASE_IP = "http://192.168.0.53:8085";
//        public final static String BASE_IP = "http://10.65.37.225:8085";
        public final static String BASE_IP = "http://xxjf.cdjg.chengdu.gov.cn:8090";

        public final static String BASE_URL = BASE_IP+"/jyptdbctl/video/getTvVideo?";

        public final static String BASE_URL_TRUE = "http://bj.migucloud.com/vod2/v1/download_spotviurl?";

        public final static String BASE_URL_COMPLETE = BASE_IP+"/jyptdbctl/video/playComplete?";

    }
}
