package com.yan.luaeditor.vtl.view;

import static com.yan.luaeditor.vtl.view.AttributeUtils.applyCornerFamily;
import static com.yan.luaeditor.vtl.view.AttributeUtils.applyCornerSize;
import static com.yan.luaeditor.vtl.view.AttributeUtils.applyPadding;
import static com.yan.luaeditor.vtl.view.AttributeUtils.applySinglePadding;
import static com.yan.luaeditor.vtl.view.AttributeUtils.dpToPx;
import static com.yan.luaeditor.vtl.view.AttributeUtils.parseIntSafely;
import static com.yan.luaeditor.vtl.view.AttributeUtils.parsePorterDuffMode;
import static com.yan.luaeditor.vtl.view.AttributeUtils.parseTextAlignment;
import static com.yan.luaeditor.vtl.view.AttributeUtils.parseTextStyle;
import static com.yan.luaeditor.vtl.view.AttributeUtils.setDrawable;
import static com.yan.luaeditor.vtl.view.AttributeUtils.spToPx;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Material Design控件属性处理器，处理所有Material组件
 */
public class MaterialAttributeProcessor implements ViewAttributeProcessor, AttributeConstants {

    @Override
    public boolean processAttribute(View view, String attributeName, String attributeValue) {

        if (view instanceof MaterialButton) {
            return processMaterialButtonAttributes((MaterialButton) view, attributeName, attributeValue);
        } else if (view instanceof TextInputLayout) {
            return processTextInputLayoutAttributes((TextInputLayout) view, attributeName, attributeValue);
        } else if (view instanceof MaterialCardView) {
            return processMaterialCardViewAttributes((MaterialCardView) view, attributeName, attributeValue);
        } else if (view instanceof TabLayout) {
            return processTabLayoutAttributes((TabLayout) view, attributeName, attributeValue);
        } else if (view instanceof BottomNavigationView) {
            return processBottomNavigationViewAttributes((BottomNavigationView) view, attributeName, attributeValue);
        } else if (view instanceof FloatingActionButton) {
            return processFloatingActionButtonAttributes((FloatingActionButton) view, attributeName, attributeValue);
        } else if (view instanceof NavigationView) {
            return processNavigationViewAttributes((NavigationView) view, attributeName, attributeValue);
        } else if (view instanceof Chip) {
            return processChipAttributes((Chip) view, attributeName, attributeValue);
        } else if (view instanceof ChipGroup) {
            return processChipGroupAttributes((ChipGroup) view, attributeName, attributeValue);
        } else if (view instanceof Slider) {
            return processSliderAttributes((Slider) view, attributeName, attributeValue);
        } else if (view instanceof ShapeableImageView) {
            return processShapeableImageViewAttributes((ShapeableImageView) view, attributeName, attributeValue);
        } else if (view instanceof CircularProgressIndicator) {
            return processCircularProgressIndicatorAttributes((CircularProgressIndicator) view, attributeName, attributeValue);
        } else if (view instanceof LinearProgressIndicator) {
            return processLinearProgressIndicatorAttributes((LinearProgressIndicator) view, attributeName, attributeValue);
        } else if (view instanceof MaterialToolbar) {
            return processMaterialToolbarAttributes((MaterialToolbar) view, attributeName, attributeValue);
        } else if (view instanceof TextInputEditText) {
            return processTextInputEditTextAttributes((TextInputEditText) view, attributeName, attributeValue);
        } else if (view instanceof CheckBox) {
            return processMaterialCheckBoxAttributes((CheckBox) view, attributeName, attributeValue);
        } else if (view instanceof RadioButton) {
            return processMaterialRadioButtonAttributes((RadioButton) view, attributeName, attributeValue);
        } else if (view instanceof SwitchMaterial) {
            return processSwitchMaterialAttributes((SwitchMaterial) view, attributeName, attributeValue);
        }

        return false;
    }
    private static final String TAG = "MaterialProcessor";

    /**
     * 处理MaterialCardView属性
     */
    public boolean processMaterialCardViewAttributes(MaterialCardView cardView, String attributeName, String attributeValue) {
        Context context = cardView.getContext();

        switch (attributeName) {
            case "cardBackgroundColor":
                try {
                    cardView.setCardBackgroundColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid cardBackgroundColor: " + attributeValue);
                }
                return true;

            case "cardElevation":
                try {
                    float elevation = parseDimension(context, attributeValue);
                    cardView.setCardElevation(elevation);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid cardElevation: " + attributeValue);
                }
                return true;

            case "maxCardElevation":
                try {
                    float maxElevation = parseDimension(context, attributeValue);
                    cardView.setMaxCardElevation(maxElevation);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid maxCardElevation: " + attributeValue);
                }
                return true;

            case "cornerRadius":
                try {
                    float radius = parseDimension(context, attributeValue);
                    cardView.setRadius(radius);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid cornerRadius: " + attributeValue);
                }
                return true;

            case "strokeWidth":
                try {
                    int width = dpToPx(context, Float.parseFloat(attributeValue));
                    cardView.setStrokeWidth(width);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid strokeWidth: " + attributeValue);
                }
                return true;

            case "strokeColor":
                try {
                    cardView.setStrokeColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid strokeColor: " + attributeValue);
                }
                return true;

            case "contentPadding":
                String[] paddingValues = attributeValue.split(",");
                if (paddingValues.length == 4) {
                    try {
                        int left = dpToPx(context, Float.parseFloat(paddingValues[0]));
                        int top = dpToPx(context, Float.parseFloat(paddingValues[1]));
                        int right = dpToPx(context, Float.parseFloat(paddingValues[2]));
                        int bottom = dpToPx(context, Float.parseFloat(paddingValues[3]));
                        cardView.setContentPadding(left, top, right, bottom);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid contentPadding: " + attributeValue);
                    }
                }
                return true;

            case "useCompatPadding":
                cardView.setUseCompatPadding(Boolean.parseBoolean(attributeValue));
                return true;

            case "preventCornerOverlap":
                cardView.setPreventCornerOverlap(Boolean.parseBoolean(attributeValue));
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理TabLayout属性
     */
    public boolean processTabLayoutAttributes(TabLayout tabLayout, String attributeName, String attributeValue) {
        Context context = tabLayout.getContext();

        switch (attributeName) {
            case "tabMode":
                if ("scrollable".equals(attributeValue)) {
                    tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                } else {
                    tabLayout.setTabMode(TabLayout.MODE_FIXED);
                }
                return true;

            case "tabGravity":
                if ("center".equals(attributeValue)) {
                    tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
                } else {
                    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                }
                return true;

            case "tabIndicatorColor":
                try {
                    tabLayout.setSelectedTabIndicatorColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid tabIndicatorColor: " + attributeValue);
                }
                return true;

            case "tabIndicatorHeight":
                try {
                    int height = dpToPx(context, Float.parseFloat(attributeValue));
                    tabLayout.setSelectedTabIndicatorHeight(height);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid tabIndicatorHeight: " + attributeValue);
                }
                return true;

            case "tabTextColor":
                try {
                    int color = Color.parseColor(attributeValue);
                    tabLayout.setTabTextColors(color, tabLayout.getTabTextColors().getColorForState(new int[]{android.R.attr.state_selected}, color));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid tabTextColor: " + attributeValue);
                }
                return true;

            case "tabSelectedTextColor":
                try {
                    int selectedColor = Color.parseColor(attributeValue);
                    tabLayout.setTabTextColors(tabLayout.getTabTextColors().getDefaultColor(), selectedColor);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid tabSelectedTextColor: " + attributeValue);
                }
                return true;

            case "addTab":
                String[] tabParts = attributeValue.split("\\|");
                String tabText = tabParts[0];
                int iconRes = 0;

                if (tabParts.length > 1) {
                    try {
                        iconRes = Integer.parseInt(tabParts[1]);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid tab icon: " + tabParts[1]);
                    }
                }

                TabLayout.Tab tab = tabLayout.newTab().setText(tabText);
                if (iconRes != 0) {
                    tab.setIcon(iconRes);
                }
                tabLayout.addTab(tab);
                return true;

            case "tabBackground":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    tabLayout.setBackgroundResource(resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid tabBackground: " + attributeValue);
                }
                return true;

            case "selectedTabPosition":
                try {
                    tabLayout.setScrollPosition(Integer.parseInt(attributeValue), 0, true);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid selectedTabPosition: " + attributeValue);
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理BottomNavigationView属性
     */
    public boolean processBottomNavigationViewAttributes(BottomNavigationView bottomNav, String attributeName, String attributeValue) {
        Context context = bottomNav.getContext();

        switch (attributeName) {
            case "menu":
                try {
                    int menuRes = Integer.parseInt(attributeValue);
                    bottomNav.inflateMenu(menuRes);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid menu resource: " + attributeValue);
                }
                return true;

            case "selectedItemId":
                try {
                    bottomNav.setSelectedItemId(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid selectedItemId: " + attributeValue);
                }
                return true;

            case "labelVisibilityMode":
                switch (attributeValue) {
                    case "auto":
                        bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_AUTO);
                        break;
                    case "selected":
                        bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_SELECTED);
                        break;
                    case "labeled":
                        bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
                        break;
                    case "unlabeled":
                        bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_UNLABELED);
                        break;
                    default:
                        Log.w(TAG, "Invalid labelVisibilityMode: " + attributeValue);
                }
                return true;

            case "itemBackground":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    bottomNav.setItemBackgroundResource(resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid itemBackground: " + attributeValue);
                }
                return true;

            case "elevation":
                try {
                    float elevation = parseDimension(context, attributeValue);
                    bottomNav.setElevation(elevation);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid elevation: " + attributeValue);
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理FloatingActionButton属性
     */
    public boolean processFloatingActionButtonAttributes(FloatingActionButton fab, String attributeName, String attributeValue) {
        Context ctx = fab.getContext();
        switch (attributeName) {
            /* 图片 */
            case "src": case "imageResource": setDrawable(fab, attributeValue, fab::setImageDrawable); return true;

            /* 颜色 & 涟漪 */
            case "rippleColor": try { fab.setRippleColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid rippleColor"); } return true;
            case "backgroundTint": try { fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid backgroundTint"); } return true;

            /* 形状 & 大小 */
            case "elevation": fab.setElevation(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "size":
                switch (attributeValue) {
                    case "mini": fab.setSize(FloatingActionButton.SIZE_MINI); break;
                    case "normal": default: fab.setSize(FloatingActionButton.SIZE_NORMAL); break;
                } return true;
            case "cornerRadius": fab.setCustomSize(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true; // 新版支持 cornerRadius
            case "shapeAppearance":
                // 如需完整 ShapeAppearanceModel：Log.w(TAG, "shapeAppearance requires custom implementation");
                return true;

            /* 行为 & 状态 */
            case "clickable": fab.setClickable(Boolean.parseBoolean(attributeValue)); return true;
            case "enabled": fab.setEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "focusable": fab.setFocusable(Boolean.parseBoolean(attributeValue)); return true;

            /* 通用外观 */
            case "padding": applyPadding(fab, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(fab, attributeName, attributeValue); return true;

            default: return false;
        }
    }

    /**
     * 处理NavigationView属性
     */
    public boolean processNavigationViewAttributes(NavigationView navView, String attributeName, String attributeValue) {
        Context ctx = navView.getContext();
        switch (attributeName) {
            /* 菜单 & 头布局 */
            case "menu": try { navView.inflateMenu(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid menu"); } return true;
            case "headerLayout": try { navView.inflateHeaderView(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid headerLayout"); } return true;

            /* 外观 */
            case "itemBackground": setDrawable(navView, attributeValue, navView::setItemBackground); return true;
            case "itemIconTint": try { navView.setItemIconTintList(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid itemIconTint"); } return true;
            case "itemTextColor": try { navView.setItemTextColor(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid itemTextColor"); } return true;
            case "itemHorizontalPadding": navView.setItemHorizontalPadding(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "itemVerticalPadding": navView.setItemVerticalPadding(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            /* 通用 */
            case "padding": applyPadding(navView, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(navView, attributeName, attributeValue); return true;

            default: return false;
        }
    }

    /**
     * 处理Chip属性
     */
    public boolean processChipAttributes(Chip chip, String attributeName, String attributeValue) {
        Context ctx = chip.getContext();
        switch (attributeName) {
            /* 文本 & 样式 */
            case "text": chip.setText(attributeValue); return true;
            case "textSize": chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, parseFloatSafely(attributeValue, 14)); return true;
            case "textColor": try { chip.setTextColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid textColor"); } return true;
            case "textStyle": chip.setTypeface(chip.getTypeface(), parseTextStyle(attributeValue)); return true;

            /* 图标 */
            case "chipIcon": setDrawable(chip, attributeValue, chip::setChipIcon); return true;
            case "closeIcon": setDrawable(chip, attributeValue, chip::setCloseIcon); return true;
            case "closeIconEnabled": chip.setCloseIconEnabled(Boolean.parseBoolean(attributeValue)); return true;

            /* 状态 */
            case "checked": chip.setChecked(Boolean.parseBoolean(attributeValue)); return true;
            case "checkable": chip.setCheckable(Boolean.parseBoolean(attributeValue)); return true;
            case "clickable": chip.setClickable(Boolean.parseBoolean(attributeValue)); return true;

            /* 背景 & 外观 */
            case "chipBackgroundColor": try { chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid chipBackgroundColor"); } return true;
            case "chipStrokeColor": try { chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid chipStrokeColor"); } return true;
            case "chipStrokeWidth": chip.setChipStrokeWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "chipCornerRadius": chip.setChipCornerRadius(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            /* 通用 */
            case "padding": applyPadding(chip, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(chip, attributeName, attributeValue); return true;

            default: return false;
        }
    }

    /**
     * 处理ChipGroup属性
     */
    public boolean processChipGroupAttributes(ChipGroup chipGroup, String attributeName, String attributeValue) {
        Context ctx = chipGroup.getContext();
        switch (attributeName) {
            /* 选择 & 间距 */
            case "selectedChipId": chipGroup.check(parseIntSafely(attributeValue, View.NO_ID)); return true;
            case "chipSpacing": chipGroup.setChipSpacing(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "chipSpacingHorizontal": chipGroup.setChipSpacingHorizontal(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "chipSpacingVertical": chipGroup.setChipSpacingVertical(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            /* 方向 & 行为 */
            case "singleSelection": chipGroup.setSingleSelection(Boolean.parseBoolean(attributeValue)); return true;
            case "selectionRequired": chipGroup.setSelectionRequired(Boolean.parseBoolean(attributeValue)); return true;

            default: return false;
        }
    }

    /**
     * 处理Slider属性
     */
    public boolean processSliderAttributes(Slider slider, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "value":
                try {
                    slider.setValue(Float.parseFloat(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid value: " + attributeValue);
                }
                return true;

            case "valueFrom":
                try {
                    slider.setValueFrom(Float.parseFloat(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid valueFrom: " + attributeValue);
                }
                return true;

            case "valueTo":
                try {
                    slider.setValueTo(Float.parseFloat(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid valueTo: " + attributeValue);
                }
                return true;

            case "stepSize":
                try {
                    slider.setStepSize(Float.parseFloat(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid stepSize: " + attributeValue);
                }
                return true;


            case "trackHeight":
                try {
                    slider.setTrackHeight(dpToPx(slider.getContext(), Float.parseFloat(attributeValue)));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid trackHeight: " + attributeValue);
                }
                return true;

            case "enabled":
                slider.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理ShapeableImageView属性
     */
    public boolean processShapeableImageViewAttributes(ShapeableImageView imageView, String attributeName, String attributeValue) {
        Context ctx = imageView.getContext();
        switch (attributeName) {
            /* 图片源 & 缩放 */
            case "src": setDrawable(imageView, attributeValue, imageView::setImageDrawable); return true;
            case "scaleType": imageView.setScaleType(parseScaleType(attributeValue)); return true;

            /* 形状与圆角 */
            case "cornerRadius":
                float radius = dpToPx(ctx, parseFloatSafely(attributeValue, 0));
                ShapeAppearanceModel.Builder builder = imageView.getShapeAppearanceModel().toBuilder();
                builder.setAllCorners(CornerFamily.ROUNDED, radius);
                imageView.setShapeAppearanceModel(builder.build());
                return true;

            case "cornerFamily":
                int family = "rounded".equals(attributeValue) ? CornerFamily.ROUNDED : CornerFamily.CUT;
                imageView.setShapeAppearanceModel(imageView.getShapeAppearanceModel().toBuilder().setAllCorners(family, 0).build());
                return true;

            case "cornerFamilyTopLeft": case "cornerFamilyTopRight": case "cornerFamilyBottomLeft": case "cornerFamilyBottomRight":
                applyCornerFamily(imageView, attributeName, attributeValue); return true;

            case "cornerSizeTopLeft": case "cornerSizeTopRight": case "cornerSizeBottomLeft": case "cornerSizeBottomRight":
                applyCornerSize(imageView, attributeName, attributeValue); return true;

            /* 描边 */
            case "strokeWidth":
                imageView.setStrokeWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "strokeColor":
                imageView.setStrokeColor(ColorStateList.valueOf(Color.parseColor(attributeValue))); return true;

            /* 通用 */
            case "padding": applyPadding(imageView, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(imageView, attributeName, attributeValue); return true;
            case "contentDescription": imageView.setContentDescription(attributeValue); return true;

            default: return false;
        }
    }

    /**
     * 处理CircularProgressIndicator属性
     */
    public boolean processCircularProgressIndicatorAttributes(CircularProgressIndicator progress, String attributeName, String attributeValue) {
        Context context = progress.getContext();

        switch (attributeName) {
            case "progress":
                try {
                    progress.setProgress(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid progress: " + attributeValue);
                }
                return true;

            case "max":
                try {
                    progress.setMax(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid max: " + attributeValue);
                }
                return true;

            case "indeterminate":
                progress.setIndeterminate(Boolean.parseBoolean(attributeValue));
                return true;

            case "indicatorColor":
                try {
                    progress.setIndicatorColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid indicatorColor: " + attributeValue);
                }
                return true;

            case "trackColor":
                try {
                    progress.setTrackColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid trackColor: " + attributeValue);
                }
                return true;


            case "visibility":
                progress.setVisibility("visible".equals(attributeValue) ? View.VISIBLE :
                        "invisible".equals(attributeValue) ? View.INVISIBLE : View.GONE);
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理LinearProgressIndicator属性
     */
    public boolean processLinearProgressIndicatorAttributes(LinearProgressIndicator progress, String attributeName, String attributeValue) {
        Context context = progress.getContext();

        switch (attributeName) {
            case "progress":
                try {
                    progress.setProgress(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid progress: " + attributeValue);
                }
                return true;

            case "max":
                try {
                    progress.setMax(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid max: " + attributeValue);
                }
                return true;

            case "indeterminate":
                progress.setIndeterminate(Boolean.parseBoolean(attributeValue));
                return true;

            case "indicatorColor":
                try {
                    progress.setIndicatorColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid indicatorColor: " + attributeValue);
                }
                return true;

            case "trackColor":
                try {
                    progress.setTrackColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid trackColor: " + attributeValue);
                }
                return true;


            default:
                return false;
        }
    }

    /**
     * 处理MaterialToolbar属性
     */
    public boolean processMaterialToolbarAttributes(MaterialToolbar toolbar, String attributeName, String attributeValue) {
        Context ctx = toolbar.getContext();
        switch (attributeName) {
            /* --- 标题 --- */
            case "title": toolbar.setTitle(attributeValue); return true;
            case "subtitle": toolbar.setSubtitle(attributeValue); return true;
            case "titleTextColor": try { toolbar.setTitleTextColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid titleTextColor"); } return true;
            case "subtitleTextColor": try { toolbar.setSubtitleTextColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid subtitleTextColor"); } return true;

            /* --- 图标 --- */
            case "navigationIcon": try { toolbar.setNavigationIcon(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid navigationIcon"); } return true;
            case "logo": try { toolbar.setLogo(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid logo"); } return true;
            case "navigationContentDescription": toolbar.setNavigationContentDescription(attributeValue); return true;

            /* --- 菜单 --- */
            case "menu": try { toolbar.inflateMenu(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid menu resource"); } return true;
            case "onMenuItemClick": /* 需外部 setOnMenuItemClickListener */ return true;

            /* --- 外观 --- */
            case "elevation": toolbar.setElevation(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "titleMarginStart": toolbar.setTitleMarginStart(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "titleMarginEnd": toolbar.setTitleMarginEnd(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "titleMarginTop": toolbar.setTitleMarginTop(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "titleMarginBottom": toolbar.setTitleMarginBottom(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            /* --- 通用 --- */
            case "backgroundTint": try { toolbar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid backgroundTint"); } return true;
            case "padding": applyPadding(toolbar, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(toolbar, attributeName, attributeValue); return true;

            default: return false;
        }
    }

    /**
     * 处理MaterialAlertDialogBuilder属性
     */
    public boolean processMaterialAlertDialogAttributes(MaterialAlertDialogBuilder dialogBuilder, String attributeName, String attributeValue) {
        Context context = dialogBuilder.getContext();

        switch (attributeName) {
            case "title":
                dialogBuilder.setTitle(attributeValue);
                return true;

            case "message":
                dialogBuilder.setMessage(attributeValue);
                return true;

            case "positiveButton":
                String[] positiveParts = attributeValue.split("\\|");
                if (positiveParts.length >= 1) {
                    dialogBuilder.setPositiveButton(positiveParts[0], (dialog, which) -> {
                        //  positive button click listener
                    });
                }
                return true;

            case "negativeButton":
                String[] negativeParts = attributeValue.split("\\|");
                if (negativeParts.length >= 1) {
                    dialogBuilder.setNegativeButton(negativeParts[0], (dialog, which) -> {
                        // negative button click listener
                    });
                }
                return true;

            case "neutralButton":
                String[] neutralParts = attributeValue.split("\\|");
                if (neutralParts.length >= 1) {
                    dialogBuilder.setNeutralButton(neutralParts[0], (dialog, which) -> {
                        // neutral button click listener
                    });
                }
                return true;

            case "icon":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    dialogBuilder.setIcon(resId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid icon: " + attributeValue);
                }
                return true;

            case "cancelable":
                dialogBuilder.setCancelable(Boolean.parseBoolean(attributeValue));
                return true;

            case "view":
                try {
                    int layoutRes = Integer.parseInt(attributeValue);
                    dialogBuilder.setView(layoutRes);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid view: " + attributeValue);
                }
                return true;

            case "show":
                if (Boolean.parseBoolean(attributeValue)) {
                    dialogBuilder.show();
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * 处理BottomSheetDialog属性
     */
    public boolean processBottomSheetDialogAttributes(BottomSheetDialog bottomSheet, String attributeName, String attributeValue) {
        Context context = bottomSheet.getContext();

        switch (attributeName) {
            case "contentView":
                try {
                    int layoutRes = Integer.parseInt(attributeValue);
                    bottomSheet.setContentView(layoutRes);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid contentView: " + attributeValue);
                }
                return true;

            case "cancelable":
                bottomSheet.setCancelable(Boolean.parseBoolean(attributeValue));
                return true;

            case "dismissWithAnimation":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bottomSheet.setDismissWithAnimation(Boolean.parseBoolean(attributeValue));
                }
                return true;

            case "show":
                if (Boolean.parseBoolean(attributeValue)) {
                    bottomSheet.show();
                }
                return true;

            case "dismiss":
                if (Boolean.parseBoolean(attributeValue)) {
                    bottomSheet.dismiss();
                }
                return true;

            default:
                return false;
        }
    }


    /**
     * 处理TextInputEditText属性
     */
    public boolean processTextInputEditTextAttributes(TextInputEditText editText, String attributeName, String attributeValue) {
        Context ctx = editText.getContext();
        switch (attributeName) {
            /* --- 文本 --- */
            case "text": editText.setText(attributeValue); return true;
            case "hint": editText.setHint(attributeValue); return true;
            case "textSize": editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, parseFloatSafely(attributeValue, 14)); return true;
            case "textColor": try { editText.setTextColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid textColor"); } return true;
            case "hintTextColor": try { editText.setHintTextColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid hintTextColor"); } return true;
            case "textAllCaps": editText.setAllCaps(Boolean.parseBoolean(attributeValue)); return true;
            case "textStyle": editText.setTypeface(editText.getTypeface(), parseTextStyle(attributeValue)); return true;
            case "textAlignment": editText.setTextAlignment(parseTextAlignment(attributeValue)); return true;

            /* --- 输入 --- */
            case "inputType": editText.setInputType(parseInputType(attributeValue)); return true;
            case "imeOptions": editText.setImeOptions(parseImeOptions(attributeValue)); return true;
            case "maxLength": editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(parseIntSafely(attributeValue, Integer.MAX_VALUE))}); return true;
            case "maxLines": editText.setMaxLines(parseIntSafely(attributeValue, Integer.MAX_VALUE)); return true;
            case "minLines": editText.setMinLines(parseIntSafely(attributeValue, 1)); return true;
            case "lines": editText.setLines(parseIntSafely(attributeValue, 1)); return true;
            case "singleLine": editText.setSingleLine(Boolean.parseBoolean(attributeValue)); return true;
            case "password": editText.setTransformationMethod(Boolean.parseBoolean(attributeValue) ? PasswordTransformationMethod.getInstance() : null); return true;
            case "selectAllOnFocus": editText.setSelectAllOnFocus(Boolean.parseBoolean(attributeValue)); return true;
            case "cursorVisible": editText.setCursorVisible(Boolean.parseBoolean(attributeValue)); return true;

            /* --- 状态 --- */
            case "enabled": editText.setEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "clickable": editText.setClickable(Boolean.parseBoolean(attributeValue)); return true;
            case "focusable": editText.setFocusable(Boolean.parseBoolean(attributeValue)); return true;
            case "focusableInTouchMode": editText.setFocusableInTouchMode(Boolean.parseBoolean(attributeValue)); return true;

            /* --- 尺寸/边距 --- */
            case "padding": applyPadding(editText, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(editText, attributeName, attributeValue); return true;
            case "minWidth": editText.setMinimumWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "minHeight": editText.setMinimumHeight(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            /* --- 其他 --- */
            case "contentDescription": editText.setContentDescription(attributeValue); return true;
            default: return false;
        }
    }

    /**
     * 处理Material CheckBox属性
     */
    public boolean processMaterialCheckBoxAttributes(CheckBox checkBox, String attributeName, String attributeValue) {
        Context ctx = checkBox.getContext();
        switch (attributeName) {
            case "text": checkBox.setText(attributeValue); return true;
            case "textSize": checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, parseFloatSafely(attributeValue, 14)); return true;
            case "textColor": try { checkBox.setTextColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid textColor"); } return true;
            case "textStyle": checkBox.setTypeface(checkBox.getTypeface(), parseTextStyle(attributeValue)); return true;
            case "textAlignment": checkBox.setTextAlignment(parseTextAlignment(attributeValue)); return true;

            case "checked": checkBox.setChecked(Boolean.parseBoolean(attributeValue)); return true;
            case "enabled": checkBox.setEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "clickable": checkBox.setClickable(Boolean.parseBoolean(attributeValue)); return true;
            case "focusable": checkBox.setFocusable(Boolean.parseBoolean(attributeValue)); return true;

            /* --- 按钮图标 & 着色 --- */
            case "buttonDrawable": setDrawable(checkBox, attributeValue, checkBox::setButtonDrawable); return true;
            case "buttonTint": try { checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid buttonTint"); } return true;

            /* --- 尺寸/边距 --- */
            case "padding": applyPadding(checkBox, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(checkBox, attributeName, attributeValue); return true;
            case "minWidth": checkBox.setMinimumWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "minHeight": checkBox.setMinimumHeight(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            default: return false;
        }
    }

    /**
     * 处理Material RadioButton属性
     */
    public boolean processMaterialRadioButtonAttributes(RadioButton radioButton, String attributeName, String attributeValue) {
        Context ctx = radioButton.getContext();
        switch (attributeName) {
            case "text": radioButton.setText(attributeValue); return true;
            case "textSize": radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, parseFloatSafely(attributeValue, 14)); return true;
            case "textColor": try { radioButton.setTextColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid textColor"); } return true;
            case "textStyle": radioButton.setTypeface(radioButton.getTypeface(), parseTextStyle(attributeValue)); return true;
            case "textAlignment": radioButton.setTextAlignment(parseTextAlignment(attributeValue)); return true;

            case "checked": radioButton.setChecked(Boolean.parseBoolean(attributeValue)); return true;
            case "enabled": radioButton.setEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "clickable": radioButton.setClickable(Boolean.parseBoolean(attributeValue)); return true;
            case "focusable": radioButton.setFocusable(Boolean.parseBoolean(attributeValue)); return true;

            /* --- 按钮图标 & 着色 --- */
            case "buttonDrawable": setDrawable(radioButton, attributeValue, radioButton::setButtonDrawable); return true;
            case "buttonTint": try { radioButton.setButtonTintList(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid buttonTint"); } return true;

            /* --- 尺寸/边距 --- */
            case "padding": applyPadding(radioButton, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(radioButton, attributeName, attributeValue); return true;
            case "minWidth": radioButton.setMinimumWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "minHeight": radioButton.setMinimumHeight(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            default: return false;
        }
    }

    /**
     * 处理SwitchMaterial属性
     */
    public boolean processSwitchMaterialAttributes(SwitchMaterial switchMaterial, String attributeName, String attributeValue) {
        Context context = switchMaterial.getContext();

        switch (attributeName) {
            case "textOn":
                switchMaterial.setTextOn(attributeValue);
                return true;

            case "textOff":
                switchMaterial.setTextOff(attributeValue);
                return true;

            case "checked":
                switchMaterial.setChecked(Boolean.parseBoolean(attributeValue));
                return true;

            case "textSize":
                try {
                    float size = parseSpValue(attributeValue);
                    switchMaterial.setTextSize(size);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid textSize: " + attributeValue);
                }
                return true;

            case "textColor":
                try {
                    switchMaterial.setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid textColor: " + attributeValue);
                }
                return true;

            case "enabled":
                switchMaterial.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;

            default:
                return false;
        }
    }

    // 辅助方法：解析尺寸
    private float parseDimension(Context context, String value) throws NumberFormatException {
        if (value.endsWith("dp")) {
            return dpToPx(context, Float.parseFloat(value.replace("dp", "")));
        } else if (value.endsWith("sp")) {
            return spToPx(context, Float.parseFloat(value.replace("sp", "")));
        } else if (value.endsWith("px")) {
            return Float.parseFloat(value.replace("px", ""));
        }
        return Float.parseFloat(value);
    }

    // 辅助方法：解析SP值
    private float parseSpValue(String value) throws NumberFormatException {
        if (value.endsWith("sp")) {
            return Float.parseFloat(value.replace("sp", ""));
        }
        return Float.parseFloat(value);
    }


    // 辅助方法：解析图片缩放类型
    private ImageView.ScaleType parseScaleType(String scaleType) {
        switch (scaleType) {
            case "center": return ImageView.ScaleType.CENTER;
            case "centerCrop": return ImageView.ScaleType.CENTER_CROP;
            case "centerInside": return ImageView.ScaleType.CENTER_INSIDE;
            case "fitCenter": return ImageView.ScaleType.FIT_CENTER;
            case "fitEnd": return ImageView.ScaleType.FIT_END;
            case "fitStart": return ImageView.ScaleType.FIT_START;
            case "fitXY": return ImageView.ScaleType.FIT_XY;
            case "matrix": return ImageView.ScaleType.MATRIX;
            default: return ImageView.ScaleType.FIT_CENTER;
        }
    }

    // 辅助方法：解析输入类型
    private int parseInputType(String inputType) {
        int type = 0;
        String[] types = inputType.split("\\|");

        for (String t : types) {
            switch (t.trim()) {
                case "text": type |= android.text.InputType.TYPE_CLASS_TEXT; break;
                case "number": type |= android.text.InputType.TYPE_CLASS_NUMBER; break;
                case "phone": type |= android.text.InputType.TYPE_CLASS_PHONE; break;
                case "datetime": type |= android.text.InputType.TYPE_CLASS_DATETIME; break;
                case "email": type |= android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; break;
                case "password": type |= android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD; break;
                case "visiblePassword": type |= android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD; break;
                case "multiline": type |= android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE; break;
                case "autocorrect": type |= android.text.InputType.TYPE_TEXT_FLAG_AUTO_CORRECT; break;
                case "capitalizeWords": type |= android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS; break;
            }
        }

        return type;
    }

    // 辅助方法：解析输入法选项
    private int parseImeOptions(String imeOptions) {
        switch (imeOptions) {
            case "actionDone": return android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
            case "actionNext": return android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;
            case "actionGo": return android.view.inputmethod.EditorInfo.IME_ACTION_GO;
            case "actionSearch": return android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;
            case "actionSend": return android.view.inputmethod.EditorInfo.IME_ACTION_SEND;
            case "flagNoFullscreen": return android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN;
            case "flagNoExtractUi": return android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI;
            default: return 0;
        }
    }
    /**
     * 处理MaterialButton属性
     */
    protected boolean processMaterialButtonAttributes(MaterialButton materialButton, String attributeName, String attributeValue) {
        Context ctx = materialButton.getContext();

        switch (attributeName) {
            /* --- 文本 --- */
            case "text": materialButton.setText(attributeValue); return true;
            case "textSize": materialButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, parseFloatSafely(attributeValue, 14)); return true;
            case "textColor": try { materialButton.setTextColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid textColor"); } return true;
            case "textAllCaps": materialButton.setAllCaps(Boolean.parseBoolean(attributeValue)); return true;

            /* --- 背景 & 形状 --- */
            case "cornerRadius": materialButton.setCornerRadius(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "backgroundTint": try { materialButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid background tint"); } return true;
            case "backgroundTintMode": try { materialButton.setBackgroundTintMode(parsePorterDuffMode(attributeValue)); } catch (Exception e) { logWarning("Invalid tint mode"); } return true;

            /* --- 图标 --- */
            case "icon": try { materialButton.setIconResource(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid icon"); } return true;
            case "iconSize": materialButton.setIconSize(dpToPx(ctx, parseFloatSafely(attributeValue, 24))); return true;
            case "iconTint": try { materialButton.setIconTint(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid icon tint"); } return true;
            case "iconPadding": materialButton.setIconPadding(dpToPx(ctx, parseFloatSafely(attributeValue, 8))); return true;
            case "iconGravity": switch (attributeValue) {
                case "start": materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_START); return true;
                case "textStart": materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START); return true;
                case "end": materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_END); return true;
                case "textEnd": materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_END); return true;
                case "top": materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_TOP); return true;
                case "textTop": materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP); return true;
            } return true;

            /* --- 描边 --- */
            case "strokeWidth": materialButton.setStrokeWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "strokeColor": try { materialButton.setStrokeColor(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid stroke color"); } return true;

            /* --- 涟漪 / elevation --- */
            case "rippleColor": try { materialButton.setRippleColor(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid ripple color"); } return true;
            case "elevation": materialButton.setElevation(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "translationZ": materialButton.setTranslationZ(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            /* --- 状态 --- */
            case "enabled": materialButton.setEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "clickable": materialButton.setClickable(Boolean.parseBoolean(attributeValue)); return true;
            case "focusable": materialButton.setFocusable(Boolean.parseBoolean(attributeValue)); return true;
            case "checkable": materialButton.setCheckable(Boolean.parseBoolean(attributeValue)); return true;
            case "checked": materialButton.setChecked(Boolean.parseBoolean(attributeValue)); return true;

            /* --- 通用外观 --- */
            case "minWidth": materialButton.setMinimumWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "minHeight": materialButton.setMinimumHeight(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "padding": applyPadding(materialButton, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(materialButton, attributeName, attributeValue); return true;
            case "contentDescription": materialButton.setContentDescription(attributeValue); return true;

            /* --- 更多可补充 --- */
            default: return false;
        }
    }

    /**
     * 处理TextInputLayout属性
     */
    public boolean processTextInputLayoutAttributes(TextInputLayout textInputLayout, String attributeName, String attributeValue) {
        Context ctx = textInputLayout.getContext();

        switch (attributeName) {
            /* --- 文本 --- */
            case "hint": textInputLayout.setHint(attributeValue); return true;
            case "error": textInputLayout.setError(attributeValue); return true;
            case "errorEnabled": textInputLayout.setErrorEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "helperText": textInputLayout.setHelperText(attributeValue); return true;
            case "helperTextEnabled": textInputLayout.setHelperTextEnabled(Boolean.parseBoolean(attributeValue)); return true;

            /* --- 计数器 --- */
            case "counterEnabled": textInputLayout.setCounterEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "counterMaxLength": textInputLayout.setCounterMaxLength(parseIntSafely(attributeValue, 0)); return true;

            /* --- 提示开关 --- */
            case "hintEnabled": textInputLayout.setHintEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "hintAnimationEnabled": textInputLayout.setHintAnimationEnabled(Boolean.parseBoolean(attributeValue)); return true;

            /* --- 密码开关 --- */
            case "passwordToggleEnabled": textInputLayout.setPasswordVisibilityToggleEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "passwordToggleDrawable":
                try { textInputLayout.setPasswordVisibilityToggleDrawable(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid toggle drawable"); }
                return true;

            /* --- 边框 --- */
            case "boxBackgroundMode":
                switch (attributeValue) {
                    case "none": textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE); break;
                    case "outline": textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE); break;
                    case "filled": textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_FILLED); break;
                }
                return true;
            case "boxBackgroundColor":
                try { textInputLayout.setBoxBackgroundColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid box color"); }
                return true;
            case "boxStrokeColor":
                try { textInputLayout.setBoxStrokeColor(Color.parseColor(attributeValue)); } catch (Exception e) { logWarning("Invalid stroke color"); }
                return true;
            case "boxStrokeWidth":
                textInputLayout.setBoxStrokeWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;
            case "boxStrokeWidthFocused":
                textInputLayout.setBoxStrokeWidthFocused(dpToPx(ctx, parseFloatSafely(attributeValue, 0)));
                return true;

            /* --- 圆角 --- */
            case "boxCornerRadius":
                float r = dpToPx(ctx, parseFloatSafely(attributeValue, 0));
                textInputLayout.setBoxCornerRadii(r, r, r, r);
                return true;
            case "boxCornerRadiusTopStart": case "boxCornerRadiusTopEnd":
            case "boxCornerRadiusBottomStart": case "boxCornerRadiusBottomEnd":
                float r2 = dpToPx(ctx, parseFloatSafely(attributeValue, 0));
                float[] corners = new float[]{textInputLayout.getBoxCornerRadiusTopStart(), textInputLayout.getBoxCornerRadiusTopEnd(),
                        textInputLayout.getBoxCornerRadiusBottomStart(), textInputLayout.getBoxCornerRadiusBottomEnd()};
                switch (attributeName) {
                    case "boxCornerRadiusTopStart": corners[0] = r2; break;
                    case "boxCornerRadiusTopEnd": corners[1] = r2; break;
                    case "boxCornerRadiusBottomStart": corners[2] = r2; break;
                    case "boxCornerRadiusBottomEnd": corners[3] = r2; break;
                }
                textInputLayout.setBoxCornerRadii(corners[0], corners[1], corners[2], corners[3]);
                return true;

            /* --- 图标 --- */
            case "startIconDrawable":
                try { textInputLayout.setStartIconDrawable(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid start icon"); }
                return true;
            case "startIconContentDescription":
                textInputLayout.setStartIconContentDescription(attributeValue); return true;
            case "startIconTint":
                try { textInputLayout.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid start tint"); }
                return true;

            case "endIconDrawable":
                try { textInputLayout.setEndIconDrawable(Integer.parseInt(attributeValue)); } catch (Exception e) { logWarning("Invalid end icon"); }
                return true;
            case "endIconContentDescription":
                textInputLayout.setEndIconContentDescription(attributeValue); return true;
            case "endIconTint":
                try { textInputLayout.setEndIconTintList(ColorStateList.valueOf(Color.parseColor(attributeValue))); } catch (Exception e) { logWarning("Invalid end tint"); }
                return true;
            case "endIconMode":
                switch (attributeValue) {
                    case "password_toggle": textInputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE); break;
                    case "clear_text": textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT); break;
                    case "custom": textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM); break;
                    case "none": default: textInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE); break;
                }
                return true;

            /* --- 通用外观 --- */
            case "enabled": textInputLayout.setEnabled(Boolean.parseBoolean(attributeValue)); return true;
            case "padding": applyPadding(textInputLayout, attributeValue); return true;
            case "paddingLeft": case "paddingTop": case "paddingRight": case "paddingBottom":
                applySinglePadding(textInputLayout, attributeName, attributeValue); return true;
            case "minWidth": textInputLayout.setMinimumWidth(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;
            case "minHeight": textInputLayout.setMinimumHeight(dpToPx(ctx, parseFloatSafely(attributeValue, 0))); return true;

            default: return false;
        }
    }


    protected float parseFloatSafely(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void logWarning(String message) {
        Log.w(TAG, message);
    }
}
