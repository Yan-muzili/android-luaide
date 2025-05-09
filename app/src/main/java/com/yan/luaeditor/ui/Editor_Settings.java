package com.yan.luaeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.yan.luaeditor.adapter.SetListAdapter;
import com.yan.luaide.R;
import com.yan.luaide.databinding.EditorSettingsBinding;

import java.util.ArrayList;
import java.util.List;

public class Editor_Settings extends AppCompatActivity {
    EditorSettingsBinding binding;
    MaterialToolbar materialToolbar;
    RecyclerView recyclerView;
    private SetListAdapter itemAdapter;
    private List<SetListAdapter.ItemModel> itemList;
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
        binding=EditorSettingsBinding.inflate(getLayoutInflater());
        materialToolbar=binding.activityEditorSetToolbar;
        recyclerView=binding.editorSetList;
        setContentView(binding.getRoot());
        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("编辑器设置");
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
        itemList.add(new SetListAdapter.ItemModel("符号栏","编辑底部符号栏"));
        //itemList.add(new SetListAdapter.ItemModel("背景","选择IDE背景颜色（需要重启）"));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new SetListAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);
        itemAdapter.setOnItemClickListener(new SetListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (itemList.get(position).getTitle()){
                    case "符号栏":
                        startActivity(new Intent(Editor_Settings.this,Symbol_Settings.class));
                        break;
                }
            }
        });
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
