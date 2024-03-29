package tvvedio.hc.com.tvvedio.zxing.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tvvedio.hc.com.tvvedio.R;
import tvvedio.hc.com.tvvedio.zxing.camera.CameraManager;
import tvvedio.hc.com.tvvedio.zxing.decoding.CaptureActivityHandler;
import tvvedio.hc.com.tvvedio.zxing.decoding.InactivityTimer;
import tvvedio.hc.com.tvvedio.zxing.view.ViewfinderView;


/**
 * Initial the camera
 *
 * @author Ryan.Tang
 */
public class CaptureActivity extends Activity implements Callback {

    private static final int REQUEST_CODE_SCAN_GALLERY = 100;

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private ImageView back;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private ProgressDialog mProgress;
    private String photo_path;
    private Bitmap scanBitmap;
    //	private Button cancelScanButton;
    public static final int RESULT_CODE_QR_SCAN = 0xA1;
    public static final String INTENT_EXTRA_KEY_QR_SCAN = "qr_scan_result";
    public static final String INTENT_EXTRA_KEY_QR_SCAN_MIN = "qr_min_result";
    private static String gsonResult = null;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
//		cancelScanButton = (Button) this.findViewById(R.id.btn_cancel_scan);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.scanner_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.scan_local:
//                //打开手机中的相册
//                Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
//                innerIntent.setType("image/*");
//                Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
//                this.startActivityForResult(wrapperIntent, REQUEST_CODE_SCAN_GALLERY);
//                return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        Message msg = new Message();
        msg.what = View.VISIBLE;
        mHandler.sendMessage(msg);

        //FIXME
        if (TextUtils.isEmpty(resultString) || !resultString.contains("vinCode")) {
//            startActivity(new Intent(this, ErrorActivity.class));
//        } else {
            getDataSync(resultString);
        }
        //CaptureActivity.this.finish();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case View.VISIBLE:

                    break;
                case View.INVISIBLE:

                    break;
            }
        }
    };
    private void getDataSync(final String result) {


        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                //http://222.212.90.203/electricbicycle/Certificate/pares?url=http://www.cqccms.com.cn/incoc/GSViewEbike!viewCocEbike.action?vinCode=117321900000001
                .url("http://222.212.90.203:8044/electricbicycle/Certificate/pares?url=" + result)//请求接口。如果需要传参拼接到接口后面。
                .build();


        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("lylog", "onFailure: ?");

                finish();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
            }
        });
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}