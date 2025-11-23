package com.yan.luaeditor.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.ipc.RootService;
import com.topjohnwu.superuser.nio.FileSystemManager;
import com.yan.luaeditor.tools.ThreadManager;
import com.yan.luaeditor.tools.memorytool.AIDLService;
import com.yan.luaide.ILuaideMemoryTool;
import com.yan.luaide.LuaUtil;
import com.yan.luaide.R;

import java.util.HashMap;

public class MemoryTest extends AppCompatActivity {
    public static final String TAG = "EXAMPLE";
    TextView tv_pid;
    TextView tv_searchResultCount;
    EditText et_input;
    Button btn_search;
    Button btn_clear;
    EditText et_etAddr;
    EditText et_etVal;
    Button btn_edit;


    private AIDLConnection aidlConn;
    private AIDLConnection daemonConn;
    private FileSystemManager remoteFS;

    class AIDLConnection implements ServiceConnection {

        private final boolean isDaemon;

        AIDLConnection(boolean b) {
            isDaemon = b;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "AIDL onServiceConnected");
            if (isDaemon) {
                daemonConn = this;
            } else {
                aidlConn = this;
            }

            ILuaideMemoryTool ipc = ILuaideMemoryTool.Stub.asInterface(service);
            try {
                int ss=ipc.startMemoryMonitoring("com.yan.luaide");
                Toast.makeText(MemoryTest.this,ss+"",Toast.LENGTH_SHORT).show();
                //if(ss==0)Toast.makeText(MemoryTest.this,"",Toast.LENGTH_SHORT).show();
                while (ipc.getMemoryMonitoringResults().length==0){

                }
                ThreadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //StringBuffer sb=new StringBuffer();
                            HashMap<Long,Integer> map=new HashMap<>();
                            for (String ii:ipc.getMemoryMonitoringResults()) {
                                String[] ss = ii.split("-");
                                long start=Long.parseLong(ss[0], 16);
                                long end=Long.parseLong(ss[1], 16);
                                for (long i=start;i<=end;i+=4) {
                                    //System.out.println(i + "=" + ipc.getValue(ipc.getPid("com.yan.luaide"), i));
                                    map.put(i,ipc.getValue(ipc.getPid("com.yan.luaide"), i));
                                    LuaUtil.save2("/sdcard/Luaide/memorytest.txt",LuaUtil.longToHex(i,true,true,false)+"="+ipc.getValue(ipc.getPid("com.yan.luaide"), i)+"\n");
                                    //sb.append(i + "=" + ipc.getValue(ipc.getPid("com.yan.luaide"), i) + "\n");
                                }
                                //LuaUtil.save2("/sdcard/Luaide/test.txt",map.toString());
                            }
                            /*ThreadManager.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_searchResultCount.setText(map.toString());
                                }
                            });*/

                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

            } catch (RemoteException e) {
                System.out.println(e.getMessage());
            }
            try {
                int pid = ipc.getPid("com.yan.luaide");
                System.out.println("AIDL PID : " + pid);
                tv_pid.setText(String.valueOf(pid));
                btn_clear.setOnClickListener(v -> {
                    Thread thread = new Thread(() -> {
                        try {
                            ipc.clearResults();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                    Toast.makeText(MemoryTest.this, "清除结果完成", Toast.LENGTH_SHORT).show();
                });
                btn_search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String input = et_input.getText().toString();

                        Thread thread = new Thread(() -> {
                            try {
                                runOnUiThread(() -> {
                                    Toast.makeText(MemoryTest.this, "开始搜索", Toast.LENGTH_SHORT).show();
                                });
                                int count = ipc.searchNumberDWORD(pid, Integer.parseInt(input));
                                tv_searchResultCount.setText(String.valueOf(ipc.getResultsCount()));
                                runOnUiThread(() -> {
                                    Toast.makeText(MemoryTest.this, "搜索完成", Toast.LENGTH_SHORT).show();
                                });
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        });
                        thread.start();
                    }
                });

                btn_edit.setOnClickListener(v -> {
                    Thread thread = new Thread(() -> {
                        try {
                            long address = Long.decode(et_etAddr.getText().toString());
                            int value = Integer.parseInt(et_etVal.getText().toString());
                            ipc.setValue(pid, address, value);
                            runOnUiThread(() -> {
                                Toast.makeText(MemoryTest.this, "修改成功", Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(MemoryTest.this, "修改失败", Toast.LENGTH_SHORT).show();
                            });
                            e.printStackTrace();
                        }

                    });
                    thread.start();
                });

            } catch (RemoteException e) {
                Log.e(TAG, "Remote error", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "AIDL onServiceDisconnected");
            if (isDaemon) {
                daemonConn = null;
            } else {
                aidlConn = null;
                remoteFS = null;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memorytest);
        tv_pid = findViewById(R.id.tv_PID);
        tv_searchResultCount = findViewById(R.id.tv_searchResultCount);
        et_input = findViewById(R.id.et_input);
        btn_search = findViewById(R.id.btn_search);
        btn_clear = findViewById(R.id.btn_clear);
        et_etAddr = findViewById(R.id.et_etAddr);
        et_etVal = findViewById(R.id.et_etVal);
        btn_edit = findViewById(R.id.btn_edit);

        if (aidlConn == null) {
            Intent intent = new Intent(this, AIDLService.class);
            RootService.bind(intent, new AIDLConnection(false));
        } else {
            RootService.unbind(aidlConn);
        }
    }
}
