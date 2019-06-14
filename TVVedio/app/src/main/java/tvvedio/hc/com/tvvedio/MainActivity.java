package tvvedio.hc.com.tvvedio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;
import tvvedio.hc.com.tvvedio.upload.HttpService;
import tvvedio.hc.com.tvvedio.utils.BitmapUtils;
import tvvedio.hc.com.tvvedio.view.MySeekBar;

/**
 * Created by ly on 2019/6/11.
 */

public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, View.OnClickListener, HttpService.HttpServiceResult {
    @BindView(R.id.video_surface)
    SurfaceView mSurfaceView;
    @BindView(R.id.iv_qrcode)
    ImageView qrcodeImageView;
    @BindView(R.id.video_progress)
    MySeekBar seekBar;
    @BindView(R.id.video_time)
    TextView videoTime;
    @BindView(R.id.gif_networkwait)
    GifImageView gif_networkwait;
    @BindView(R.id.iv_error)
    LinearLayout iv_error;
    @BindView(R.id.scan_progress)
    LinearLayout scan_progress;
    @BindView(R.id.button_play)
    ImageView bt_play;
    @BindView(R.id.button_pause)
    ImageView bt_pause;

    @BindView(R.id.rl_qrcode)
    RelativeLayout rl_qrcode;

    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private int currentPosition = 0;
    boolean canPlay = true;
    Map<String, String> listmap = new HashMap<>();
    Map<String, String> urlList = new HashMap<>();

    String truelList = null;
    String uid, token;
    private final static int SUCCESS = 1;
    private final static int PLAY_COMPLETE = 2;
    private final static int PLAY_DELETE_URL = 3;
    private final static int PLAY_DEFAULT = 4;
    private final static int QRCODE_IMAGE_UPDATA = 5;

    int count = 0;
    private static int THREE_MIN = 3 * 60 * 1000;
    String uuid = null;
    String timeout = null;
    String base64 = null;

    Context cotext;
    String ANDROID_ID;

    long starttime, endTime, trueEndtime, timeoutStart;

    private boolean playDefault = false;

    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestPermission(this);
        listmap.put("curPage", "1");
        cotext = getApplicationContext();
        HttpService.getInstance().getURLData(listmap, this);


        ANDROID_ID = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);

        Log.i("lylog  ANDROID_ID", ANDROID_ID);
        initView();

        mythread.start();
    }

    private void requestPermission(MainActivity mainActivity) {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(mainActivity.getApplicationContext(),
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(mainActivity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mediaPlayer = new MediaPlayer();
        surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(mSurfaceHolderCallBack);
        mSurfaceView.setOnClickListener(this);
        bt_play.setOnClickListener(this);
        bt_pause.setOnClickListener(this);

        LayerDrawable layerDrawable = (LayerDrawable)
                seekBar.getProgressDrawable();
        Drawable dra = layerDrawable.getDrawable(2);
        dra.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);
        seekBar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//
//                    long duration2 = mediaPlayer.getDuration();
//                    mediaPlayer.seekTo(progress * 1000);
//                    bt_play.setVisibility(View.GONE);
//                    mediaPlayer.start();
//                    long current = mediaPlayer.getCurrentPosition();
//                    long currentTime = System.currentTimeMillis();
//                    Log.i("lylog", " duration2 = " + duration2 + "  current =" + current + "  progress = " + progress);
//
//                    //boolean TimeDis = (currentTime - starttime) > duration2;//保证大于一节的时间
//                    if ((duration2 - current) < THREE_MIN) {
//                        HttpService.getInstance().getQcCodeIamage(ANDROID_ID, MainActivity.this);
//                    }
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private SurfaceHolder.Callback mSurfaceHolderCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
//            playVideo(url);
            if (playDefault) {
                Log.i("lylog", " sssssssssss");
                playVideo("http://bjcdn2.vod.migucloud.com/mgc_transfiles/200010145/2019/5/19/1EYo9UNOF95HCK30a3mHY/cld640p/video_1EYo9UNOF95HCK30a3mHY_cld640p.m3u8");
                playDefault = false;
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (playDefault) {
                Log.i("lylog", " surfaceChanged sssssssssss");
                playVideo("http://bjcdn2.vod.migucloud.com/mgc_transfiles/200010145/2019/5/19/1EYo9UNOF95HCK30a3mHY/cld640p/video_1EYo9UNOF95HCK30a3mHY_cld640p.m3u8");
                playDefault = false;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        }
    };

    private void playVideo(String url) {
        mediaPlayer.reset();
        gif_networkwait.setVisibility(View.VISIBLE);
        // 设置声音效果
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // 设置播放完成监听
        mediaPlayer.setOnCompletionListener(this);

        // 设置媒体加载完成以后回调函数。
        mediaPlayer.setOnPreparedListener(this);
        // 错误监听回调函数
        mediaPlayer.setOnErrorListener(this);
        // 设置缓存变化监听
        mediaPlayer.setOnBufferingUpdateListener(this);
        //网络请求：
        try {
            mediaPlayer.setDataSource(url);
//            mediaPlayer.setDataSource(path);
            // 设置异步加载视频，包括两种方式 prepare()同步，prepareAsync()异步
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.i("lylog", "palyvideo Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (canPlay) {
            gif_networkwait.setVisibility(View.VISIBLE);
            Message msg = new Message();
            msg.what = PLAY_COMPLETE;
            mHandler.sendMessage(msg);
            trueEndtime = System.currentTimeMillis();
            isUpdate = false;//播放完不轮询了
//            playVideo(truelList);
        }
        Log.i("lylog", " media onCompletion count = " + count);
    }

    private boolean playBreak = false;
    private boolean isContinue = true;

    @Override
    public void onPrepared(MediaPlayer mp) {
        starttime = endTime = trueEndtime = timeoutStart =0; //每次播放新的都要清零
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpService.getInstance().getQcCodeIamage(ANDROID_ID, MainActivity.this);
            }
        }).start();
        canPlay = true;
        isUpdate = true;//新的视频还是要轮询
        gif_networkwait.setVisibility(View.GONE);
        bt_play.setVisibility(View.GONE);

        int duration2 = mediaPlayer.getDuration() / 1000;
        seekBar.setMax(duration2);
        Log.i("lylog", " onPrepared d = " + duration2);
        videoTime.setText(calculateTime(duration2));
        mediaPlayer.start();

        starttime = System.currentTimeMillis();
        timeoutStart = starttime;
        endTime = starttime + mediaPlayer.getDuration();

        Log.d("lylog", " starttime - endtime = " + (endTime - starttime));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!playBreak) {
                    if (isContinue) {
                        int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);

//                        long duration2 = mediaPlayer.getDu*+-*ration();

                        long currentTime = System.currentTimeMillis();
//                        Log.i("lylog"," gengxin  ui  currentTime ="+currentTime + " timeoutStart= "+timeoutStart + "  bolean > 3min ="+Integer.valueOf(timeout) * 60 * 1000);
                        if (!"".equals(timeout) && !TextUtils.isEmpty(timeout) &&(currentTime - timeoutStart) > Integer.valueOf(timeout) * 60 * 1000) {
                            HttpService.getInstance().getQcCodeIamage(ANDROID_ID, MainActivity.this);
                            Log.i("lylog"," gengxin  ui ");
                            timeoutStart = currentTime;
                        }
////                        boolean TimeDis = (currentTime - starttime) > duration2;
//                        long current = mediaPlayer.getCurrentPosition();
//                        if ((duration2 - current) < THREE_MIN) {
//                            isUpdate = true;
////                            HttpService.getInstance().getQcCodeIamage(ANDROID_ID, MainActivity.this);
//                        }
                        Log.i("lylog", " ");
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d("lylog", " onError  = ");
        canPlay = false;
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

        /*if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentPosition = mediaPlayer.getCurrentPosition();
        }

        seekBar.setProgress((int) (((float) currentPosition) * 100 / mediaPlayer.getDuration()));//精确进度条*/

    }


    private String calculateTime(int time) {
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

    private void play(int currentPosition) {
        Log.i("lylog", "currentPosition =  " + currentPosition);
        mediaPlayer.seekTo(currentPosition);
        mediaPlayer.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_surface:
                if (mediaPlayer.isPlaying()) {
                    bt_pause.setVisibility(View.VISIBLE);
                    scan_progress.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bt_pause.setVisibility(View.GONE);
                                    scan_progress.setVisibility(View.GONE);
                                }
                            });

                        }
                    }).start();
                } else {
                    bt_play.setVisibility(View.VISIBLE);
                    scan_progress.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.button_play:
                play(currentPosition);
                bt_play.setVisibility(View.GONE);
                scan_progress.setVisibility(View.GONE);
                break;
            case R.id.button_pause:
                mediaPlayer.pause();
                bt_pause.setVisibility(View.GONE);
                bt_play.setVisibility(View.VISIBLE);
                scan_progress.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setallInvisible(int visible) {
        scan_progress.setVisibility(visible);
        bt_pause.setVisibility(visible);
        bt_play.setVisibility(visible);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentPosition = 0;
        mediaPlayer.release();
        isContinue = false;
        playBreak = true;
        isUpdate = false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        return handleKeyEvent(action, keyCode) || super.dispatchKeyEvent(event);

    }

    private boolean handleKeyEvent(int action, int keyCode) {
        Log.i("lylog", " handleKeyEvent");
        if (action != KeyEvent.ACTION_DOWN)
            return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:

                if (mediaPlayer.isPlaying()) {
                    Toast.makeText(this, " 点击确认键暂停", Toast.LENGTH_LONG).show();
                    mediaPlayer.pause();
                    bt_pause.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, " 点击确认键开始", Toast.LENGTH_LONG).show();
                    play(currentPosition);
                    canPlay = true;
                    bt_pause.setVisibility(View.GONE);
                }
                isContinue = true;
                //确定键enter
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                //向下键
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                //向上键
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //向左键
                isContinue = false;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //向右键
                isContinue = false;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void success(String result, int code) {
        if (code == 1) {
            urlList = JsonUtils.getIncetance().getURLlist(result);
            Log.i("lylog", " success code = 1 size =" + urlList.toString());
            if (null != urlList.get("url")) {
                HttpService.getInstance().getTokenUid(getApplicationContext(), this);
            } else {
//                HttpService.getInstance().getURLData(listmap, this);
                Log.i("lylogs", "  truelList =" + truelList);
                if (TextUtils.isEmpty(truelList)) {
                    Log.i("lylogs", " play 默认");
                    Message msg = new Message();
                    msg.what = PLAY_DEFAULT;
                    mHandler.sendMessage(msg);
                } else {
                    playVideo(truelList);
                }

            }
        }

        if (code == 2) {
            truelList = result; //真正的url
            Message msg = new Message();
            msg.what = SUCCESS;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private Thread mythread = new Thread() {
        @Override
        public void run() {
            super.run();
            while (true) {
                if (isUpdate) {
                    HttpService.getInstance().getQRState(uuid, ANDROID_ID, MainActivity.this);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean isUpdate = false;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS:
                    Log.i("lylog", "SUCCESS truelList =" + truelList);
                    playVideo(truelList);

                    //数据库删除本次的url，获取最新的权重
                    Message msg1 = new Message();
                    msg1.what = PLAY_DELETE_URL;
                    mHandler.sendMessage(msg1);
                    break;
                case PLAY_COMPLETE:
                    Log.i("lylog", " PLAY_COMPLETE");
                    HttpService.getInstance().getURLData(listmap, MainActivity.this);
                    qrcodeImageView.setImageBitmap(null);  //每次播放完成 都要删除 二维码
                    break;
                case PLAY_DELETE_URL:
                    HttpService.getInstance().deleteUrlFromServer(urlList.get("url"));
                    break;
                case QRCODE_IMAGE_UPDATA:
                    isUpdate = true; //二维码重新更新 looping也需要打开去监听
                    Log.i("lylog", "  QRCODE_IMAGE_UPDATA");
                    if (!TextUtils.isEmpty(base64)) {
                        if (bitmap != null) {
                            bitmap.recycle();
                        }
                        bitmap = BitmapUtils.stringToBitmap(base64);
                        qrcodeImageView.setImageBitmap(bitmap);



                       /* //一直要请求看是否被扫码
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (isUpdate) {
                                    HttpService.getInstance().getQRState(uuid, ANDROID_ID, MainActivity.this);
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();*/

                    }
                    break;
                case PLAY_DEFAULT:
                    // 避免surface没建立好久开始播放
                    Log.i("lylog", " PLAY_DEFAULT");
                    if (surfaceHolder.isCreating()) {
                        Log.i("lylogss", " isCreating");
                        playDefault = true;
                    } else {
                        playVideo("http://bjcdn2.vod.migucloud.com/mgc_transfiles/200010145/2019/5/19/1EYo9UNOF95HCK30a3mHY/cld640p/video_1EYo9UNOF95HCK30a3mHY_cld640p.m3u8");
                    }

                    break;
            }
        }
    };

    @Override
    public void tokenCallback(String uid, String token) {
        this.uid = uid;
        this.token = token;
        if (!"".equals(token) && !"".equals(uid) && null != urlList.get("url")) {
            HttpService.getInstance().getTrueUrl(urlList.get("url").replace("\"", ""), uid, token, this, getApplicationContext());
        }
    }

    @Override
    public void failed(String result) {

    }

    @Override
    public void qrcodeUuidAndLimitTime(Map<String, String> qrcode) {

        uuid = qrcode.get("uuid");//uuid 唯一编号
        base64 = "data:image/jpeg;base64," + qrcode.get("base64QrCode");//二维码
        timeout = qrcode.get("yxq"); // 二维码显示限制的时间 分钟

        timeoutStart = System.currentTimeMillis();

        Log.i("lylog", " THREE_MIN = " + THREE_MIN);
        Message msg = new Message();
        msg.what = QRCODE_IMAGE_UPDATA;
        mHandler.sendMessage(msg);
    }

    //身份证号码callback
    @Override
    public void sfzhmCallBack(String sfzhm) {
//        isUpdate = false;
        Log.i("lylog", " sfzhmCallBack sfzhm =" + sfzhm);
        Log.i("lylog", " sfzhmCallBack theme =" + urlList.get("theme"));
        //如果callback回来 分两部分，一部分 生成新的二维码，另一部分把上个用户信息向服务器请求下去
        // 需要的参数 有uuid（uuid），sbbh(设备编号)，sfzmhm（身份证编号），kssj（开始时间），jssj（结束时间），kjmc（课程名称）
        long time = System.currentTimeMillis();
        HttpService.getInstance().sendInfotoH5Server(uuid, ANDROID_ID, sfzhm.replace("\"", ""), starttime, time, urlList.get("theme").replace("\"", ""));

        //同时也要更新二维码
//        if(isUpdate){
        HttpService.getInstance().getQcCodeIamage(ANDROID_ID, MainActivity.this);
//        }

    }

}
