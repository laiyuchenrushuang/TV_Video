//package tvvedio.hc.com.tvvedio.utils;
//
//import android.app.DownloadManager;
//import android.database.ContentObserver;
//import android.database.Cursor;
//import android.os.Handler;
//
///**
// * Created by ly on 2019/6/13.
// */
//
//public class DownloadChangeObserver extends ContentObserver {
//    /**
//     * Creates a content observer.
//     *
//     * @param handler The handler to run {@link #onChange} on, or null if none.
//     */
//    public DownloadChangeObserver(Handler handler) {
//        super(handler);
//    }
//
//    @Override
//    public void onChange(boolean selfChange) {
//        super.onChange(selfChange);
//        updateView();
//    }
//
//    private void updateView() {
//        int[] bytesAndStatus = new int[]{0, 0, 0};
//        DownloadManager.Query query = new DownloadManager.Query().setFilterById(mReqId);
//        Cursor c = null;
//        try {
//            c = mDownloadManager.query(query);
//            if (c != null && c.moveToFirst()) {
//                //已经下载的字节数
//                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//                //总需下载的字节数
//                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//                //状态所在的列索引
//                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            }
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//        }
//
//        if (mUpdateListener != null) {
//            mUpdateListener.update(bytesAndStatus[0], bytesAndStatus[1]);
//        }
//
//        Log.i(TAG, "下载进度：" + bytesAndStatus[0] + "/" + bytesAndStatus[1] + "");
//    }
//
//
//}
