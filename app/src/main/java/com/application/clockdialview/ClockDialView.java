package com.application.clockdialview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class ClockDialView extends View {
    public ClockDialView(Context context) {
        super(context);
        initView(context, null);

    }

    public ClockDialView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs);
    }

    /*
     * 默认初始时间 6:30,分开写两份是因为绘制时圆环内的时间和中间的时间不同步更新
     * ps:可能是我菜了想不到更好的写法,至少这样能用,不会出现数字闪烁的问题
     */
    private int centerHourText = 6;
    private int centerMinuteText = 30;
    private int innerHourText = 6;
    private int outerMinuteText = 30;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth=getMeasuredWidth();
        // 因为希望绘制的表盘是正方形的,所以两者都设置成了 width
        setMeasuredDimension(measureWidth,measureWidth);

    }

    /*
     * 绘制的主要参数
     */

    // 从中心点到圆环中间的半径
    private float radius = 0;
    // 内圈圆环的宽度
    private float innerArcWidth=150;
    // 外圈圆环的宽度
    private float outerArcWidth=150;

    // 背景颜色
    int backGroundColor = Color.parseColor("#222222");
    // 内圈圆环背景颜色
    int innerBackGroundColor = Color.parseColor("#1c1c1c");
    // 外圈圆环背景颜色
    int outerBackGroundColor = Color.parseColor("#181818");
    // 外圈圆环外面的阴影颜色
    int shadowColor = Color.parseColor("#333333");

    // 内圈选中的背景颜色
    int innerSelectedColor = Color.parseColor("#242424");
    // 外圈选中的背景颜色
    int outerSelectedColor = Color.parseColor("#1d1d1d");

    // 内圈数字的字体颜色
    int innerHourTextColor = Color.WHITE;
    // 外圈数字的字体颜色
    int outerMinuteTextColor = Color.WHITE;
    // 中心时间的字体颜色
    int centerTextColor = Color.WHITE;

    // 内圈数字的字体大小
    int innerHourTextSize = 48;
    // 外圈数字的字体大小
    int outerMinuteTextSize = 64;
    // 中心时间的字体大小
    int centerTextSize = 96;

    // view 相对于中心的水平偏移
    int offsetX = 0;
    // view 相对于中心的垂直偏移,需要遮住 view 的上面一部分,否则看到的数字是不连续的
    int offsetY = 240;

    private Paint paint=new Paint();
    // 计算滑动角度的初始点
    private final Point beginPoint=new Point();
    // 结束点
    private final Point endPoint=new Point();
    // 中心点,默认 1080p 的手机以 540,540 作为中心
    private final Point centerPoint=new Point(540,540);

    /*
     * getTime() 获取当前时间
     * setTime() 设置显示的时间
     */
    public int[] getTime(){
        return new int[]{centerHourText, centerMinuteText};
    }
    public void setTime(int[] time){
        this.centerHourText =time[0];this.centerMinuteText =time[1];
        this.innerHourText =time[0];this.outerMinuteText =time[1];
        invalidate();}
    /*
     * 从 attrs 中读取并设置自定义属性
     */
    private void initView(Context context, @Nullable AttributeSet attrs){
        if(attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockDialView);
            backGroundColor = typedArray.getColor(R.styleable.ClockDialView_backgroundColor,backGroundColor);
            innerBackGroundColor = typedArray.getColor(R.styleable.ClockDialView_inner_BackgroundColor,innerBackGroundColor);
            outerBackGroundColor = typedArray.getColor(R.styleable.ClockDialView_outer_BackgroundColor,outerBackGroundColor);
            shadowColor = typedArray.getColor(R.styleable.ClockDialView_shadowColor,shadowColor);

            innerSelectedColor = typedArray.getColor(R.styleable.ClockDialView_inner_SelectedColor,innerSelectedColor);
            outerSelectedColor = typedArray.getColor(R.styleable.ClockDialView_outer_SelectedColor,outerSelectedColor);

            innerHourTextColor = typedArray.getColor(R.styleable.ClockDialView_inner_HourTextColor,innerHourTextColor);
            outerMinuteTextColor = typedArray.getColor(R.styleable.ClockDialView_outer_MinuteTextColor,outerMinuteTextColor);
            centerTextColor = typedArray.getColor(R.styleable.ClockDialView_center_TextColor,centerTextColor);

            radius = typedArray.getFloat(R.styleable.ClockDialView_radius,radius);
            innerArcWidth = typedArray.getFloat(R.styleable.ClockDialView_innerArcWidth,innerArcWidth);
            outerArcWidth = typedArray.getFloat(R.styleable.ClockDialView_outerArcWidth,outerArcWidth);

            innerHourTextSize = typedArray.getInteger(R.styleable.ClockDialView_inner_HourTextSize,innerHourTextSize);
            outerMinuteTextSize = typedArray.getInteger(R.styleable.ClockDialView_outer_MinuteTextSize,outerMinuteTextSize);
            centerTextSize = typedArray.getInteger(R.styleable.ClockDialView_center_TextSize,centerTextSize);

            offsetX = typedArray.getInteger(R.styleable.ClockDialView_offsetX,offsetX);
            offsetY = typedArray.getInteger(R.styleable.ClockDialView_offsetY,offsetY);

            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.setBackgroundColor(backGroundColor);
        this.setClickable(true);
        centerPoint.set(getWidth()/2,getWidth()/2);
        if(radius == 0) {
            radius = centerPoint.x + 50;
        }
        // 设置 view 的偏移
        centerPoint.x-=offsetX;
        centerPoint.y-=offsetY;
        // 绘制背景
        drawBackground(canvas);
        // 绘制内圈数字
        drawInnerText(canvas);
        // 绘制外圈数字
        drawOuterText(canvas);
        // 绘制中心数字
        drawCenterText(canvas);
        super.onDraw(canvas);
    }
    // 内圈转动角度
    private float innerRotateAngle =0;
    // 外圈转动角度
    private float outerRotateAngle =0;
    // 回弹结束角度
    private float endRotateAngle;

    private void drawInnerText(Canvas canvas){
        canvas.save();

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(innerHourTextColor);
        paint.setTextSize(innerHourTextSize);

        canvas.rotate(-150,centerPoint.x,centerPoint.y);
        float textRotate= innerRotateAngle %30;
        canvas.rotate(textRotate,centerPoint.x,centerPoint.y);
        String[] strings=new String[12];
        for(int i=-5;i<7;i++){
            int text= innerHourText +i;
            if(text>12) text-=24;
            if(text<0) text+=24;
            strings[i+5]=String.valueOf(text);
        }

        int thisCanvasRotate = (int) textRotate;

        for(int i=0;i<11;i++) {
            Rect bounds=new Rect();
            //设置字体透明渐变,以中心点正下方的透明度为 0,向左右两边递增
            paint.setAlpha(255 - Math.abs(150 - thisCanvasRotate));
            // 获取数字边框大小,让字体居中绘制
            paint.getTextBounds(strings[i],0,strings[i].length(),bounds);
            canvas.drawText(strings[i], centerPoint.x -bounds.width()/2, centerPoint.y+radius-outerArcWidth-innerArcWidth/2+bounds.height()/2, paint);
            canvas.rotate(30,centerPoint.x,centerPoint.y);
            thisCanvasRotate+=30;

        }
        canvas.restore();
    }
    // 绘制外圈数字
    private void drawOuterText(Canvas canvas){
        canvas.save();

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(outerMinuteTextColor);
        paint.setTextSize(outerMinuteTextSize);

        canvas.rotate(-150,centerPoint.x,centerPoint.y);
        float textRotate= outerRotateAngle %30;
        canvas.rotate(textRotate,centerPoint.x,centerPoint.y);

        int thisCanvasrotate = (int) textRotate;

        String[] strings=new String[11];

        for(int i=-5;i<6;i++){
            int text= outerMinuteText +i;
            if(text>59) text-=60;
            if(text<0) text+=60;
            strings[i+5]=String.valueOf(text);
        }
        for(int i=0;i<11;i++) {
            Rect bounds=new Rect();
            //设置字体透明渐变
            paint.setAlpha(255 - Math.abs(150 - thisCanvasrotate));
            paint.getTextBounds(strings[i],0,strings[i].length(),bounds);
            canvas.drawText(strings[i], centerPoint.x -bounds.width()/2, centerPoint.y+radius-outerArcWidth/2+bounds.height()/2, paint);
            canvas.rotate(30,centerPoint.x,centerPoint.y);
            thisCanvasrotate+=30;
        }

        canvas.restore();
    }
    // 绘制内圈数字
    private void drawCenterText(Canvas canvas){
        canvas.save();

        paint.reset();
        paint.setColor(centerTextColor);
        paint.setAntiAlias(true);
        paint.setTextSize(centerTextSize);

        int[] time=getTime();
        String strHour;
        if(time[0]<10) strHour="0"+time[0];
        else strHour=time[0]+"";
        String strMinute;
        if(time[1]<10) strMinute="0"+time[1];
        else strMinute=time[1]+"";
        String str=strHour+":"+strMinute;

        Rect rect=new Rect();
        paint.getTextBounds(str,0,str.length(),rect);
        canvas.drawText(str,centerPoint.x-rect.width()/2,centerPoint.y+rect.height()/2,paint);

        canvas.restore();
    }
    // 绘制背景
    private void drawBackground(Canvas canvas){

        int centerX=centerPoint.x,centerY=centerPoint.y;

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        // 外圈
        paint.setStrokeWidth(outerArcWidth);
        paint.setColor(outerBackGroundColor);
        Path path=new Path();
        path.addCircle(centerX,centerY,radius-outerArcWidth/2, Path.Direction.CW);
        canvas.drawPath(path,paint);
        // 内圈
        paint.setStrokeWidth(innerArcWidth);
        paint.setColor(innerBackGroundColor);
        path.reset();
        path.addCircle(centerX,centerY,radius-outerArcWidth-innerArcWidth/2, Path.Direction.CW);
        canvas.drawPath(path,paint);
        // 内圈选中
        path.reset();
        path.addArc(centerPoint.x-radius+outerArcWidth+innerArcWidth/2,centerPoint.y-radius+outerArcWidth+innerArcWidth/2,
                centerPoint.x+radius-outerArcWidth-innerArcWidth/2,centerPoint.y+radius-outerArcWidth-innerArcWidth/2,
                80,20);

        paint.setColor(innerSelectedColor);
        canvas.drawPath(path,paint);
        //外圈选中
        path.reset();
        path.addArc(centerPoint.x-radius+outerArcWidth/2,centerPoint.y-radius+outerArcWidth/2,
                centerPoint.x+radius-outerArcWidth/2,centerPoint.y+radius-outerArcWidth/2,
                82,16);
        paint.setColor(outerSelectedColor);
        canvas.drawPath(path,paint);
        // 外圈外面的阴影
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(shadowColor);
        paint.setMaskFilter(new BlurMaskFilter(100, BlurMaskFilter.Blur.OUTER));
        path.addCircle(centerX,centerY,radius-outerArcWidth/2, Path.Direction.CW);
        canvas.drawPath(path,paint);


    }
    // 判断触摸位置在内圈还是外圈
    private boolean innerArcflag=false;
    private boolean outerArcFlag=false;
    // 存储触摸时的时间作为基准改变触摸结束后的时间
    private int[] tempTime = new int[2];
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point point=new Point((int)event.getX(),(int)event.getY());

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // 判断当前触摸位置在内圈还是外圈
                innerArcflag = isTouchInInnerArc(point);
                outerArcFlag = isTouchInOuterArc(point);
                // 将内外圈旋转角度归 0
                innerRotateAngle = 0;
                outerRotateAngle = 0;
                beginPoint.set((int)event.getX(),(int)event.getY());

                tempTime[0] = centerHourText;
                tempTime[1] = centerMinuteText;
                break;
            case MotionEvent.ACTION_MOVE:
                // 处理内圈移动事件
                if(innerArcflag) {
                    endPoint.set((int) event.getX(), (int) event.getY());
                    // 中心点作为中心,计算手指滑过的角度
                    innerRotateAngle += calAngle(centerPoint, beginPoint, endPoint);
                    beginPoint.set((int) event.getX(), (int) event.getY());

                    //防抖动,只在转过角度大于 15° 的情况下才改变中间的时间
                    int tempAngle;
                    if (innerRotateAngle > 0) {
                        if (innerRotateAngle % 30 > 15)
                            tempAngle = (int) innerRotateAngle / 30 * 30 + 30;
                        else tempAngle = (int) innerRotateAngle / 30 * 30;
                    } else {
                        if (innerRotateAngle % 30 > -15)
                            tempAngle = (int) innerRotateAngle / 30 * 30;
                        else tempAngle = (int) innerRotateAngle / 30 * 30 - 30;
                    }
                    innerHourText = (int) innerRotateAngle / -30 + tempTime[0];
                    innerHourText = innerHourText % 24;
                    if (innerHourText < 0) innerHourText += 24;

                    centerHourText = tempAngle / -30 + tempTime[0];
                    centerHourText = centerHourText % 24;
                    if (centerHourText < 0) centerHourText += 24;

                    invalidate();
                    break;
                }
                // 处理外圈移动事件
                if(outerArcFlag) {
                    endPoint.set((int)event.getX(),(int)event.getY());
                    outerRotateAngle +=calAngle(centerPoint,beginPoint,endPoint);

                    //防抖动
                    int tempAngle;
                    if(outerRotateAngle >0) {
                        if (outerRotateAngle % 30 > 15)
                            tempAngle = (int) outerRotateAngle / 30 * 30 +30;
                        else tempAngle = (int) outerRotateAngle / 30 * 30 ;
                    } else {
                        if(outerRotateAngle %30>-15)
                            tempAngle=(int) outerRotateAngle /30*30;
                        else tempAngle=(int) outerRotateAngle /30*30-30;
                    }

                    outerMinuteText =(int)-outerRotateAngle /30 + tempTime[1];
                    outerMinuteText = outerMinuteText %60;
                    if(outerMinuteText <0) outerMinuteText +=60;

                    centerMinuteText =(int)-tempAngle/30 + tempTime[1];
                    centerMinuteText = centerMinuteText %60;
                    if(centerMinuteText <0) centerMinuteText +=60;

                    beginPoint.set((int)event.getX(),(int)event.getY());
                    invalidate();
                    break;
                }

            case MotionEvent.ACTION_UP:
                // 处理手指抬起后内圈事件
                if(innerArcflag) {
                    innerArcflag = false;

                    endRotateAngle = 0;

                    if (innerRotateAngle > 0) {
                        if (innerRotateAngle % 30 > 15)
                            endRotateAngle = (int) innerRotateAngle / 30 * 30 + 30;
                        else endRotateAngle = (int) innerRotateAngle / 30 * 30;
                    } else {
                        if (innerRotateAngle % 30 > -15)
                            endRotateAngle = (int) innerRotateAngle / 30 * 30;
                        else endRotateAngle = (int) innerRotateAngle / 30 * 30 - 30;
                    }

                    centerHourText = (int) endRotateAngle / -30 + tempTime[0];
                    centerHourText = centerHourText % 24;
                    if (centerHourText < 0) centerHourText += 24;

                    // 设置回弹动画
                    ObjectAnimator animator =
                            ObjectAnimator.ofFloat(this, "innerRotateAngle", innerRotateAngle, endRotateAngle);
                    animator.setDuration(200);
                    animator.start();
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            innerHourText = (int) endRotateAngle / -30 + tempTime[0];
                            innerHourText = innerHourText % 24;
                            if (innerHourText < 0) innerHourText += 24;
                            super.onAnimationEnd(animation);
                        }
                    });

                    innerRotateAngle = 0;

                    invalidate();
                }
                // 处理手指抬起后外圈事件
                if(outerArcFlag) {
                    outerArcFlag=false;
                    if(outerRotateAngle >0) {
                        if (outerRotateAngle % 30 > 15)
                            endRotateAngle = (int) outerRotateAngle / 30 * 30 +30;
                        else endRotateAngle = (int) outerRotateAngle / 30 * 30 ;
                    } else {
                        if(outerRotateAngle %30>-15)
                            endRotateAngle =(int) outerRotateAngle /30*30;
                        else endRotateAngle =(int) outerRotateAngle /30*30-30;
                    }

                    centerMinuteText =(int)endRotateAngle / -30 + tempTime[1];
                    centerMinuteText = centerMinuteText %60;
                    if(centerMinuteText <0) centerMinuteText +=60;

                    // 设置回弹动画
                    ObjectAnimator animator =
                            ObjectAnimator.ofFloat(this, "outerRotateAngle", outerRotateAngle, endRotateAngle);
                    animator.setDuration(200);
                    animator.start();
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            outerMinuteText =(int)endRotateAngle /- 30 + tempTime[1];
                            outerMinuteText = outerMinuteText %60;
                            if(outerMinuteText <0) outerMinuteText +=60;
                            super.onAnimationEnd(animation);
                        }
                    });

                    outerRotateAngle =0;
                    invalidate();
                }
        }

        return super.onTouchEvent(event);
    }

    /*
     * 设置动画需要的相关函数
     */
    public float getInnerRotateAngle(){return innerRotateAngle;}
    public void setInnerRotateAngle(float innerRotateAngle){this.innerRotateAngle = innerRotateAngle;invalidate();}
    public float getOuterRotateAngle(){return outerRotateAngle;}
    public void setOuterRotateAngle(float outerRotateAngle){this.outerRotateAngle = outerRotateAngle;invalidate();}

    private boolean isTouchInInnerArc(Point point){
        float dx=point.x-centerPoint.x;
        float dy=point.y-centerPoint.y;
        float distance=(float)Math.sqrt(dx*dx+dy*dy);
        return distance > getWidth()/2-outerArcWidth-innerArcWidth && distance < getWidth()/2 -outerArcWidth;
    }
    private boolean isTouchInOuterArc(Point point){
        float dx=point.x-centerPoint.x;
        float dy=point.y-centerPoint.y;
        float distance=(float)Math.sqrt(dx*dx+dy*dy);
        return distance > getWidth()/2-outerArcWidth && distance < getWidth()/2;
    }

    // 计算两点相对于中心点间的角度
    private float calAngle(Point center,Point first,Point second){
        double x1=first.x-center.x,y1=first.y-center.y;
        double x2=second.x-center.x,y2=second.y-center.y;
        double value=(x1*x2 + y1*y2) / (Math.sqrt(x1*x1 + y1*y1) * Math.sqrt(x2*x2 + y2*y2));
        if(value>1) value=1;
        if (value<-1) value=-1;
        boolean isClockwise= (x1*y2 - x2*y1)>0;
        return isClockwise?(float)Math.toDegrees(Math.acos(value)):(float)-Math.toDegrees(Math.acos(value));
    }
}
