package com.yan.luaeditor.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yan.luaeditor.tools.ZoomableImageView;
import com.yan.luaide.R;

public class ImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);
        ZoomableImageView imageView=findViewById(R.id.image_activity);
        Intent intent=getIntent();
        imageView.setImageBitmap(BitmapFactory.decodeFile(intent.getStringExtra("imagePath")));
    }
}
