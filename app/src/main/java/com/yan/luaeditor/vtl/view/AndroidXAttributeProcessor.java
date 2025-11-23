package com.yan.luaeditor.vtl.view;

import static com.yan.luaeditor.vtl.view.AttributeUtils.dpToPx;
import static com.yan.luaeditor.vtl.view.AttributeUtils.parseIntSafely;
import static com.yan.luaeditor.vtl.view.AttributeUtils.spToPx;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BasePageAdapter;
import android.widget.LuaBasePagerAdapter;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * AndroidX控件属性处理器，处理AndroidX库中的所有控件
 */
public class AndroidXAttributeProcessor implements ViewAttributeProcessor, AttributeConstants {
    private static final String TAG = "AttrProcessor";
    private final BaseViewAttributeProcessor baseProcessor = new BaseViewAttributeProcessor();

    @Override
    public boolean processAttribute(View view, String attributeName, String attributeValue) {
        // 先尝试用基础处理器处理通用属性
        if (baseProcessor.processAttribute(view, attributeName, attributeValue)) {
            return true;
        }

        // 处理AndroidX特有控件
        if (view instanceof RecyclerView) {
            return processRecyclerViewAttributes((RecyclerView) view, attributeName, attributeValue);
        } else if (view instanceof ViewPager2) {
            return processViewPager2Attributes((ViewPager2) view, attributeName, attributeValue);
        } else if (view instanceof Toolbar) {
            return processToolbarAttributes((Toolbar) view, attributeName, attributeValue);
        } else if (view instanceof NestedScrollView) {
            return processNestedScrollViewAttributes((NestedScrollView) view, attributeName, attributeValue);
        } else if (view instanceof SearchView) {
            return processSearchViewAttributes((SearchView) view, attributeName, attributeValue);
        } else if (view instanceof CardView) {
            return processCardViewAttributes((CardView) view, attributeName, attributeValue);
        } else if (view instanceof TextInputLayout) {
            return processTextInputLayoutAttributes((TextInputLayout) view, attributeName, attributeValue);
        } else if (view instanceof ViewPager) {
            return processViewPagerAttributes((ViewPager) view, attributeName, attributeValue);
        } else if (view instanceof DrawerLayout) {
            return processDrawerLayoutAttributes((DrawerLayout) view, attributeName, attributeValue);
        } else if (view instanceof NavigationView) {
            return processNavigationViewAttributes((NavigationView) view, attributeName, attributeValue);
        } else if (view instanceof CoordinatorLayout) {
            return processCoordinatorLayoutAttributes((CoordinatorLayout) view, attributeName, attributeValue);
        } else if (view instanceof AppBarLayout) {
            return processAppBarLayoutAttributes((AppBarLayout) view, attributeName, attributeValue);
        } else if (view instanceof CollapsingToolbarLayout) {
            return processCollapsingToolbarLayoutAttributes((CollapsingToolbarLayout) view, attributeName, attributeValue);
        } else if (view instanceof TabLayout) {
            return processTabLayoutAttributes((TabLayout) view, attributeName, attributeValue);
        } else if (view instanceof MaterialButton) {
            return processMaterialButtonAttributes((MaterialButton) view, attributeName, attributeValue);
        }

        return false;
    }

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




    private int parseGravity(String gravityStr) {
        int gravity = 0;
        String[] parts = gravityStr.split("\\|");
        for (String part : parts) {
            switch (part.trim()) {
                case "top":
                    gravity |= Gravity.TOP;
                    break;
                case "bottom":
                    gravity |= Gravity.BOTTOM;
                    break;
                case "left":
                    gravity |= Gravity.LEFT;
                    break;
                case "right":
                    gravity |= Gravity.RIGHT;
                    break;
                case "center":
                    gravity |= Gravity.CENTER;
                    break;
                case "center_vertical":
                    gravity |= Gravity.CENTER_VERTICAL;
                    break;
                case "center_horizontal":
                    gravity |= Gravity.CENTER_HORIZONTAL;
                    break;
            }
        }
        return gravity;
    }

    public void logWarning(String message) {
        Log.w(TAG, message);
    }

    /**
     * 处理Toolbar属性
     */
    public boolean processToolbarAttributes(Toolbar toolbar, String attributeName, String attributeValue) {
        Context context = toolbar.getContext();

        switch (attributeName) {
            case "title":
                toolbar.setTitle(attributeValue);
                return true;
            case "subtitle":
                toolbar.setSubtitle(attributeValue);
                return true;
            case "titleTextColor":
                try {
                    toolbar.setTitleTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid titleTextColor: " + attributeValue);
                }
                return true;
            case "subtitleTextColor":
                try {
                    toolbar.setSubtitleTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid subtitleTextColor: " + attributeValue);
                }
                return true;
            case "navigationIcon":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    toolbar.setNavigationIcon(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid navigationIcon: " + attributeValue);
                }
                return true;
            case "logo":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    toolbar.setLogo(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid logo: " + attributeValue);
                }
                return true;
            case "menu":
                try {
                    int menuRes = Integer.parseInt(attributeValue);
                    toolbar.inflateMenu(menuRes);
                } catch (NumberFormatException e) {
                    logWarning("Invalid menu resource: " + attributeValue);
                }
                return true;
            case "elevation":
                try {
                    float elevation = parseDimension(context, attributeValue);
                    toolbar.setElevation(elevation);
                } catch (NumberFormatException e) {
                    logWarning("Invalid elevation: " + attributeValue);
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理NestedScrollView属性
     */
    public boolean processNestedScrollViewAttributes(NestedScrollView nestedScrollView, String attributeName, String attributeValue) {
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
                    default:
                        logWarning("Invalid scrollbars value: " + attributeValue);
                        return false;
                }
                return true;
            case "smoothScrollingEnabled":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    nestedScrollView.setSmoothScrollingEnabled(Boolean.parseBoolean(attributeValue));
                } else {
                    logWarning("smoothScrollingEnabled requires API level 23+");
                }
                return true;
            case "isNestedScrollingEnabled":
                nestedScrollView.setNestedScrollingEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "overScrollMode":
                switch (attributeValue) {
                    case "always":
                        nestedScrollView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
                        break;
                    case "ifContentScrolls":
                        nestedScrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
                        break;
                    case "never":
                        nestedScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                        break;
                    default:
                        logWarning("Invalid overScrollMode value: " + attributeValue);
                        return false;
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理SearchView属性
     */
    public boolean processSearchViewAttributes(SearchView searchView, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "queryHint":
                searchView.setQueryHint(attributeValue);
                return true;
            case "query":
                searchView.setQuery(attributeValue, false);
                return true;
            case "iconified":
            case "isIconified":
                searchView.setIconified(Boolean.parseBoolean(attributeValue));
                return true;
            case "iconifiedByDefault":
                searchView.setIconifiedByDefault(Boolean.parseBoolean(attributeValue));
                return true;
            case "submitButtonEnabled":
            case "isSubmitButtonEnabled":
                searchView.setSubmitButtonEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "queryRefinementEnabled":
            case "isQueryRefinementEnabled":
                searchView.setQueryRefinementEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "maxWidth":
                try {
                    searchView.setMaxWidth(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    logWarning("Invalid maxWidth: " + attributeValue);
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理CardView属性
     */
    public boolean processCardViewAttributes(CardView cardView, String attributeName, String attributeValue) {
        Context context = cardView.getContext();

        switch (attributeName) {
            case "cardBackgroundColor":
                try {
                    cardView.setCardBackgroundColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid cardBackgroundColor: " + attributeValue);
                }
                return true;
            case "cardElevation":
                try {
                    float elevation = parseDimension(context, attributeValue);
                    cardView.setCardElevation(elevation);
                } catch (NumberFormatException e) {
                    logWarning("Invalid cardElevation: " + attributeValue);
                }
                return true;
            case "maxCardElevation":
                try {
                    float maxElevation = parseDimension(context, attributeValue);
                    cardView.setMaxCardElevation(maxElevation);
                } catch (NumberFormatException e) {
                    logWarning("Invalid maxCardElevation: " + attributeValue);
                }
                return true;
            case "radius":
                try {
                    float radius = parseDimension(context, attributeValue);
                    cardView.setRadius(radius);
                } catch (NumberFormatException e) {
                    logWarning("Invalid radius: " + attributeValue);
                }
                return true;
            case "contentPadding":
                String[] paddingValues = attributeValue.replaceAll("dp","").split(",");
                if (paddingValues.length == 4) {
                    try {
                        int left = dpToPx(context, Float.parseFloat(paddingValues[0]));
                        int top = dpToPx(context, Float.parseFloat(paddingValues[1]));
                        int right = dpToPx(context, Float.parseFloat(paddingValues[2]));
                        int bottom = dpToPx(context, Float.parseFloat(paddingValues[3]));
                        cardView.setContentPadding(left, top, right, bottom);
                    } catch (NumberFormatException e) {
                        logWarning("Invalid contentPadding: " + attributeValue);
                    }
                } else {
                    logWarning("Invalid contentPadding format. Expected 4 values: " + attributeValue);
                }
                return true;
            case "useCompatPadding":
            case "cardUseCompatPadding":
                cardView.setUseCompatPadding(Boolean.parseBoolean(attributeValue));
                return true;
            case "preventCornerOverlap":
            case "cardPreventCornerOverlap":
                cardView.setPreventCornerOverlap(Boolean.parseBoolean(attributeValue));
                return true;
            default:
                return false;
        }
    }

    public boolean processTextInputLayoutAttributes(TextInputLayout textInputLayout, String attributeName, String attributeValue) {
        Context context = textInputLayout.getContext();

        switch (attributeName) {
            case "hint":
                textInputLayout.setHint(attributeValue);
                return true;
            case "error":
                textInputLayout.setError(attributeValue);
                return true;
            case "errorEnabled":
                textInputLayout.setErrorEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "helperText":
                textInputLayout.setHelperText(attributeValue);
                return true;
            case "helperTextEnabled":
                textInputLayout.setHelperTextEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "passwordToggleEnabled":
                textInputLayout.setPasswordVisibilityToggleEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "passwordToggleDrawable":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    textInputLayout.setPasswordVisibilityToggleDrawable(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid passwordToggleDrawable: " + attributeValue);
                }
                return true;
            case "counterEnabled":
                textInputLayout.setCounterEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "counterMaxLength":
                try {
                    textInputLayout.setCounterMaxLength(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    logWarning("Invalid counterMaxLength: " + attributeValue);
                }
                return true;
            case "boxBackgroundMode":
                switch (attributeValue) {
                    case "none":
                        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
                        break;
                    case "outline":
                        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                        break;
                    case "filled":
                        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_FILLED);
                        break;
                    default:
                        logWarning("Invalid boxBackgroundMode: " + attributeValue);
                }
                return true;
            case "boxStrokeColor":
                try {
                    int color = Color.parseColor(attributeValue);
                    textInputLayout.setBoxStrokeColor(color);
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid boxStrokeColor: " + attributeValue);
                }
                return true;
            case "boxStrokeWidth":
                try {
                    int width = dpToPx(context, Float.parseFloat(attributeValue));
                    textInputLayout.setBoxStrokeWidth(width);
                } catch (NumberFormatException e) {
                    logWarning("Invalid boxStrokeWidth: " + attributeValue);
                }
                return true;
            case "boxStrokeWidthFocused":
                try {
                    int width = dpToPx(context, Float.parseFloat(attributeValue));
                    textInputLayout.setBoxStrokeWidthFocused(width);
                } catch (NumberFormatException e) {
                    logWarning("Invalid boxStrokeWidthFocused: " + attributeValue);
                }
                return true;
            case "endIconMode":
                switch (attributeValue) {
                    case "none":
                        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                        break;
                    case "custom":
                        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                        break;
                    case "password_toggle":
                        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                        break;
                    case "clear_text":
                        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
                        break;
                    default:
                        logWarning("Invalid endIconMode: " + attributeValue);
                }
                return true;
            case "endIconDrawable":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    textInputLayout.setEndIconDrawable(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid endIconDrawable: " + attributeValue);
                }
                return true;
            case "endIconContentDescription":
                textInputLayout.setEndIconContentDescription(attributeValue);
                return true;
            case "startIconDrawable":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    textInputLayout.setStartIconDrawable(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid startIconDrawable: " + attributeValue);
                }
                return true;
            case "startIconContentDescription":
                textInputLayout.setStartIconContentDescription(attributeValue);
                return true;
            case "hintEnabled":
                textInputLayout.setHintEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "hintAnimationEnabled":
                textInputLayout.setHintAnimationEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理ViewPager属性
     */
    public boolean processViewPagerAttributes(ViewPager viewPager, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "currentItem":
                try {
                    viewPager.setCurrentItem(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    logWarning("Invalid currentItem: " + attributeValue);
                }
                return true;
            case "offscreenPageLimit":
                try {
                    viewPager.setOffscreenPageLimit(Integer.parseInt(attributeValue));
                } catch (NumberFormatException e) {
                    logWarning("Invalid offscreenPageLimit: " + attributeValue);
                }
                return true;
            case "isScrollContainer":
                viewPager.setScrollContainer(Boolean.parseBoolean(attributeValue));
                return true;
            case "pageMargin":
                try {
                    int margin = Integer.parseInt(attributeValue);
                    viewPager.setPageMargin(margin);
                } catch (NumberFormatException e) {
                    logWarning("Invalid pageMargin: " + attributeValue);
                }
                return true;
            case "pageMarginDrawable":
            case "pageMarginDrawableResource":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    viewPager.setPageMarginDrawable(ContextCompat.getDrawable(viewPager.getContext(), resId));
                } catch (NumberFormatException e) {
                    logWarning("Invalid pageMarginDrawable: " + attributeValue);
                }
                return true;
            default:
                return false;
        }
    }

    public boolean processDrawerLayoutAttributes(DrawerLayout drawerLayout, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "drawerElevation":
                try {
                    float elevation = Float.parseFloat(attributeValue);
                    drawerLayout.setDrawerElevation(elevation);
                } catch (NumberFormatException e) {
                    logWarning("Invalid drawerElevation: " + attributeValue);
                }
                return true;
            case "scrimColor":
                try {
                    drawerLayout.setScrimColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid scrimColor: " + attributeValue);
                }
                return true;
            case "drawerLockMode":
                int lockMode = DrawerLayout.LOCK_MODE_UNLOCKED;
                switch (attributeValue) {
                    case "locked_closed":
                        lockMode = DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
                        break;
                    case "locked_open":
                        lockMode = DrawerLayout.LOCK_MODE_LOCKED_OPEN;
                        break;
                    case "unlocked":
                        lockMode = DrawerLayout.LOCK_MODE_UNLOCKED;
                        break;
                }
                drawerLayout.setDrawerLockMode(lockMode);
                return true;
            case "statusBarBackground":
                try {
                    if (attributeValue.startsWith("#")) {
                        drawerLayout.setStatusBarBackground(new ColorDrawable(Color.parseColor(attributeValue)));
                    } else {
                        int resId = Integer.parseInt(attributeValue);
                        drawerLayout.setStatusBarBackground(ContextCompat.getDrawable(drawerLayout.getContext(), resId));
                    }
                } catch (Exception e) {
                    logWarning("Invalid statusBarBackground: " + attributeValue);
                }
                return true;
            case "fitsSystemWindows":
                drawerLayout.setFitsSystemWindows(Boolean.parseBoolean(attributeValue));
                return true;
            case "clipToPadding":
                drawerLayout.setClipToPadding(Boolean.parseBoolean(attributeValue));
                return true;
            case "clipChildren":
                drawerLayout.setClipChildren(Boolean.parseBoolean(attributeValue));
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理NavigationView属性
     */
    public boolean processNavigationViewAttributes(NavigationView navigationView, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "menu":
                try {
                    int menuRes = Integer.parseInt(attributeValue);
                    navigationView.inflateMenu(menuRes);
                } catch (NumberFormatException e) {
                    logWarning("Invalid menu resource: " + attributeValue);
                }
                return true;
            case "headerLayout":
                try {
                    int layoutRes = Integer.parseInt(attributeValue);
                    navigationView.inflateHeaderView(layoutRes);
                } catch (NumberFormatException e) {
                    logWarning("Invalid headerLayout: " + attributeValue);
                }
                return true;
            case "itemBackground":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    navigationView.setItemBackgroundResource(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid itemBackground: " + attributeValue);
                }
                return true;
            case "itemIconTint":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    navigationView.setItemIconTintList(ContextCompat.getColorStateList(navigationView.getContext(), resId));
                } catch (NumberFormatException e) {
                    logWarning("Invalid itemIconTint: " + attributeValue);
                }
                return true;
            case "itemTextColor":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    navigationView.setItemTextColor(ContextCompat.getColorStateList(navigationView.getContext(), resId));
                } catch (NumberFormatException e) {
                    logWarning("Invalid itemTextColor: " + attributeValue);
                }
                return true;
            case "itemHorizontalPadding":
                try {
                    int padding = dpToPx(navigationView.getContext(), Float.parseFloat(attributeValue));
                    navigationView.setItemHorizontalPadding(padding);
                } catch (NumberFormatException e) {
                    logWarning("Invalid itemHorizontalPadding: " + attributeValue);
                }
                return true;
            case "itemVerticalPadding":
                try {
                    int padding = dpToPx(navigationView.getContext(), Float.parseFloat(attributeValue));
                    navigationView.setItemVerticalPadding(padding);
                } catch (NumberFormatException e) {
                    logWarning("Invalid itemVerticalPadding: " + attributeValue);
                }
                return true;
            case "elevation":
                try {
                    float elevation = dpToPx(navigationView.getContext(), Float.parseFloat(attributeValue));
                    navigationView.setElevation(elevation);
                } catch (NumberFormatException e) {
                    logWarning("Invalid elevation: " + attributeValue);
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理CoordinatorLayout属性
     */
    public boolean processCoordinatorLayoutAttributes(CoordinatorLayout coordinatorLayout, String attributeName, String attributeValue) {
        Context context = coordinatorLayout.getContext();

        switch (attributeName) {
            case "statusBarBackground":
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (attributeValue.startsWith("#")) {
                            Drawable colorDrawable = new ColorDrawable(Color.parseColor(attributeValue));
                            coordinatorLayout.setStatusBarBackground(colorDrawable);
                        } else {
                            int resId = Integer.parseInt(attributeValue);
                            Drawable drawable = ContextCompat.getDrawable(context, resId);
                            coordinatorLayout.setStatusBarBackground(drawable);
                        }
                    }
                } catch (Exception e) {
                    logWarning("Invalid statusBarBackground: " + attributeValue);
                }
                return true;
            case "fitsSystemWindows":
                coordinatorLayout.setFitsSystemWindows(Boolean.parseBoolean(attributeValue));
                return true;
            case "clipToPadding":
                coordinatorLayout.setClipToPadding(Boolean.parseBoolean(attributeValue));
                return true;
            case "clipChildren":
                coordinatorLayout.setClipChildren(Boolean.parseBoolean(attributeValue));
                return true;
            case "background":
                try {
                    if (attributeValue.startsWith("#")) {
                        Drawable colorDrawable = new ColorDrawable(Color.parseColor(attributeValue));
                        coordinatorLayout.setBackground(colorDrawable);
                    } else {
                        int resId = Integer.parseInt(attributeValue);
                        Drawable drawable = ContextCompat.getDrawable(context, resId);
                        coordinatorLayout.setBackground(drawable);
                    }
                } catch (Exception e) {
                    logWarning("Invalid background: " + attributeValue);
                }
                return true;
            case "padding":
                try {
                    int padding = dpToPx(context, Float.parseFloat(attributeValue));
                    coordinatorLayout.setPadding(padding, padding, padding, padding);
                } catch (NumberFormatException e) {
                    logWarning("Invalid padding: " + attributeValue);
                }
                return true;
            case "paddingStart":
                try {
                    int paddingStart = dpToPx(context, Float.parseFloat(attributeValue));
                    coordinatorLayout.setPaddingRelative(paddingStart, coordinatorLayout.getPaddingTop(), coordinatorLayout.getPaddingEnd(), coordinatorLayout.getPaddingBottom());
                } catch (NumberFormatException e) {
                    logWarning("Invalid paddingStart: " + attributeValue);
                }
                return true;
            case "paddingTop":
                try {
                    int paddingTop = dpToPx(context, Float.parseFloat(attributeValue));
                    coordinatorLayout.setPadding(coordinatorLayout.getPaddingLeft(), paddingTop, coordinatorLayout.getPaddingRight(), coordinatorLayout.getPaddingBottom());
                } catch (NumberFormatException e) {
                    logWarning("Invalid paddingTop: " + attributeValue);
                }
                return true;
            case "paddingEnd":
                try {
                    int paddingEnd = dpToPx(context, Float.parseFloat(attributeValue));
                    coordinatorLayout.setPaddingRelative(coordinatorLayout.getPaddingStart(), coordinatorLayout.getPaddingTop(), paddingEnd, coordinatorLayout.getPaddingBottom());
                } catch (NumberFormatException e) {
                    logWarning("Invalid paddingEnd: " + attributeValue);
                }
                return true;
            case "paddingBottom":
                try {
                    int paddingBottom = dpToPx(context, Float.parseFloat(attributeValue));
                    coordinatorLayout.setPadding(coordinatorLayout.getPaddingLeft(), coordinatorLayout.getPaddingTop(), coordinatorLayout.getPaddingRight(), paddingBottom);
                } catch (NumberFormatException e) {
                    logWarning("Invalid paddingBottom: " + attributeValue);
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理AppBarLayout属性
     */
    public boolean processAppBarLayoutAttributes(AppBarLayout appBarLayout, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "elevation":
                try {
                    float elevation = Float.parseFloat(attributeValue);
                    appBarLayout.setElevation(elevation);
                } catch (NumberFormatException e) {
                    logWarning("Invalid elevation: " + attributeValue);
                }
                return true;
            case "expanded":
                appBarLayout.setExpanded(Boolean.parseBoolean(attributeValue));
                return true;
            case "liftOnScroll":
                appBarLayout.setLiftOnScroll(Boolean.parseBoolean(attributeValue));
                return true;
            case "liftOnScrollTargetViewId":
                try {
                    int targetViewId = Integer.parseInt(attributeValue);
                    appBarLayout.setLiftOnScrollTargetViewId(targetViewId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid liftOnScrollTargetViewId: " + attributeValue);
                }
                return true;
            case "scrollIndicators":
                int indicators = 0;
                String[] parts = attributeValue.split("\\|");
                for (String part : parts) {
                    switch (part.trim()) {
                        case "top":
                            indicators |= AppBarLayout.SCROLL_INDICATOR_TOP;
                            break;
                        case "bottom":
                            indicators |= AppBarLayout.SCROLL_INDICATOR_BOTTOM;
                            break;
                    }
                }
                appBarLayout.setScrollIndicators(indicators);
                return true;
            case "statusBarForeground":
                try {
                    int colorResId = Integer.parseInt(attributeValue);
                    appBarLayout.setStatusBarForeground(ContextCompat.getDrawable(appBarLayout.getContext(), colorResId));
                } catch (NumberFormatException e) {
                    logWarning("Invalid statusBarForeground: " + attributeValue);
                }
                return true;
            case "background":
                try {
                    int backgroundResId = Integer.parseInt(attributeValue);
                    appBarLayout.setBackgroundResource(backgroundResId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid background: " + attributeValue);
                }
                return true;
            case "clipToPadding":
                appBarLayout.setClipToPadding(Boolean.parseBoolean(attributeValue));
                return true;
            case "clipChildren":
                appBarLayout.setClipChildren(Boolean.parseBoolean(attributeValue));
                return true;
            case "fitsSystemWindows":
                appBarLayout.setFitsSystemWindows(Boolean.parseBoolean(attributeValue));
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理CollapsingToolbarLayout属性
     */
    public boolean processCollapsingToolbarLayoutAttributes(CollapsingToolbarLayout collapsingToolbar,
                                                            String attributeName, String attributeValue) {
        Context context = collapsingToolbar.getContext();

        switch (attributeName) {
            case "title":
                collapsingToolbar.setTitle(attributeValue);
                return true;
            case "expandedTitleGravity":
                try {
                    int gravity = parseGravity(attributeValue);
                    collapsingToolbar.setExpandedTitleGravity(gravity);
                } catch (Exception e) {
                    logWarning("Invalid expandedTitleGravity: " + attributeValue);
                }
                return true;
            case "collapsedTitleGravity":
                try {
                    int gravity = parseGravity(attributeValue);
                    collapsingToolbar.setCollapsedTitleGravity(gravity);
                } catch (Exception e) {
                    logWarning("Invalid collapsedTitleGravity: " + attributeValue);
                }
                return true;
            case "expandedTitleTextAppearance":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    collapsingToolbar.setExpandedTitleTextAppearance(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid expandedTitleTextAppearance: " + attributeValue);
                }
                return true;
            case "collapsedTitleTextAppearance":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    collapsingToolbar.setCollapsedTitleTextAppearance(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid collapsedTitleTextAppearance: " + attributeValue);
                }
                return true;
            case "contentScrim":
                try {
                    if (attributeValue.startsWith("#")) {
                        collapsingToolbar.setContentScrim(new ColorDrawable(Color.parseColor(attributeValue)));
                    } else {
                        int resId = Integer.parseInt(attributeValue);
                        collapsingToolbar.setContentScrim(ContextCompat.getDrawable(context, resId));
                    }
                } catch (Exception e) {
                    logWarning("Invalid contentScrim: " + attributeValue);
                }
                return true;
            case "statusBarScrim":
                try {
                    if (attributeValue.startsWith("#")) {
                        collapsingToolbar.setStatusBarScrim(new ColorDrawable(Color.parseColor(attributeValue)));
                    } else {
                        int resId = Integer.parseInt(attributeValue);
                        collapsingToolbar.setStatusBarScrim(ContextCompat.getDrawable(context, resId));
                    }
                } catch (Exception e) {
                    logWarning("Invalid statusBarScrim: " + attributeValue);
                }
                return true;
            case "layout_scrollFlags":
                int flags = 0;
                String[] parts = attributeValue.split("\\|");
                for (String part : parts) {
                    switch (part.trim()) {
                        case "scroll":
                            flags |= AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
                            break;
                        case "exitUntilCollapsed":
                            flags |= AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED;
                            break;
                        case "enterAlways":
                            flags |= AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
                            break;
                        case "enterAlwaysCollapsed":
                            flags |= AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
                            break;
                        case "snap":
                            flags |= AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP;
                            break;
                    }
                }
                ViewGroup.LayoutParams params = collapsingToolbar.getLayoutParams();
                if (params instanceof AppBarLayout.LayoutParams) {
                    ((AppBarLayout.LayoutParams) params).setScrollFlags(flags);
                    collapsingToolbar.setLayoutParams(params);
                }
                return true;
            case "expandedTitleMargin":
                try {
                    int[] margins = Arrays.stream(attributeValue.split(","))
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    if (margins.length == 4) {
                        collapsingToolbar.setExpandedTitleMargin(margins[0], margins[1], margins[2], margins[3]);
                    } else {
                        logWarning("Invalid expandedTitleMargin format. Expected 4 values: " + attributeValue);
                    }
                } catch (NumberFormatException e) {
                    logWarning("Invalid expandedTitleMargin: " + attributeValue);
                }
                return true;
            case "expandedTitleMarginStart":
                try {
                    int marginStart = Integer.parseInt(attributeValue);
                    collapsingToolbar.setExpandedTitleMarginStart(marginStart);
                } catch (NumberFormatException e) {
                    logWarning("Invalid expandedTitleMarginStart: " + attributeValue);
                }
                return true;
            case "expandedTitleMarginEnd":
                try {
                    int marginEnd = Integer.parseInt(attributeValue);
                    collapsingToolbar.setExpandedTitleMarginEnd(marginEnd);
                } catch (NumberFormatException e) {
                    logWarning("Invalid expandedTitleMarginEnd: " + attributeValue);
                }
                return true;
            case "expandedTitleMarginTop":
                try {
                    int marginTop = Integer.parseInt(attributeValue);
                    collapsingToolbar.setExpandedTitleMarginTop(marginTop);
                } catch (NumberFormatException e) {
                    logWarning("Invalid expandedTitleMarginTop: " + attributeValue);
                }
                return true;
            case "expandedTitleMarginBottom":
                try {
                    int marginBottom = Integer.parseInt(attributeValue);
                    collapsingToolbar.setExpandedTitleMarginBottom(marginBottom);
                } catch (NumberFormatException e) {
                    logWarning("Invalid expandedTitleMarginBottom: " + attributeValue);
                }
                return true;
            case "expandedTitleColor":
                try {
                    int color = Color.parseColor(attributeValue);
                    collapsingToolbar.setExpandedTitleColor(color);
                } catch (Exception e) {
                    logWarning("Invalid expandedTitleColor: " + attributeValue);
                }
                return true;
            case "collapsedTitleColor":
                try {
                    int color = Color.parseColor(attributeValue);
                    collapsingToolbar.setCollapsedTitleTextColor(color);
                } catch (Exception e) {
                    logWarning("Invalid collapsedTitleColor: " + attributeValue);
                }
                return true;
            case "background":
                try {
                    int backgroundResId = Integer.parseInt(attributeValue);
                    collapsingToolbar.setBackgroundResource(backgroundResId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid background: " + attributeValue);
                }
                return true;
            case "clipToPadding":
                collapsingToolbar.setClipToPadding(Boolean.parseBoolean(attributeValue));
                return true;
            case "clipChildren":
                collapsingToolbar.setClipChildren(Boolean.parseBoolean(attributeValue));
                return true;
            case "fitsSystemWindows":
                collapsingToolbar.setFitsSystemWindows(Boolean.parseBoolean(attributeValue));
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
                } else if ("fixed".equals(attributeValue)) {
                    tabLayout.setTabMode(TabLayout.MODE_FIXED);
                } else {
                    logWarning("Invalid tabMode: " + attributeValue);
                    return false;
                }
                return true;
            case "tabGravity":
                if ("center".equals(attributeValue)) {
                    tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
                } else if ("fill".equals(attributeValue)) {
                    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                } else {
                    logWarning("Invalid tabGravity: " + attributeValue);
                    return false;
                }
                return true;
            case "tabIndicatorColor":
                try {
                    int color = Color.parseColor(attributeValue);
                    tabLayout.setSelectedTabIndicatorColor(color);
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid tabIndicatorColor: " + attributeValue);
                }
                return true;
            case "tabIndicatorHeight":
                try {
                    int height = dpToPx(context, Float.parseFloat(attributeValue));
                    tabLayout.setSelectedTabIndicatorHeight(height);
                } catch (NumberFormatException e) {
                    logWarning("Invalid tabIndicatorHeight: " + attributeValue);
                }
                return true;
            case "tabTextColor":
                try {
                    int color = Color.parseColor(attributeValue);
                    tabLayout.setTabTextColors(color, color);
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid tabTextColor: " + attributeValue);
                }
                return true;
            case "tabSelectedTextColor":
                try {
                    int selectedColor = Color.parseColor(attributeValue);
                    tabLayout.setTabTextColors(tabLayout.getTabTextColors().getDefaultColor(), selectedColor);
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid tabSelectedTextColor: " + attributeValue);
                }
                return true;
            case "addTab":
                tabLayout.addTab(tabLayout.newTab().setText(attributeValue));
                return true;
            case "setupWithViewPager":
                try {
                    ViewPager viewPager = tabLayout.getRootView().findViewById(Integer.parseInt(attributeValue));
                    if (viewPager != null) {
                        tabLayout.setupWithViewPager(viewPager);
                    }
                } catch (Exception e) {
                    logWarning("Error setting up with ViewPager: " + e.getMessage());
                }
                return true;
            case "tabInlineLabel":
                tabLayout.setInlineLabel(Boolean.parseBoolean(attributeValue));
                return true;
            case "tabUnboundedRipple":
                tabLayout.setUnboundedRipple(Boolean.parseBoolean(attributeValue));
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理MaterialButton属性
     */
    public boolean processMaterialButtonAttributes(MaterialButton materialButton, String attributeName, String attributeValue) {
        Context context = materialButton.getContext();

        switch (attributeName) {
            case "text":
                materialButton.setText(attributeValue);
                return true;
            case "textSize":
                try {
                    float size = Float.parseFloat(attributeValue.replace("sp", ""));
                    materialButton.setTextSize(size);
                } catch (NumberFormatException e) {
                    logWarning("Invalid textSize: " + attributeValue);
                }
                return true;
            case "textColor":
                try {
                    materialButton.setTextColor(Color.parseColor(attributeValue));
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid textColor: " + attributeValue);
                }
                return true;
            case "cornerRadius":
                try {
                    float radius = parseDimension(context, attributeValue);
                    materialButton.setCornerRadius((int) radius);
                } catch (NumberFormatException e) {
                    logWarning("Invalid cornerRadius: " + attributeValue);
                }
                return true;
            case "icon":
                try {
                    int resId = Integer.parseInt(attributeValue);
                    materialButton.setIconResource(resId);
                } catch (NumberFormatException e) {
                    logWarning("Invalid icon resource: " + attributeValue);
                }
                return true;
            case "iconSize":
                try {
                    int size = dpToPx(context, Float.parseFloat(attributeValue));
                    materialButton.setIconSize(size);
                } catch (NumberFormatException e) {
                    logWarning("Invalid iconSize: " + attributeValue);
                }
                return true;
            case "strokeWidth":
                try {
                    int width = dpToPx(context, Float.parseFloat(attributeValue));
                    materialButton.setStrokeWidth(width);
                } catch (NumberFormatException e) {
                    logWarning("Invalid strokeWidth: " + attributeValue);
                }
                return true;
            case "strokeColor":
                try {
                    int color = Color.parseColor(attributeValue);
                    materialButton.setStrokeColor(ColorStateList.valueOf(color));
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid strokeColor: " + attributeValue);
                }
                return true;
            case "rippleColor":
                try {
                    int color = Color.parseColor(attributeValue);
                    materialButton.setRippleColor(ColorStateList.valueOf(color));
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid rippleColor: " + attributeValue);
                }
                return true;
            case "backgroundTint":
                try {
                    int color = Color.parseColor(attributeValue);
                    materialButton.setBackgroundTintList(ColorStateList.valueOf(color));
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid backgroundTint: " + attributeValue);
                }
                return true;
            case "textAllCaps":
                materialButton.setAllCaps(Boolean.parseBoolean(attributeValue));
                return true;
            case "enabled":
                materialButton.setEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理RecyclerView属性
     */
    protected boolean processRecyclerViewAttributes(RecyclerView recyclerView, String attributeName, String attributeValue) {
        Context context = recyclerView.getContext();

        switch (attributeName) {
            case "layoutManager":
                setupRecyclerViewLayoutManager(recyclerView, attributeValue);
                return true;
            case "spanCount":
                updateRecyclerViewSpanCount(recyclerView, attributeValue);
                return true;
            case "hasFixedSize":
                recyclerView.setHasFixedSize(Boolean.parseBoolean(attributeValue));
                return true;
            case "itemAnimator":
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                return true;
            case "divider":
                setupRecyclerViewDivider(recyclerView, attributeValue);
                return true;
            case "reverseLayout":
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                if (lm instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) lm).setReverseLayout(Boolean.parseBoolean(attributeValue));
                } else if (lm instanceof StaggeredGridLayoutManager) {
                    ((StaggeredGridLayoutManager) lm).setReverseLayout(Boolean.parseBoolean(attributeValue));
                }
                return true;
            case "stackFromEnd":
                lm = recyclerView.getLayoutManager();
                if (lm instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) lm).setStackFromEnd(Boolean.parseBoolean(attributeValue));
                }
                return true;
            case "nestedScrollingEnabled":
                recyclerView.setNestedScrollingEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "clipToPadding":
                recyclerView.setClipToPadding(Boolean.parseBoolean(attributeValue));
                return true;
            case "clipChildren":
                recyclerView.setClipChildren(Boolean.parseBoolean(attributeValue));
                return true;
            case "padding":
                try {
                    int padding = dpToPx(context, Float.parseFloat(attributeValue));
                    recyclerView.setPadding(padding, padding, padding, padding);
                } catch (NumberFormatException e) {
                    logWarning("Invalid padding: " + attributeValue);
                }
                return true;
            case "paddingStart":
                try {
                    int paddingStart = dpToPx(context, Float.parseFloat(attributeValue));
                    recyclerView.setPaddingRelative(paddingStart, recyclerView.getPaddingTop(), recyclerView.getPaddingEnd(), recyclerView.getPaddingBottom());
                } catch (NumberFormatException e) {
                    logWarning("Invalid paddingStart: " + attributeValue);
                }
                return true;
            case "paddingTop":
                try {
                    int paddingTop = dpToPx(context, Float.parseFloat(attributeValue));
                    recyclerView.setPadding(recyclerView.getPaddingLeft(), paddingTop, recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());
                } catch (NumberFormatException e) {
                    logWarning("Invalid paddingTop: " + attributeValue);
                }
                return true;
            case "paddingEnd":
                try {
                    int paddingEnd = dpToPx(context, Float.parseFloat(attributeValue));
                    recyclerView.setPaddingRelative(recyclerView.getPaddingStart(), recyclerView.getPaddingTop(), paddingEnd, recyclerView.getPaddingBottom());
                } catch (NumberFormatException e) {
                    logWarning("Invalid paddingEnd: " + attributeValue);
                }
                return true;
            case "paddingBottom":
                try {
                    int paddingBottom = dpToPx(context, Float.parseFloat(attributeValue));
                    recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight(), paddingBottom);
                } catch (NumberFormatException e) {
                    logWarning("Invalid paddingBottom: " + attributeValue);
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理ViewPager2属性
     */
    protected boolean processViewPager2Attributes(ViewPager2 viewPager2, String attributeName, String attributeValue) {
        switch (attributeName) {
            case "orientation":
                viewPager2.setOrientation("vertical".equals(attributeValue) ?
                        ViewPager2.ORIENTATION_VERTICAL : ViewPager2.ORIENTATION_HORIZONTAL);
                return true;
            case "offscreenPageLimit":
                viewPager2.setOffscreenPageLimit(parseIntSafely(attributeValue, 1));
                return true;
            case "currentItem":
                viewPager2.setCurrentItem(parseIntSafely(attributeValue, 0));
                return true;
            case "userInputEnabled":
            case "isUserInputEnabled":
                viewPager2.setUserInputEnabled(Boolean.parseBoolean(attributeValue));
                return true;
            case "pageMargin":
            case "pageMarginPixels":
                try {
                    int margin = attributeName.equals("pageMargin") ?
                            dpToPx(viewPager2.getContext(), Float.parseFloat(attributeValue)) :
                            Integer.parseInt(attributeValue);
                    setViewPager2PageMargin(viewPager2, margin);
                } catch (NumberFormatException e) {
                    logWarning("Invalid " + attributeName + ": " + attributeValue);
                }
                return true;
            default:
                return false;
        }
    }

    private void setViewPager2PageMargin(ViewPager2 viewPager2, int margin) {
        RecyclerView recyclerView = (RecyclerView) viewPager2.getChildAt(0);
        if (recyclerView != null) {
            recyclerView.addItemDecoration(new ViewPager2PageMarginItemDecoration(margin));
        }
    }

    private static class ViewPager2PageMarginItemDecoration extends RecyclerView.ItemDecoration {
        private final int margin;

        public ViewPager2PageMarginItemDecoration(int margin) {
            this.margin = margin;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = margin;
            outRect.right = margin;
        }
    }

    private void setupRecyclerViewLayoutManager(RecyclerView recyclerView, String layoutManagerType) {
        Context context = recyclerView.getContext();
        RecyclerView.LayoutManager layoutManager = null;

        switch (layoutManagerType) {
            case "linear":
            case "linear_vertical":
                layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                break;
            case "linear_horizontal":
                layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                break;
            case "grid":
                layoutManager = new GridLayoutManager(context, 2);
                break;
            case "staggered":
                layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                break;
            default:
                layoutManager = new LinearLayoutManager(context);
        }

        if (layoutManager != null) {
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    private void updateRecyclerViewSpanCount(RecyclerView recyclerView, String spanCount) {
        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (lm instanceof GridLayoutManager) {
            ((GridLayoutManager) lm).setSpanCount(parseIntSafely(spanCount, 2));
        } else if (lm instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) lm).setSpanCount(parseIntSafely(spanCount, 2));
        }
    }

    private void setupRecyclerViewDivider(RecyclerView recyclerView, String divider) {
        Context context = recyclerView.getContext();
        int orientation = LinearLayoutManager.VERTICAL;

        if (divider.contains("horizontal")) {
            orientation = LinearLayoutManager.HORIZONTAL;
            String[] parts = divider.split(":");
            if (parts.length > 1) {
                divider = parts[1];
            }
        }

        try {
            recyclerView.addItemDecoration(new DividerItemDecoration(context, orientation));
        } catch (NumberFormatException e) {
            logWarning("Invalid divider resource: " + divider);
        }
    }
}
