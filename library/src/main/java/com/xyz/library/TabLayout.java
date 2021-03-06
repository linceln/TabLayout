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
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabLayout extends LinearLayout {

    private ViewPager viewPager;

    private Path path = new Path();

    // ViewPager滑动监听
    private OnPageChangeListener listener;

    private int itemCount;

    // indicator
    private Paint indicatorPaint;
    private int itemWidth;
    private float indicatorHeight = getDp(3);
    private float indicatorPadding;

    // line
    private Paint linePaint;
    private int lineColor = Color.LTGRAY;

    // 滑动量
    private float scrollOffset = 0f;

    // 滑动量是否超过一半
    private boolean isPastHalf = false;

    public TabLayout(Context context) {
        this(context, null, 0);
    }

    public TabLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
            }

            @Override
            public void onPageScrolled(final int position, float offset, int px) {

                if (offset >= 0.0f && offset <= 0.5f) {
                    if (!isPastHalf) {
                        isPastHalf = true;
                        setSelected(position);
                    }
                    scrollOffset = itemWidth * offset * 2;
                    getPath(position);
                    invalidate();
                } else if (offset > 0.5f && offset < 1.0f) {
                    if (isPastHalf) {
                        isPastHalf = false;
                        setSelected(position + 1);
                    }
                    scrollOffset = itemWidth - itemWidth * (1 - offset) * 2;
                    path = new Path();
                    path.moveTo(position * itemWidth + scrollOffset + indicatorPadding, getHeight());
                    path.lineTo((position + 2) * itemWidth - indicatorPadding, getHeight());
                    path.lineTo((position + 2) * itemWidth - indicatorPadding, getHeight() - indicatorHeight);
                    path.lineTo(position * itemWidth + scrollOffset + indicatorPadding, getHeight() - indicatorHeight);
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
        itemWidth = (w / itemCount);

        // 默认 indicator 长度为 item 的 1/3
        // 如果有 TextView，则按第一个 TextView 的文字长度，这里的长度计算逻辑可以随意发挥
        indicatorPadding = itemWidth / 3;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                indicatorPadding = (itemWidth - getTextWidth((TextView) view)) / 2;
                break;
            }
        }

        getPath(0);
        setOnItemClickListener();

    }

    private void getPath(int position) {
        path = new Path();
        path.moveTo(position * itemWidth + indicatorPadding, getHeight());
        path.lineTo((position + 1) * itemWidth + scrollOffset - indicatorPadding, getHeight());
        path.lineTo((position + 1) * itemWidth + scrollOffset - indicatorPadding, getHeight() - indicatorHeight);
        path.lineTo(position * itemWidth + indicatorPadding, getHeight() - indicatorHeight);
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

    private float getTextWidth(TextView tv) {
        TextPaint paint = tv.getPaint();
        return paint.measureText(tv.getText().toString().trim());
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
