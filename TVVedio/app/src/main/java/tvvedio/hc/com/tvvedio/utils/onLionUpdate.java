package tvvedio.hc.com.tvvedio.utils;

import android.app.DownloadManager;
import android.net.Uri;

import java.io.File;

/**
 * Created by ly on 2019/6/13.
 */
//
//public class onLionUpdate {
//    public void downloadApk(String apkUrl, String title, String desc) {
//        // fix bug : 装不了新版本，在下载之前应该删除已有文件
//        File apkFile = new File(weakReference.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "test.apk");
//
//        if (apkFile != null && apkFile.exists()) {
//
//            apkFile.delete();
//
//        }
//
//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
//        //设置title
//        request.setTitle(title);
//        // 设置描述
//        request.setDescription(desc);
//        // 完成后显示通知栏
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setDestinationInExternalFilesDir(weakReference.get(), Environment.DIRECTORY_DOWNLOADS, "test.apk");
//        //在手机SD卡上创建一个download文件夹
//        // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir() ;
//        //指定下载到SD卡的/download/my/目录下
//        // request.setDestinationInExternalPublicDir("/codoon/","test.apk");
//
//        request.setMimeType("application/vnd.android.package-archive");
//        //记住reqId
//        mReqId = mDownloadManager.enqueue(request);
//    }
//}
