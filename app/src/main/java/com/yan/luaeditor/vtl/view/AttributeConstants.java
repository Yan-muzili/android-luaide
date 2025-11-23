package com.yan.luaeditor.vtl.view;

import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

/**
 * 包含所有属性相关的常量定义
 */
public interface AttributeConstants {
    // 可见性
    int VISIBLE = View.VISIBLE;
    int INVISIBLE = View.INVISIBLE;
    int GONE = View.GONE;

    // 尺寸模式
    int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;

    // 方向
    int VERTICAL = LinearLayout.VERTICAL;
    int HORIZONTAL = LinearLayout.HORIZONTAL;

    // 重力相关常量
    int BOTTOM = Gravity.BOTTOM;
    int CENTER = Gravity.CENTER;
    int CENTER_HORIZONTAL = Gravity.CENTER_HORIZONTAL;
    int CENTER_VERTICAL = Gravity.CENTER_VERTICAL;
    int FILL_HORIZONTAL = Gravity.FILL_HORIZONTAL;
    int FILL_VERTICAL = Gravity.FILL_VERTICAL;
    int LEFT = Gravity.LEFT;
    int RIGHT = Gravity.RIGHT;
    int START = Gravity.START;
    int END = Gravity.END;
    int TOP = Gravity.TOP;

    // 输入类型
    int INPUT_TEXT = InputType.TYPE_CLASS_TEXT;
    int INPUT_TEXT_CAP_CHARACTERS = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
    int INPUT_TEXT_CAP_WORDS = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS;
    int INPUT_TEXT_CAP_SENTENCES = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
    int INPUT_TEXT_MULTI_LINE = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
    int INPUT_TEXT_PASSWORD = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
    int INPUT_TEXT_VISIBLE_PASSWORD = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
    int INPUT_TEXT_EMAIL_ADDRESS = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    int INPUT_NUMBER = InputType.TYPE_CLASS_NUMBER;
    int INPUT_NUMBER_SIGNED = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
    int INPUT_NUMBER_DECIMAL = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    int INPUT_PHONE = InputType.TYPE_CLASS_PHONE;
    int INPUT_DATETIME = InputType.TYPE_CLASS_DATETIME;
    int INPUT_DATE = InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE;
    int INPUT_TIME = InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME;

    // IME选项
    int IME_ACTION_UNSPECIFIED = EditorInfo.IME_ACTION_UNSPECIFIED;
    int IME_ACTION_NONE = EditorInfo.IME_ACTION_NONE;
    int IME_ACTION_GO = EditorInfo.IME_ACTION_GO;
    int IME_ACTION_SEARCH = EditorInfo.IME_ACTION_SEARCH;
    int IME_ACTION_SEND = EditorInfo.IME_ACTION_SEND;
    int IME_ACTION_NEXT = EditorInfo.IME_ACTION_NEXT;
    int IME_ACTION_DONE = EditorInfo.IME_ACTION_DONE;
    int IME_ACTION_PREVIOUS = EditorInfo.IME_ACTION_PREVIOUS;

    // 缩放类型
    int SCALE_MATRIX = 0;
    int SCALE_FIT_XY = 1;
    int SCALE_FIT_START = 2;
    int SCALE_FIT_CENTER = 3;
    int SCALE_FIT_END = 4;
    int SCALE_CENTER = 5;
    int SCALE_CENTER_CROP = 6;
    int SCALE_CENTER_INSIDE = 7;

    // RelativeLayout布局参数
    int LAYOUT_ABOVE = RelativeLayout.ABOVE;
    int LAYOUT_BELOW = RelativeLayout.BELOW;
    int LAYOUT_ALIGN_LEFT = RelativeLayout.ALIGN_LEFT;
    int LAYOUT_ALIGN_RIGHT = RelativeLayout.ALIGN_RIGHT;
    int LAYOUT_ALIGN_TOP = RelativeLayout.ALIGN_TOP;
    int LAYOUT_ALIGN_BOTTOM = RelativeLayout.ALIGN_BOTTOM;
    int LAYOUT_CENTER_HORIZONTAL = RelativeLayout.CENTER_HORIZONTAL;
    int LAYOUT_CENTER_VERTICAL = RelativeLayout.CENTER_VERTICAL;
    int LAYOUT_CENTER_IN_PARENT = RelativeLayout.CENTER_IN_PARENT;
    int LAYOUT_TO_LEFT_OF = RelativeLayout.LEFT_OF;
    int LAYOUT_TO_RIGHT_OF = RelativeLayout.RIGHT_OF;
    int LAYOUT_ALIGN_PARENT_LEFT = RelativeLayout.ALIGN_PARENT_LEFT;
    int LAYOUT_ALIGN_PARENT_RIGHT = RelativeLayout.ALIGN_PARENT_RIGHT;
    int LAYOUT_ALIGN_PARENT_TOP = RelativeLayout.ALIGN_PARENT_TOP;
    int LAYOUT_ALIGN_PARENT_BOTTOM = RelativeLayout.ALIGN_PARENT_BOTTOM;

    // ConstraintLayout常量


    // AndroidX特有常量
    int RECYCLER_LINEAR = 0;
    int RECYCLER_GRID = 1;
    int RECYCLER_STAGGERED = 2;
    int VIEWPAGER_ORIENTATION_HORIZONTAL = ViewPager2.ORIENTATION_HORIZONTAL;
    int VIEWPAGER_ORIENTATION_VERTICAL = ViewPager2.ORIENTATION_VERTICAL;

    // Material特有常量
    int TAB_MODE_FIXED = TabLayout.MODE_FIXED;
    int TAB_MODE_SCROLLABLE = TabLayout.MODE_SCROLLABLE;
    int TAB_GRAVITY_FILL = TabLayout.GRAVITY_FILL;
    int TAB_GRAVITY_CENTER = TabLayout.GRAVITY_CENTER;
    int BOTTOM_NAVIGATION_LABEL_VISIBILITY_AUTO = BottomNavigationView.LABEL_VISIBILITY_AUTO;
    int BOTTOM_NAVIGATION_LABEL_VISIBILITY_SELECTED = BottomNavigationView.LABEL_VISIBILITY_SELECTED;
    int BOTTOM_NAVIGATION_LABEL_VISIBILITY_LABELED = BottomNavigationView.LABEL_VISIBILITY_LABELED;
    int BOTTOM_NAVIGATION_LABEL_VISIBILITY_UNLABELED = BottomNavigationView.LABEL_VISIBILITY_UNLABELED;

}
    