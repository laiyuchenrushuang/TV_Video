package tvvedio.hc.com.tvvedio.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

/**
 * Created by ly on 2019/6/13.
 */

public class BitmapUtils {

    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        Bitmap newBitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string.split(",")[1], Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            // 计算缩放比例.
            float scaleWidth = ((float) 180) / width;
            float scaleHeight = ((float) 180) / height;
            // 取得想要缩放的matrix参数.
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 得到新的图片.
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newBitmap;
    }

}
