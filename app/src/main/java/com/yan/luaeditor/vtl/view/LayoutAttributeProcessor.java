package com.yan.luaeditor.vtl.view;

import static com.yan.luaeditor.vtl.view.AttributeUtils.dpToPx;
import static com.yan.luaeditor.vtl.view.AttributeUtils.parseFloatSafely;
import static com.yan.luaeditor.vtl.view.AttributeUtils.parseIntSafely;
import static com.yan.luaeditor.vtl.view.AttributeUtils.setDrawable;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.DrawerLayout;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import java.util.function.BiConsumer;

/**
 * 布局容器属性处理器，处理所有布局相关的属性
 */
public class LayoutAttributeProcessor implements ViewAttributeProcessor, AttributeConstants {
    private final BaseViewAttributeProcessor baseProcessor = new BaseViewAttributeProcessor();

    @Override
    public boolean processAttribute(View view, String attributeName, String attributeValue) {
        // 先尝试用基础处理器处理通用属性
        if (baseProcessor.processAttribute(view, attributeName, attributeValue)) {
            return true;
        }

        // 处理布局容器
        if (view instanceof LinearLayout) {
            return processLinearLayoutAttributes((LinearLayout) view, attributeName, attributeValue);
        } else if (view instanceof RelativeLayout) {
            return processRelativeLayoutAttributes((RelativeLayout) view, attributeName, attributeValue);
        } else if (view instanceof FrameLayout) {
            return processFrameLayoutAttributes((FrameLayout) view, attributeName, attributeValue);
        } else if (view instanceof ConstraintLayout) {
            return processConstraintLayoutAttributes((ConstraintLayout) view, attributeName, attributeValue);
        } else if (view instanceof TableLayout) {
            return processTableLayoutAttributes((TableLayout) view, attributeName, attributeValue);
        } else if (view instanceof GridLayout) {
            return processGridLayoutAttributes((GridLayout) view, attributeName, attributeValue);
        } else if (view instanceof AbsoluteLayout) {
            return processAbsoluteLayoutAttributes((AbsoluteLayout) view, attributeName, attributeValue);
        } else if (view instanceof CoordinatorLayout) {
            return processCoordinatorLayoutAttributes((CoordinatorLayout) view, attributeName, attributeValue);
        } else if (view instanceof DrawerLayout) {
            return processDrawerLayoutAttributes((DrawerLayout) view, attributeName, attributeValue);
        } else if (view instanceof NestedScrollView) {
            return processNestedScrollViewAttributes((NestedScrollView) view, attributeName, attributeValue);
        }

        return false;
    }
    private static final String TAG = "LayoutProcessor";

    /**
     * 处理ConstraintLayout属性
     */
    public boolean processConstraintLayoutAttributes(ConstraintLayout cl, String attributeName, String attributeValue) {
        ConstraintSet set = new ConstraintSet();
        set.clone(cl);
        int id = cl.getId();
        switch (attributeName) {
            /* 约束关系 */
            case "layout_constraintLeft_toLeftOf":
            case "layout_constraintLeft_toRightOf":
            case "layout_constraintRight_toLeftOf":
            case "layout_constraintRight_toRightOf":
            case "layout_constraintTop_toTopOf":
            case "layout_constraintTop_toBottomOf":
            case "layout_constraintBottom_toTopOf":
            case "layout_constraintBottom_toBottomOf":
            case "layout_constraintBaseline_toBaselineOf":
            case "layout_constraintStart_toStartOf":
            case "layout_constraintStart_toEndOf":
            case "layout_constraintEnd_toStartOf":
            case "layout_constraintEnd_toEndOf":
                applyConstraint(cl, attributeName, attributeValue);
                return true;

            /* 偏移 & 比例 */
            case "layout_constraintHorizontal_bias":
                set.setHorizontalBias(id, parseFloatSafely(attributeValue, 0.5f));
                return true;
            case "layout_constraintVertical_bias":
                set.setVerticalBias(id, parseFloatSafely(attributeValue, 0.5f));
                return true;
            case "layout_constraintDimensionRatio":
                set.setDimensionRatio(id, attributeValue);
                return true;

            /* 链样式 */
            case "layout_constraintHorizontal_chainStyle":
                set.setHorizontalChainStyle(id, parseChainStyle(attributeValue));
                return true;
            case "layout_constraintVertical_chainStyle":
                set.setVerticalChainStyle(id, parseChainStyle(attributeValue));
                return true;

            /* 圆形约束 */
            case "layout_constraintCircle":
                String[] c = attributeValue.split(",");
                if (c.length >= 3) {
                    int target = parseIntSafely(c[0], ConstraintSet.PARENT_ID);
                    float angle = parseFloatSafely(c[1], 0);
                    float radius = dpToPx(cl.getContext(), parseFloatSafely(c[2], 0));
                    set.constrainCircle(id, target, (int) radius, angle);
                }
                return true;

            /* 尺寸 / 边距 */
            case "layout_constraintWidth_min":
                set.constrainMinWidth(id, dpToPx(cl.getContext(), parseFloatSafely(attributeValue, 0)));
                return true;
            case "layout_constraintWidth_max":
                set.constrainMaxWidth(id, dpToPx(cl.getContext(), parseFloatSafely(attributeValue, 0)));
                return true;
            case "layout_constraintHeight_min":
                set.constrainMinHeight(id, dpToPx(cl.getContext(), parseFloatSafely(attributeValue, 0)));
                return true;
            case "layout_constraintHeight_max":
                set.constrainMaxHeight(id, dpToPx(cl.getContext(), parseFloatSafely(attributeValue, 0)));
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理TableLayout属性
     */
    public boolean processTableLayoutAttributes(TableLayout tl, String attributeName, String attributeValue) {
        Context ctx = tl.getContext();
        switch (attributeName) {
            case "stretchColumns":
                if ("*".equals(attributeValue)) tl.setStretchAllColumns(true);
                else setColumnFlags(tl, attributeValue, tl::setColumnStretchable);
                return true;
            case "shrinkColumns":
                if ("*".equals(attributeValue)) tl.setShrinkAllColumns(true);
                else setColumnFlags(tl, attributeValue, tl::setColumnShrinkable);
                return true;
            case "collapseColumns":
                setColumnFlags(tl, attributeValue, tl::setColumnCollapsed);
                return true;
            case "divider":
                setDrawable(tl, attributeValue, tl::setDividerDrawable);
                return true;
            case "showDividers":
                tl.setShowDividers(parseShowDividers(attributeValue));
                return true;
            case "dividerPadding":
                tl.setDividerPadding(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;
            case "padding":
                AttributeUtils.applyPadding(tl, attributeValue);
                return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                AttributeUtils.applySinglePadding(tl, attributeName, attributeValue);
                return true;
            default: return false;
        }
    }
    private void setColumnFlags(TableLayout tl, String v, BiConsumer<Integer, Boolean> setter) {
        if ("*".equals(v)) return;
        for (String s : v.split(",")) setter.accept(parseIntSafely(s, 0), true);
    }
    /**
     * 处理GridLayout属性
     */
    public boolean processGridLayoutAttributes(GridLayout gl, String attributeName, String attributeValue) {
        Context ctx = gl.getContext();
        switch (attributeName) {
            case "columnCount": gl.setColumnCount(parseIntSafely(attributeValue, 1)); return true;
            case "rowCount": gl.setRowCount(parseIntSafely(attributeValue, 1)); return true;
            case "orientation": gl.setOrientation("vertical".equals(attributeValue) ? GridLayout.VERTICAL : GridLayout.HORIZONTAL); return true;
            case "alignmentMode": gl.setAlignmentMode("alignBounds".equals(attributeValue) ? GridLayout.ALIGN_BOUNDS : GridLayout.ALIGN_MARGINS); return true;
            case "columnOrderPreserved": gl.setColumnOrderPreserved(Boolean.parseBoolean(attributeValue)); return true;
            case "rowOrderPreserved": gl.setRowOrderPreserved(Boolean.parseBoolean(attributeValue)); return true;
            case "useDefaultMargins": gl.setUseDefaultMargins(Boolean.parseBoolean(attributeValue)); return true;
            case "padding": AttributeUtils.applyPadding(gl, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                AttributeUtils.applySinglePadding(gl, attributeName, attributeValue); return true;
            default: return false;
        }
    }

    /**
     * 处理AbsoluteLayout属性（已过时，但仍提供支持）
     */
    @Deprecated
    public boolean processAbsoluteLayoutAttributes(AbsoluteLayout absoluteLayout, String attributeName, String attributeValue) {
        Context context = absoluteLayout.getContext();

        switch (attributeName) {
            case "layout_x":
                try {
                    float x = AttributeUtils.parseDimension(context, attributeValue);
                    ViewGroup.LayoutParams params = absoluteLayout.getLayoutParams();
                    if (params instanceof AbsoluteLayout.LayoutParams) {
                        ((AbsoluteLayout.LayoutParams) params).x = (int) x;
                        absoluteLayout.setLayoutParams(params);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid layout_x: " + attributeValue);
                }
                return true;

            case "layout_y":
                try {
                    float y = AttributeUtils.parseDimension(context, attributeValue);
                    ViewGroup.LayoutParams params = absoluteLayout.getLayoutParams();
                    if (params instanceof AbsoluteLayout.LayoutParams) {
                        ((AbsoluteLayout.LayoutParams) params).y = (int) y;
                        absoluteLayout.setLayoutParams(params);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid layout_y: " + attributeValue);
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理CoordinatorLayout属性
     */
    public boolean processCoordinatorLayoutAttributes(CoordinatorLayout cl, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "statusBarBackground":
                setDrawable(cl, attributeValue, cl::setStatusBarBackground);
                return true;
            case "fitsSystemWindows":
                cl.setFitsSystemWindows(Boolean.parseBoolean(attributeValue));
                return true;
            case "padding": AttributeUtils.applyPadding(cl, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                AttributeUtils.applySinglePadding(cl, attributeName, attributeValue); return true;
            default: return false;
        }
    }

    /**
     * 处理DrawerLayout属性
     */
    public boolean processDrawerLayoutAttributes(DrawerLayout dl, String attributeName, String attributeValue) {
        Context ctx = dl.getContext();
        switch (attributeName) {
            case "drawerElevation":
                dl.setDrawerElevation(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;
            case "scrimColor":
                dl.setScrimColor(Color.parseColor(attributeValue));
                return true;
            case "drawerLockMode":
                String[] parts = attributeValue.split(",");
                int lock = "locked_closed".equals(parts[0]) ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                        "locked_open".equals(parts[0])   ? DrawerLayout.LOCK_MODE_LOCKED_OPEN :
                                DrawerLayout.LOCK_MODE_UNLOCKED;
                int gravity = parts.length > 1 ? AttributeUtils.parseGravity(parts[1]) : Gravity.START;
                dl.setDrawerLockMode(lock, gravity);
                return true;
            default: return false;
        }
    }

    /**
     * 处理NestedScrollView属性
     */
    public boolean processNestedScrollViewAttributes(NestedScrollView nestedScrollView, String attributeName, String attributeValue) {
        Context context = nestedScrollView.getContext();

        switch (attributeName) {
            case "fillViewport":
                nestedScrollView.setFillViewport(Boolean.parseBoolean(attributeValue));
                return true;

            case "scrollbars":
                switch (attributeValue) {
                    case "none":
                        nestedScrollView.setVerticalScrollBarEnabled(false);
                        nestedScrollView.setHorizontalScrollBarEnabled(false);
                        break;
                    case "vertical":
                        nestedScrollView.setVerticalScrollBarEnabled(true);
                        nestedScrollView.setHorizontalScrollBarEnabled(false);
                        break;
                    case "horizontal":
                        nestedScrollView.setVerticalScrollBarEnabled(false);
                        nestedScrollView.setHorizontalScrollBarEnabled(true);
                        break;
                    case "both":
                        nestedScrollView.setVerticalScrollBarEnabled(true);
                        nestedScrollView.setHorizontalScrollBarEnabled(true);
                        break;
                }
                return true;

            case "smoothScrollingEnabled":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    nestedScrollView.setSmoothScrollingEnabled(Boolean.parseBoolean(attributeValue));
                }
                return true;

            case "isNestedScrollingEnabled":
                nestedScrollView.setNestedScrollingEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            case "scrollBarStyle":
                try {
                    int style = AttributeUtils.parseScrollBarStyle(attributeValue);
                    nestedScrollView.setScrollBarStyle(style);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid scrollBarStyle: " + attributeValue);
                }
                return true;

            case "overScrollMode":
                try {
                    int mode = AttributeUtils.parseOverScrollMode(attributeValue);
                    nestedScrollView.setOverScrollMode(mode);
                } catch (Exception e) {
                    Log.w(TAG, "Invalid overScrollMode: " + attributeValue);
                }
                return true;

            case "scrollTo":
                String[] coords = attributeValue.split(",");
                if (coords.length == 2) {
                    try {
                        int x = Integer.parseInt(coords[0].trim());
                        int y = Integer.parseInt(coords[1].trim());
                        nestedScrollView.scrollTo(x, y);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid scrollTo coordinates: " + attributeValue);
                    }
                }
                return true;

            case "smoothScrollTo":
                String[] smoothCoords = attributeValue.split(",");
                if (smoothCoords.length == 2) {
                    try {
                        int x = Integer.parseInt(smoothCoords[0].trim());
                        int y = Integer.parseInt(smoothCoords[1].trim());
                        nestedScrollView.smoothScrollTo(x, y);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid smoothScrollTo coordinates: " + attributeValue);
                    }
                }
                return true;

            default:
                return false;
        }
    }

    // 辅助方法：应用约束布局约束
    private void applyConstraint(ConstraintLayout constraintLayout, String constraintName, String value) {
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);

        String[] parts = constraintName.split("_");
        String targetSide = parts[1];
        String relation = parts[2];
        String target = parts[3];

        try {
            int viewId = constraintLayout.getId();
            int targetId = "parent".equals(value) ? ConstraintSet.PARENT_ID : Integer.parseInt(value);

            int sourceSide = getConstraintSide(targetSide);
            int targetSideConstraint = getConstraintSide(target);

            set.connect(viewId, sourceSide, targetId, targetSideConstraint);
            set.applyTo(constraintLayout);
        } catch (Exception e) {
            Log.w(TAG, "Error applying constraint: " + constraintName + "=" + value, e);
        }
    }

    // 辅助方法：应用约束布局偏差值
    private void applyBias(ConstraintLayout constraintLayout, String biasName, float bias) {
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);

        int viewId = constraintLayout.getId();
        if (biasName.contains("Horizontal")) {
            set.setHorizontalBias(viewId, bias);
        } else {
            set.setVerticalBias(viewId, bias);
        }

        set.applyTo(constraintLayout);
    }

    // 辅助方法：应用约束布局尺寸比例
    private void applyDimensionRatio(ConstraintLayout constraintLayout, String ratio) {
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);
        set.setDimensionRatio(constraintLayout.getId(), ratio);
        set.applyTo(constraintLayout);
    }

    // 辅助方法：应用约束布局链样式
    private void applyChainStyle(ConstraintLayout constraintLayout, String chainName, int chainStyle) {
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);

        if (chainName.contains("Horizontal")) {
            set.setHorizontalChainStyle(constraintLayout.getId(), chainStyle);
        } else {
            set.setVerticalChainStyle(constraintLayout.getId(), chainStyle);
        }

        set.applyTo(constraintLayout);
    }

    // 辅助方法：应用圆形约束
    private void applyCircleConstraint(ConstraintLayout constraintLayout, int targetId, float angle, float radius) {
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);
        set.constrainCircle(constraintLayout.getId(), targetId, (int) radius, angle);
        set.applyTo(constraintLayout);
    }

    // 辅助方法：应用网格布局权重
    private void applyGridWeight(GridLayout gridLayout, String weightType, String value) {
        try {
            float weight = Float.parseFloat(value);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) gridLayout.getLayoutParams();

            gridLayout.setLayoutParams(params);
        } catch (Exception e) {
            Log.w(TAG, "Error applying grid weight: " + weightType + "=" + value, e);
        }
    }

    // 辅助方法：解析约束方向
    private int getConstraintSide(String side) {
        switch (side) {
            case "Left": return ConstraintSet.LEFT;
            case "Right": return ConstraintSet.RIGHT;
            case "Top": return ConstraintSet.TOP;
            case "Bottom": return ConstraintSet.BOTTOM;
            case "Baseline": return ConstraintSet.BASELINE;
            default: return ConstraintSet.LEFT;
        }
    }

    // 辅助方法：解析链样式
    private int parseChainStyle(String style) {
        switch (style) {
            case "spread": return ConstraintSet.CHAIN_SPREAD;
            case "spread_inside": return ConstraintSet.CHAIN_SPREAD_INSIDE;
            case "packed": return ConstraintSet.CHAIN_PACKED;
            default: return ConstraintSet.CHAIN_SPREAD;
        }
    }

    // 辅助方法：解析滚动条样式
    private int parseScrollBarStyle(String style) {
        switch (style) {
            case "insideOverlay": return View.SCROLLBARS_INSIDE_OVERLAY;
            case "insideInset": return View.SCROLLBARS_INSIDE_INSET;
            case "outsideOverlay": return View.SCROLLBARS_OUTSIDE_OVERLAY;
            case "outsideInset": return View.SCROLLBARS_OUTSIDE_INSET;
            default: return View.SCROLLBARS_INSIDE_OVERLAY;
        }
    }

    // 辅助方法：解析过度滚动模式
    private int parseOverScrollMode(String mode) {
        switch (mode) {
            case "always": return View.OVER_SCROLL_ALWAYS;
            case "never": return View.OVER_SCROLL_NEVER;
            case "ifContentScrolls": return View.OVER_SCROLL_IF_CONTENT_SCROLLS;
            default: return View.OVER_SCROLL_IF_CONTENT_SCROLLS;
        }
    }

    /**
     * 处理LinearLayout属性
     */
    protected boolean processLinearLayoutAttributes(LinearLayout ll, String attributeName, String attributeValue) {
        Context ctx = ll.getContext();
        switch (attributeName) {
            /* 方向与对齐 */
            case "orientation":
                ll.setOrientation("horizontal".equalsIgnoreCase(attributeValue)
                        ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
                return true;
            case "gravity":
                ll.setGravity(AttributeUtils.parseGravity(attributeValue));
                return true;
            case "baselineAligned":
                ll.setBaselineAligned(Boolean.parseBoolean(attributeValue));
                return true;

            /* 分割线 */
            case "divider":
                setDrawable(ll, attributeValue, ll::setDividerDrawable);
                return true;
            case "showDividers":
                ll.setShowDividers(parseShowDividers(attributeValue));
                return true;
            case "dividerPadding":
                ll.setDividerPadding(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            /* 权重 */
            case "weightSum":
                ll.setWeightSum(parseFloatSafely(attributeValue, 0));
                return true;

            /* 子控件对齐 */
            case "baselineAlignedChildIndex":
                ll.setBaselineAlignedChildIndex(parseIntSafely(attributeValue, -1));
                return true;

            /* 间距 */
            case "padding":
                AttributeUtils.applyPadding(ll, attributeValue);
                return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                AttributeUtils.applySinglePadding(ll, attributeName, attributeValue);
                return true;

            /* 通用外观 */
            case "background":
                setDrawable(ll, attributeValue, ll::setBackground);
                return true;
            case "minWidth":
                ll.setMinimumWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;
            case "minHeight":
                ll.setMinimumHeight(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            default: return false;
        }
    }

    /* ==================  RelativeLayout  ================== */
    protected boolean processRelativeLayoutAttributes(RelativeLayout rl, String attributeName, String attributeValue) {
        switch (attributeName) {
            /* 自身属性 */
            case "gravity":
                rl.setGravity(AttributeUtils.parseGravity(attributeValue));
                return true;
            case "ignoreGravity":
                rl.setIgnoreGravity(parseIntSafely(attributeValue, View.NO_ID));
                return true;

            /* 分割线 / 前景 */
            case "foreground":
                setDrawable(rl, attributeValue, rl::setForeground);
                return true;
            case "foregroundGravity":
                rl.setForegroundGravity(AttributeUtils.parseGravity(attributeValue));
                return true;

            /* 子控件规则（示例）*/
            default:
                return processRelativeLayoutRules(rl, attributeName, attributeValue);
        }
    }

    /* ==================  子控件规则处理示例  ================== */
    protected boolean processRelativeLayoutRules(RelativeLayout rl, String name, String value) {
        if (!name.startsWith("layout_")) return false;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) rl.getLayoutParams();
        int rule = getRelativeRule(name);
        if (rule == -1) return false;
        if ("true".equalsIgnoreCase(value)) {
            lp.addRule(rule);
        } else if ("false".equalsIgnoreCase(value)) {
            lp.removeRule(rule);
        } else {
            int targetId = "parent".equals(value) ? RelativeLayout.TRUE : parseIntSafely(value, View.NO_ID);
            lp.addRule(rule, targetId);
        }
        rl.setLayoutParams(lp);
        return true;
    }

    private int getRelativeRule(String name) {
        switch (name) {
            case "layout_above": return RelativeLayout.ABOVE;
            case "layout_below": return RelativeLayout.BELOW;
            case "layout_toLeftOf": return RelativeLayout.LEFT_OF;
            case "layout_toRightOf": return RelativeLayout.RIGHT_OF;
            case "layout_alignBaseline": return RelativeLayout.ALIGN_BASELINE;
            case "layout_alignTop": return RelativeLayout.ALIGN_TOP;
            case "layout_alignBottom": return RelativeLayout.ALIGN_BOTTOM;
            case "layout_alignLeft": return RelativeLayout.ALIGN_LEFT;
            case "layout_alignRight": return RelativeLayout.ALIGN_RIGHT;
            case "layout_alignParentTop": return RelativeLayout.ALIGN_PARENT_TOP;
            case "layout_alignParentBottom": return RelativeLayout.ALIGN_PARENT_BOTTOM;
            case "layout_alignParentLeft": return RelativeLayout.ALIGN_PARENT_LEFT;
            case "layout_alignParentRight": return RelativeLayout.ALIGN_PARENT_RIGHT;
            case "layout_centerInParent": return RelativeLayout.CENTER_IN_PARENT;
            case "layout_centerHorizontal": return RelativeLayout.CENTER_HORIZONTAL;
            case "layout_centerVertical": return RelativeLayout.CENTER_VERTICAL;
            default: return -1;
        }
    }

    public static PorterDuff.Mode parsePorterDuffMode(String modeStr) {
        if (modeStr == null) return PorterDuff.Mode.SRC_OVER;

        switch (modeStr.toLowerCase().trim()) {
            case "clear":
                return PorterDuff.Mode.CLEAR;
            case "src":
                return PorterDuff.Mode.SRC;
            case "dst":
                return PorterDuff.Mode.DST;
            case "src_over":
                return PorterDuff.Mode.SRC_OVER;
            case "dst_over":
                return PorterDuff.Mode.DST_OVER;
            case "src_in":
                return PorterDuff.Mode.SRC_IN;
            case "dst_in":
                return PorterDuff.Mode.DST_IN;
            case "src_out":
                return PorterDuff.Mode.SRC_OUT;
            case "dst_out":
                return PorterDuff.Mode.DST_OUT;
            case "src_atop":
                return PorterDuff.Mode.SRC_ATOP;
            case "dst_atop":
                return PorterDuff.Mode.DST_ATOP;
            case "xor":
                return PorterDuff.Mode.XOR;
            case "darken":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return PorterDuff.Mode.DARKEN;
                }
                break;
            case "lighten":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return PorterDuff.Mode.LIGHTEN;
                }
                break;
            case "multiply":
                return PorterDuff.Mode.MULTIPLY;
            case "screen":
                return PorterDuff.Mode.SCREEN;
            case "add":
                return PorterDuff.Mode.ADD;
            case "overlay":
                return PorterDuff.Mode.OVERLAY;
        }
        return PorterDuff.Mode.SRC_OVER; // 默认值
    }
    /**
     * 处理FrameLayout属性
     */
    protected boolean processFrameLayoutAttributes(FrameLayout frameLayout, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "foregroundGravity":
                frameLayout.setForegroundGravity(AttributeUtils.parseGravity(attributeValue));
                return true;

            case "measureAllChildren":
                frameLayout.setMeasureAllChildren(Boolean.parseBoolean(attributeValue));
                return true;

            case "foreground":
                if (attributeValue.startsWith("@")) {
                    int resId = Integer.parseInt(attributeValue.substring(1));
                    frameLayout.setForeground(getContext(frameLayout).getResources().getDrawable(resId));
                } else if (attributeValue.startsWith("#")) {
                    frameLayout.setForeground(new ColorDrawable(Color.parseColor(attributeValue)));
                }
                return true;

            case "foregroundTint":
                frameLayout.setForegroundTintList(ColorStateList.valueOf(Color.parseColor(attributeValue)));
                return true;

            case "foregroundTintMode":
                frameLayout.setForegroundTintMode(parsePorterDuffMode(attributeValue));
                return true;

            case "layout_gravity":
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                params.gravity = AttributeUtils.parseGravity(attributeValue);
                frameLayout.setLayoutParams(params);
                return true;

            case "margin":
                int margin = Integer.parseInt(attributeValue);
                FrameLayout.LayoutParams marginParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                marginParams.setMargins(margin, margin, margin, margin);
                frameLayout.setLayoutParams(marginParams);
                return true;

            case "marginLeft":
                FrameLayout.LayoutParams leftMarginParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                leftMarginParams.leftMargin = Integer.parseInt(attributeValue);
                frameLayout.setLayoutParams(leftMarginParams);
                return true;

            case "marginTop":
                FrameLayout.LayoutParams topMarginParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                topMarginParams.topMargin = Integer.parseInt(attributeValue);
                frameLayout.setLayoutParams(topMarginParams);
                return true;

            case "marginRight":
                FrameLayout.LayoutParams rightMarginParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                rightMarginParams.rightMargin = Integer.parseInt(attributeValue);
                frameLayout.setLayoutParams(rightMarginParams);
                return true;

            case "marginBottom":
                FrameLayout.LayoutParams bottomMarginParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                bottomMarginParams.bottomMargin = Integer.parseInt(attributeValue);
                frameLayout.setLayoutParams(bottomMarginParams);
                return true;

            case "marginHorizontal":
                int horizontalMargin = Integer.parseInt(attributeValue);
                FrameLayout.LayoutParams horizontalParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                horizontalParams.leftMargin = horizontalMargin;
                horizontalParams.rightMargin = horizontalMargin;
                frameLayout.setLayoutParams(horizontalParams);
                return true;

            case "marginVertical":
                int verticalMargin = Integer.parseInt(attributeValue);
                FrameLayout.LayoutParams verticalParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                verticalParams.topMargin = verticalMargin;
                verticalParams.bottomMargin = verticalMargin;
                frameLayout.setLayoutParams(verticalParams);
                return true;

            case "width":
                FrameLayout.LayoutParams widthParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                widthParams.width = (int) AttributeUtils.parseDimension(getContext(frameLayout),attributeValue);
                frameLayout.setLayoutParams(widthParams);
                return true;

            case "height":
                FrameLayout.LayoutParams heightParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
                heightParams.height = (int) AttributeUtils.parseDimension(getContext(frameLayout),attributeValue);
                frameLayout.setLayoutParams(heightParams);
                return true;

            // 基础View属性（继承自View）
            case "id":
                frameLayout.setId(Integer.parseInt(attributeValue.substring(1)));
                return true;

            case "background":
                if (attributeValue.startsWith("@")) {
                    int resId = Integer.parseInt(attributeValue.substring(1));
                    frameLayout.setBackground(getContext(frameLayout).getResources().getDrawable(resId));
                } else if (attributeValue.startsWith("#")) {
                    frameLayout.setBackground(new ColorDrawable(Color.parseColor(attributeValue)));
                }
                return true;

            case "padding":
                int padding = Integer.parseInt(attributeValue);
                frameLayout.setPadding(padding, padding, padding, padding);
                return true;

            case "paddingLeft":
                frameLayout.setPadding(
                        Integer.parseInt(attributeValue),
                        frameLayout.getPaddingTop(),
                        frameLayout.getPaddingRight(),
                        frameLayout.getPaddingBottom()
                );
                return true;

            case "paddingTop":
                frameLayout.setPadding(
                        frameLayout.getPaddingLeft(),
                        Integer.parseInt(attributeValue),
                        frameLayout.getPaddingRight(),
                        frameLayout.getPaddingBottom()
                );
                return true;

            case "paddingRight":
                frameLayout.setPadding(
                        frameLayout.getPaddingLeft(),
                        frameLayout.getPaddingTop(),
                        Integer.parseInt(attributeValue),
                        frameLayout.getPaddingBottom()
                );
                return true;

            case "paddingBottom":
                frameLayout.setPadding(
                        frameLayout.getPaddingLeft(),
                        frameLayout.getPaddingTop(),
                        frameLayout.getPaddingRight(),
                        Integer.parseInt(attributeValue)
                );
                return true;

            case "paddingHorizontal":
                int horizontalPadding = Integer.parseInt(attributeValue);
                frameLayout.setPadding(
                        horizontalPadding,
                        frameLayout.getPaddingTop(),
                        horizontalPadding,
                        frameLayout.getPaddingBottom()
                );
                return true;

            case "paddingVertical":
                int verticalPadding = Integer.parseInt(attributeValue);
                frameLayout.setPadding(
                        frameLayout.getPaddingLeft(),
                        verticalPadding,
                        frameLayout.getPaddingRight(),
                        verticalPadding
                );
                return true;

            case "visibility":
                frameLayout.setVisibility(parseVisibility(attributeValue));
                return true;

            case "alpha":
                frameLayout.setAlpha(Float.parseFloat(attributeValue));
                return true;

            case "elevation":
                frameLayout.setElevation(Float.parseFloat(attributeValue));
                return true;

            case "rotation":
                frameLayout.setRotation(Float.parseFloat(attributeValue));
                return true;

            case "rotationX":
                frameLayout.setRotationX(Float.parseFloat(attributeValue));
                return true;

            case "rotationY":
                frameLayout.setRotationY(Float.parseFloat(attributeValue));
                return true;

            case "scaleX":
                frameLayout.setScaleX(Float.parseFloat(attributeValue));
                return true;

            case "scaleY":
                frameLayout.setScaleY(Float.parseFloat(attributeValue));
                return true;

            case "translationX":
                frameLayout.setTranslationX(Float.parseFloat(attributeValue));
                return true;

            case "translationY":
                frameLayout.setTranslationY(Float.parseFloat(attributeValue));
                return true;

            case "translationZ":
                frameLayout.setTranslationZ(Float.parseFloat(attributeValue));
                return true;

            default:
                return false;
        }
    }

    protected int parseGravity(String gravity) {
        return baseProcessor.parseGravity(gravity);
    }
    private int parseVisibility(String visibilityStr) {
        switch (visibilityStr) {
            case "visible": return View.VISIBLE;
            case "invisible": return View.INVISIBLE;
            case "gone": return View.GONE;
            default: return View.VISIBLE;
        }
    }
    protected int parseShowDividers(String showDividers) {
        int result = 0;
        String[] parts = showDividers.split("\\|");
        for (String part : parts) {
            switch (part.trim()) {
                case "none":
                    return LinearLayout.SHOW_DIVIDER_NONE;
                case "beginning":
                    result |= LinearLayout.SHOW_DIVIDER_BEGINNING;
                    break;
                case "middle":
                    result |= LinearLayout.SHOW_DIVIDER_MIDDLE;
                    break;
                case "end":
                    result |= LinearLayout.SHOW_DIVIDER_END;
                    break;
            }
        }
        return result;
    }
    


}
