package com.yan.luaeditor.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.yan.luaeditor.adapter.SetListAdapter;
import com.yan.luaeditor.tools.DrawableUtil;
import com.yan.luaide.R;
import com.yan.luaide.databinding.SymbolSettingsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Symbol_Settings extends AppCompatActivity {
    SymbolSettingsBinding binding;
    MaterialToolbar materialToolbar;
    RecyclerView recyclerView;
    TextView symbolmenu;
    private SetListAdapter itemAdapter;
    private List<SetListAdapter.ItemModel> itemList;
    SharedPreferences sps,sps2;
    SharedPreferences.Editor ed,ed2;
    List<String> symbols=new ArrayList<>();
    List<String> equivalents=new ArrayList<>();
    List<String> num=new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences Scheme = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        if ((Scheme.getInt("Background", 0) == 0)) {
            if ((Scheme.getInt("Scheme", -1)) != -1) {
                switch (Scheme.getInt("Scheme", -1)) {
                    case 0:
                        setTheme(R.style.Theme_AndroidIDE_BlueWave);
                        break;
                    case 1:
                        setTheme(R.style.Theme_AndroidIDE_SunnyGlow);
                        break;
                    case 2:
                        setTheme(R.style.Theme_Material3_Blue_NoActionBar);
                        break;
                    case 3:
                        setTheme(R.style.Theme_Material3_Green_NoActionBar);
                        break;
                    case 4:
                        setTheme(R.style.Theme_Material3_Orange_NoActionBar);
                        break;
                    case 5:
                        setTheme(R.style.Theme_Material3_Brown_NoActionBar);
                        break;
                    case 6:
                        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
                        break;
                }
            }
        } else {
            if ((Scheme.getInt("Scheme", -1)) != -1) {
                switch (Scheme.getInt("Scheme", -1)) {
                    case 0:
                        setTheme(R.style.Theme_AndroidIDE_BlueWave_Dark);
                        break;
                    case 1:
                        setTheme(R.style.Theme_AndroidIDE_SunnyGlow_Dark);
                        break;
                    case 2:
                        setTheme(R.style.Theme_Material3_Blue_Dark_NoActionBar);
                        break;
                    case 3:
                        setTheme(R.style.Theme_Material3_Green_Dark_NoActionBar);
                        break;
                    case 4:
                        setTheme(R.style.Theme_Material3_Orange_Dark_NoActionBar);
                        break;
                    case 5:
                        setTheme(R.style.Theme_Material3_Brown_Dark_NoActionBar);
                        break;
                    case 6:
                        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
                        break;
                }
            }
        }
        binding=SymbolSettingsBinding.inflate(getLayoutInflater());
        materialToolbar=binding.activitySymbolSetToolbar;
        recyclerView=binding.symbolSetList;
        symbolmenu=binding.symbolMenu;
        DrawableUtil.setDrawableColor(symbolmenu,0xff000000);
        setContentView(binding.getRoot());
        init();
    }

    public void init(){
        symbols=new ArrayList<>();
        equivalents=new ArrayList<>();
        sps = getSharedPreferences("EditorSymbol", Context.MODE_PRIVATE);
        sps2 = getSharedPreferences("Equivalents", Context.MODE_PRIVATE);
        ed = sps.edit();
        ed2=sps2.edit();
        ed = sps.edit();
        if (sps!=null){
            Map<String, ?> symbol_list = sps.getAll();
            Set<String> keys=symbol_list.keySet();
            for (String key:keys) {
                symbols.add(sps.getString(key,null));
                equivalents.add(sps2.getString(key,null));
                num.add(key);
            }
        }
        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("符号栏设置");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, true);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.R.attr.colorSecondary);
            if (Build.VERSION.SDK_INT >= 34) {
                window.getInsetsController().setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        }
        View contentView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, insets) -> {
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (!isKeyboardVisible) {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                v.setPadding(v.getPaddingLeft(),
                        statusBarHeight,
                        v.getPaddingRight(),
                        v.getPaddingBottom());
            }
            return insets;
        });
        itemList = new ArrayList<>();
        for (int i = 0; i < symbols.size(); i++) {
            itemList.add(new SetListAdapter.ItemModel(symbols.get(i),equivalents.get(i)));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new SetListAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);
        itemAdapter.setOnItemClickListener(new SetListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //YanDialog.show(Symbol_Settings.this,symbols[position],equivalents[position]);
            }
        });
        itemAdapter.setOnItemLongClickListener(new SetListAdapter.OnItemLongClickListener() {
            @Override
            public void onLongItemClick(int position) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(Symbol_Settings.this);
                alertDialogBuilder
                        .setTitle("删除符号")
                        .setMessage("是否删除符号"+symbols.get(position)+"？")
                        .setPositiveButton("确定",((dialog, which) -> {
                            ed.remove(num.get(position));
                            ed2.remove(num.get(position));
                            System.out.println(symbols.get(position)+position);
                            symbols.remove(position);
                            equivalents.remove(position);
                            num.remove(position);
                            ed.commit();
                            ed2.commit();
                            init();
                        }))
                        .setNegativeButton("取消",((dialog, which) -> {
                            dialog.dismiss();
                        }));
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
        symbolmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show(Symbol_Settings.this);
            }
        });
    }
    public void show(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.new_symbol, null);
        TextInputEditText input = dialogView.findViewById(R.id.symbol_input);
        TextInputEditText text = dialogView.findViewById(R.id.symbol_text);
        MaterialButton put_symbol_true = dialogView.findViewById(R.id.put_symbol_true);
        MaterialButton put_symbol_false = dialogView.findViewById(R.id.put_symbol_false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        put_symbol_false.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        put_symbol_true.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed.putString(itemList.size()+"",input.getText().toString());
                ed2.putString(itemList.size()+"",text.getText().toString());
                ed.commit();
                ed2.commit();
                dialog.dismiss();
                init();
            }
        });
        dialog.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
