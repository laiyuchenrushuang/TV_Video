package tvvedio.hc.com.tvvedio.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ly on 2019/6/13.
 */

public class ToastUtils {

    public static void shortToast(Context context, String s) {
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }
}
