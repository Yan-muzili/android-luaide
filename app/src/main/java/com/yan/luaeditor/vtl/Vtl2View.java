package com.yan.luaeditor.vtl;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewDragHelper;

import com.yan.luaeditor.vtl.view.ViewAttributeHelper;

public class Vtl2View {
    private static View createView(Context ctx, String name) {
        String[] prefixes = {"android.widget.", "android.view.", "android.webkit."};
        try {
            for (String p : prefixes) {
                try {
                    return (View) Class.forName(p + name)
                            .getConstructor(Context.class)
                            .newInstance(ctx);
                } catch (ClassNotFoundException ignore) {}
            }
            return (View) Class.forName(name)
                    .getConstructor(Context.class)
                    .newInstance(ctx);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create view " + name, e);
        }
    }
    public static View build(Context ctx, VNode node) {
        View view = createView(ctx, node.tag);
        for (String s:node.attrs.keySet())
            ViewAttributeHelper.applyAttribute(view,s, node.attrs.get(s));
        if (view instanceof ViewGroup) {
            for (VNode child : node.children) {
                ((ViewGroup) view).addView(build(ctx, child));
            }
        }
        return view;
    }
    private static int dp(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private static int parseSize(String val) {
        if ("match".equals(val)) return ViewGroup.LayoutParams.MATCH_PARENT;
        if ("wrap".equals(val)) return ViewGroup.LayoutParams.WRAP_CONTENT;
        float dp = Float.parseFloat(val.replace("dp", ""));
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void applyAttr(View v, String key, String val) {
        switch (key) {
            case "text":
                if (v instanceof TextView) {
                    ((TextView) v).setText(val);
                } else {
                    System.out.println("Warning: 'text' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "width":
                int w = parseSize(val);
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (lp == null) {
                    lp = new ViewGroup.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT);
                } else {
                    lp.width = w;
                }
                v.setLayoutParams(lp);
                break;
            case "height":
                int h = parseSize(val);
                lp = v.getLayoutParams();
                if (lp == null) {
                    lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, h);
                } else {
                    lp.height = h;
                }
                v.setLayoutParams(lp);
                break;
            case "orientation":
                if (v instanceof LinearLayout) {
                    ((LinearLayout) v).setOrientation(
                            "horizontal".equals(val) ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
                } else {
                    System.out.println("Warning: 'orientation' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "padding":
                String[] paddingArgs = val.split(",");
                if (paddingArgs.length == 4) {
                    int left = dp(v.getContext(), Float.parseFloat(paddingArgs[0].replace("dp", "")));
                    int top = dp(v.getContext(), Float.parseFloat(paddingArgs[1].replace("dp", "")));
                    int right = dp(v.getContext(), Float.parseFloat(paddingArgs[2].replace("dp", "")));
                    int bottom = dp(v.getContext(), Float.parseFloat(paddingArgs[3].replace("dp", "")));
                    v.setPadding(left, top, right, bottom);
                } else {
                    System.out.println("Warning: 'padding' attribute requires 4 values (left,top,right,bottom)");
                }
                break;
            case "background":
                try {
                    v.setBackgroundColor(Color.parseColor(val));
                } catch (IllegalArgumentException e) {
                    System.out.println("Warning: Invalid color value for 'background': " + val);
                }
                break;
            case "src":
                if (v instanceof ImageView) {
                    try {
                        int resId = Integer.parseInt(val);
                        ((ImageView) v).setImageResource(resId);
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid resource ID for 'src': " + val);
                    }
                } else {
                    System.out.println("Warning: 'src' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "hint":
                if (v instanceof EditText) {
                    ((EditText) v).setHint(val);
                } else {
                    System.out.println("Warning: 'hint' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "enabled":
                v.setEnabled(Boolean.parseBoolean(val));
                break;
            case "clickable":
                v.setClickable(Boolean.parseBoolean(val));
                break;
            case "longClickable":
                v.setLongClickable(Boolean.parseBoolean(val));
                break;
            case "focusable":
                v.setFocusable(Boolean.parseBoolean(val));
                break;
            case "gravity":
                if (v instanceof TextView) {
                    ((TextView) v).setGravity(parseGravity(val));
                } else {
                    System.out.println("Warning: 'gravity' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "textSize":
                if (v instanceof TextView) {
                    float size = Float.parseFloat(val.replace("sp", ""));
                    ((TextView) v).setTextSize(size);
                } else {
                    System.out.println("Warning: 'textSize' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "textColor":
                if (v instanceof TextView) {
                    try {
                        ((TextView) v).setTextColor(Color.parseColor(val));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Warning: Invalid color value for 'textColor': " + val);
                    }
                } else {
                    System.out.println("Warning: 'textColor' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "layout_weight":
                if (v.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) v.getLayoutParams()).weight = Float.parseFloat(val);
                } else {
                    System.out.println("Warning: 'layout_weight' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "layout_gravity":
                if (v.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) v.getLayoutParams()).gravity = parseGravity(val);
                } else {
                    System.out.println("Warning: 'layout_gravity' attribute is not applicable to " + v.getClass().getSimpleName());
                }
                break;
            case "layout_margin":
                String[] marginArgs = val.split(",");
                if (marginArgs.length == 4) {
                    int marginLeft = dp(v.getContext(), Float.parseFloat(marginArgs[0].replace("dp", "")));
                    int marginTop = dp(v.getContext(), Float.parseFloat(marginArgs[1].replace("dp", "")));
                    int marginRight = dp(v.getContext(), Float.parseFloat(marginArgs[2].replace("dp", "")));
                    int marginBottom = dp(v.getContext(), Float.parseFloat(marginArgs[3].replace("dp", "")));
                    if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).setMargins(marginLeft, marginTop, marginRight, marginBottom);
                    } else {
                        System.out.println("Warning: 'layout_margin' attribute is not applicable to " + v.getClass().getSimpleName());
                    }
                } else {
                    System.out.println("Warning: 'layout_margin' attribute requires 4 values (left,top,right,bottom)");
                }
                break;
            default:
                System.out.println("Warning: Unknown attribute '" + key + "'");
        }
    }

    private static int parseGravity(String val) {
        switch (val) {
            case "left":
                return android.view.Gravity.LEFT;
            case "right":
                return android.view.Gravity.RIGHT;
            case "center":
                return android.view.Gravity.CENTER;
            case "center_vertical":
                return android.view.Gravity.CENTER_VERTICAL;
            case "center_horizontal":
                return android.view.Gravity.CENTER_HORIZONTAL;
            case "top":
                return android.view.Gravity.TOP;
            case "bottom":
                return android.view.Gravity.BOTTOM;
            default:
                return android.view.Gravity.NO_GRAVITY;
        }
    }
}
