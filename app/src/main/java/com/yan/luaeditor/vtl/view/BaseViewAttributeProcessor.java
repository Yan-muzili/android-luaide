package com.yan.luaeditor.vtl.view;

import static com.yan.luaeditor.vtl.view.AttributeUtils.parseFloatSafely;
import static com.yan.luaeditor.vtl.view.AttributeUtils.parseIntSafely;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import androidx.core.content.ContextCompat;

/**
 * 基础控件属性处理器，处理所有View的通用属性
 */
public class BaseViewAttributeProcessor implements ViewAttributeProcessor, AttributeConstants {

    @Override
    public boolean processAttribute(View view, String attributeName, String attributeValue) {
        if (processCommonAttributes(view, attributeName, attributeValue)) {
            return true;
        }

        if (view instanceof TextView) {
            return processTextViewAttributes((TextView) view, attributeName, attributeValue);
        } else if (view instanceof ImageView) {
            return processImageViewAttributes((ImageView) view, attributeName, attributeValue);
        } else if (view instanceof Button) {
            return processButtonAttributes((Button) view, attributeName, attributeValue);
        } else if (view instanceof EditText) {
            return processEditTextAttributes((EditText) view, attributeName, attributeValue);
        } else if (view instanceof CompoundButton) {
            return processCompoundButtonAttributes((CompoundButton) view, attributeName, attributeValue);
        } else if (view instanceof CheckBox) {
            return processCheckBoxAttributes((CheckBox) view, attributeName, attributeValue);
        } else if (view instanceof RadioButton) {
            return processRadioButtonAttributes((RadioButton) view, attributeName, attributeValue);
        } else if (view instanceof Switch) {
            return processSwitchAttributes((Switch) view, attributeName, attributeValue);
        } else if (view instanceof ProgressBar) {
            return processProgressBarAttributes((ProgressBar) view, attributeName, attributeValue);
        } else if (view instanceof SeekBar) {
            return processSeekBarAttributes((SeekBar) view, attributeName, attributeValue);
        } else if (view instanceof RatingBar) {
            return processRatingBarAttributes((RatingBar) view, attributeName, attributeValue);
        } else if (view instanceof Spinner) {
            return processSpinnerAttributes((Spinner) view, attributeName, attributeValue);
        } else if (view instanceof ListView) {
            return processListViewAttributes((ListView) view, attributeName, attributeValue);
        } else if (view instanceof GridView) {
            return processGridViewAttributes((GridView) view, attributeName, attributeValue);
        } else if (view instanceof ScrollView) {
            return processScrollViewAttributes((ScrollView) view, attributeName, attributeValue);
        } else if (view instanceof HorizontalScrollView) {
            return processHorizontalScrollViewAttributes((HorizontalScrollView) view, attributeName, attributeValue);
        } else if (view instanceof WebView) {
            return processWebViewAttributes((WebView) view, attributeName, attributeValue);
        } else if (view instanceof ViewFlipper) {
            return processViewFlipperAttributes((ViewFlipper) view, attributeName, attributeValue);
        } else if (view instanceof ImageButton) {
            return processImageButtonAttributes((ImageButton) view, attributeName, attributeValue);
        } else if (view instanceof ToggleButton) {
            return processToggleButtonAttributes((ToggleButton) view, attributeName, attributeValue);
        }
        
        return false;
    }
    private static final String TAG = "BasicWidgetsProcessor";

    /**
     * 处理CompoundButton属性
     */
    public boolean processCompoundButtonAttributes(CompoundButton compoundButton,
                                                   String attributeName,
                                                   String attributeValue) {
        Context ctx = compoundButton.getContext();
        switch (attributeName) {
            /* -------- 状态与图标 -------- */
            case "checked":
                compoundButton.setChecked(Boolean.parseBoolean(attributeValue));
                return true;

            case "button":
                try {
                    compoundButton.setButtonDrawable(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid button drawable: " + attributeValue);
                }
                return true;

            case "buttonTint":
                try {
                    ColorStateList tint = ColorStateList.valueOf(Color.parseColor(attributeValue));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        compoundButton.setButtonTintList(tint);
                    }
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid buttonTint: " + attributeValue);
                }
                return true;

            case "buttonTintMode":
                PorterDuff.Mode mode = parsePorterDuffMode(attributeValue);
                if (mode != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    compoundButton.setButtonTintMode(mode);
                }
                return true;

            /* -------- 文本相关 -------- */
            case "text":
                if (compoundButton instanceof TextView) {
                    ((TextView) compoundButton).setText(attributeValue);
                }
                return true;

            case "textSize":
                try {
                    ((TextView) compoundButton).setTextSize(TypedValue.COMPLEX_UNIT_SP,
                            parseFloatSafely(attributeValue, 14));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid textSize: " + attributeValue);
                }
                return true;

            case "textColor":
                try {
                    ((TextView) compoundButton).setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid textColor: " + attributeValue);
                }
                return true;

            case "textStyle":
                ((TextView) compoundButton).setTypeface(
                        ((TextView) compoundButton).getTypeface(),
                        parseTextStyle(attributeValue));
                return true;

            case "textAllCaps":
                if (compoundButton instanceof TextView) {
                    ((TextView) compoundButton).setAllCaps(Boolean.parseBoolean(attributeValue));
                }
                return true;

            case "gravity":
                if (compoundButton instanceof TextView) {
                    ((TextView) compoundButton).setGravity(parseGravity(attributeValue));
                }
                return true;

            /* -------- 交互与事件 -------- */
            case "onCheckedChangeListener":
                compoundButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // 默认空实现，可在外部替换
                });
                return true;

            case "clickable":
                compoundButton.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "enabled":
                compoundButton.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "longClickable":
                compoundButton.setLongClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                compoundButton.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                compoundButton.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            /* -------- 内容描述 -------- */
            case "contentDescription":
                compoundButton.setContentDescription(attributeValue);
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理CheckBox属性
     */
    public boolean processCheckBoxAttributes(CheckBox checkBox, String attributeName, String attributeValue) {
        // 先尝试用 CompoundButton 的处理方法
        if (processCompoundButtonAttributes(checkBox, attributeName, attributeValue)) {
            return true;
        }

        Context ctx = checkBox.getContext();

        switch (attributeName) {
            /* ---------- 文本相关 ---------- */
            case "text":
                checkBox.setText(attributeValue);
                return true;

            case "textSize":
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                        parseFloatSafely(attributeValue, 14));
                return true;

            case "textColor":
                try {
                    checkBox.setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid textColor: " + attributeValue);
                }
                return true;

            case "textStyle":
                checkBox.setTypeface(checkBox.getTypeface(), parseTextStyle(attributeValue));
                return true;

            case "textAllCaps":
                checkBox.setAllCaps(Boolean.parseBoolean(attributeValue));
                return true;

            case "gravity":
                checkBox.setGravity(parseGravity(attributeValue));
                return true;

            /* ---------- 图标相关 ---------- */
            case "buttonDrawable":
                try {
                    checkBox.setButtonDrawable(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid buttonDrawable: " + attributeValue);
                }
                return true;

            case "buttonTint":
                try {
                    ColorStateList tint = ColorStateList.valueOf(Color.parseColor(attributeValue));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        checkBox.setButtonTintList(tint);
                    }
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid buttonTint: " + attributeValue);
                }
                return true;

            /* ---------- 尺寸与内边距 ---------- */
            case "minWidth":
                checkBox.setMinimumWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            case "minHeight":
                checkBox.setMinimumHeight(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            case "padding":
                String[] pads = attributeValue.split(",");
                if (pads.length == 4) {
                    checkBox.setPadding(
                            dpToPx(ctx, parseFloatSafely(pads[0], 0)),
                            dpToPx(ctx, parseFloatSafely(pads[1], 0)),
                            dpToPx(ctx, parseFloatSafely(pads[2], 0)),
                            dpToPx(ctx, parseFloatSafely(pads[3], 0)));
                } else {
                    Log.w(TAG, "padding expects 4 comma-separated values");
                }
                return true;

            case "paddingLeft":
                checkBox.setPadding(dpToPx(ctx, parseFloatSafely(attributeValue, 0)),
                        checkBox.getPaddingTop(),
                        checkBox.getPaddingRight(),
                        checkBox.getPaddingBottom());
                return true;

            case "paddingTop":
                checkBox.setPadding(checkBox.getPaddingLeft(),
                        dpToPx(ctx, parseFloatSafely(attributeValue, 0)),
                        checkBox.getPaddingRight(),
                        checkBox.getPaddingBottom());
                return true;

            case "paddingRight":
                checkBox.setPadding(checkBox.getPaddingLeft(),
                        checkBox.getPaddingTop(),
                        dpToPx(ctx, parseFloatSafely(attributeValue, 0)),
                        checkBox.getPaddingBottom());
                return true;

            case "paddingBottom":
                checkBox.setPadding(checkBox.getPaddingLeft(),
                        checkBox.getPaddingTop(),
                        checkBox.getPaddingRight(),
                        dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            /* ---------- 交互与状态 ---------- */
            case "enabled":
                checkBox.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "clickable":
                checkBox.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "longClickable":
                checkBox.setLongClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                checkBox.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                checkBox.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            case "contentDescription":
                checkBox.setContentDescription(attributeValue);
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理RadioButton属性
     */
    public boolean processRadioButtonAttributes(RadioButton radioButton,
                                                String attributeName,
                                                String attributeValue) {
        if (processCompoundButtonAttributes(radioButton, attributeName, attributeValue)) {
            return true;
        }

        Context ctx = radioButton.getContext();

        switch (attributeName) {
            /* -------- 文本相关 -------- */
            case "text":
                radioButton.setText(attributeValue);
                return true;

            case "textSize":
                try {
                    radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                            parseFloatSafely(attributeValue, 14));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid textSize: " + attributeValue);
                }
                return true;

            case "textColor":
                try {
                    radioButton.setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid textColor: " + attributeValue);
                }
                return true;

            case "textStyle":
                radioButton.setTypeface(radioButton.getTypeface(),
                        parseTextStyle(attributeValue));
                return true;

            case "textAllCaps":
                radioButton.setAllCaps(Boolean.parseBoolean(attributeValue));
                return true;

            case "gravity":
                radioButton.setGravity(parseGravity(attributeValue));
                return true;

            /* -------- 按钮图标 -------- */
            case "buttonDrawable":
                try {
                    radioButton.setButtonDrawable(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid buttonDrawable: " + attributeValue);
                }
                return true;

            case "buttonTint":
                try {
                    ColorStateList tint = ColorStateList.valueOf(Color.parseColor(attributeValue));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        radioButton.setButtonTintList(tint);
                    }
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid buttonTint: " + attributeValue);
                }
                return true;

            /* -------- 布局与外观 -------- */
            case "minWidth":
                radioButton.setMinimumWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            case "minHeight":
                radioButton.setMinimumHeight(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            case "padding":
                String[] pads = attributeValue.split(",");
                if (pads.length == 4) {
                    int l = dpToPx(ctx, parseFloatSafely(pads[0], 0));
                    int t = dpToPx(ctx, parseFloatSafely(pads[1], 0));
                    int r = dpToPx(ctx, parseFloatSafely(pads[2], 0));
                    int b = dpToPx(ctx, parseFloatSafely(pads[3], 0));
                    radioButton.setPadding(l, t, r, b);
                } else {
                    Log.w(TAG, "padding expects 4 comma-separated values");
                }
                return true;

            case "paddingLeft":
                radioButton.setPadding(dpToPx(ctx, parseFloatSafely(attributeValue, 0)),
                        radioButton.getPaddingTop(),
                        radioButton.getPaddingRight(),
                        radioButton.getPaddingBottom());
                return true;

            case "paddingTop":
                radioButton.setPadding(radioButton.getPaddingLeft(),
                        dpToPx(ctx, parseFloatSafely(attributeValue, 0)),
                        radioButton.getPaddingRight(),
                        radioButton.getPaddingBottom());
                return true;

            case "paddingRight":
                radioButton.setPadding(radioButton.getPaddingLeft(),
                        radioButton.getPaddingTop(),
                        dpToPx(ctx, parseFloatSafely(attributeValue, 0)),
                        radioButton.getPaddingBottom());
                return true;

            case "paddingBottom":
                radioButton.setPadding(radioButton.getPaddingLeft(),
                        radioButton.getPaddingTop(),
                        radioButton.getPaddingRight(),
                        dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            /* -------- 状态与交互 -------- */
            case "enabled":
                radioButton.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "clickable":
                radioButton.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "longClickable":
                radioButton.setLongClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                radioButton.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                radioButton.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            case "contentDescription":
                radioButton.setContentDescription(attributeValue);
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理Switch属性
     */
    public boolean processSwitchAttributes(Switch switchView, String attributeName, String attributeValue) {
        // 先尝试用CompoundButton的处理方法
        if (processCompoundButtonAttributes(switchView, attributeName, attributeValue)) {
            return true;
        }

        Context context = switchView.getContext();

        switch (attributeName) {
            case "textOn":
                switchView.setTextOn(attributeValue);
                return true;

            case "textOff":
                switchView.setTextOff(attributeValue);
                return true;

            case "text":
                switchView.setText(attributeValue);
                return true;

            case "textSize":
                try {
                    float size = parseSpValue(attributeValue);
                    switchView.setTextSize(size);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid textSize: " + attributeValue);
                }
                return true;

            case "textColor":
                try {
                    switchView.setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid textColor: " + attributeValue);
                }
                return true;

            case "thumb":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    switchView.setThumbResource(resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid thumb resource: " + attributeValue);
                }
                return true;

            case "track":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    switchView.setTrackResource(resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid track resource: " + attributeValue);
                }
                return true;

            case "showText":
                switchView.setShowText(Boolean.parseBoolean(attributeValue));
                return true;

            case "splitTrack":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    switchView.setSplitTrack(Boolean.parseBoolean(attributeValue));
                }
                return true;

            case "thumbTextPadding":
                try {
                    int padding = dpToPx(context, Float.parseFloat(attributeValue));
                    switchView.setThumbTextPadding(padding);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid thumbTextPadding: " + attributeValue);
                }
                return true;

            case "switchMinWidth":
                try {
                    int width = dpToPx(context, Float.parseFloat(attributeValue));
                    switchView.setSwitchMinWidth(width);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid switchMinWidth: " + attributeValue);
                }
                return true;

            case "switchPadding":
                try {
                    int padding = dpToPx(context, Float.parseFloat(attributeValue));
                    switchView.setSwitchPadding(padding);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid switchPadding: " + attributeValue);
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理ProgressBar属性
     */
    public boolean processProgressBarAttributes(ProgressBar progressBar, String attributeName, String attributeValue) {
        Context context = progressBar.getContext();

        switch (attributeName) {
            case "progress":
                try {
                    progressBar.setProgress(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid progress: " + attributeValue);
                }
                return true;

            case "max":
                try {
                    progressBar.setMax(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid max: " + attributeValue);
                }
                return true;

            case "secondaryProgress":
                try {
                    progressBar.setSecondaryProgress(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid secondaryProgress: " + attributeValue);
                }
                return true;

            case "indeterminate":
                progressBar.setIndeterminate(Boolean.parseBoolean(attributeValue));
                return true;

            case "indeterminateDrawable":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    progressBar.setIndeterminateDrawable(ContextCompat.getDrawable(context, resId));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid indeterminateDrawable: " + attributeValue);
                }
                return true;

            case "progressDrawable":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    progressBar.setProgressDrawable(ContextCompat.getDrawable(context, resId));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid progressDrawable: " + attributeValue);
                }
                return true;

            case "visibility":
                progressBar.setVisibility("visible".equals(attributeValue) ? View.VISIBLE :
                        "invisible".equals(attributeValue) ? View.INVISIBLE : View.GONE);
                return true;

            case "minHeight":
                try {
                    int height = dpToPx(context, Float.parseFloat(attributeValue));
                    progressBar.setMinimumHeight(height);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid minHeight: " + attributeValue);
                }
                return true;

            case "minWidth":
                try {
                    int width = dpToPx(context, Float.parseFloat(attributeValue));
                    progressBar.setMinimumWidth(width);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid minWidth: " + attributeValue);
                }
                return true;

            case "enabled":
                progressBar.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "clickable":
                progressBar.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                progressBar.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                progressBar.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            case "contentDescription":
                progressBar.setContentDescription(attributeValue);
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理SeekBar属性
     */
    public boolean processSeekBarAttributes(SeekBar seekBar, String attributeName, String attributeValue) {
        // 先尝试用ProgressBar的处理方法
        if (processProgressBarAttributes(seekBar, attributeName, attributeValue)) {
            return true;
        }

        Context context = seekBar.getContext();

        switch (attributeName) {
            case "thumb":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    seekBar.setThumb(ContextCompat.getDrawable(context, resId));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid thumb: " + attributeValue);
                }
                return true;

            case "thumbOffset":
                try {
                    seekBar.setThumbOffset(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid thumbOffset: " + attributeValue);
                }
                return true;

            case "splitTrack":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    seekBar.setSplitTrack(Boolean.parseBoolean(attributeValue));
                }
                return true;

            case "progress":
                try {
                    seekBar.setProgress(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid progress: " + attributeValue);
                }
                return true;

            case "secondaryProgress":
                try {
                    seekBar.setSecondaryProgress(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid secondaryProgress: " + attributeValue);
                }
                return true;

            case "max":
                try {
                    seekBar.setMax(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid max: " + attributeValue);
                }
                return true;

            case "keyProgressIncrement":
                try {
                    seekBar.setKeyProgressIncrement(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid keyProgressIncrement: " + attributeValue);
                }
                return true;

            case "enabled":
                seekBar.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "clickable":
                seekBar.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                seekBar.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                seekBar.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            case "contentDescription":
                seekBar.setContentDescription(attributeValue);
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理RatingBar属性
     */
    public boolean processRatingBarAttributes(RatingBar ratingBar, String attributeName, String attributeValue) {
        Context context = ratingBar.getContext();

        switch (attributeName) {
            case "rating":
                try {
                    ratingBar.setRating(Float.parseFloat(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid rating: " + attributeValue);
                }
                return true;

            case "numStars":
                try {
                    ratingBar.setNumStars(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid numStars: " + attributeValue);
                }
                return true;

            case "stepSize":
                try {
                    ratingBar.setStepSize(Float.parseFloat(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid stepSize: " + attributeValue);
                }
                return true;

            case "max":
                try {
                    ratingBar.setMax(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid max: " + attributeValue);
                }
                return true;

            case "isIndicator":
                ratingBar.setIsIndicator(Boolean.parseBoolean(attributeValue));
                return true;

            case "indicator":
                ratingBar.setIsIndicator(Boolean.parseBoolean(attributeValue));
                return true;

            case "smallWidgets":
                // 设置小部件样式
                ratingBar.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                return true;

            case "largeWidgets":
                // 设置大部件样式
                ratingBar.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                return true;

            case "enabled":
                ratingBar.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "clickable":
                ratingBar.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                ratingBar.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                ratingBar.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            case "contentDescription":
                ratingBar.setContentDescription(attributeValue);
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理Spinner属性
     */
    public boolean processSpinnerAttributes(Spinner spinner, String attributeName, String attributeValue) {
        Context context = spinner.getContext();

        switch (attributeName) {
            case "entries":
                try {
                    // 假设传入的是字符串数组资源ID
                    int entriesRes = Integer.parseInt(attributeValue);
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                            context, entriesRes, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                } catch (NumberFormatException e) {
                    // 也支持直接传入逗号分隔的字符串列表
                    String[] entries = attributeValue.split(",");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            context, android.R.layout.simple_spinner_item, entries);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                }
                return true;

            case "prompt":
                spinner.setPrompt(attributeValue);
                return true;

            case "selection":
                try {
                    spinner.setSelection(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid selection: " + attributeValue);
                }
                return true;

            case "dropDownHorizontalOffset":
                try {
                    spinner.setDropDownHorizontalOffset(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid dropDownHorizontalOffset: " + attributeValue);
                }
                return true;

            case "dropDownVerticalOffset":
                try {
                    spinner.setDropDownVerticalOffset(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid dropDownVerticalOffset: " + attributeValue);
                }
                return true;

            case "dropDownWidth":
                try {
                    spinner.setDropDownWidth(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid dropDownWidth: " + attributeValue);
                }
                return true;

            case "gravity":
                try {
                    int gravity = parseGravity(attributeValue);
                    spinner.setGravity(gravity);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid gravity: " + attributeValue);
                }
                return true;

            case "background":
                try {
                    if (attributeValue.startsWith("#")) {
                        spinner.setBackgroundColor(Color.parseColor(attributeValue));
                    } else {
                        int resId = Integer.parseInt(attributeValue);
                        spinner.setBackgroundResource(resId);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid background: " + attributeValue);
                }
                return true;

            case "padding":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    spinner.setPadding(dpToPx(context, padding), dpToPx(context, padding), dpToPx(context, padding), dpToPx(context, padding));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid padding: " + attributeValue);
                }
                return true;

            case "paddingLeft":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    spinner.setPadding(dpToPx(context, padding), spinner.getPaddingTop(), spinner.getPaddingRight(), spinner.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingLeft: " + attributeValue);
                }
                return true;

            case "paddingTop":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    spinner.setPadding(spinner.getPaddingLeft(), dpToPx(context, padding), spinner.getPaddingRight(), spinner.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingTop: " + attributeValue);
                }
                return true;

            case "paddingRight":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    spinner.setPadding(spinner.getPaddingLeft(), spinner.getPaddingTop(), dpToPx(context, padding), spinner.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingRight: " + attributeValue);
                }
                return true;

            case "paddingBottom":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    spinner.setPadding(spinner.getPaddingLeft(), spinner.getPaddingTop(), spinner.getPaddingRight(), dpToPx(context, padding));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingBottom: " + attributeValue);
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理ListView属性
     */
    public boolean processListViewAttributes(ListView listView, String attributeName, String attributeValue) {
        Context context = listView.getContext();

        switch (attributeName) {
            case "entries":
                try {
                    // 假设传入的是字符串数组资源ID
                    int entriesRes = Integer.parseInt(attributeValue);
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                            context, entriesRes, android.R.layout.simple_list_item_1);
                    listView.setAdapter(adapter);
                } catch (NumberFormatException e) {
                    // 也支持直接传入逗号分隔的字符串列表
                    String[] entries = attributeValue.split(",");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            context, android.R.layout.simple_list_item_1, entries);
                    listView.setAdapter(adapter);
                }
                return true;

            case "choiceMode":
                try {
                    int mode = Integer.parseInt(attributeValue);
                    listView.setChoiceMode(mode);
                } catch (NumberFormatException e) {
                    switch (attributeValue) {
                        case "none":
                            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
                            break;
                        case "single":
                            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                            break;
                        case "multiple":
                            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                            break;
                        case "multiple_modal":
                            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                            break;
                        default:
                            Log.w(TAG, "Invalid choiceMode: " + attributeValue);
                    }
                }
                return true;

            case "listSelector":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    listView.setSelector(resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid listSelector: " + attributeValue);
                }
                return true;

            case "divider":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    listView.setDivider(ContextCompat.getDrawable(context, resId));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid divider: " + attributeValue);
                }
                return true;

            case "dividerHeight":
                try {
                    int height = dpToPx(context, Float.parseFloat(attributeValue));
                    listView.setDividerHeight(height);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid dividerHeight: " + attributeValue);
                }
                return true;

            case "emptyView":
                try {
                    int viewId = Integer.parseInt(attributeValue);
                    View emptyView = listView.getRootView().findViewById(viewId);
                    if (emptyView != null) {
                        listView.setEmptyView(emptyView);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Invalid emptyView: " + attributeValue);
                }
                return true;

            case "transcriptMode":
                try {
                    int mode = Integer.parseInt(attributeValue);
                    listView.setTranscriptMode(mode);
                } catch (NumberFormatException e) {
                    switch (attributeValue) {
                        case "normal":
                            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
                            break;
                        case "always_scroll":
                            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                            break;
                        case "disabled":
                            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
                            break;
                        default:
                            Log.w(TAG, "Invalid transcriptMode: " + attributeValue);
                    }
                }
                return true;

            case "stackFromBottom":
                listView.setStackFromBottom(Boolean.parseBoolean(attributeValue));
                return true;

            case "smoothScrollbar":
                listView.setSmoothScrollbarEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "fastScrollEnabled":
                listView.setFastScrollEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "scrollbarStyle":
                try {
                    int style = parseScrollBarStyle(attributeValue);
                    listView.setScrollBarStyle(style);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid scrollbarStyle: " + attributeValue);
                }
                return true;

            case "overScrollMode":
                try {
                    int mode = parseOverScrollMode(attributeValue);
                    listView.setOverScrollMode(mode);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid overScrollMode: " + attributeValue);
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理GridView属性
     */
    public boolean processGridViewAttributes(GridView gridView, String attributeName, String attributeValue) {
        Context context = gridView.getContext();

        switch (attributeName) {
            case "numColumns":
                try {
                    gridView.setNumColumns(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    if ("auto_fit".equals(attributeValue)) {
                        gridView.setNumColumns(GridView.AUTO_FIT);
                    } else {
                        Log.w(TAG, "Invalid numColumns: " + attributeValue);
                    }
                }
                return true;

            case "columnWidth":
                try {
                    gridView.setColumnWidth(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid columnWidth: " + attributeValue);
                }
                return true;

            case "horizontalSpacing":
                try {
                    gridView.setHorizontalSpacing(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid horizontalSpacing: " + attributeValue);
                }
                return true;

            case "verticalSpacing":
                try {
                    gridView.setVerticalSpacing(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid verticalSpacing: " + attributeValue);
                }
                return true;

            case "stretchMode":
                try {
                    int mode = Integer.parseInt(attributeValue);
                    gridView.setStretchMode(mode);
                } catch (NumberFormatException e) {
                    switch (attributeValue) {
                        case "none":
                            gridView.setStretchMode(GridView.NO_STRETCH);
                            break;
                        case "columnWidth":
                            gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                            break;
                        case "spacingUniform":
                            gridView.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
                            break;
                        default:
                            Log.w(TAG, "Invalid stretchMode: " + attributeValue);
                    }
                }
                return true;

            case "gravity":
                try {
                    int gravity = parseGravity(attributeValue);
                    gridView.setGravity(gravity);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid gravity: " + attributeValue);
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理ScrollView属性
     */
    public boolean processScrollViewAttributes(ScrollView scrollView, String attributeName, String attributeValue) {
        Context context = scrollView.getContext();

        switch (attributeName) {
            case "fillViewport":
                scrollView.setFillViewport(Boolean.parseBoolean(attributeValue));
                return true;

            case "scrollbars":
                switch (attributeValue) {
                    case "none":
                        scrollView.setVerticalScrollBarEnabled(false);
                        break;
                    case "vertical":
                        scrollView.setVerticalScrollBarEnabled(true);
                        break;
                    default:
                        Log.w(TAG, "Invalid scrollbars for ScrollView: " + attributeValue);
                }
                return true;

            case "scrollbarStyle":
                try {
                    int style = parseScrollBarStyle(attributeValue);
                    scrollView.setScrollBarStyle(style);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid scrollbarStyle: " + attributeValue);
                }
                return true;

            case "overScrollMode":
                try {
                    int mode = parseOverScrollMode(attributeValue);
                    scrollView.setOverScrollMode(mode);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid overScrollMode: " + attributeValue);
                }
                return true;

            case "scrollTo":
                String[] coords = attributeValue.split(",");
                if (coords.length == 2) {
                    try {
                        int x = dpToPx(context, Float.parseFloat(coords[0].trim()));
                        int y = dpToPx(context, Float.parseFloat(coords[1].trim()));
                        scrollView.scrollTo(x, y);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid scrollTo coordinates: " + attributeValue);
                    }
                } else {
                    Log.w(TAG, "Invalid scrollTo format. Expected 'x,y': " + attributeValue);
                }
                return true;

            case "smoothScrollTo":
                String[] smoothCoords = attributeValue.split(",");
                if (smoothCoords.length == 2) {
                    try {
                        int x = dpToPx(context, Float.parseFloat(smoothCoords[0].trim()));
                        int y = dpToPx(context, Float.parseFloat(smoothCoords[1].trim()));
                        scrollView.smoothScrollTo(x, y);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid smoothScrollTo coordinates: " + attributeValue);
                    }
                } else {
                    Log.w(TAG, "Invalid smoothScrollTo format. Expected 'x,y': " + attributeValue);
                }
                return true;

            case "isNestedScrollingEnabled":
                scrollView.setNestedScrollingEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "isVerticalScrollBarEnabled":
                scrollView.setVerticalScrollBarEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理HorizontalScrollView属性
     */
    public boolean processHorizontalScrollViewAttributes(HorizontalScrollView scrollView, String attributeName, String attributeValue) {
        Context context = scrollView.getContext();
        switch (attributeName) {
            case "fillViewport":
                scrollView.setFillViewport(Boolean.parseBoolean(attributeValue));
                return true;

            case "scrollbars":
                switch (attributeValue) {
                    case "none":
                        scrollView.setHorizontalScrollBarEnabled(false);
                        break;
                    case "horizontal":
                        scrollView.setHorizontalScrollBarEnabled(true);
                        break;
                    default:
                        Log.w(TAG, "Invalid scrollbars for HorizontalScrollView: " + attributeValue);
                }
                return true;

            case "scrollbarStyle":
                try {
                    int style = parseScrollBarStyle(attributeValue);
                    scrollView.setScrollBarStyle(style);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid scrollbarStyle: " + attributeValue, e);
                }
                return true;

            case "overScrollMode":
                try {
                    int mode = parseOverScrollMode(attributeValue);
                    scrollView.setOverScrollMode(mode);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid overScrollMode: " + attributeValue, e);
                }
                return true;

            case "smoothScrollingEnabled":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    scrollView.setSmoothScrollingEnabled(Boolean.parseBoolean(attributeValue));
                }
                return true;

            case "isNestedScrollingEnabled":
                scrollView.setNestedScrollingEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "isHorizontalScrollBarEnabled":
                scrollView.setHorizontalScrollBarEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "isVerticalScrollBarEnabled":
                scrollView.setVerticalScrollBarEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            default:
                return false;
        }
    }


    /**
     * 处理WebView属性
     */
    public boolean processWebViewAttributes(WebView webView, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "url":
                webView.loadUrl(attributeValue);
                return true;

            case "html":
                webView.loadData(attributeValue, "text/html", "UTF-8");
                return true;

            case "javascriptEnabled":
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "supportZoom":
                WebSettings zoomSettings = webView.getSettings();
                zoomSettings.setSupportZoom(Boolean.parseBoolean(attributeValue));
                return true;

            case "builtInZoomControls":
                WebSettings zoomCtrlSettings = webView.getSettings();
                zoomCtrlSettings.setBuiltInZoomControls(Boolean.parseBoolean(attributeValue));
                return true;

            case "displayZoomControls":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    WebSettings zoomDispSettings = webView.getSettings();
                    zoomDispSettings.setDisplayZoomControls(Boolean.parseBoolean(attributeValue));
                }
                return true;

            case "cacheMode":
                WebSettings cacheSettings = webView.getSettings();
                try {
                    int mode = Integer.parseInt(attributeValue);
                    cacheSettings.setCacheMode(mode);
                } catch (NumberFormatException e) {
                    switch (attributeValue) {
                        case "default":
                            cacheSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
                            break;
                        case "no_cache":
                            cacheSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                            break;
                        case "cache_else_network":
                            cacheSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                            break;
                        case "cache_only":
                            cacheSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
                            break;
                        default:
                            Log.w(TAG, "Invalid cacheMode: " + attributeValue);
                    }
                }
                return true;

            case "userAgentString":
                WebSettings userAgentSettings = webView.getSettings();
                userAgentSettings.setUserAgentString(attributeValue);
                return true;

            case "scrollBarStyle":
                try {
                    int style = parseScrollBarStyle(attributeValue);
                    webView.setScrollBarStyle(style);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid scrollBarStyle: " + attributeValue);
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理ViewFlipper属性
     */
    public boolean processViewFlipperAttributes(ViewFlipper viewFlipper, String attributeName, String attributeValue) {
        Context context = viewFlipper.getContext();

        switch (attributeName) {
            case "displayedChild":
                try {
                    viewFlipper.setDisplayedChild(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid displayedChild: " + attributeValue);
                }
                return true;

            case "flipInterval":
                try {
                    viewFlipper.setFlipInterval(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid flipInterval: " + attributeValue);
                }
                return true;

            case "autoStart":
                viewFlipper.setAutoStart(Boolean.parseBoolean(attributeValue));
                return true;

            case "startFlipping":
                if (Boolean.parseBoolean(attributeValue)) {
                    viewFlipper.startFlipping();
                }
                return true;

            case "stopFlipping":
                if (Boolean.parseBoolean(attributeValue)) {
                    viewFlipper.stopFlipping();
                }
                return true;

            case "inAnimation":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    viewFlipper.setInAnimation(context, resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid inAnimation: " + attributeValue);
                }
                return true;

            case "outAnimation":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    viewFlipper.setOutAnimation(context, resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid outAnimation: " + attributeValue);
                }
                return true;

            case "addView":
                try {
                    int layoutRes = Integer.parseInt(attributeValue);
                    View view = View.inflate(context, layoutRes, null);
                    viewFlipper.addView(view);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid addView resource: " + attributeValue);
                }
                return true;

            case "removeAllViews":
                viewFlipper.removeAllViews();
                return true;

            case "removeViewAt":
                try {
                    int index = Integer.parseInt(attributeValue);
                    if (index >= 0 && index < viewFlipper.getChildCount()) {
                        viewFlipper.removeViewAt(index);
                    } else {
                        Log.w(TAG, "Invalid removeViewAt index: " + attributeValue);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid removeViewAt index: " + attributeValue);
                }
                return true;


            default:
                return false;
        }
    }

    /**
     * 处理ImageButton属性
     */
    public boolean processImageButtonAttributes(ImageButton imageButton, String attributeName, String attributeValue) {
        Context context = imageButton.getContext();

        switch (attributeName) {
            case "src":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    imageButton.setImageResource(resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid image resource: " + attributeValue);
                }
                return true;

            case "scaleType":
                try {
                    ImageView.ScaleType scaleType = parseScaleType(attributeValue);
                    imageButton.setScaleType(scaleType);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid scaleType: " + attributeValue);
                }
                return true;

            case "adjustViewBounds":
                imageButton.setAdjustViewBounds(Boolean.parseBoolean(attributeValue));
                return true;

            case "maxWidth":
                try {
                    imageButton.setMaxWidth(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid maxWidth: " + attributeValue);
                }
                return true;

            case "maxHeight":
                try {
                    imageButton.setMaxHeight(dpToPx(context, Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid maxHeight: " + attributeValue);
                }
                return true;

            case "contentDescription":
                imageButton.setContentDescription(attributeValue);
                return true;

            case "clickable":
                imageButton.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "background":
                try {
                    if (attributeValue.startsWith("#")) {
                        imageButton.setBackgroundColor(Color.parseColor(attributeValue));
                    } else {
                        int resId = Integer.parseInt(attributeValue);
                        imageButton.setBackgroundResource(resId);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid background: " + attributeValue);
                }
                return true;

            case "padding":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    imageButton.setPadding(dpToPx(context, padding), dpToPx(context, padding), dpToPx(context, padding), dpToPx(context, padding));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid padding: " + attributeValue);
                }
                return true;

            case "paddingLeft":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    imageButton.setPadding(dpToPx(context, padding), imageButton.getPaddingTop(), imageButton.getPaddingRight(), imageButton.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingLeft: " + attributeValue);
                }
                return true;

            case "paddingTop":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    imageButton.setPadding(imageButton.getPaddingLeft(), dpToPx(context, padding), imageButton.getPaddingRight(), imageButton.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingTop: " + attributeValue);
                }
                return true;

            case "paddingRight":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    imageButton.setPadding(imageButton.getPaddingLeft(), imageButton.getPaddingTop(), dpToPx(context, padding), imageButton.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingRight: " + attributeValue);
                }
                return true;

            case "paddingBottom":
                try {
                    float padding = Float.parseFloat(attributeValue);
                    imageButton.setPadding(imageButton.getPaddingLeft(), imageButton.getPaddingTop(), imageButton.getPaddingRight(), dpToPx(context, padding));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingBottom: " + attributeValue);
                }
                return true;

            case "enabled":
                imageButton.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "longClickable":
                imageButton.setLongClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                imageButton.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                imageButton.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理ToggleButton属性
     */
    public boolean processToggleButtonAttributes(ToggleButton toggleButton, String attributeName, String attributeValue) {
        if (processCompoundButtonAttributes(toggleButton, attributeName, attributeValue)) {
            return true;
        }

        Context context = toggleButton.getContext();

        switch (attributeName) {
            case "textOn":
                toggleButton.setTextOn(attributeValue);
                return true;

            case "textOff":
                toggleButton.setTextOff(attributeValue);
                return true;

            case "text":
                toggleButton.setText(attributeValue);
                return true;

            case "textSize":
                try {
                    float size = parseSpValue(attributeValue);
                    toggleButton.setTextSize(size);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid textSize: " + attributeValue);
                }
                return true;

            case "textColor":
                try {
                    toggleButton.setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid textColor: " + attributeValue);
                }
                return true;

            case "textStyle":
                toggleButton.setTypeface(toggleButton.getTypeface(), parseTextStyle(attributeValue));
                return true;

            case "typeface":
                applyTypeface(toggleButton, attributeValue);
                return true;

            case "gravity":
                toggleButton.setGravity(parseGravity(attributeValue));
                return true;

            case "padding":
                String[] paddingValues = attributeValue.split(",");
                if (paddingValues.length == 4) {
                    int left = dpToPx(context, parseFloatSafely(paddingValues[0], 0));
                    int top = dpToPx(context, parseFloatSafely(paddingValues[1], 0));
                    int right = dpToPx(context, parseFloatSafely(paddingValues[2], 0));
                    int bottom = dpToPx(context, parseFloatSafely(paddingValues[3], 0));
                    toggleButton.setPadding(left, top, right, bottom);
                } else {
                    Log.w(TAG, "Invalid padding format. Expected 4 values: " + attributeValue);
                }
                return true;

            case "paddingLeft":
                toggleButton.setPadding(
                        dpToPx(context, parseFloatSafely(attributeValue, 0)),
                        toggleButton.getPaddingTop(),
                        toggleButton.getPaddingRight(),
                        toggleButton.getPaddingBottom()
                );
                return true;

            case "paddingTop":
                toggleButton.setPadding(
                        toggleButton.getPaddingLeft(),
                        dpToPx(context, parseFloatSafely(attributeValue, 0)),
                        toggleButton.getPaddingRight(),
                        toggleButton.getPaddingBottom()
                );
                return true;

            case "paddingRight":
                toggleButton.setPadding(
                        toggleButton.getPaddingLeft(),
                        toggleButton.getPaddingTop(),
                        dpToPx(context, parseFloatSafely(attributeValue, 0)),
                        toggleButton.getPaddingBottom()
                );
                return true;

            case "paddingBottom":
                toggleButton.setPadding(
                        toggleButton.getPaddingLeft(),
                        toggleButton.getPaddingTop(),
                        toggleButton.getPaddingRight(),
                        dpToPx(context, parseFloatSafely(attributeValue, 0))
                );
                return true;

            case "background":
                setBackground(toggleButton, attributeValue);
                return true;

            case "enabled":
                toggleButton.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "clickable":
                toggleButton.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "longClickable":
                toggleButton.setLongClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                toggleButton.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                toggleButton.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            case "visibility":
                switch (attributeValue) {
                    case "visible":
                        toggleButton.setVisibility(View.VISIBLE);
                        break;
                    case "invisible":
                        toggleButton.setVisibility(View.INVISIBLE);
                        break;
                    case "gone":
                        toggleButton.setVisibility(View.GONE);
                        break;
                    default:
                        Log.w(TAG, "Invalid visibility value: " + attributeValue);
                        return false;
                }
                return true;

            default:
                return false;
        }
    }
    private int parseTextStyle(String textStyle) {
        int style = 0;
        String[] styles = textStyle.split("\\|");

        for (String s : styles) {
            switch (s.trim()) {
                case "bold":
                    style |= android.graphics.Typeface.BOLD;
                    break;
                case "italic":
                    style |= android.graphics.Typeface.ITALIC;
                    break;
                case "bold_italic":
                    style |= android.graphics.Typeface.BOLD_ITALIC;
                    break;
            }
        }

        return style;
    }


    private int parseScrollBarStyle(String style) {
        switch (style) {
            case "insideOverlay": return View.SCROLLBARS_INSIDE_OVERLAY;
            case "insideInset": return View.SCROLLBARS_INSIDE_INSET;
            case "outsideOverlay": return View.SCROLLBARS_OUTSIDE_OVERLAY;
            case "outsideInset": return View.SCROLLBARS_OUTSIDE_INSET;
            default: return View.SCROLLBARS_INSIDE_OVERLAY;
        }
    }

    private int parseOverScrollMode(String mode) {
        switch (mode) {
            case "always": return View.OVER_SCROLL_ALWAYS;
            case "never": return View.OVER_SCROLL_NEVER;
            case "ifContentScrolls": return View.OVER_SCROLL_IF_CONTENT_SCROLLS;
            default: return View.OVER_SCROLL_IF_CONTENT_SCROLLS;
        }
    }

    // 辅助方法：解析SP值
    private float parseSpValue(String value) throws NumberFormatException {
        if (value.endsWith("sp")) {
            return Float.parseFloat(value.replace("sp", ""));
        }
        return Float.parseFloat(value);
    }

    // 辅助方法：dp转px
    private int dpToPx(Context context, float dpToPx) {
        return (int) (dpToPx * context.getResources().getDisplayMetrics().density + 0.5f);
    }
    /**
     * 处理所有View的通用属性
     */
    protected boolean processCommonAttributes(View view, String attributeName, String attributeValue) {
        Context context = getContext(view);
        ViewGroup.LayoutParams lp = view.getLayoutParams();

        switch (attributeName) {
            case "id":
                try {
                    view.setId(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    AttributeUtils.logWarning("Invalid id value: " + attributeValue);
                }
                return true;

            case "width":
                if (lp != null) {
                    lp.width = AttributeUtils.parseLayoutDimension(attributeValue, context);
                    view.setLayoutParams(lp);
                }
                return true;

            case "height":
                if (lp != null) {
                    lp.height = AttributeUtils.parseLayoutDimension(attributeValue, context);
                    view.setLayoutParams(lp);
                }
                return true;

            case "padding":
                AttributeUtils.applyPadding(view, attributeValue);
                return true;

            case "paddingLeft":
            case "paddingTop":
            case "paddingRight":
            case "paddingBottom":
                AttributeUtils.applySinglePadding(view, attributeName, attributeValue);
                return true;

            case "margin":
                AttributeUtils.applyMargin(view, attributeValue);
                return true;

            case "background":
                AttributeUtils.setBackground(view, attributeValue);
                return true;

            case "enabled":
                view.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "visibility":
                switch (attributeValue) {
                    case "visible": view.setVisibility(View.VISIBLE); break;
                    case "invisible": view.setVisibility(View.INVISIBLE); break;
                    case "gone": view.setVisibility(View.GONE); break;
                    default:
                        AttributeUtils.logWarning("Invalid visibility value: " + attributeValue);
                        return false;
                }
                return true;

            case "alpha":
                view.setAlpha(parseFloatSafely(attributeValue, 1.0f));
                return true;

            default:
                return false;
        }
    }
    
    /**
     * 处理TextView及其子类属性
     */
    protected boolean processTextViewAttributes(TextView textView, String attributeName, String attributeValue) {
        Context context = getContext(textView);

        switch (attributeName) {
            case "text":
                textView.setText(attributeValue);
                return true;

            case "textSize":
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, parseFloatSafely(attributeValue, 14));
                return true;

            case "textColor":
                try {
                    textView.setTextColor(Color.parseColor(attributeValue));
                } catch (Exception e) {
                    AttributeUtils.logWarning("Invalid text color: " + attributeValue);
                }
                return true;

            case "textStyle":
                AttributeUtils.applyTextStyle(textView, attributeValue);
                return true;

            case "typeface":
                AttributeUtils.applyTypeface(textView, attributeValue);
                return true;

            case "gravity":
                textView.setGravity(AttributeUtils.parseGravity(attributeValue));
                return true;

            case "inputType":
                if (textView instanceof EditText) {
                    ((EditText) textView).setInputType(AttributeUtils.parseInputType(attributeValue));
                }
                return true;

            default:
                return false;
        }
    }

    protected boolean processImageViewAttributes(ImageView imageView, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "src":
                AttributeUtils.setImageSource(imageView, attributeValue);
                return true;

            case "scaleType":
                imageView.setScaleType(AttributeUtils.parseScaleType(attributeValue));
                return true;

            default:
                return false;
        }
    }

    // 工具方法
    protected int parseDimension(String value, Context context) {
        if ("wrap_content".equals(value)) {
            return WRAP_CONTENT;
        } else if ("match_parent".equals(value) || "fill_parent".equals(value)) {
            return MATCH_PARENT;
        } else {
            try {
                return dpToPx(context, Float.parseFloat(value));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }


    

    
    protected void setBackground(View view, String value) {
        Context context = getContext(view);
        if (context == null) return;
        
        try {
            if (value.startsWith("#")) {
                view.setBackgroundColor(Color.parseColor(value));
            } else {
                int resId = Integer.parseInt(value);
                view.setBackgroundResource(resId);
            }
        } catch (Exception e) {
            logWarning("Invalid background value: " + value);
        }
    }
    
    protected void setForeground(View view, String value) {
        Context context = getContext(view);
        if (context == null) return;
        
        try {
            if (value.startsWith("#")) {
                view.setForeground(new ColorDrawable(Color.parseColor(value)));
            } else {
                int resId = Integer.parseInt(value);
                view.setForeground(context.getDrawable(resId));
            }
        } catch (Exception e) {
            logWarning("Invalid foreground value: " + value);
        }
    }
    
    // 更多工具方法（解析输入类型、缩放类型等）
    protected int parseInputType(String inputType) {
        switch (inputType) {
            case "text":
                return INPUT_TEXT;
            case "textCapCharacters":
                return INPUT_TEXT_CAP_CHARACTERS;
            case "textCapWords":
                return INPUT_TEXT_CAP_WORDS;
            case "textCapSentences":
                return INPUT_TEXT_CAP_SENTENCES;
            case "textMultiLine":
                return INPUT_TEXT_MULTI_LINE;
            case "textPassword":
                return INPUT_TEXT_PASSWORD;
            case "textVisiblePassword":
                return INPUT_TEXT_VISIBLE_PASSWORD;
            case "textEmailAddress":
                return INPUT_TEXT_EMAIL_ADDRESS;
            case "number":
                return INPUT_NUMBER;
            case "numberSigned":
                return INPUT_NUMBER_SIGNED;
            case "numberDecimal":
                return INPUT_NUMBER_DECIMAL;
            case "phone":
                return INPUT_PHONE;
            case "datetime":
                return INPUT_DATETIME;
            case "date":
                return INPUT_DATE;
            case "time":
                return INPUT_TIME;
            default:
                return INPUT_TEXT;
        }
    }
    
    protected ImageView.ScaleType parseScaleType(String scaleType) {
        switch (scaleType) {
            case "matrix":
                return ImageView.ScaleType.MATRIX;
            case "fitXY":
                return ImageView.ScaleType.FIT_XY;
            case "fitStart":
                return ImageView.ScaleType.FIT_START;
            case "fitCenter":
                return ImageView.ScaleType.FIT_CENTER;
            case "fitEnd":
                return ImageView.ScaleType.FIT_END;
            case "center":
                return ImageView.ScaleType.CENTER;
            case "centerCrop":
                return ImageView.ScaleType.CENTER_CROP;
            case "centerInside":
                return ImageView.ScaleType.CENTER_INSIDE;
            default:
                return ImageView.ScaleType.FIT_CENTER;
        }
    }
    
    protected void setImageSource(ImageView imageView, String value) {
        Context context = getContext(imageView);
        if (context == null) return;
        
        try {
            int resId = Integer.parseInt(value);
            imageView.setImageResource(resId);
        } catch (NumberFormatException e) {
            logWarning("Invalid image resource: " + value);
        }
    }
    
    protected TextUtils.TruncateAt parseEllipsize(String ellipsize) {
        switch (ellipsize) {
            case "end":
                return TextUtils.TruncateAt.END;
            case "start":
                return TextUtils.TruncateAt.START;
            case "middle":
                return TextUtils.TruncateAt.MIDDLE;
            case "marquee":
                return TextUtils.TruncateAt.MARQUEE;
            default:
                return null;
        }
    }
    int parseGravity(String gravity) {
        int result = Gravity.NO_GRAVITY;
        String[] parts = gravity.split("\\|");
        for (String part : parts) {
            switch (part.trim()) {
                case "top":
                    result |= Gravity.TOP;
                    break;
                case "bottom":
                    result |= Gravity.BOTTOM;
                    break;
                case "left":
                    result |= Gravity.LEFT;
                    break;
                case "right":
                    result |= Gravity.RIGHT;
                    break;
                case "center":
                    result |= Gravity.CENTER;
                    break;
                case "center_vertical":
                    result |= Gravity.CENTER_VERTICAL;
                    break;
                case "center_horizontal":
                    result |= Gravity.CENTER_HORIZONTAL;
                    break;
                case "fill":
                    result |= Gravity.FILL;
                    break;
                case "fill_vertical":
                    result |= Gravity.FILL_VERTICAL;
                    break;
                case "fill_horizontal":
                    result |= Gravity.FILL_HORIZONTAL;
                    break;
                case "clip_vertical":
                    result |= Gravity.CLIP_VERTICAL;
                    break;
                case "clip_horizontal":
                    result |= Gravity.CLIP_HORIZONTAL;
                    break;
                case "start":
                    result |= Gravity.START;
                    break;
                case "end":
                    result |= Gravity.END;
                    break;
            }
        }
        return result;
    }
    protected void applyTextStyle(TextView textView, String style) {
        int typefaceStyle = Typeface.NORMAL;
        switch (style) {
            case "bold":
                typefaceStyle = Typeface.BOLD;
                break;
            case "italic":
                typefaceStyle = Typeface.ITALIC;
                break;
            case "bold_italic":
                typefaceStyle = Typeface.BOLD_ITALIC;
                break;
            case "normal":
            default:
                typefaceStyle = Typeface.NORMAL;
                break;
        }
        textView.setTypeface(textView.getTypeface(), typefaceStyle);
    }
    
    protected void applyTypeface(TextView textView, String typeface) {
        Typeface tf = null;
        switch (typeface) {
            case "sans":
                tf = Typeface.SANS_SERIF;
                break;
            case "serif":
                tf = Typeface.SERIF;
                break;
            case "monospace":
                tf = Typeface.MONOSPACE;
                break;
            default:
                try {
                    tf = Typeface.createFromAsset(textView.getContext().getAssets(), typeface);
                } catch (Exception e) {
                    logWarning("Invalid typeface: " + typeface);
                    tf = Typeface.DEFAULT;
                }
        }
        textView.setTypeface(tf);
    }
    
    protected void applyTextDecoration(TextView textView, String decoration) {
        int flags = textView.getPaintFlags();
        
        // 清除现有装饰
        flags &= ~(Paint.UNDERLINE_TEXT_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
        
        // 应用新装饰
        switch (decoration) {
            case "underline":
                flags |= Paint.UNDERLINE_TEXT_FLAG;
                break;
            case "strikethrough":
                flags |= Paint.STRIKE_THRU_TEXT_FLAG;
                break;
            case "both":
                flags |= Paint.UNDERLINE_TEXT_FLAG | Paint.STRIKE_THRU_TEXT_FLAG;
                break;
            case "none":
            default:
                // 保持清除后的状态
                break;
        }
        
        textView.setPaintFlags(flags);
    }
    
    protected PorterDuff.Mode parsePorterDuffMode(String mode) {
        switch (mode) {
            case "add":
                return PorterDuff.Mode.ADD;
            case "clear":
                return PorterDuff.Mode.CLEAR;
            case "darken":
                return PorterDuff.Mode.DARKEN;
            case "dst":
                return PorterDuff.Mode.DST;
            case "dstAtop":
                return PorterDuff.Mode.DST_ATOP;
            case "dstIn":
                return PorterDuff.Mode.DST_IN;
            case "dstOut":
                return PorterDuff.Mode.DST_OUT;
            case "dstOver":
                return PorterDuff.Mode.DST_OVER;
            case "lighten":
                return PorterDuff.Mode.LIGHTEN;
            case "multiply":
                return PorterDuff.Mode.MULTIPLY;
            case "overlay":
                return PorterDuff.Mode.OVERLAY;
            case "screen":
                return PorterDuff.Mode.SCREEN;
            case "src":
                return PorterDuff.Mode.SRC;
            case "srcAtop":
                return PorterDuff.Mode.SRC_ATOP;
            case "srcIn":
                return PorterDuff.Mode.SRC_IN;
            case "srcOut":
                return PorterDuff.Mode.SRC_OUT;
            case "srcOver":
                return PorterDuff.Mode.SRC_OVER;
            case "xor":
                return PorterDuff.Mode.XOR;
            default:
                return null;
        }
    }
    
    // 处理Button属性
    protected boolean processButtonAttributes(Button button, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "textAllCaps":
                button.setAllCaps(Boolean.parseBoolean(attributeValue));
                return true;

            case "drawableLeft":
                setCompoundDrawable(button, attributeValue, 0);
                return true;

            case "drawableRight":
                setCompoundDrawable(button, attributeValue, 2);
                return true;

            case "drawableTop":
                setCompoundDrawable(button, attributeValue, 1);
                return true;

            case "drawableBottom":
                setCompoundDrawable(button, attributeValue, 3);
                return true;

            case "drawablePadding":
                button.setCompoundDrawablePadding(dpToPx(getContext(button), parseFloatSafely(attributeValue, 0)));
                return true;

            case "background":
                try {
                    if (attributeValue.startsWith("#")) {
                        button.setBackgroundColor(Color.parseColor(attributeValue));
                    } else {
                        int resId = Integer.parseInt(attributeValue);
                        button.setBackgroundResource(resId);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid background: " + attributeValue);
                }
                return true;

            case "textColor":
                try {
                    button.setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid textColor: " + attributeValue);
                }
                return true;

            case "textSize":
                try {
                    float size = parseFloatSafely(attributeValue, 0);
                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid textSize: " + attributeValue);
                }
                return true;

            case "enabled":
                button.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "clickable":
                button.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "longClickable":
                button.setLongClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusable":
                button.setFocusable(Boolean.parseBoolean(attributeValue));
                return true;

            case "focusableInTouchMode":
                button.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue));
                return true;

            case "padding":
                try {
                    float padding = parseFloatSafely(attributeValue, 0);
                    button.setPadding(dpToPx(getContext(button), padding), dpToPx(getContext(button), padding),
                            dpToPx(getContext(button), padding), dpToPx(getContext(button), padding));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid padding: " + attributeValue);
                }
                return true;

            case "paddingLeft":
                try {
                    float padding = parseFloatSafely(attributeValue, 0);
                    button.setPadding(dpToPx(getContext(button), padding), button.getPaddingTop(),
                            button.getPaddingRight(), button.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingLeft: " + attributeValue);
                }
                return true;

            case "paddingTop":
                try {
                    float padding = parseFloatSafely(attributeValue, 0);
                    button.setPadding(button.getPaddingLeft(), dpToPx(getContext(button), padding),
                            button.getPaddingRight(), button.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingTop: " + attributeValue);
                }
                return true;

            case "paddingRight":
                try {
                    float padding = parseFloatSafely(attributeValue, 0);
                    button.setPadding(button.getPaddingLeft(), button.getPaddingTop(),
                            dpToPx(getContext(button), padding), button.getPaddingBottom());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingRight: " + attributeValue);
                }
                return true;

            case "paddingBottom":
                try {
                    float padding = parseFloatSafely(attributeValue, 0);
                    button.setPadding(button.getPaddingLeft(), button.getPaddingTop(),
                            button.getPaddingRight(), dpToPx(getContext(button), padding));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid paddingBottom: " + attributeValue);
                }
                return true;

            default:
                return processTextViewAttributes(button, attributeName, attributeValue);
        }
    }
    
    private void setCompoundDrawable(Button button, String value, int position) {
        Context context = getContext(button);
        if (context == null) return;
        
        Drawable[] drawables = button.getCompoundDrawables();
        try {
            int resId = Integer.parseInt(value);
            Drawable drawable = ContextCompat.getDrawable(context, resId);
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
            
            switch (position) {
                case 0: // left
                    button.setCompoundDrawables(drawable, drawables[1], drawables[2], drawables[3]);
                    break;
                case 1: // top
                    button.setCompoundDrawables(drawables[0], drawable, drawables[2], drawables[3]);
                    break;
                case 2: // right
                    button.setCompoundDrawables(drawables[0], drawables[1], drawable, drawables[3]);
                    break;
                case 3: // bottom
                    button.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawable);
                    break;
            }
        } catch (NumberFormatException e) {
            logWarning("Invalid drawable resource: " + value);
        }
    }
    

    protected boolean processEditTextAttributes(EditText editText, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "inputType":
                editText.setInputType(parseInputType(attributeValue));
                return true;

            case "imeOptions":
                editText.setImeOptions(parseImeOptions(attributeValue));
                return true;

            case "maxLength":
                int maxLength = parseIntSafely(attributeValue, 0);
                if (maxLength > 0) {
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
                }
                return true;

            case "digits":
                editText.setKeyListener(DigitsKeyListener.getInstance(attributeValue));
                return true;

            case "password":
                boolean isPassword = Boolean.parseBoolean(attributeValue);
                if (isPassword) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                return true;

            case "selectAllOnFocus":
                editText.setSelectAllOnFocus(Boolean.parseBoolean(attributeValue));
                return true;

            case "cursorVisible":
                editText.setCursorVisible(Boolean.parseBoolean(attributeValue));
                return true;

            case "hint":
                editText.setHint(attributeValue);
                return true;

            case "text":
                editText.setText(attributeValue);
                return true;

            case "textColor":
                try {
                    editText.setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid textColor: " + attributeValue);
                }
                return true;

            case "textSize":
                try {
                    float size = parseSpValue(attributeValue);
                    editText.setTextSize(size);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid textSize: " + attributeValue);
                }
                return true;

            case "singleLine":
                editText.setSingleLine(Boolean.parseBoolean(attributeValue));
                return true;

            case "enabled":
                editText.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "editable":
                editText.setFocusable(Boolean.parseBoolean(attributeValue));
                editText.setClickable(Boolean.parseBoolean(attributeValue));
                return true;

            case "gravity":
                try {
                    int gravity = parseGravity(attributeValue);
                    editText.setGravity(gravity);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid gravity: " + attributeValue);
                }
                return true;

            default:
                return processTextViewAttributes(editText, attributeName, attributeValue);
        }
    }

    private int parseImeOptions(String imeOptions) {
        switch (imeOptions) {
            case "actionNone":
                return EditorInfo.IME_ACTION_NONE;
            case "actionGo":
                return EditorInfo.IME_ACTION_GO;
            case "actionSearch":
                return EditorInfo.IME_ACTION_SEARCH;
            case "actionSend":
                return EditorInfo.IME_ACTION_SEND;
            case "actionNext":
                return EditorInfo.IME_ACTION_NEXT;
            case "actionDone":
                return EditorInfo.IME_ACTION_DONE;
            case "actionPrevious":
                return EditorInfo.IME_ACTION_PREVIOUS;
            default:
                Log.w(TAG, "Invalid imeOptions: " + imeOptions);
                return EditorInfo.IME_ACTION_NONE;
        }
    }
}
