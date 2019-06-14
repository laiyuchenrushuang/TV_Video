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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;
import tvvedio.hc.com.tvvedio.upload.HttpService;
import tvvedio.hc.com.tvvedio.utils.BitmapUtils;
import tvvedio.hc.com.tvvedio.utils.TimeUtils;
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
    private static final int GET_NET_URLDM = 1; //获取url的代码值
    private final static int PLAY_COMPLETE = 2; //播放完成
    private final static int PLAY_DELETE_URL = 3; //在网络列表播放需要删除列表的url
    private final static int PLAY_DEFAULT = 4;  //播放默认视频
    private final static int QRCODE_IMAGE_UPDATA = 5; //二维码更新
    private static final int DEFAULT_PLAY_ONLION = 6; //播放默认视频Onlion
    private final static int SUCCESS = 7; //获取网络视频列表成功播放

    int count = 0;
    private static int THREE_MIN = 3 * 60 * 1000;
    String uuid = null;
    String timeout = null;
    String base64 = null;

    Context cotext;
    String ANDROID_ID;

    long starttime, timeoutStart;

    private boolean playDefault = false;

    private Bitmap bitmap;

    ArrayList<Map<String, String>> urlDMList = new ArrayList<>();

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
        HttpService.getInstance(this).getURLData(listmap, this);


        ANDROID_ID = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);

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
////            playVideo(url);
//            if (playDefault) {
//                Log.i("lylog", " sssssssssss");
//                playVideo("http://bjcdn2.vod.migucloud.com/mgc_transfiles/200010145/2019/5/19/1EYo9UNOF95HCK30a3mHY/cld640p/video_1EYo9UNOF95HCK30a3mHY_cld640p.m3u8");
//                playDefault = false;
//            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            if (playDefault) {
//                Log.i("lylog", " surfaceChanged sssssssssss");
//                playVideo("http://bjcdn2.vod.migucloud.com/mgc_transfiles/200010145/2019/5/19/1EYo9UNOF95HCK30a3mHY/cld640p/video_1EYo9UNOF95HCK30a3mHY_cld640p.m3u8");
//                playDefault = false;
//            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
//            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                currentPosition = mediaPlayer.getCurrentPosition();
//                mediaPlayer.stop();
//            }
        }
    };

    private void playVideo(String url) {
        Log.i("lylog", "<result final>地址 = " + url + "\n" + " 主题 = " + currenttheme);
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
            isUpdate = false;//播放完不轮询了
//            playVideo(truelList);
        }
        Log.i("lylog", " media onCompletion count = " + count);
    }

    private boolean playBreak = false;
    private boolean isContinue = true;

    @Override
    public void onPrepared(MediaPlayer mp) {
        starttime = timeoutStart = 0; //每次播放新的都要清零
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpService.getInstance(MainActivity.this).getQcCodeIamage(ANDROID_ID, MainActivity.this);
            }
        }).start();
        canPlay = true;
        isUpdate = true;//新的视频还是要轮询
        gif_networkwait.setVisibility(View.GONE);
        bt_play.setVisibility(View.GONE);

        int duration2 = mediaPlayer.getDuration() / 1000;
        seekBar.setMax(duration2);
        Log.i("lylog", " onPrepared d = " + duration2);
        videoTime.setText(TimeUtils.calculateTime(duration2));
        mediaPlayer.start();

        starttime = System.currentTimeMillis();
        timeoutStart = starttime;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!playBreak) {
                    if (isContinue) {

                        currentPosition = mediaPlayer.getCurrentPosition();
                        int position = currentPosition / 1000;
                        seekBar.setProgress(position);

                        long currentTime = System.currentTimeMillis();
                        if (!"".equals(timeout) && !TextUtils.isEmpty(timeout) && (currentTime - timeoutStart) > Integer.valueOf(timeout) * 60 * 1000) {
                            HttpService.getInstance(MainActivity.this).getQcCodeIamage(ANDROID_ID, MainActivity.this);
                            Log.i("lylog", " gengxin  ui ");
                            timeoutStart = currentTime;
                        }
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
        Log.i("lylog", " onError  = ");
        canPlay = false;
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
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
        if (code == GET_NET_URLDM) {
            urlList = JsonUtils.getIncetance().getURLlist(result);
            Log.i("lylog", " success code = 1 urlList =" + urlList.toString());
            if (null != urlList.get("url")) {
                currenttheme = urlList.get("theme");
                HttpService.getInstance(this).getTokenUid(getApplicationContext(), this);
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

        if (code == SUCCESS) {
            truelList = result; //真正的url
            Message msg = new Message();
            msg.what = SUCCESS;
            mHandler.sendMessage(msg);
        }
        if (code == DEFAULT_PLAY_ONLION) {
            String randomUrl = result; //真正的url
            Message msg = new Message();
            msg.obj = randomUrl;
            msg.what = DEFAULT_PLAY_ONLION;
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
                    HttpService.getInstance(MainActivity.this).getQRState(uuid, ANDROID_ID, MainActivity.this);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean isUpdate = false;
    private String currenttheme = null;
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
                    HttpService.getInstance(MainActivity.this).getURLData(listmap, MainActivity.this);
                    qrcodeImageView.setImageBitmap(null);  //每次播放完成 都要删除 二维码
                    break;
                case PLAY_DELETE_URL:
                    HttpService.getInstance(MainActivity.this).deleteUrlFromServer(urlList.get("url"));
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
                    }
                    break;
                case PLAY_DEFAULT:
                    // 避免surface没建立好久开始播放
                    Log.i("lylog", " PLAY_DEFAULT");

                    if (!"".equals(msg.arg1) && msg.arg1 == DEFAULT_PLAY_ONLION && urlDMList.size() > 0) {
                        int randomNum = (int) (Math.random() * urlDMList.size());

                        String randomUrldm = urlDMList.get(randomNum).get("url").replace("\"", "").replace("\\n", "");//去掉“，去掉\n

                        currenttheme = urlDMList.get(randomNum).get("theme");

                        HttpService.getInstance(MainActivity.this).getTrueUrl(randomUrldm, uid, token, MainActivity.this, DEFAULT_PLAY_ONLION);
                    } else {
                        HttpService.getInstance(MainActivity.this).getRandomURLForDefaultPlay(MainActivity.this);
//                            playVideo("http://bjcdn2.vod.migucloud.com/mgc_transfiles/200010145/2019/5/19/1EYo9UNOF95HCK30a3mHY/cld640p/video_1EYo9UNOF95HCK30a3mHY_cld640p.m3u8");
                    }

                    break;

                case DEFAULT_PLAY_ONLION:
                    Log.i("lylog", " DEFAULT_PLAY_ONLION");
                    String randomUrl = (String) msg.obj;
                    playVideo(randomUrl);
                    break;
            }
        }
    };

    @Override
    public void tokenCallback(String uid, String token) {
        this.uid = uid;
        this.token = token;
        if (!"".equals(token) && !"".equals(uid) && null != urlList.get("url")) {
            HttpService.getInstance(MainActivity.this).getTrueUrl(urlList.get("url").replace("\"", ""), uid, token, this, SUCCESS);
        }
    }

    @Override
    public void failed(String result) {
        Log.i("lylog", " urldmList get failed");
        Message msg = new Message();
        msg.what = PLAY_DEFAULT;
        mHandler.sendMessage(msg);
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

        long time = System.currentTimeMillis(); //扫码当前时间
        if (urlList.size() > 0) {
            HttpService.getInstance(MainActivity.this).sendInfotoH5Server(uuid, ANDROID_ID, sfzhm.replace("\"", ""), starttime, time, urlList.get("theme").replace("\"", ""));
        }

        //同时也要更新二维码
//        if(isUpdate){
        HttpService.getInstance(MainActivity.this).getQcCodeIamage(ANDROID_ID, MainActivity.this);
//        }

    }

    @Override
    public void RandomURLForDefaultPlayCallback(String uid, String token, ArrayList<Map<String, String>> urlDmList) {
        this.urlDMList = urlDmList;
        this.uid = uid;
        this.token = token;

        Message msg = new Message();
        msg.what = PLAY_DEFAULT;
        msg.arg1 = DEFAULT_PLAY_ONLION;
        mHandler.sendMessage(msg);

    }

}
