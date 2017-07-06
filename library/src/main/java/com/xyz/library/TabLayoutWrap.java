package com.xyz.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

public class TabLayoutWrap extends LinearLayout {

    private ViewPager viewPager;

    private Path path = new Path();

    // ViewPager滑动监听
    private OnPageChangeListener listener;

    private int itemCount;

    // indicator
    private Paint indicatorPaint;
    private float currentPosStart;
    private float currentPosEnd;
    private int currentItemWidth;
    private int nextItemWidth;
    //    private int itemWidth;
    private float indicatorHeight = getDp(4);
    private float indicatorPadding = 0;

    // line
    private Paint linePaint;
    private int lineColor = Color.LTGRAY;

    // 滑动量
//    private float scrollOffset = 0f;
    private float firstHalfScrollOffset = 0f;
    private float secondHalfScrollOffset = 0f;

    // 滑动量是否超过一半
    private boolean isPastHalf = false;

    public TabLayoutWrap(Context context) {
        this(context, null, 0);
    }

    public TabLayoutWrap(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayoutWrap(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TabLayoutWrap(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(LinearLayout.HORIZONTAL);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        // 自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabLayout);
        if (a.hasValue(R.styleable.TabLayout_indicatorHeight)) {
            indicatorHeight = a.getDimension(R.styleable.TabLayout_indicatorHeight, indicatorHeight);
        }
        int indicatorColor = Color.parseColor("#FF4081");
        if (a.hasValue(R.styleable.TabLayout_indicatorColor)) {
            indicatorColor = a.getColor(R.styleable.TabLayout_indicatorColor, indicatorColor);
        }
        if (a.hasValue(R.styleable.TabLayout_lineColor)) {
            lineColor = a.getColor(R.styleable.TabLayout_lineColor, lineColor);
        }
        if (a.hasValue(R.styleable.TabLayout_indicatorPadding)) {
            indicatorPadding = a.getDimension(R.styleable.TabLayout_indicatorPadding, indicatorPadding);
        }
        a.recycle();

        // 初始化画笔(indicator)
        indicatorPaint = new Paint();
        indicatorPaint.setAntiAlias(true);
        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setPathEffect(new CornerPathEffect(indicatorHeight));
        // 初始化画笔(line)
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(lineColor);
    }

    /**
     * 设置当前页
     *
     * @param position
     */
    public void setCurrentItem(final int position) {
        checkViewPager();
        post(new Runnable() {
            @Override
            public void run() {
                // after measure and layout
                checkIndexOutOfBoundary(position);
                getPath(position);
                setSelected(position);
                viewPager.setCurrentItem(position);
                invalidate();
            }
        });
    }

    /**
     * 设置ViewPager监听
     *
     * @param listener
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 设置indicator渐变色
     *
     * @param colors
     */
    public void setShaderColors(final int[] colors) {
        post(new Runnable() {
            @Override
            public void run() {
                indicatorPaint.setShader(new LinearGradient(indicatorPadding, getHeight(),
                        getActualWidth() - indicatorPadding, getHeight() - indicatorHeight,
                        colors, null, Shader.TileMode.CLAMP));
            }
        });
    }


    /**
     * 关联ViewPager滑动
     *
     * @param vp
     */
    public void setViewPager(ViewPager vp) {
        viewPager = vp;
        checkViewPager();
        setSelected(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (listener != null) {
                    listener.onPageSelected(position);
                }
                currentItemWidth = getChildAt(position).getMeasuredWidth();
                if (position == itemCount - 1) {
                    nextItemWidth = getChildAt(position).getMeasuredWidth();
                } else {
                    nextItemWidth = getChildAt(position + 1).getMeasuredWidth();
                }

                currentPosStart = 0f;
                currentPosEnd = 0f;
                for (int i = 0; i < position; i++) {
                    if (i == 0) {
                        currentPosStart += 0;
                    } else {
                        currentPosStart += getChildAt(i).getMeasuredWidth();
                    }
                    if (position == itemCount - 1) {
                        currentPosEnd += currentPosStart + getChildAt(i).getMeasuredWidth();
                    } else {
                        currentPosEnd += currentPosStart + getChildAt(i + 1).getMeasuredWidth();
                    }
                }
            }

            @Override
            public void onPageScrolled(final int position, float offset, int px) {


                if (offset >= 0.0f && offset <= 0.5f) {
                    if (!isPastHalf) {
                        isPastHalf = true;
                        setSelected(position);
                    }
                    firstHalfScrollOffset = nextItemWidth * offset * 2;
                    getPath(position);
                    invalidate();
                } else if (offset > 0.5f && offset < 1.0f) {
                    if (isPastHalf) {
                        isPastHalf = false;
                        setSelected(position + 1);
                    }
                    secondHalfScrollOffset = currentItemWidth - currentItemWidth * (1 - offset) * 2;
                    path = new Path();
                    path.moveTo(currentPosStart + secondHalfScrollOffset + indicatorPadding, getHeight());
                    path.lineTo(currentPosEnd + currentItemWidth + nextItemWidth - indicatorPadding, getHeight());
                    path.lineTo(currentPosStart + currentItemWidth + nextItemWidth - indicatorPadding, getHeight() - indicatorHeight);
                    path.lineTo(currentPosEnd + secondHalfScrollOffset + indicatorPadding, getHeight() - indicatorHeight);
                    path.close();
                    invalidate();
                }
                if (listener != null) {
                    listener.onPageScrolled(position, offset, px);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (listener != null) {
                    listener.onPageScrollStateChanged(state);
                }
            }
        });
    }

    // ViewGroup容器组件的绘制，当它没有背景时直接调用的是dispatchDraw()方法, 而绕过了draw()方法，
    // 当它有背景的时候就调用draw()方法, 再调用dispatchDraw()方法
    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        // draw line
        canvas.drawLine(0, getHeight(), getRight(), getHeight(), linePaint);
        // draw indicator
        canvas.drawPath(path, indicatorPaint);
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    /**
     * after measured
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        itemCount = getChildCount();
//        itemWidth = (w / itemCount);
        currentItemWidth = getChildAt(0).getMeasuredWidth();
        nextItemWidth = getChildAt(1).getMeasuredWidth();
        getPath(0);
        setOnItemClickListener();
    }

    private void getPath(int position) {
        path = new Path();
        path.moveTo(currentPosStart + indicatorPadding, getHeight());
        path.lineTo(currentPosEnd + currentItemWidth + firstHalfScrollOffset - indicatorPadding, getHeight());
        path.lineTo(currentPosEnd + currentItemWidth + firstHalfScrollOffset - indicatorPadding, getHeight() - indicatorHeight);
        path.lineTo(currentPosStart + indicatorPadding, getHeight() - indicatorHeight);
        path.close();
    }

    /**
     * 设置某个item为选中状态
     *
     * @param position
     */
    private void setSelected(int position) {
        setUnselected();
        View view = getChildAt(position);
        if (view == null) {
            throw new NullPointerException("invoke setTabTitle() of setTabViews() first");
        }
        view.setSelected(true);
    }

    private void setUnselected() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view != null) {
                view.setSelected(false);
            }
        }
    }

//    /**
//     * selector
//     */
//    private ColorStateList createColorStateList(int normal, int selected) {
//
//        int[] colors = new int[]{normal, selected};
//
//        int[][] states = new int[2][];
//
//        states[0] = new int[]{-android.R.attr.state_selected};
//        states[1] = new int[]{android.R.attr.state_selected};
//
//        return new ColorStateList(states, colors);
//    }

    /**
     * 设置item点击事件
     */
    private void setOnItemClickListener() {
        itemCount = getChildCount();
        for (int i = 0; i < itemCount; i++) {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(j);
                }
            });
        }
    }

    /**
     * 获得控件宽度
     *
     * @return pixel
     */
    private int getActualWidth() {
        return getMeasuredWidth();
    }

    private int getDp(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    private void checkViewPager() {
        if (viewPager == null) {
            throw new NullPointerException("ViewPager is null");
        }
    }

    private void checkIndexOutOfBoundary(int position) {
        if (position > itemCount - 1) {
            throw new IndexOutOfBoundsException("size is " + itemCount + ", index is " + position);
        }
    }

    /**
     * ViewPager被占用的接口
     */
    public interface OnPageChangeListener {
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }
}