package tvvedio.hc.com.tvvedio.upload;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.SeekBar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tvvedio.hc.com.tvvedio.JsonUtils;
import tvvedio.hc.com.tvvedio.MainActivity;
import tvvedio.hc.com.tvvedio.utils.Utils;

/**
 * Created by ly on 2019/6/12.
 */

public class HttpService {
    private static HttpService mHttpService;

    public static HttpService getInstance() {
        if (mHttpService == null) {
            synchronized (HttpService.class) {
                if (mHttpService == null) {
                    mHttpService = new HttpService();
                }
            }
        }
        return mHttpService;
    }

    public void getURLData(Map map, final HttpService.HttpServiceResult callback) {
//        map.put("type", "1");
//        map.put("curPage", "1");
        map.put("pageSize", "1");//获取最新的
        //192.168.0.53:8085/jyptdbctl/video/getTvVideo?curPage=1&pageSize=10

        String url = Utils.NetWorkUtil.BASE_IP + "/jyptdbctl/video/getTvVideo?" + "curPage=" + map.get("curPage") + "&" + "pageSize=" + map.get("pageSize");
        Log.d("lylog", "getURLData type = " + map.get("type") + " curPage" + map.get("curPage"));
        Log.i("lylog", "getURLData url = " + url);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("lylogNet", " response error1");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.success(response.body().string(), 1);
                } else {
                    callback.failed(response.body().string());
                    Log.d("lylogNet", " response error2");
                }
            }
        });
    }

    public void getTokenUid(final Context context, final HttpService.HttpServiceResult callback) {
        String url = Utils.NetWorkUtil.BASE_IP + "/jyptdbctl/video/getUidToken";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Utils.showToast(context, "Token 获取失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                String uid = JsonUtils.getIncetance().getElement("uid", result);
                String token = JsonUtils.getIncetance().getElement("token", result);
                Log.d("lylog", " uid = " + uid + " token = " + token);
                callback.tokenCallback(uid, token);
            }
        });

    }

    public void getTrueUrl(String url, String uid, String token, final HttpServiceResult callback, final Context context) {

        String urlall = "http://bj.migucloud.com/vod2/v1/download_spotviurl?" + "uid=" + uid + "&token=" + token + "&vid=" + url + "&vtype=0,1,2";
        Log.i("lylog", "  ssss = " + urlall);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(urlall)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Utils.showToast(context, "trueUrl 获取失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();

                String urlTrue = JsonUtils.getIncetance().getTrueUrl(result);
                Log.i("lylog", " urlTrue =" + urlTrue);
                if (!"".equals(urlTrue)) {
                    callback.success(urlTrue, 2);
                }

            }
        });


    }

    public void deleteUrlFromServer(String path) {
        //http://192.168.0.53:8085/jyptdbctl/video/playComplete?path=2TFnY8P4F0HafwAWaWZmHn
        String url = Utils.NetWorkUtil.BASE_URL_COMPLETE + "path=" + path.replace("\"", "");
        Log.i("lylog", " deleteUrlFromServer url = " + url);
        requstHttpserver(url);
    }

    private void requstHttpserver(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("lylog", " url 删除失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("lylog", " url 删除成功");
                }
            }
        });
    }


    public void getQcCodeIamage(String ANDROID_ID, final HttpServiceResult callbak) {

        String requestUrl = Utils.NetWorkUtil.BASE_IP + "/jyptdbctl/qr/getQr?" + "sbbh=" + ANDROID_ID;
        Log.d("lylog","  new url "+requestUrl);
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // 设置是否使用缓存  默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("GET");
            //urlConn设置请求头信息
            //设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "application/json");
            //设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            // 开始连接
            urlConn.connect();
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                String result = streamToString(urlConn.getInputStream());
                Log.i("lylog ss", "Get方式请求成功，result--->" + result);
//                String string = response.body().string();
                    Map<String, String> qrcode = new HashMap<>();
                    qrcode = JsonUtils.getIncetance().getQrImaget(result);
                    callbak.qrcodeUuidAndLimitTime(qrcode);
            } else {
                Log.i("lylog ss", "Get方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();


        } catch (IOException e) {
            Log.i("lylog ss", "Get方式请求失败  IOException ");
            e.printStackTrace();
        }


    }

    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return
     */
    public String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.i("lylog Exception", e.toString());
            return null;
        }
    }

    //轮询的时候避免出现内存溢出 我还是不要在不停的创建对象
    OkHttpClient okHttpClientLunXun = new OkHttpClient();
    JsonParser parserLunXun = new JsonParser();

    public void getQRState(String uuid, String android_id, final HttpServiceResult callback) {
        String url = Utils.NetWorkUtil.BASE_IP + "/jyptdbctl/qr/getQrffective?" + "uuid=" + uuid + "&sbbh=" + android_id;
        Log.i("lylog", "getQRState url =" + url);

        final Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClientLunXun.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("lylog", "  请求okhttp失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String string = response.body().string();
                    //但是还是去解析
                    JsonObject jsonObject = (JsonObject) parserLunXun.parse(string);
                    JsonObject json = jsonObject.get("data").getAsJsonObject();
                    Log.i("lylog", "getQRState json.get(\"smbz\") =" +  "  json.get(\"sfzmhm\") = "+json.get("sfzmhm") + " equal = "+("".equals(json.get("sfzmhm"))  + " text ="+TextUtils.isEmpty(json.get("sfzmhm")+"")));
                    if (!"".equals(json.get("smbz")) && json.get("smbz").getAsString().equals("1")) {//smbz的状态1 为被人扫码了
                        if (json.get("sfzmhm")!=null && !"null".equals(json.get("sfzmhm").getAsString()) && json.get("sfzmhm").getAsString().length() != 0) {
                            String sfzhm = json.get("sfzmhm").getAsString();//身份证号码
                            Log.i("lylog", "getQRState sfzhm =" + sfzhm);
                            callback.sfzhmCallBack(sfzhm);
                        }
                    }

                }
            }
        });
    }

    public void sendInfotoH5Server(String uuid, String android_id, String sfzhm, long starttime, long trueEndtime, String theme) {
        String urls = Utils.NetWorkUtil.BASE_IP + "/jyptdbctl/qr/submitstudy?" + "uuid=" + uuid + "&sbbh=" + android_id + "&sfzmhm=" + sfzhm + "&kssj=" + starttime + "&jssj=" + trueEndtime + "&kjmc=" + theme;

        Log.d("lylog","  sendInfotoH5Server new url ");
        try {
            URL url = new URL(urls);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // 设置是否使用缓存  默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("GET");
            //urlConn设置请求头信息
            //设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "application/json");
            //设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            // 开始连接
            urlConn.connect();
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                String result = streamToString(urlConn.getInputStream());
                Log.i("lylog ss", "Get方式请求成功，result--->" + result);
//                String string = response.body().string();
            } else {
                Log.i("lylog ss", "Get方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();


        } catch (IOException e) {
            Log.i("lylog ss", "Get方式请求失败  IOException ");
            e.printStackTrace();
        }


    }

    public interface HttpServiceResult {
        void success(String result, int code);

        void tokenCallback(String uid, String token);

        void failed(String result);


        void qrcodeUuidAndLimitTime(Map<String, String> qrcode);

        void sfzhmCallBack(String sfzhm);
    }
}
