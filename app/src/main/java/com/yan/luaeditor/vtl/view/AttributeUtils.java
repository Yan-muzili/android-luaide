package com.yan.luaeditor.vtl.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * 统一的属性处理工具类，避免重复代码
 */
public class AttributeUtils implements AttributeConstants {
    private static final String TAG = "AttributeUtils";

    
    /**
     * 安全解析整数
     */
    public static int parseIntSafely(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.replaceAll("sp","").replaceAll("dp",""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 安全解析浮点数
     */
    public static float parseFloatSafely(String value, float defaultValue) {
        try {
            return Float.parseFloat(value.replaceAll("sp","").replaceAll("dp",""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 解析尺寸值（支持dp、sp、px）
     */
    public static float parseDimension(Context context, String value) throws NumberFormatException {
        if (value.endsWith("dp")) {
            return dpToPx(context, Float.parseFloat(value.replace("dp", "")));
        } else if (value.endsWith("sp")) {
            return spToPx(context, Float.parseFloat(value.replace("sp", "")));
        } else if (value.endsWith("px")) {
            return Float.parseFloat(value.replace("px", ""));
        }
        return Float.parseFloat(value);
    }

    /**
     * dp转px
     */
    public static int dpToPx(Context context, float dp) {
        if (context == null) return 0;
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, 
            context.getResources().getDisplayMetrics()
        );
    }
    public static int parseTextStyle(String textStyle) {
        switch (textStyle) {
            case "normal":
                return Typeface.NORMAL;
            case "bold":
                return Typeface.BOLD;
            case "italic":
                return Typeface.ITALIC;
            case "bold_italic":
                return Typeface.BOLD_ITALIC;
            default:
                Log.w(TAG, "Invalid textStyle: " + textStyle);
                return Typeface.NORMAL;
        }
    }
    public static int parseTextAlignment(String value) {
        switch (value) {
            case "inherit":    default: return View.TEXT_ALIGNMENT_INHERIT;
            case "gravity":    return View.TEXT_ALIGNMENT_GRAVITY;
            case "textStart":  return View.TEXT_ALIGNMENT_TEXT_START;
            case "textEnd":    return View.TEXT_ALIGNMENT_TEXT_END;
            case "center":     return View.TEXT_ALIGNMENT_CENTER;
            case "viewStart":  return View.TEXT_ALIGNMENT_VIEW_START;
            case "viewEnd":    return View.TEXT_ALIGNMENT_VIEW_END;
        }
    }
    public static void applyCornerFamily(ShapeableImageView view, String key, String family) {
        int cornerFamily = "rounded".equals(family) ? CornerFamily.ROUNDED : CornerFamily.CUT;
        ShapeAppearanceModel.Builder builder = view.getShapeAppearanceModel().toBuilder();
        switch (key) {
            case "cornerFamilyTopLeft":     builder.setTopLeftCorner(cornerFamily, view.getShapeAppearanceModel().getTopLeftCornerSize()); break;
            case "cornerFamilyTopRight":    builder.setTopRightCorner(cornerFamily, view.getShapeAppearanceModel().getTopRightCornerSize()); break;
            case "cornerFamilyBottomLeft":  builder.setBottomLeftCorner(cornerFamily, view.getShapeAppearanceModel().getBottomLeftCornerSize()); break;
            case "cornerFamilyBottomRight": builder.setBottomRightCorner(cornerFamily, view.getShapeAppearanceModel().getBottomRightCornerSize()); break;
        }
        view.setShapeAppearanceModel(builder.build());
    }
    public static void applyCornerSize(ShapeableImageView view, String key, String value) {
        float size = dpToPx(view.getContext(), parseFloatSafely(value, 0));
        ShapeAppearanceModel.Builder builder = view.getShapeAppearanceModel().toBuilder();
        switch (key) {
            case "cornerSizeTopLeft":     builder.setTopLeftCornerSize(size); break;
            case "cornerSizeTopRight":    builder.setTopRightCornerSize(size); break;
            case "cornerSizeBottomLeft":  builder.setBottomLeftCornerSize(size); break;
            case "cornerSizeBottomRight": builder.setBottomRightCornerSize(size); break;
        }
        view.setShapeAppearanceModel(builder.build());
    }
    public static int parseScrollBarStyle(String v) {
        switch (v) {
            case "insideOverlay": return View.SCROLLBARS_INSIDE_OVERLAY;
            case "insideInset": return View.SCROLLBARS_INSIDE_INSET;
            case "outsideOverlay": return View.SCROLLBARS_OUTSIDE_OVERLAY;
            case "outsideInset": return View.SCROLLBARS_OUTSIDE_INSET;
            default: return View.SCROLLBARS_INSIDE_OVERLAY;
        }
    }
    public static int parseOverScrollMode(String v) {
        switch (v) {
            case "always": return View.OVER_SCROLL_ALWAYS;
            case "never": return View.OVER_SCROLL_NEVER;
            case "ifContentScrolls": return View.OVER_SCROLL_IF_CONTENT_SCROLLS;
            default: return View.OVER_SCROLL_IF_CONTENT_SCROLLS;
        }
    }
    /**
     * sp转px
     */
    public static int spToPx(Context context, float sp) {
        if (context == null) return 0;
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, 
            context.getResources().getDisplayMetrics()
        );
    }

    /**
     * 解析布局尺寸
     */
    public static int parseLayoutDimension(String value, Context context) {
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

    /**
     * 解析重力值
     */
    public static int parseGravity(String gravity) {
        int result = Gravity.NO_GRAVITY;
        String[] parts = gravity.split("\\|");
        for (String part : parts) {
            switch (part.trim()) {
                case "top": result |= Gravity.TOP; break;
                case "bottom": result |= Gravity.BOTTOM; break;
                case "left": result |= Gravity.LEFT; break;
                case "right": result |= Gravity.RIGHT; break;
                case "center": result |= Gravity.CENTER; break;
                case "center_vertical": result |= Gravity.CENTER_VERTICAL; break;
                case "center_horizontal": result |= Gravity.CENTER_HORIZONTAL; break;
                case "fill": result |= Gravity.FILL; break;
                case "fill_vertical": result |= Gravity.FILL_VERTICAL; break;
                case "fill_horizontal": result |= Gravity.FILL_HORIZONTAL; break;
                case "start": result |= Gravity.START; break;
                case "end": result |= Gravity.END; break;
            }
        }
        return result;
    }

    /**
     * 解析输入类型
     */
    public static int parseInputType(String inputType) {
        switch (inputType) {
            case "text": return INPUT_TEXT;
            case "textCapCharacters": return INPUT_TEXT_CAP_CHARACTERS;
            case "textCapWords": return INPUT_TEXT_CAP_WORDS;
            case "textCapSentences": return INPUT_TEXT_CAP_SENTENCES;
            case "textMultiLine": return INPUT_TEXT_MULTI_LINE;
            case "textPassword": return INPUT_TEXT_PASSWORD;
            case "textVisiblePassword": return INPUT_TEXT_VISIBLE_PASSWORD;
            case "textEmailAddress": return INPUT_TEXT_EMAIL_ADDRESS;
            case "number": return INPUT_NUMBER;
            case "numberSigned": return INPUT_NUMBER_SIGNED;
            case "numberDecimal": return INPUT_NUMBER_DECIMAL;
            case "phone": return INPUT_PHONE;
            case "datetime": return INPUT_DATETIME;
            case "date": return INPUT_DATE;
            case "time": return INPUT_TIME;
            default: return INPUT_TEXT;
        }
    }

    /**
     * 解析IME选项
     */
    public static int parseImeOptions(String imeOptions) {
        switch (imeOptions) {
            case "actionDone": return IME_ACTION_DONE;
            case "actionNext": return IME_ACTION_NEXT;
            case "actionGo": return IME_ACTION_GO;
            case "actionSearch": return IME_ACTION_SEARCH;
            case "actionSend": return IME_ACTION_SEND;
            case "actionNone": return IME_ACTION_NONE;
            case "actionPrevious": return IME_ACTION_PREVIOUS;
            default: return IME_ACTION_UNSPECIFIED;
        }
    }

    /**
     * 解析图片缩放类型
     */
    public static ImageView.ScaleType parseScaleType(String scaleType) {
        switch (scaleType) {
            case "matrix": return ImageView.ScaleType.MATRIX;
            case "fitXY": return ImageView.ScaleType.FIT_XY;
            case "fitStart": return ImageView.ScaleType.FIT_START;
            case "fitCenter": return ImageView.ScaleType.FIT_CENTER;
            case "fitEnd": return ImageView.ScaleType.FIT_END;
            case "center": return ImageView.ScaleType.CENTER;
            case "centerCrop": return ImageView.ScaleType.CENTER_CROP;
            case "centerInside": return ImageView.ScaleType.CENTER_INSIDE;
            default: return ImageView.ScaleType.FIT_CENTER;
        }
    }

    /**
     * 解析文本省略模式
     */
    public static TextUtils.TruncateAt parseEllipsize(String ellipsize) {
        switch (ellipsize) {
            case "end": return TextUtils.TruncateAt.END;
            case "start": return TextUtils.TruncateAt.START;
            case "middle": return TextUtils.TruncateAt.MIDDLE;
            case "marquee": return TextUtils.TruncateAt.MARQUEE;
            default: return null;
        }
    }

    /**
     * 解析PorterDuff模式
     */
    public static PorterDuff.Mode parsePorterDuffMode(String mode) {
        switch (mode) {
            case "add": return PorterDuff.Mode.ADD;
            case "clear": return PorterDuff.Mode.CLEAR;
            case "darken": return PorterDuff.Mode.DARKEN;
            case "lighten": return PorterDuff.Mode.LIGHTEN;
            case "multiply": return PorterDuff.Mode.MULTIPLY;
            case "overlay": return PorterDuff.Mode.OVERLAY;
            case "screen": return PorterDuff.Mode.SCREEN;
            case "src": return PorterDuff.Mode.SRC;
            case "srcAtop": return PorterDuff.Mode.SRC_ATOP;
            case "srcIn": return PorterDuff.Mode.SRC_IN;
            case "srcOut": return PorterDuff.Mode.SRC_OUT;
            case "srcOver": return PorterDuff.Mode.SRC_OVER;
            case "xor": return PorterDuff.Mode.XOR;
            default: return null;
        }
    }

    /**
     * 统一的内边距设置
     */
    public static void applyPadding(View view, String paddingValue) {
        Context context = view.getContext();
        String[] values = paddingValue.split(",");
        if (values.length == 4) {
            int left = dpToPx(context, parseFloatSafely(values[0], 0));
            int top = dpToPx(context, parseFloatSafely(values[1], 0));
            int right = dpToPx(context, parseFloatSafely(values[2], 0));
            int bottom = dpToPx(context, parseFloatSafely(values[3], 0));
            view.setPadding(left, top, right, bottom);
        } else {
            logWarning("Invalid padding format. Expected 4 values: " + paddingValue);
        }
    }

    /**
     * 单个内边距设置
     */
    public static void applySinglePadding(View view, String paddingType, String value) {
        Context context = view.getContext();
        int px = dpToPx(context, parseFloatSafely(value, 0));
        int left = view.getPaddingLeft();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();

        switch (paddingType) {
            case "paddingLeft": left = px; break;
            case "paddingTop": top = px; break;
            case "paddingRight": right = px; break;
            case "paddingBottom": bottom = px; break;
        }
        view.setPadding(left, top, right, bottom);
    }

    /**
     * 统一的外边距设置
     */
    public static void applyMargin(View view, String marginValue) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams)) return;

        Context context = view.getContext();
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) lp;
        String[] values = marginValue.split(",");
        
        if (values.length == 4) {
            int left = dpToPx(context, parseFloatSafely(values[0], 0));
            int top = dpToPx(context, parseFloatSafely(values[1], 0));
            int right = dpToPx(context, parseFloatSafely(values[2], 0));
            int bottom = dpToPx(context, parseFloatSafely(values[3], 0));
            marginParams.setMargins(left, top, right, bottom);
            view.setLayoutParams(lp);
        } else {
            logWarning("Invalid margin format. Expected 4 values: " + marginValue);
        }
    }

    /**
     * 设置背景
     */
    public static void setBackground(View view, String value) {
        Context context = view.getContext();
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

    /**
     * 应用文本样式
     */
    public static void applyTextStyle(TextView textView, String style) {
        int typefaceStyle = Typeface.NORMAL;
        switch (style) {
            case "bold": typefaceStyle = Typeface.BOLD; break;
            case "italic": typefaceStyle = Typeface.ITALIC; break;
            case "bold_italic": typefaceStyle = Typeface.BOLD_ITALIC; break;
            case "normal":
            default: typefaceStyle = Typeface.NORMAL; break;
        }
        textView.setTypeface(textView.getTypeface(), typefaceStyle);
    }

    /**
     * 应用字体
     */
    public static void applyTypeface(TextView textView, String typeface) {
        Typeface tf = null;
        switch (typeface) {
            case "sans": tf = Typeface.SANS_SERIF; break;
            case "serif": tf = Typeface.SERIF; break;
            case "monospace": tf = Typeface.MONOSPACE; break;
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

    /**
     * 应用文本装饰
     */
    public static void applyTextDecoration(TextView textView, String decoration) {
        int flags = textView.getPaintFlags();
        flags &= ~(Paint.UNDERLINE_TEXT_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
        
        switch (decoration) {
            case "underline": flags |= Paint.UNDERLINE_TEXT_FLAG; break;
            case "strikethrough": flags |= Paint.STRIKE_THRU_TEXT_FLAG; break;
            case "both": flags |= Paint.UNDERLINE_TEXT_FLAG | Paint.STRIKE_THRU_TEXT_FLAG; break;
            case "none":
            default: break;
        }
        textView.setPaintFlags(flags);
    }

    /**
     * 统一的日志警告
     */
    public static void logWarning(String message) {
        Log.w(TAG, message);
    }

    /**
     * 设置图片资源
     */
    public static void setImageSource(android.widget.ImageView imageView, String value) {
        Context context = imageView.getContext();
        if (context == null) return;
        
        try {
            int resId = Integer.parseInt(value);
            imageView.setImageResource(resId);
        } catch (NumberFormatException e) {
            logWarning("Invalid image resource: " + value);
        }
    }

    /**
     * 设置Drawable
     */
    public static void setDrawable(View view, String value, java.util.function.Consumer<Drawable> setter) {
        try {
            if (value.startsWith("#")) {
                setter.accept(new ColorDrawable(Color.parseColor(value)));
            } else {
                setter.accept(ContextCompat.getDrawable(view.getContext(), Integer.parseInt(value)));
            }
        } catch (Exception e) {
            logWarning("Invalid drawable: " + value);
        }
    }
}
