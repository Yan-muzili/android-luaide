package com.yan.luaeditor.tools;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkRequestUtils {

    private Handler handler;

    public NetworkRequestUtils() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void sendGetRequest(String urlString, final OnResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        String response = readInputStream(inputStream);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onResponse(jsonObject);
                                }
                            });
                        } catch (JSONException e) {
                            // 不是 JSON 数据，当作普通文字处理
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onResponse(response);
                                }
                            });
                        }
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onError("请求失败，响应码：" + responseCode);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError("网络请求异常：" + e.getMessage());
                        }
                    });
                } finally {
                    if (connection!= null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public void sendPostRequest(String urlString, String postData, final OnResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(postData);
                    outputStream.flush();
                    outputStream.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        String response = readInputStream(inputStream);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onResponse(jsonObject);
                                }
                            });
                        } catch (JSONException e) {
                            // 不是 JSON 数据，当作普通文字处理
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onResponse(response);
                                }
                            });
                        }
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onError("请求失败，响应码：" + responseCode);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError("网络请求异常：" + e.getMessage());
                        }
                    });
                } finally {
                    if (connection!= null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine())!= null) {
            stringBuilder.append(line);
        }
        reader.close();
        return stringBuilder.toString();
    }

    public interface OnResponseListener {
        void onResponse(JSONObject jsonObject);

        void onResponse(String text);

        void onError(String error);
    }
}
