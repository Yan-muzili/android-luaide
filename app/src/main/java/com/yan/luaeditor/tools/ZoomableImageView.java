package com.yan.luaeditor.tools;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class ZoomableImageView extends ImageView {

    // 定义操作模式
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // 用于图片变换的矩阵
    private Matrix matrix = new Matrix();
    private float[] matrixValues = new float[9];
    // 记录上一次触摸点和起始触摸点
    private PointF last = new PointF();
    private PointF start = new PointF();
    // 最小和最大缩放比例
    private float minScale = 1f;
    private float maxScale = 5f;
    private float[] m;

    // 缩放手势检测器
    private ScaleGestureDetector mScaleDetector;
    private Context context;

    public ZoomableImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        // 监听布局完成事件，图片加载完成后进行居中处理
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                centerImage();
            }
        });

        setOnTouchListener((v, event) -> {
            mScaleDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    // 单指按下，进入拖动模式
                    last.set(curr);
                    start.set(last);
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    // 双指按下，进入缩放模式
                    last.set(curr);
                    start.set(last);
                    mode = ZOOM;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == ZOOM) {
                        // 双指缩放逻辑由 ScaleGestureDetector 处理
                    } else if (mode == DRAG) {
                        // 计算拖动偏移量
                        float dx = curr.x - last.x;
                        float dy = curr.y - last.y;

                        // 获取当前的缩放比例
                        float scale = getScale();
                        // 计算可拖动的范围
                        float fixTransX = getFixDragTrans(dx, getWidth(), getDrawable().getIntrinsicWidth() * scale);
                        float fixTransY = getFixDragTrans(dy, getHeight(), getDrawable().getIntrinsicHeight() * scale);

                        // 应用平移变换
                        matrix.postTranslate(fixTransX, fixTransY);
                        // 修正图片位置，防止超出边界
                        fixTrans();
                        // 更新上一次触摸点
                        last.set(curr);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    // 手指抬起，结束当前操作模式
                    mode = NONE;
                    break;
            }

            // 更新图片的矩阵变换
            setImageMatrix(matrix);
            // 重绘视图
            invalidate();
            return true;
        });
    }

    // 获取当前的缩放比例
    public float getScale() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    // 缩放手势监听器
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = getScale();
            float scaleFactor = detector.getScaleFactor();

            // 确保缩放比例在最小和最大缩放比例之间
            if (scale * scaleFactor < maxScale && scale * scaleFactor > minScale) {
                // 应用缩放变换
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                // 修正图片位置，防止超出边界
                fixTrans();
            }
            return true;
        }
    }

    // 修正图片的平移位置，防止超出边界
    private void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, getWidth(), getDrawable().getIntrinsicWidth() * getScale());
        float fixTransY = getFixTrans(transY, getHeight(), getDrawable().getIntrinsicHeight() * getScale());

        if (fixTransX != 0 || fixTransY != 0) {
            matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    // 计算修正后的平移量
    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    // 计算可拖动的偏移量
    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    // 让图片居中显示
    private void centerImage() {
        if (getDrawable() == null) {
            return;
        }
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int drawableWidth = getDrawable().getIntrinsicWidth();
        int drawableHeight = getDrawable().getIntrinsicHeight();

        float scaleX = (float) viewWidth / drawableWidth;
        float scaleY = (float) viewHeight / drawableHeight;
        float scale = Math.min(scaleX, scaleY);

        matrix.reset();
        matrix.postScale(scale, scale);

        float dx = (viewWidth - drawableWidth * scale) / 2;
        float dy = (viewHeight - drawableHeight * scale) / 2;
        matrix.postTranslate(dx, dy);

        setImageMatrix(matrix);
    }
}