package com.aliyun.auikits.karaoke.room.widget.pitch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSONException;
import com.aliyun.auikits.karaoke.room.widget.pitch.model.MusicPitch;
import com.aliyun.auikits.karaoke.room.widget.pitch.model.MusicPitchScore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PitchView extends FrameLayout {

    public final static int MAX_BACKGROUND_LINE_NUM = 5;//最大背景线数量
    public final static int MUSIC_PITCH_NUM = 20;//音阶数，可自行定义
    public final static int MUSIC_MAX_PITCH = 90;//最大音高值,不可更改
    public final static int MUSIC_MIN_PITCH = 10;//最小音高值，不可更改
    public final static long TIME_ELAPSED_ON_SCREEN = 1150 ;//屏幕中已经唱过的时间（控件开始至竖线这一段表示的时间），可自行定义
    public final static long TIME_TO_PLAY_ON_SCREEN = 2750 ;//屏幕中还未唱的时间（竖线至控件末尾这一段表示的时间），可自行定义
    public long ESTIMATED_CALL_INTERVAL = 60;//击中块时间间隔(调用 setCurrentSongProgress 方法的大致时间间隔)
    public long ESTIMATED_CALL_INTERVAL_OFFSET = ESTIMATED_CALL_INTERVAL/2;//击中块时间间隔偏移

    public long TRIANGLE_ANIM_TIME = ESTIMATED_CALL_INTERVAL;//三角形动画的时间

    /**
     * 分数动画的几个时间段
     */
    public int ANIM_START_TIME = 200;
    public int ANIM_STARTING_TIME = 400;
    public int ANIM_RUNNING_TIME = 400;
    public int ANIM_END_TIME = 300;
    public int ANIM_TOTAL_TIME = ANIM_START_TIME + ANIM_STARTING_TIME + ANIM_RUNNING_TIME + ANIM_END_TIME;//动画总时间

    /**
     * 三角形音调指示器的大小
     */
    public float TRIANGLE_WIDTH ;
    public float TRIANGLE_HEIGHT ;

    /**
     *屏幕最左侧的时间点，即 （当前时间-TIME_ELAPSED_ON_SCREEN）
     */
    private long mStartTime = 0;


    // 所有字体颜色
    private final int mStandardPitchColor = Color.parseColor("#FFFCFCFD");  // 默认音高线颜色
    private final int mHitPitchColor = Color.parseColor("#00BCD4");  // 击中音高线颜色
    private final int mPitchIndicatorColor = Color.parseColor("#FFFFFF");  // 音调指示器颜色
    private final int mStaffColor = Color.parseColor("#33FFFFFF"); //五线谱横线颜色
    private final int mVerticalLineColor = Color.parseColor("#FFA87BF1"); //竖线颜色
    private final int mScoreTextColor = Color.parseColor("#FFFFFF"); //分数文本颜色

    private float mBackgroundLineHeight = 0.5f;
    private float mTimeLineWidth = 0.5f;

    private float mUnitWidth;//单位毫秒时间长度
    private float mRectHeight;//音高线的高度
    private float mMidX;//当前高音值三角形的x坐标
    private float mMidY;//当前高音值三角形的y坐标
    private final List<MusicPitch> mMusicPitchList = new ArrayList<>();//全部音高数据
    private final List<MusicPitch> mHitRectList = new ArrayList<>();//击中音高数据
    private int mCurrentMusicPitch;//当前音高值
    private long mCurrentSongTime;//当前歌曲时间戳


    private Paint mStandardPitchPaint;
    private Paint mHitPitchPaint;
    private Paint mStaffPaint;
    private Paint mVerticalLinePaint;
    private Paint mTrianglePaint;
    private Paint mScorePaint;

    PathInterpolator pathInterpolator = new PathInterpolator(0.5f,0.5f,0.5f,0.5f);
    private float mScoreOffsetY = 0;
    private float mScoreOffsetX = 0;
    private float mAnimHeight = 0;

    private final List<MusicPitchScore> scoreList = new ArrayList<>();
    private ArrayList<Integer> curLineScores = new ArrayList<>();
    private ArrayList<Integer> scoresPerLine = new ArrayList<>();

    /**
     *  演唱得分难度
     *  取值范围[0-100]，默认值为 10
     *  值越小难度越低，演唱者越容易得高分
     */
    private int mScoreLevel = 10;

    /**
     *  演唱评分偏移量
     *  取值范围 [-100,100]，默认值为 0
     *  最终得分会在计算时在原有得分基础上加上偏移量
     */
    private int mScoreCompensationOffsetLevel = 0;

    public PitchView(Context context) {
        this(context,null);
    }

    public PitchView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public void init() {
        initView();
        TRIANGLE_WIDTH = getRawSize(5f);
        TRIANGLE_HEIGHT = getRawSize(7f);
    }

    private void initView() {

        initAllPaints();
        mBackgroundLineHeight = getRawSize(0.5f);
        mTimeLineWidth = getRawSize(0.5f);
        mScoreOffsetY = getRawSize(0f);
        mScoreOffsetX = getRawSize(0f);
        Rect lineBound = new Rect();
        mScorePaint.getTextBounds("+0", 0, "+0".length(), lineBound);
        mAnimHeight = getRawSize(30f) + lineBound.height();
    }


    /**
     * 初始化画笔
     */
    private void initAllPaints() {
        mStandardPitchPaint = new Paint();
        mStandardPitchPaint.setDither(true);
        mStandardPitchPaint.setAntiAlias(true);
        mStandardPitchPaint.setAlpha(1);
        mStandardPitchPaint.setColor(mStandardPitchColor);


        mHitPitchPaint = new Paint();
        mHitPitchPaint.setDither(true);
        mHitPitchPaint.setAntiAlias(true);
        mHitPitchPaint.setColor(mHitPitchColor);

        mStaffPaint = new Paint();
        mStaffPaint.setDither(true);
        mStaffPaint.setAntiAlias(true);
        mStaffPaint.setColor(mStaffColor);

        mVerticalLinePaint = new Paint();
        mVerticalLinePaint.setDither(true);
        mVerticalLinePaint.setAntiAlias(true);
        mVerticalLinePaint.setColor(mVerticalLineColor);


        mTrianglePaint = new Paint();
        mTrianglePaint.setDither(true);
        mTrianglePaint.setAntiAlias(true);
        mTrianglePaint.setColor(mPitchIndicatorColor);
        mTrianglePaint.setStyle(Paint.Style.FILL);


        mScorePaint = new Paint();
        mScorePaint.setDither(true);
        mScorePaint.setAntiAlias(true);
        mScorePaint.setTextSize(getRawSize(14));
        mScorePaint.setColor(mScoreTextColor);

    }

    private void addHitRectList(MusicPitch pitch){

        if(pitch == null){
            return;
        }

        if(mHitRectList.size() == 0){
            mHitRectList.add(pitch);
            return;
        }

        int position = binarySearch(mHitRectList,pitch.startTime);
        if(position == -1){
            MusicPitch endPitch = mHitRectList.get(mHitRectList.size()-1);
            if((endPitch.startTime + endPitch.duration + 1) < pitch.startTime){
                mHitRectList.add(pitch);
            }else {
                int size = mHitRectList.size();
                for(int i= 0; i < size - 1;i++){
                    if(mHitRectList.get(i).startTime  > pitch.startTime){
                        mHitRectList.add(i,pitch);
                        break;
                    }
                }
            }
        }else {
            MusicPitch positionPitch = mHitRectList.get(position);
            if(positionPitch.getPitch() == pitch.getPitch()){
                positionPitch.setDuration(pitch.startTime + pitch.duration - positionPitch.startTime);
            }else {
                mHitRectList.add(position+1,pitch);
            }
        }
    }


    private static int binarySearch(List<MusicPitch> pitchList, long key) {
        int low, mid, high;
        low = 0;// 最小下标
        high = pitchList.size() - 1;
        while (low <= high) {
            mid = (high + low) / 2;
            MusicPitch temp = pitchList.get(mid);
            if (key > (temp.startTime + temp.duration + 1)) {
                low = mid + 1;
            } else if (key < temp.startTime) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    private int getCurrentMusicIndex(List<MusicPitch> pitchList,long time){
        int size = pitchList.size();

        for(int i = 0; i < size;i++) {
            MusicPitch pitch = pitchList.get(i);
            if(time <=  (pitch.startTime + pitch.duration)){
                return i;
            }
        }
        return -1;
    }

    private List<Integer> getCurrentMusicList(List<MusicPitch> pitchList,long startTime,long duration){
        int size = pitchList.size();
        ArrayList<Integer> currentMusicList = new ArrayList<>();

        for(int i = 0; i < size;i++) {
            MusicPitch pitch = pitchList.get(i);

            if((startTime + duration) < pitch.startTime){
                break;
            }

            if(startTime <=  (pitch.startTime + pitch.duration) || (startTime +duration ) <= (pitch.startTime + pitch.duration)){
                currentMusicList.add(i);
            }


        }
        return currentMusicList;
    }

    /**
     * 即时显示录唱的准确度
     * Return: int 返回即时显示录唱的准确度
     * 显示准确程度（0无声，1错误（偏小），2偏离（偏小）3准确，4偏离（偏大），5错误（偏大））（此处
     2,3,4都认为是准确的）
     */
    private int getOffsetPitch(int pitch) {
        int offsetScale = -1;
        switch (pitch) {
            case 1:
                offsetScale = -2;
                break;
            case 2:
            case 3:
            case 4:
                offsetScale = 0;
                break;
            case 5:
                offsetScale = 2;
                break;
            case 0:
            default:
                break;
        }
        return offsetScale;
    }

    private int getOffsetPitch(int pitch, int stdPitch) {
        int offsetScale = -1;
        if (pitch == 0) {
            return offsetScale;
        }
        if (pitch>= stdPitch - 3 &&
                pitch<= stdPitch + 3) {
            offsetScale = 0;
        } else {
            offsetScale = pitch > stdPitch ? pitch - stdPitch : stdPitch - pitch;
        }

        return offsetScale;
    }

    private void calculateScore(int pitch, int stdPitch) {
        double inputTone = pitch; //getPitchTone(pitch);
        double stdTone = stdPitch; //getPitchTone(stdPitch);
        float match = 1.0f - ((float)mScoreLevel / 100.0f) * Math.abs((float)(inputTone - stdTone)) + ((float)mScoreCompensationOffsetLevel / 100.0f);
        match = Math.max(0.0f, match);
        match = Math.min(1.0f, match);

        int curScore = Math.round(match * 100.0f);
        curLineScores.add(curScore);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed,left,top,right,bottom);
        // 单位毫秒时间长度总宽度除以屏幕总时间得到单位毫秒时间长度
        mUnitWidth = getWidth() * 1.0f / (TIME_TO_PLAY_ON_SCREEN + TIME_ELAPSED_ON_SCREEN);
        // 音高线的高度，等于控件总高度，最后除以总的音阶数
        mRectHeight = (float) (getHeight() - getPaddingTop() - getPaddingBottom()) / MUSIC_PITCH_NUM;
        //根据已唱时间和未唱时间，算出中间那根线的x坐标
        mMidX = getWidth() * 1.0f * TIME_ELAPSED_ON_SCREEN / (TIME_TO_PLAY_ON_SCREEN +
                TIME_ELAPSED_ON_SCREEN) ;
        //三角形默认y值处于音阶0点
        mMidY = getPitchTop(0);
        float halfHeight  = (getHeight() - getPaddingTop() - getPaddingBottom())/2.0f;
        mAnimHeight = Math.min( mAnimHeight , halfHeight);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        drawBackground(canvas);
        // 绘制标准音高线
        drawRect(canvas);
        // 绘制击中音高线和其他效果
        drawHitRect(canvas);
        //绘制三角形
        drawPitchPoint(canvas);
        super.dispatchDraw(canvas);
    }

    private void drawBackground(Canvas canvas){

        int num = MAX_BACKGROUND_LINE_NUM;

        float lineSpacing = (float) ((getHeight()-getPaddingTop() - getPaddingBottom() - num * mBackgroundLineHeight) / (num - 1.0));

        for(int i = 0; i < num;i++) {
            float startX = 0;
            float startY = i * (mBackgroundLineHeight + lineSpacing) + getPaddingTop();
            float endX = getWidth();
            float endY = i * (mBackgroundLineHeight + lineSpacing) + mBackgroundLineHeight + getPaddingTop();
            canvas.drawLine(startX, startY, endX, endY, mStaffPaint);
        }

        canvas.drawLine(mMidX,getPaddingTop(),mMidX+mTimeLineWidth,getHeight()- getPaddingBottom(), mVerticalLinePaint);
    }


    private void drawRect(Canvas canvas) {

        if(mMusicPitchList == null){
            return;
        }

        // 音高线实际上就是一个个rect，计算出left, top, right, bottom执行drawRect绘制
        int startIndex = getCurrentMusicIndex(mMusicPitchList,mStartTime);
        int endIndex = getCurrentMusicIndex(mMusicPitchList,mStartTime + TIME_ELAPSED_ON_SCREEN + TIME_TO_PLAY_ON_SCREEN);

        if(startIndex == -1){
            return;
        }

        if(endIndex == -1){
            endIndex = mMusicPitchList.size()-1;
        }

        List<MusicPitch> data = mergeMusicPitch(mMusicPitchList,startIndex,endIndex + 1);
        for(MusicPitch musicPitch: data) {
            float left = (musicPitch.getStartTime() - mStartTime) * mUnitWidth;
            float right = left + musicPitch.getDuration() * mUnitWidth;
            // 根据音高值确定top，先算出当前应该在第几个音阶，再乘以每阶高度
            float top = getPitchTop(musicPitch.getPitch());
            // 音高线粗细
            float bottom = top + mRectHeight;
            if (right >= left) {
                // 用自定义的画笔绘制
                canvas.drawRoundRect(left, top, right, bottom,mRectHeight/2.0f,mRectHeight/2.0f, mStandardPitchPaint);
//                canvas.drawRect(left, top, right, bottom, mMusicPitchPaint);
            }
        }

    }



    private void drawHitRect(Canvas canvas) {
        //与drawRect逻辑类似，使用mHitRectList里的数据画出击中的音高线

        if(mHitRectList == null){
            return;
        }

        // 音高线实际上就是一个个rect，计算出left, top, right, bottom执行drawRect绘制
        int startIndex = getCurrentMusicIndex(mHitRectList,mStartTime);
        int endIndex = getCurrentMusicIndex(mHitRectList,mStartTime + TIME_ELAPSED_ON_SCREEN + TIME_TO_PLAY_ON_SCREEN);

        if(startIndex == -1){
            return;
        }

        if(endIndex == -1){
            endIndex = mHitRectList.size()-1;
        }

        List<MusicPitch> data = mergeMusicPitch(mHitRectList,startIndex,endIndex + 1);
        for(MusicPitch musicPitch: data) {
            float left = (musicPitch.getStartTime() - mStartTime) * mUnitWidth;
            float right = left + musicPitch.getDuration() * mUnitWidth;
            // 根据音高值确定top，先算出当前应该在第几个音阶，再乘以每阶高度
            float top = getPitchTop(musicPitch.getPitch());
            // 音高线粗细
            float bottom = top + mRectHeight;
            if (right >= left) {
                // 用自定义的画笔绘制
                canvas.drawRoundRect(left, top, right, bottom,mRectHeight/2.0f,mRectHeight/2.0f, mHitPitchPaint);
//                canvas.drawRect(left, top, right, bottom, mHitPitchPaint);
            }
        }


    }
    private void drawPitchPoint(Canvas canvas) {
        // 画当前音高值，此处是用一个三角形图标表示当前高音值位置

        Path path = new Path();
        path.moveTo(mMidX-TRIANGLE_WIDTH,mMidY-TRIANGLE_HEIGHT/2);
        path.lineTo(mMidX,mMidY);
        path.lineTo(mMidX-TRIANGLE_WIDTH,mMidY+TRIANGLE_HEIGHT/2);
        path.close();
        canvas.drawPath(path,mTrianglePaint);


        Iterator<MusicPitchScore> it = scoreList.iterator();
        long time = System.currentTimeMillis();
        while (it.hasNext()){
            MusicPitchScore score = it.next();
            if(time - score.getStartTime() > ANIM_TOTAL_TIME){
                it.remove();
            }else {
                mScorePaint.setAlpha(getAlpha(time - score.getStartTime()));

                String text = "+" + score.getScore();
                float textWidth = mScorePaint.measureText(text);
                Rect lineBound = new Rect();
                mScorePaint.getTextBounds(text, 0, text.length(), lineBound);
                float textHeight = lineBound.height();
                if (mScoreOffsetX < 0) {
                    canvas.drawText(text, mMidX + mScoreOffsetX - textWidth, getTop(score.getTop() + mScoreOffsetY + textHeight / 2, time - score.getStartTime()), mScorePaint);
                } else {
                    canvas.drawText(text, mMidX + mScoreOffsetX, getTop(score.getTop() + mScoreOffsetY + textHeight / 2, time - score.getStartTime()), mScorePaint);
                }
            }

        }
    }

    private int getAlpha(long time){
        if(time - (ANIM_TOTAL_TIME - ANIM_END_TIME) > 0){
            return (int) ((1- (time - (ANIM_TOTAL_TIME - ANIM_END_TIME)) * 1.0f/(ANIM_TOTAL_TIME - ANIM_END_TIME)) * 255);
        }else  if(time  > ANIM_START_TIME){
            return 255;
        }else {
            return (int) (time * 1.0f / ANIM_START_TIME * 255);
        }
    }

    private float getTop(float top,long time){

        if(top < mAnimHeight){

            return mAnimHeight - getTopOffset(time);

        }else{

            return top - getTopOffset(time);
        }

    }

    private float getTopOffset(long time){
        if(time < (ANIM_START_TIME + ANIM_STARTING_TIME)){
            return 0;
        }else {
            return getInterpolatorValue((time - (ANIM_START_TIME  + ANIM_STARTING_TIME)* 1.0f) / (ANIM_RUNNING_TIME + ANIM_END_TIME)) * mAnimHeight;
        }
    }

    private float getInterpolatorValue(float value){
        return pathInterpolator.getInterpolation(value);
    }


    // 根据音高值算出top值
    private float getPitchTop(int pitch){

        if(pitch > MUSIC_MAX_PITCH){
            return getPitchTop(MUSIC_MAX_PITCH) - mRectHeight / 2;
        }

        if(pitch < MUSIC_MIN_PITCH){
            return getPitchTop(MUSIC_MIN_PITCH) + mRectHeight / 2;
        }


        return  (int)((MUSIC_MAX_PITCH - pitch-1) * MUSIC_PITCH_NUM /(MUSIC_MAX_PITCH -
                MUSIC_MIN_PITCH)) * mRectHeight + getPaddingTop();
    }

    private double getPitchTone(int pitch) {
        double eps = 1e-6;
        return Math.max(0, Math.log(pitch / 55.0 + eps) / Math.log(2)) * 12;
    }

    private float getRawSize(float size) {
        Context context = getContext();
        Resources resources;
        if (context == null) {
            resources = Resources.getSystem();
        } else {
            resources = context.getResources();
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, resources.getDisplayMetrics());
    }

    private List<MusicPitch> mergeMusicPitch(List<MusicPitch> pitchList,int statIndex,int endIndex){
        List<MusicPitch> temp = new ArrayList<>();
        if(pitchList == null){
            return temp;
        }

        int size = pitchList.size();

        if(endIndex >= size){
            endIndex = size-1;
        }

        for(int i=statIndex; i < size && i <= endIndex;i++){

            MusicPitch pitch = pitchList.get(i);

            if(pitch == null){
                continue;
            }

            temp.add(new MusicPitch(pitch.getStartTime(),pitch.getDuration(),pitch.getPitch()));


        }
        return temp;

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

    /*
     * ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ *
     *                                                                                             对外API                                                                                        *
     * ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ *
     * */

    public void setStandardPitch(String pitchJson) throws JSONException {
        if (pitchJson == null) {
            return;
        }
        List<MusicPitch> musicPitchList = PitchViewHelper.parseMidiFile(pitchJson);

        mMusicPitchList.addAll(musicPitchList);
    }


    /**
     * 传入音高线数组
     */
    public void setStandardPitch(List<MusicPitch> pitchList){
        reset();
        mMusicPitchList.addAll(pitchList);
        postInvalidate();
    }

    /**
     * 设置当前播放时间和音阶，两个参数必须同时设置，保证数据同步性
     *
     * @param progress 当前播放时间
     * @param pitch 实时音高
     */
    public void setCurrentSongProgress(long progress, int pitch){
        mStartTime = progress - TIME_ELAPSED_ON_SCREEN;
        mCurrentSongTime = progress;
        mCurrentMusicPitch = pitch;
        // 根据当前时间找到对应的音高线数据
        List<Integer> indexList = getCurrentMusicList(mMusicPitchList,mCurrentSongTime - ESTIMATED_CALL_INTERVAL - ESTIMATED_CALL_INTERVAL_OFFSET, ESTIMATED_CALL_INTERVAL + ESTIMATED_CALL_INTERVAL_OFFSET);

        for(Integer currentMusicIndex: indexList) {
            int offsetPitch = -1;

            if (currentMusicIndex >= 0) { //能够找到索引，有音高对应的情况
                MusicPitch musicPitch = mMusicPitchList.get(currentMusicIndex);
                if (musicPitch != null) {
                    offsetPitch = getOffsetPitch(pitch, musicPitch.pitch);
                }
                if (offsetPitch == -1) {
                    mCurrentMusicPitch = 0;//无声情况
                } else {
                    // MusicPitch musicPitch = mMusicPitchList.get(currentMusicIndex);
                    // 根据偏差计算出实际音高值
                    if (musicPitch != null) {
                        mCurrentMusicPitch = musicPitch.getPitch() + offsetPitch * 4;
                        //如果offsetPitch是0，那么当前的音高块是被击中了
                        if (offsetPitch == 0) {
                            // 击中块的开始时间不能提前当前musicPitch的开始时间，不能越界
                            int hitStartTime = (int) Math.max(musicPitch.getStartTime(),
                                    mCurrentSongTime - ESTIMATED_CALL_INTERVAL - ESTIMATED_CALL_INTERVAL_OFFSET);
                            // 击中块的开始时间不能提前当前musicPitch的结束时间，不能越界
                            int hitEndTime = (int) Math.min(musicPitch.getStartTime() +
                                    musicPitch.getDuration(), mCurrentSongTime);
                            if (hitEndTime - hitStartTime != 0) {
                                addHitRectList(new MusicPitch(hitStartTime, hitEndTime - hitStartTime,
                                        musicPitch.getPitch()));
                            }

                        }
                        // update score info
                        calculateScore(pitch, musicPitch.pitch);
                    }
                }
            } else {//找不到匹配的标准音高
                // 这种情况下，pitchOrPitchHit应该是实际音高值
                mCurrentMusicPitch = pitch;
            }
        }

        final float currentValue = mMidY;
        final long lastTime = mStartTime;
        final float nextValue = getPitchTop(mCurrentMusicPitch)+ mRectHeight / 2.0f;
        // 三角形滚动动效
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(TRIANGLE_ANIM_TIME);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMidY = currentValue + (nextValue - currentValue) * animation.getAnimatedFraction();
                mStartTime = lastTime + (int)(ESTIMATED_CALL_INTERVAL * animation.getAnimatedFraction());
                invalidateView();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMidY = nextValue;
                mStartTime = lastTime + ESTIMATED_CALL_INTERVAL;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mMidY = nextValue;
                mStartTime = lastTime + ESTIMATED_CALL_INTERVAL;
            }
        });
        valueAnimator.start();
        // 触发绘制
        postInvalidate();
    }


    /**
     * 设置高潮片段 当前播放时间和音阶，两个参数必须同时设置，保证数据同步性(仅用于高潮片段资源)
     *
     * @param progress 当前播放时间
     * @param pitch 实时音高
     * @param segmentBegin 高潮片段开始时间 （该字段在请求高潮片段资源时返回）
     * @param krcFormatOffset krc歌词对歌曲的偏移量 （该字段在krc歌词中返回）
     */
    public void setAccompanimentClipCurrentSongProgress(long progress, int pitch,long segmentBegin,long krcFormatOffset){
        setCurrentSongProgress(progress + segmentBegin - krcFormatOffset,pitch);
    }

    /**
     * 添加分数在当前时间点展示
     */
    public void addScore(int score){
        if (score > 0) {
            final long time = System.currentTimeMillis();

            post(new Runnable() {
                     @Override
                     public void run() {
                         MusicPitchScore ktvScore = new MusicPitchScore();
                         ktvScore.setStartTime(time);
                         ktvScore.setScore(score);
                         ktvScore.setTop(mMidY);
                         scoreList.add(ktvScore);
                     }
                 }
            );
        }
    }


    /**
     * 重置音高线数据
     */
    public void reset(){
        mMusicPitchList.clear();
        mHitRectList.clear();
        mCurrentMusicPitch = 0;
        mStartTime = 0;
        mCurrentSongTime = 0;
        mMidY = getPitchTop(0);
        scoresPerLine.clear();
    }

    /**
     * 获取标准音高线开始的时间
     */
    public long getPitchStartTime(){

        if(mMusicPitchList.size() > 0){
            return mMusicPitchList.get(0).getStartTime();
        }

        return 0;
    }

    /**
     * 设置UI配置
     */
    public void setUIConfig(PitchViewHelper.AliyunKTVPitchViewUIConfig AliyunKTVPitchViewUIConfig){
        mStandardPitchPaint.setColor(AliyunKTVPitchViewUIConfig.getStandardPitchColor());
        mHitPitchPaint.setColor(AliyunKTVPitchViewUIConfig.getHitPitchColor());
        mStaffPaint.setColor(AliyunKTVPitchViewUIConfig.getStaffColor());
        mVerticalLinePaint.setColor(AliyunKTVPitchViewUIConfig.getVerticalLineColor());
        mTrianglePaint.setColor(AliyunKTVPitchViewUIConfig.getPitchIndicatorColor());
        mScorePaint.setColor(AliyunKTVPitchViewUIConfig.getScoreTextColor());
        postInvalidate();
    }

    /**
     * 设置评分配置，控制演唱得分难度，两个参数最好只设置其中一个，另外一个为0
     *
     * @param scoreLevel 演唱得分难度，取值范围[0-100]，默认值为 10，值越小难度越低，演唱者越容易得高分
     * @param scoreCompensationOffsetLevel 演唱评分偏移量，取值范围 [-100,100]，默认值为 0，最终得分会在计算时在原有得分基础上加上偏移量
     */
    public void setScoreConfig(int scoreLevel, int scoreCompensationOffsetLevel) {
        mScoreLevel = scoreLevel;
        mScoreCompensationOffsetLevel = scoreCompensationOffsetLevel;
    }

    /**
     * 获取当前句得分，建议每行歌词结束时调用
     */
    public int getCurLineScore() {

        if(curLineScores.isEmpty()) {
            return 0;
        }

        int sum = 0;
        for (int score : curLineScores) {
            sum += score;
        }
        int curLineScore = sum / curLineScores.size();
        scoresPerLine.add(curLineScore);
        curLineScores.clear();
        return curLineScore;
    }

    /**
     * 获取整首歌最终得分（为各句总分）
     */
    public int getFinalScore() {
        int sum = 0;
        if (scoresPerLine.isEmpty()) {
            return 0;
        } else {
            for (int score : scoresPerLine) {
                sum += score;
            }
            return sum;// / scoresPerLine.size();
        }
    }

}
