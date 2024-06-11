package com.aliyun.auikits.karaoke.room.widget.lyric;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.widget.lyric.model.Lyric;
import com.aliyun.auikits.karaoke.room.widget.lyric.model.LyricLine;
import com.aliyun.auikits.karaoke.room.widget.lyric.model.LyricWord;

import java.util.ArrayList;
import java.util.Iterator;

public class LyricView extends View {

    public enum LinesPattern {
        TWO_LINES, Multi_LINES
    }
    private class LyricLineFeed {
        long time = 0;
        int position = 0;
    }

    private long MAX_SMOOTH_SCROLL_DURATION = 300l;
    private long TOLERANCE_TIME = 20l;

    // 所有字体颜色
    private int mHintColor = Color.parseColor("#FFFFFF"); // 提示语颜色
    private int mDefaultColor = Color.parseColor("#CCFCFCFD"); // 默认字体颜色
    private int mHighLightColor = Color.parseColor("#00BCD4"); // 当前播放位置的颜色
    private int mShaderColor = Color.parseColor("#CCFCFCFD");
    private int mShaderColorAlpha = 0x80;
    private LinesPattern mLinesPattern = LinesPattern.TWO_LINES;
    private int mLineCount = 0; // 行数

    // 所有的行高
    private float mHintTextHeight = 0f; // 提示语行高
    private float mDefaultTextHeight = 0f; // 默认字体 行高
    private float mHighLightTextHeight = 0f; // 高亮字体 行高
    private float mShaderTextHeight = 0f; // 阴影字体行高

    // 进度点大小
    private float mProcessPointRadius = 0f; // 点大小
    private float mProcessPointsGap = 0f; // 点间隔
    private int mProcessPointColor = Color.parseColor("#FFFFFF");

    // 字体大小
    private float mDefaultTextSizePx = 0f;
    private float mHighLightTextSizePx = 0f;
    private float mShaderTextSizePx = 0f;
    private float mHintTextSizePx = 0f;

    // 各TextView 偏移
    private float mDefaultTextPy = 0f;
    private float mHighLightTextPy = 0f;
    private float mShaderTextPy = 0f;

    // 最底下一行透明色
    private int mBottomTextAlpha = 0;
    private float mLineSpace = 0f; // 行间距（包含在行高中）
    private int mCurrentPlayLine = -1; // 当前播放位置对应的行数

    //设置长歌词从屏幕1/mScale处开始滚动到结束停止，当前默认为长歌词播放到1/3之一就开始滚动。
    private int mScale = 3;

    private volatile Lyric mLyric;
    private volatile long mNextLineStartTime = 0l;

    private String mDefaultHint = "LyricView";
    private Paint mHintTextPaint = null; // 提示语字体画笔（中间）
    private Paint mDefaultTextPaint = null; // 默认字体画笔
    private Paint mHighLightTextPaint = null; // 高亮字体画笔（顶部）
    private Paint mShaderTextPaint = null; // 阴影字体画笔（底下）
    private Paint mBottomTextPaint = null; // 最底下字体画笔，仅出现在滚动的时候，颜色，大小跟上面mShaderTextPaint的一致
    private Paint mProcessPointPaint = null; // 进度点画笔
    private boolean mSliding = false;

    /**
     * 歌词高亮的模式 逐行模式
     */
    private int MODE_HIGH_LIGHT_NORMAL = 0;

    /**
     * 歌词高亮的模式 逐字模式
     */
    private int MODE_HIGH_LIGHT_KRC = 1;

    /**
     * 歌词高亮的模式
     */
    private int mode = MODE_HIGH_LIGHT_KRC;
    private long mCurrentTimeMillis = 0;

    // 歌词滚动动效
    private ValueAnimator valueAnimator = null;
    private int mLineIntervalTime = 500;
    private ArrayList<LyricLineFeed> mLineFeedList = new ArrayList<>();
    private OnLyricLineFinishedListener mOnLyricLineFinishedListener = null;
    private int mLineFeedPosition = -1;

    private long mProcessPointIntervalBeforeLine = 3000l; // 提前多久显示

    private int mMaxShowLine = 4; // 最多显示行数(仅在Multi_LINES模式生效)

    private Paint mLinePaint = new Paint();

    public LyricView(Context context) {
        super(context);
        initView();
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        float defaultTextSizeForPx = getRawSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        mDefaultTextSizePx = defaultTextSizeForPx;
        mHighLightTextSizePx = defaultTextSizeForPx;
        mShaderTextSizePx = defaultTextSizeForPx;
        mHintTextSizePx = defaultTextSizeForPx;

        mProcessPointRadius = getRawSize(TypedValue.COMPLEX_UNIT_DIP, 2f);
        mProcessPointsGap = getRawSize(TypedValue.COMPLEX_UNIT_DIP, 8f);
        mProcessPointColor = getColor(R.color.white);

        mLineSpace = getRawSize(TypedValue.COMPLEX_UNIT_DIP, 4f);

        initAllPaints();
        initAllBounds();
    }

    /**
     * 初始化画笔
     */
    private void initAllPaints() {
        mHintTextPaint = new Paint();
        mHintTextPaint.setDither(true);
        mHintTextPaint.setAntiAlias(true);
        mHintTextPaint.setTextAlign(Paint.Align.CENTER);
        mHintTextPaint.setColor(mHintColor);
        mHintTextPaint.setTextSize(mHintTextSizePx);
        mDefaultTextPaint = new Paint();
        mDefaultTextPaint.setDither(true);
        mDefaultTextPaint.setAntiAlias(true);
        mDefaultTextPaint.setTextAlign(Paint.Align.CENTER);
        mDefaultTextPaint.setColor(mDefaultColor);
        mDefaultTextPaint.setTextSize(mDefaultTextSizePx);
        mHighLightTextPaint = new Paint();
        mHighLightTextPaint.setDither(true);
        mHighLightTextPaint.setAntiAlias(true);
        mHighLightTextPaint.setTextAlign(Paint.Align.CENTER);
        mHighLightTextPaint.setColor(mHighLightColor);
        mHighLightTextPaint.setTextSize(mHighLightTextSizePx);
        mShaderTextPaint = new Paint();
        mShaderTextPaint.setDither(true);
        mShaderTextPaint.setAntiAlias(true);
        mShaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mShaderTextPaint.setColor(mShaderColor);
        mShaderTextPaint.setTextSize(mShaderTextSizePx);
        mBottomTextPaint = new Paint();
        mBottomTextPaint.setDither(true);
        mBottomTextPaint.setAntiAlias(true);
        mBottomTextPaint.setTextAlign(Paint.Align.CENTER);
        // 颜色字体大小跟mShaderTextPaint的一致
        mBottomTextPaint.setColor(mShaderColor);
        mBottomTextPaint.setTextSize(mShaderTextSizePx);
        // 设置透明度
        mBottomTextPaint.setAlpha(mBottomTextAlpha);

        mProcessPointPaint = new Paint();
        mProcessPointPaint.setDither(true);
        mProcessPointPaint.setAntiAlias(true);
        mProcessPointPaint.setTextAlign(Paint.Align.CENTER);
        mProcessPointPaint.setColor(mProcessPointColor);

        mLinePaint = new Paint();
        mLinePaint.setDither(true);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(getRawSize(TypedValue.COMPLEX_UNIT_DIP, 1));
    }

    /**
     * 初始化需要的尺寸
     */
    private void initAllBounds() {
        Rect lineBound = new Rect();
        mHintTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mHintTextHeight = lineBound.height();
        mDefaultTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mDefaultTextHeight = lineBound.height();
        mHighLightTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mHighLightTextHeight = lineBound.height();
        mShaderTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mShaderTextHeight = lineBound.height();
    }


    /**
     * 直接绘制三行，不管空间是否足够。。因此自己控制字体或者控件大小吧。。
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCurrentPlayLine < 0) {
            return;
        }
        if (mLinesPattern == LinesPattern.TWO_LINES) {
            onDrawTwoLinesMode(canvas);
        } else {
            onDrawMultiLinesMode(canvas);
        }
    }

    private void onDrawTwoLinesMode(Canvas canvas) {
        int currentLineIndex = mCurrentPlayLine > 0 ? mCurrentPlayLine - 1 : mCurrentPlayLine;
        if (mLyric != null && mLyric.lineList.size() > 0 && currentLineIndex < mLyric.lineList.size()) {
            int height = getMeasuredHeight(); // height of this view
            int width = getMeasuredWidth(); // width of this view
            int paddingTop = getPaddingTop();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();

            drawProcessPoints(canvas, paddingLeft, paddingTop);

            int lineIndex[] = {-1, -1};
            lineIndex[0] = currentLineIndex % 2 == 0 ? currentLineIndex : currentLineIndex-1;
            lineIndex[1] = lineIndex[0] + 1;
            lineIndex[1] = lineIndex[1] >= mLyric.lineList.size() ? -1 : lineIndex[1];

Log.i("LyricView", "onDrawTwoLinesMode [current: " + mCurrentPlayLine + ", line0: " + lineIndex[0] + ", line1: " + lineIndex[1]);
            for (int lineIndexCnt = 0; lineIndexCnt < 2; lineIndexCnt++) {
                if (lineIndex[lineIndexCnt] >= 0 && lineIndex[lineIndexCnt] < mLyric.lineList.size()) {
                    LyricLine lyricLine = mLyric.lineList.get(lineIndex[lineIndexCnt]);
                    float lineWidth = mDefaultTextPaint.measureText(lyricLine.content);

                    float rowX = lineIndexCnt == 0 ? paddingLeft + lineWidth / 2f : width-paddingRight-lineWidth/2f;
                    float rowY = (lineIndexCnt+1) * mHighLightTextHeight + paddingTop + lineIndexCnt*mLineSpace;
                    if (mode == MODE_HIGH_LIGHT_NORMAL) {
                        // 逐行模式
                        drawHighLightLrcRow(
                                canvas, lyricLine.content, rowX, rowY
                        );
                    } else {
                        // 逐字模式
                        float progress = LyricParser.calculateCurrentKrcProcess(
                                mCurrentTimeMillis,
                                lyricLine
                        );
                        drawKaraokeHighLightLrcRow(
                                canvas, lyricLine.content,
                                progress,
                                width, rowX, rowY
                        );
                    }
                }
            }
        }
    }
    private void onDrawMultiLinesMode(Canvas canvas) {
        int currentLineIndex = mCurrentPlayLine > 0 ? mCurrentPlayLine - 1 : mCurrentPlayLine;
        if (mLyric != null && mLyric.lineList.size() > 0 && currentLineIndex < mLyric.lineList.size()) {
            int height = getMeasuredHeight(); // height of this view
            int width = getMeasuredWidth(); // width of this view
            int paddingTop = getPaddingTop();
            int paddingLeft = getPaddingLeft();
            drawProcessPoints(canvas, paddingLeft, paddingTop);

            float rowY = paddingTop;
            boolean showLastLine = mSliding || currentLineIndex == 0;
Log.i("LyricView", "onDrawMultiLinesMode [current: " + mCurrentPlayLine + ", currentLineIndex: " + currentLineIndex + ", mSliding: " + mSliding + ", showLastLine: " + showLastLine + "]");
            for (int lineIndexGapOfCurrentLine = -1; lineIndexGapOfCurrentLine < mMaxShowLine; lineIndexGapOfCurrentLine++) {
                int lineIndex = currentLineIndex + lineIndexGapOfCurrentLine;
                boolean isLastShownLine = (lineIndexGapOfCurrentLine == (mMaxShowLine-1));
                if (lineIndex >= 0 && lineIndex < mLyric.lineList.size()) {
                    LyricLine lyricLine = mLyric.lineList.get(lineIndex);
                    if (lineIndexGapOfCurrentLine == 0) { // 当前行
                        rowY = rowY + mHighLightTextHeight + mLineSpace;
                        if (mode == MODE_HIGH_LIGHT_NORMAL) {
                            // 逐行模式
                            drawHighLightLrcRow(
                                    canvas, lyricLine.content, width * 0.5f, rowY-mHighLightTextPy
                            );
                        } else {
                            // 逐字模式
                            float progress = LyricParser.calculateCurrentKrcProcess(
                                    mCurrentTimeMillis, lyricLine
                            );
                            drawKaraokeHighLightLrcRow(
                                    canvas, lyricLine.content, progress, width, width * 0.5f, rowY-mHighLightTextPy
                            );
                        }
                    } else if (showLastLine || !isLastShownLine) {
                        rowY = rowY + mShaderTextHeight + mLineSpace;
                        // 绘制底下行
                        canvas.drawText(
                                lyricLine.content,
                                width * 0.5f,
                                rowY-mShaderTextPy,
                                mShaderTextPaint
                        );
                    }
                }
            }

//            // 绘制头顶行
//            if (mCurrentPlayLine - 1 >= 0 || mCurrentPlayLine == mLyric.lineList.size()) {
//                if (mode == MODE_HIGH_LIGHT_NORMAL) {
//                    // 逐行模式
//                    drawHighLightLrcRow(
//                            canvas, mLyric.lineList.get(mCurrentPlayLine - 1).content,
//                            width * 0.5f,
//                            mHighLightTextHeight - mHighLightTextPy + paddingTop
//                    );
//                } else {
//                    // 逐字模式
//                    float progress = LyricParser.calculateCurrentKrcProcess(
//                            mCurrentTimeMillis,
//                            mLyric.lineList.get(mCurrentPlayLine - 1)
//                    );
//                    drawKaraokeHighLightLrcRow(
//                            canvas, mLyric.lineList.get(mCurrentPlayLine - 1).content,
//                            progress,
//                            width,
//                            width * 0.5f,
//                            mHighLightTextHeight - mHighLightTextPy + paddingTop
//                    );
//                }
//            }
//            if (mCurrentPlayLine < mLyric.lineList.size()) {
//                // 绘制中间行
//                canvas.drawText(
//                        mLyric.lineList.get(mCurrentPlayLine).content,
//                        width * 0.5f,
//                        mHighLightTextHeight + mLineSpace + mDefaultTextHeight - mDefaultTextPy + paddingTop,
//                        mDefaultTextPaint
//                );
//            }
//            if (mCurrentPlayLine + 1 < mLyric.lineList.size()) {
//                // 绘制底下行
//                canvas.drawText(
//                        mLyric.lineList.get(mCurrentPlayLine + 1).content,
//                        width * 0.5f,
//                        mHighLightTextHeight + mDefaultTextHeight + mShaderTextHeight - mShaderTextPy + mLineSpace * 2 + paddingTop,
//                        mShaderTextPaint
//                );
//            }
//            if (mSliding && mCurrentPlayLine + 2 < mLyric.lineList.size()) {
//                // 绘制最底下行
//                mBottomTextPaint.setAlpha(mBottomTextAlpha);
//                canvas.drawText(
//                        mLyric.lineList.get(mCurrentPlayLine + 2).content,
//                        width * 0.5f,
//                        mHighLightTextHeight + mDefaultTextHeight + mShaderTextHeight * 2 - mShaderTextPy + mLineSpace * 3 + paddingTop,
//                        mBottomTextPaint
//                );
//            }
        }
    }

    private void drawProcessPoints(Canvas canvas, int left, int top) {
        if (mCurrentTimeMillis < mNextLineStartTime) {
            long firstLineInterval = mNextLineStartTime - mCurrentTimeMillis;
            if (firstLineInterval > 0 && firstLineInterval <= mProcessPointIntervalBeforeLine) {
                int count = (int)(firstLineInterval / 1000) + 1;
                for (int i = 0; i < count; i++) {
                    float cx = left + mProcessPointRadius / 2.0f + mProcessPointsGap * i;
                    float cy = top / 2.0f;
                    canvas.drawCircle(cx, cy, mProcessPointRadius, mProcessPointPaint);
                }
            }
        }
    }

    /**
     * 逐行歌词核心高亮方法
     */
    private void drawHighLightLrcRow(Canvas canvas, String text, float rowX, float rowY) {
        canvas.drawText(text, rowX, rowY, mHighLightTextPaint);
    }

    /**
     * 逐字歌词核心高亮方法
     */
    private void drawKaraokeHighLightLrcRow(Canvas canvas, String text, float progress, int width, float rowX, float rowY) {
        // 保存临时变量 等会儿需要还原，默认文本画笔字体大小
        float defaultTextSize = mDefaultTextPaint.getTextSize();
        mDefaultTextPaint.setTextSize(mHighLightTextPaint.getTextSize());
        float highLineWidth = mDefaultTextPaint.measureText(text);
        float location = progress * highLineWidth;

        //如果歌词长于屏幕宽度就需要滚动
        if (highLineWidth > width) {
            if (location < width / mScale) {
                rowX = highLineWidth / 2.0f;
            } else {
                //歌词当前播放位置超过屏幕1/mScale处开始滚动，滚动到歌词结尾到达屏幕边缘时停止滚动。
                float offsetX = location - width / mScale;
                float widthGap = highLineWidth - width;
                if (offsetX < widthGap) {
                    rowX = highLineWidth / 2.0f - offsetX;
                } else {
                    rowX = highLineWidth / 2.0f - widthGap;
                }
            }
        }

        // 先画一层普通颜色的
        canvas.drawText(text, rowX, rowY, mDefaultTextPaint);
//        canvas.drawLine(0, rowY, canvas.getWidth(), rowY, mLinePaint); // 文本辅助线

        // 再画一层高亮颜色的
        float leftOffset = rowX - highLineWidth / 2.0f;
        // 高亮的宽度
        int highWidth = (int)(progress * highLineWidth);
        if (highWidth > 1) {
            // 用bitmap缓存动画的增量过程
            // 每次绘制都基于当前百分比绘制剪裁后的text文本
            // 动画连起来就是一个逐字歌词的过渡动画了

            // 获取文本高度信息
            Paint.FontMetrics fontMetrics = mHintTextPaint.getFontMetrics();
            float extraHeight = fontMetrics.descent;
            Bitmap textBitmap =
                    Bitmap.createBitmap(highWidth, (int)(mDefaultTextHeight+extraHeight), Bitmap.Config.ARGB_8888);
            Canvas textCanvas = new Canvas(textBitmap);
//            textCanvas.drawRect(0, 0, textBitmap.getWidth(), textBitmap.getHeight(), mHintTextPaint); //高亮框
            textCanvas.drawText(text, highLineWidth / 2f, mDefaultTextHeight, mHighLightTextPaint);
            canvas.drawBitmap(textBitmap, leftOffset, rowY-mDefaultTextHeight, mHighLightTextPaint);
        }

        // 还原，默认文本画笔字体大小
        mDefaultTextPaint.setTextSize(defaultTextSize);
    }

    /**
     * 刷新View
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    /**
     * 执行滚动动效
     */
    private void smoothScroll(int toPosition) {
        if (null == mLyric) {
            return;
        }

        mSliding = true;
        // 设置字体颜色
        mHighLightTextPaint.setColor(mHighLightColor);
        mDefaultTextPaint.setColor(mDefaultColor);
        mShaderTextPaint.setColor(mShaderColor);
        mShaderTextPaint.setAlpha(mShaderColorAlpha);

        // 数值计算动效

        long duration = LyricParser.getScrollDuration(mLyric, mCurrentPlayLine, toPosition);
        valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                .setDuration(Math.min(duration, MAX_SMOOTH_SCROLL_DURATION));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float animatedFraction = animation.getAnimatedFraction();
                        // 计算字体大小
                        mHighLightTextPaint.setTextSize(
                                (mDefaultTextSizePx - mHighLightTextSizePx) * animatedFraction + mHighLightTextSizePx);
                        mDefaultTextPaint.setTextSize(
                                (mHighLightTextSizePx - mDefaultTextSizePx) * animatedFraction + mDefaultTextSizePx);
                        mShaderTextPaint.setTextSize(
                                (mDefaultTextSizePx - mShaderTextSizePx) * animatedFraction + mShaderTextSizePx);
                        // 计算歌词位移
                        mHighLightTextPy = (mHighLightTextHeight + mLineSpace) * animatedFraction;
                        mDefaultTextPy = (mDefaultTextHeight + mLineSpace) * animatedFraction;
                        mShaderTextPy = (mShaderTextHeight + mLineSpace) * animatedFraction;
                        // 计算透明度
                        mBottomTextAlpha = (int)(mShaderColorAlpha * animatedFraction);
                        invalidateView();
                    }
                });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // 重置变量
                if (mCurrentPlayLine != toPosition) {
                    mCurrentPlayLine = toPosition;
                    Log.i("LyricView", "onAnimationEnd [mCurrentPlayLine: " + mCurrentPlayLine + "]");
                    // 重置字体大小
                    mHighLightTextPaint.setTextSize(mHighLightTextSizePx);
                    mDefaultTextPaint.setTextSize(mDefaultTextSizePx);
                    mShaderTextPaint.setTextSize(mShaderTextSizePx);
                    // 重置偏移量
                    mHighLightTextPy = 0f;
                    mDefaultTextPy = 0f;
                    mShaderTextPy = 0f;
                    // 重置透明度
                    mBottomTextAlpha = 0;
                    // 重新设置字体颜色
                    mHighLightTextPaint.setColor(mHighLightColor);
                    mDefaultTextPaint.setColor(mDefaultColor);
                    mShaderTextPaint.setColor(mShaderColor);
                    mShaderTextPaint.setAlpha(mShaderColorAlpha);
                    // 重置滑动标识
                    mSliding = false;
                    invalidateView();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    /**
     * 根据当前给定的时间戳滑动到指定位置
     *
     * @param time 时间戳
     */
    private void scrollToCurrentTimeMillis(long time) {
        int position = 0;

        // 判断是否可以进行滑动
        boolean isScrollable = mLyric != null && mLyric.lineList.size() > 0;

        if (isScrollable) {
            int i = 0;
            int size = mLineCount;
            while (i < size) {
                LyricLine lineInfo = mLyric.lineList.get(i);
                if (lineInfo.start > time) {
                    position = i;
                    break;
                }
                if (i == mLineCount - 1) {
                    position = mLineCount;
                }
                i++;
            }
        }
        if (mCurrentPlayLine != position && !mSliding) {
            smoothScroll(position);
        }
    }

    /**
     * 换行判断
     */
    private void lineFeed(long time) {
        if (mode != MODE_HIGH_LIGHT_KRC) {
            return;
        }
        OnLyricLineFinishedListener lyricLineFeedListener = mOnLyricLineFinishedListener;
        if (lyricLineFeedListener != null) {
            int position = getPosition(time);
            if (mLineCount > 0) {
                if (mLineFeedPosition + 1 != position) {
                    mLineFeedPosition = position;
                    return;
                }
                if (position == mLineCount - 1) {
                    mLineFeedPosition = position;
                    if (isNotLineFeed(position)) {
                        addLineFeed(position);
                        lyricLineFeedListener.onLyricLineFinished(
                                position,
                                mLyric.lineList.get(position)
                        );
                    }
                } else {
                    long endTime = 0;
                    if (position >= 0) {
                        LyricLine lineInfo = mLyric.lineList.get(position);
                        endTime = lineInfo.start + lineInfo.duration;
                    }
                    long lineDurationTime = mLyric.lineList.get(position + 1).start - endTime;
                    long toleranceTime = TOLERANCE_TIME < lineDurationTime ? TOLERANCE_TIME : lineDurationTime;
                    if ((time - endTime) >= toleranceTime) {
                        mLineFeedPosition = position;
                        if (isNotLineFeed(position)) {
                            addLineFeed(position);
                            lyricLineFeedListener.onLyricLineFinished(
                                    position,
                                    mLyric.lineList.get(position)
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param time 时间
     * @return 最后发生换行的position 从0开始
     */
    private int getPosition(long time) {
        int position = -1;
        int i = 0;
        int size = mLineCount;
        while (i < size) {
            LyricLine lineInfo = mLyric.lineList.get(i);
            long endTime = lineInfo.start + lineInfo.duration;
            if (lineInfo.wordList.size() > 0) {
                LyricWord word = lineInfo.wordList.get(lineInfo.wordList.size() - 1);
                endTime = lineInfo.start + word.duration + word.start;
            }
            if (time >= endTime) {
                position = i;
            }
            i++;
        }
        return position;
    }

    /**
     * 判断是否是在间隔时间内触发同一行的换行回调
     * @param position 触发回调的行，从0开始
     * @return false 表示是在间隔时间内触发
     */
    private boolean isNotLineFeed(int position) {
        boolean isNotLineFeed = true;
        long currentTime = System.currentTimeMillis();
        Iterator<LyricLineFeed> iterator = mLineFeedList.iterator();
        while (iterator.hasNext()) {
            LyricLineFeed lineFeed = iterator.next();
            if (currentTime > lineFeed.time + mLineIntervalTime) {
                iterator.remove();
            } else {
                if (lineFeed.position == position) {
                    isNotLineFeed = false;
                }
            }
        }
        return isNotLineFeed;
    }

    private void addLineFeed(int position) {
        LyricLineFeed lyricLineFeed = new LyricLineFeed();
        lyricLineFeed.position = position;
        lyricLineFeed.time = System.currentTimeMillis();
        mLineFeedList.add(lyricLineFeed);
    }

    /**
     * 重置歌词内容
     */
    private void resetLyricInfo() {
        if (mLyric != null) {
            mLyric.lineList.clear();
            mLyric = null;
        }
    }

    /**
     * 初始化控件
     */
    private void resetView() {
        resetLyricInfo();
        invalidateView();
        // 停止歌词滚动动效
        if (valueAnimator != null) {
            valueAnimator.cancel();;
        }
        mLineCount = 0;
        mDefaultTextPy = 0f;
        mHighLightTextPy = 0f;
        mShaderTextPy = 0f;
    }

    private float getRawSize(int unit, float size) {
        Context context = getContext();
        Resources resources = null == context ? Resources.getSystem() : context.getResources();
        return TypedValue.applyDimension(unit, size, resources.getDisplayMetrics());
    }

    private int getColor(int colorId) {
        Context context = getContext();
        Resources resources = null == context ? Resources.getSystem() : context.getResources();
        return resources.getColor(colorId);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ *
     *                                                                                             对外API                                                                                        *
     * ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ *
     * */
    /**
     * 设置逐字歌词
     *
     * @param lyricString info model
     */
    public void setupLyric(String lyricString, long rangeStart, long rangeEnd) {
        mCurrentPlayLine = -1;
        // 逐字歌词数据源
        mLyric = LyricParser.parseLyric(lyricString, 0, rangeEnd);
        if (!mLyric.lineList.isEmpty()) {
            mNextLineStartTime = rangeStart;//mLyric.lineList.get(0).start;
        } else {
            mNextLineStartTime = 0l;
        }
        mode = MODE_HIGH_LIGHT_KRC;
        mLineCount = mLyric.lineList.size();
        clearLyricLineFeed();
        invalidateView();
    }


    /**
     * 设置当前时间显示位置
     *
     * @param current 时间戳
     */
    public void setCurrentTimeMillis(long current) {
        Log.i("LyricView", "setCurrentTimeMillis : " + current);
        mCurrentTimeMillis = current;
        scrollToCurrentTimeMillis(current);
        lineFeed(current);
        invalidateView();
    }

    /**
     * 重置、设置歌词内容被重置后的提示内容
     *
     * @param message 提示内容
     */
    public void reset(String message) {
        mDefaultHint = message;
        resetView();
        clearLyricLineFeed();
    }

    /**
     * 设置提示语颜色
     */
    public void setHintTextColor(int color) {
        if (mHintColor != color) {
            mHintColor = color;
            mHintTextPaint.setColor(color);
        }
    }

    /**
     * 设置默认文字颜色
     */
    public void setDefaultTextColor(int color) {
        if (mDefaultColor != color) {
            mDefaultColor = color;
            mDefaultTextPaint.setColor(color);
        }
    }

    /**
     * 设置阴影文字颜色
     */
    public void setShaderTextColor(int color, int alpha) {
        if (mShaderColor != color && mShaderColorAlpha != alpha) {
            mShaderColor = color;
            mShaderColorAlpha = alpha;
            mShaderTextPaint.setColor(color);
            mShaderTextPaint.setAlpha(mShaderColorAlpha);
            mBottomTextPaint.setColor(color);
        }
    }

    /**
     * 设置高亮字体颜色
     */
    public void setHighLightTextColor(int color) {
        if (mHighLightColor != color) {
            mHighLightColor = color;
            mHighLightTextPaint.setColor(color);
        }
    }

    /**
     * 设置提示文本内容字体大小
     */
    public void setHintTextSizeSp(float size) {
        float textSizePx = getRawSize(TypedValue.COMPLEX_UNIT_SP, size);
        mHintTextSizePx = textSizePx;
        mHintTextPaint.setTextSize(textSizePx);
        Rect lineBound = new Rect();
        mHintTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mHintTextHeight = lineBound.height();
        invalidateView();
    }

    /**
     * 设置默认文本内容字体大小
     */
    public void setDefaultTextSizeSp(float size) {
        float textSizePx = getRawSize(TypedValue.COMPLEX_UNIT_SP, size);
        mDefaultTextSizePx = textSizePx;
        mDefaultTextPaint.setTextSize(textSizePx);
        Rect lineBound = new Rect();
        mDefaultTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mDefaultTextHeight = lineBound.height();
        invalidateView();
    }

    /**
     * 设置阴影字体大小
     */
    public void setShaderTextSizeSp(float size) {
        float textSizePx = getRawSize(TypedValue.COMPLEX_UNIT_SP, size);
        mShaderTextSizePx = textSizePx;
        mShaderTextPaint.setTextSize(textSizePx);
        mBottomTextPaint.setTextSize(textSizePx);
        Rect lineBound = new Rect();
        mShaderTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mShaderTextHeight = lineBound.height();
        invalidateView();
    }

    /**
     * 设置高亮字体大小
     */
    public void setHighLightTextSizeSp(float size) {
        float textSizePx = getRawSize(TypedValue.COMPLEX_UNIT_SP, size);
        mHighLightTextSizePx = textSizePx;
        mHighLightTextPaint.setTextSize(textSizePx);
        Rect lineBound = new Rect();
        mHighLightTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mHighLightTextHeight = lineBound.height();
        invalidateView();
    }

    public void setLineSpaceDp(float size) {
        mLineSpace = getRawSize(TypedValue.COMPLEX_UNIT_DIP, size);
        invalidateView();
    }

    /**
     * 设置同行触发回调间隔时间(单位毫秒),两次同一行的换行回调间隔至少n毫秒才会触发，避免播放器同步seek造成的同一行触发多次换行回调
     * 默认: 500
     */
    public void setLineIntervalTime(int intervalTime) {
        mLineIntervalTime = intervalTime;
    }

    /**
     * 重置换行记录，保证能够立马触发换行回调，通常在播放器手动seek时调用
     * 外部需要保证在此方法调用到播放器seek结束这段时间内不调用setCurrentTimeMillis()
     */
    public void clearLyricLineFeed() {
        mLineFeedPosition = -1;
        mLineFeedList.clear();
    }

    /**
     * 设置歌词换行回调监听。只在逐字歌词模式才会生效
     */
    public void setOnLyricFinishLineListener(OnLyricLineFinishedListener onLyricLineFinishedListener) {
        mOnLyricLineFinishedListener = onLyricLineFinishedListener;
    }

    public void setLinesPattern(LinesPattern mLinesPattern) {
        this.mLinesPattern = mLinesPattern;
    }
}
