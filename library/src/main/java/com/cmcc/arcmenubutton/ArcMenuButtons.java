package com.cmcc.arcmenubutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.cmcc.hfuikit.DisplayUtil;

import librarys.develop.bmr.library.R;


public class ArcMenuButtons extends RelativeLayout {

    private Context context;
    private RelativeLayout mRlView;
    private ImageButton mMenuButton;
    private ImageButton[] mItemButtons;
    private int count = 0;
    private ImageButton[] mItemViews;
    private int menuSrc;
    private View shadow;
    private int type = 0;
    private int itemNums = 0;

    private static final int RADIUS = 200;
    private static final int RADIUS_MIDDLE = 150;
    private static final int ANIMATION_DURATION_MENU = 300;// ms
    private static final int ANIMATION_DURATION_ITEM = 500;// ms
    private static final int DIP_PADDING = 200;// dp，底部Menu吸附栏的高度
    private boolean mIsMenuOpen = false;// 是否菜单打开状态
    private boolean mIsLongClick = false;// 是否处于长按操作中

    private Rect imgRectL = new Rect();
    private Rect imgRectM = new Rect();
    private Rect imgRectR = new Rect();
    private RectF[] rects = new RectF[3];

    private int mItemSize;
    private int mItemMargin;

    public ArcMenuButtons(Context context) {
        this(context, null);
    }

    public ArcMenuButtons(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    R.styleable.ArcMenu, 0, 0);
            itemNums = a.getInteger(R.styleable.ArcMenu_itemCount, 0);
            mItemSize = (int) Math.max(
                    a.getDimension(R.styleable.ArcMenu_itemSize, 0f), 0f);
            mItemMargin = (int) Math.max(
                    a.getDimension(R.styleable.ArcMenu_itemMargin, 0f), 0f);
            menuSrc = a.getResourceId(R.styleable.ArcMenu_menuSrc, 0);
            a.recycle();
        }
    }

    public ArcMenuButtons(Context context, int type) {
        this(context, null);
        this.context = context;
        this.type = type;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
        setType(type);
        int[] size = DisplayUtil.getScreenSize(context);
        int screenWidth = size[0];
        int screenHeight = size[1];
        int padding = DisplayUtil.dip2px(context, DIP_PADDING);
        // l,t,r,b
        rects[0] = new RectF(0, screenHeight - padding, 0.35f * screenWidth,
                screenHeight);
        rects[1] = new RectF(0.35f * screenWidth, screenHeight - padding,
                0.7f * screenWidth, screenHeight);
        rects[2] = new RectF(0.7f * screenWidth, screenHeight - padding,
                screenWidth, screenHeight);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        onTypeChanged(type);
    }

    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_layout,
                this, true);
        // 阴影层
        shadow = (View) view.findViewById(R.id.shadow);
        mRlView = (RelativeLayout) view.findViewById(R.id.rl_menu_group);
        mMenuButton = (ImageButton) view.findViewById(R.id.menu);
        mMenuButton.setImageResource(menuSrc);
        mItemButtons = new ImageButton[itemNums];

        mItemViews = new ImageButton[3];
        mItemViews[0] = (ImageButton) view.findViewById(R.id.iv_left);
        mItemViews[1] = (ImageButton) view.findViewById(R.id.iv_middle);
        mItemViews[2] = (ImageButton) view.findViewById(R.id.iv_right);

        // imageview 移动时不重绘，变成ImageButton可以
        // 获取位置
        mItemViews[0].getLocalVisibleRect(imgRectL);
        mItemViews[1].getLocalVisibleRect(imgRectM);
        mItemViews[2].getLocalVisibleRect(imgRectR);
        // mItemLayout.setVisibility(View.GONE);
        shadow.setVisibility(View.GONE);
        // 设置监听器
        mMenuButton.setOnLongClickListener(longClickListener);
        mMenuButton.setOnClickListener(clickListener);
        shadow.setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                mMenuButton.callOnClick();
            }

        });

    }

    OnLongClickListener longClickListener = new OnLongClickListener() {

        @SuppressLint("NewApi")
        @Override
        public boolean onLongClick(View v) {
            if (v == mMenuButton) {
                mIsLongClick = true;
                if (!mIsMenuOpen) {
                    // menu未打开的时候长按课拖动
                    shadow.setVisibility(View.VISIBLE);
                    mMenuButton.setVisibility(View.INVISIBLE);
                    // 根据type设置潜在目标位置的可视性
                    switch (type) {
                        case 0:
                            mItemViews[0].setVisibility(View.VISIBLE);
                            mItemViews[1].setVisibility(View.VISIBLE);
                            mItemViews[2].setVisibility(View.VISIBLE);
                            type = 0;
                            // 获取潜在目标位置坐标
                            // 当前位置作为起点
                            mItemViews[0].setBackgroundResource(R.drawable.habit);
                            mItemViews[0]
                                    .setOnTouchListener(new TouchClickListener());
                            mItemViews[0].setClickable(true);
                            mItemViews[1].setClickable(false);
                            mItemViews[2].setClickable(false);
                            break;
                        case 1:
                            mItemViews[0].setVisibility(View.VISIBLE);
                            mItemViews[1].setVisibility(View.VISIBLE);
                            mItemViews[2].setVisibility(View.VISIBLE);
                            type = 1;
                            // 获取潜在目标位置坐标
                            mItemViews[1].setBackgroundResource(R.drawable.habit);
                            mItemViews[1]
                                    .setOnTouchListener(new TouchClickListener());
                            mItemViews[0].setClickable(false);
                            mItemViews[1].setClickable(true);
                            mItemViews[2].setClickable(false);
                            break;
                        case 2:
                            mItemViews[0].setVisibility(View.VISIBLE);
                            mItemViews[1].setVisibility(View.VISIBLE);
                            mItemViews[2].setVisibility(View.VISIBLE);
                            type = 2;
                            mItemViews[2].setBackgroundResource(R.drawable.habit);
                            mItemViews[2]
                                    .setOnTouchListener(new TouchClickListener());
                            mItemViews[1].setClickable(false);
                            mItemViews[0].setClickable(false);
                            mItemViews[2].setClickable(true);
                            // 获取潜在目标位置坐标
                            break;
                    }

                }
                else {
                    // menu打开的时候长按无动作
                }
            }
            return true;
        }
    };

    OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == mMenuButton) {
                if (!mIsMenuOpen) {
                    if (mIsLongClick) {
                        shadow.setVisibility(View.INVISIBLE);
                        // （如点击shadow）取消长按后恢复原状态
                        mMenuButton.setVisibility(View.VISIBLE);

                        mIsLongClick = false;
                        for (int i = 0; i < mItemViews.length; i++) {
                            mItemViews[i].setVisibility(View.GONE);
                        }
                    }
                    else {
                        openMenu();
                    }
                }
                else {
                    closeMenu();
                }
            }
            else {
                if (onButtonClickListener != null) {
                    int selectedItem = v.getId();
                    onButtonClickListener.onButtonClick(v, selectedItem);
                }
            }
        }

    };

    public void openMenu() {
        mIsMenuOpen = true;
        shadow.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(mMenuButton, "rotation", 0, 180f)
                .setDuration(ANIMATION_DURATION_MENU).start();
        for (int i = 0; i < itemNums; i++) {
            doAnimateOpen(mItemButtons[i], i);
        }
    }

    public void closeMenu() {
        mIsMenuOpen = false;
        shadow.setVisibility(View.GONE);
        ObjectAnimator.ofFloat(mMenuButton, "rotation", 180f, 0)
                .setDuration(ANIMATION_DURATION_MENU).start();
        for (int i = 0; i < itemNums; i++) {
            doAnimateClose(mItemButtons[i], i);
        }
    }

    /**
     * 打开菜单的动画
     *
     * @param view  执行动画的view
     * @param index view在动画序列中的顺序
     */
    private void doAnimateOpen(View view, int index) {

        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        int[] translation = computeTrans(index);
        AnimatorSet set = new AnimatorSet();
        // 包含平移、缩放和透明度动画
        set.playTogether(ObjectAnimator.ofFloat(view, "translationX", 0,
                        translation[0]), ObjectAnimator.ofFloat(view, "translationY",
                        0, translation[1]), ObjectAnimator.ofFloat(view, "scaleX", 0f,
                        1f), ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1), ObjectAnimator
                        .ofFloat(view, "rotation", 0f, 360f, 0f, 360f, 0f,
                                360f, 0f, 360f));
        // 动画周期为500ms
        set.setDuration(ANIMATION_DURATION_ITEM).start();
        set.setInterpolator(new OvershootInterpolator(1.5f));
    }

    /**
     * 关闭菜单的动画
     *
     * @param view  执行动画的view
     * @param index view在动画序列中的顺序
     */
    private void doAnimateClose(final View view, int index) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        int[] translation = computeTrans(index);
        AnimatorSet set = new AnimatorSet();
        // 包含平移、缩放和透明度动画
        set.playTogether(ObjectAnimator.ofFloat(view, "translationX",
                translation[0], 0), ObjectAnimator.ofFloat(view,
                "translationY", translation[1], 0), ObjectAnimator.ofFloat(
                view, "scaleX", 1f, 0f), ObjectAnimator.ofFloat(view, "scaleY",
                1f, 0f), ObjectAnimator.ofFloat(view, "rotation", 360f, 0f,
                360f, 0f, 360f, 0f, 360f, 0f), ObjectAnimator.ofFloat(view,
                "alpha", 1f, 0f));
        // 为动画加上事件监听，当动画结束的时候，我们把当前view隐藏
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mMenuButton.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
                mMenuButton.setEnabled(true);
            }

        });
        set.setInterpolator(new AnticipateOvershootInterpolator(1.5f));
        set.setDuration(ANIMATION_DURATION_ITEM).start();
    }

    public boolean isMenuOpen() {
        return isMenuOpen();
    }

    private OnButtonClickListener onButtonClickListener;

    public interface OnButtonClickListener {
        void onButtonClick(View v, int id);
    }

    public void setOnButtonClickListener(
            OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    /**
     * 根据type类型计算每个item应该平移的坐标
     *
     * @param index item的位置
     * @return
     */
    private int[] computeTrans(int index) {
        int radius = 0;
        double degree = 0;
        int translationX = 0;
        int translationY = 0;
        switch (type) {
            case 2:
                // Menu在右方，向左方展开
                radius = RADIUS;
                degree = Math.PI / 2 - Math.PI * index / ((itemNums - 1) * 2);
                translationX = (int) (radius * Math.cos(degree) * -1);
                translationY = (int) (radius * Math.sin(degree) * -1);
                break;
            case 1:
                // MIDDLE
                radius = RADIUS_MIDDLE;
                degree = Math.PI - Math.PI * index / ((itemNums - 1));
                translationX = (int) (radius * Math.cos(degree) * -1);
                translationY = (int) (radius * Math.sin(degree) * -1);
                break;
            case 0:
                // Menu在左方，向右方展开
                radius = RADIUS;
                degree = Math.PI * index / ((itemNums - 1) * 2);
                translationX = (int) (radius * Math.cos(degree) * 1);
                translationY = (int) (radius * Math.sin(degree) * -1);
                break;
            default:
                break;
        }
        return new int[]
                {translationX, translationY};
    }

    private void onTypeChanged(int type) {
        // 根据type设置位置
        {
            this.type = type;
            switch (type) {
                case 0:
                    mRlView.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    break;
                case 1:
                    mRlView.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                    break;
                case 2:
                    mRlView.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                    break;
            }
            mRlView.invalidate();
        }
    }

    class TouchClickListener implements OnTouchListener {
        int lastX, lastY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    // 获取触摸事件触摸位置的原始X坐标
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;
                    int l = v.getLeft() + dx;
                    int b = v.getBottom() + dy;
                    int r = v.getRight() + dx;
                    int t = v.getTop() + dy;
                    // 利用相对坐标在父控件中重绘
                    v.layout(l, t, r, b);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    v.postInvalidate();
                    // 下面判断移动是否与潜在目标有重叠
                    break;
                case MotionEvent.ACTION_UP:
                    for (int i = 0; i < 3; i++) {
                        if (rects[i].contains(lastX, lastY)) {
                            resetImagePos(mItemViews[type], type);
                            // 更新type
                            onTypeChanged(i);
                            mItemViews[0].setVisibility(View.INVISIBLE);
                            mItemViews[1].setVisibility(View.INVISIBLE);
                            mItemViews[2].setVisibility(View.INVISIBLE);
                            mMenuButton.setVisibility(View.VISIBLE);
                            shadow.setVisibility(View.INVISIBLE);
                            mIsLongClick = false;
                            break;
                        }
                        else {
                            // 释放位置不在三种指定位置的时候，不移动
                            mItemViews[type].setVisibility(View.INVISIBLE);
                            resetImagePos(mItemViews[type], type);
                            mMenuButton.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }
            return false;
        }
    }

    /**
     * 恢复拖拽过程利用的引导图片位置
     *
     * @param v
     * @param type
     */
    private void resetImagePos(View v, int type) {
        v = mItemViews[type];
        int l = 0, t = 0, r = 0, b = 0;
        switch (type) {
            case 0:
                l = imgRectL.left;
                t = imgRectL.bottom;
                r = imgRectL.right;
                b = imgRectL.bottom;
                break;
            case 1:
                l = imgRectM.left;
                t = imgRectM.bottom;
                r = imgRectM.right;
                b = imgRectM.bottom;
                break;
            case 2:
                l = imgRectR.left;
                t = imgRectR.bottom;
                r = imgRectR.right;
                b = imgRectR.bottom;
                break;
        }
        v.setBackgroundResource(R.drawable.icon_add);
        v.layout(l, t, r, b);
        v.postInvalidate();
    }

    /**
     * 向Menu中动态添加子Item
     *
     * @param item
     */
    public void addItem(View item) {
        //利用RelativeLayout
        int sizePx = DisplayUtil.dip2px(context, mItemSize);
        int marginPx = DisplayUtil.dip2px(context, mItemMargin);
        LayoutParams params;
        if (sizePx > 0) {
            params = new LayoutParams(sizePx, sizePx);
        }
        else {
            int size = DisplayUtil.dip2px(context, 50);
            params = new LayoutParams(size, size);
        }
        if (marginPx > 0) {
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
        }
        else {
            int margin = DisplayUtil.dip2px(context, 15);
            params.setMargins(margin, margin, margin, margin);
        }
        params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.menu);
        mRlView.addView(item, params);
        mItemButtons[count] = (ImageButton) item;
        count++;
        item.setVisibility(View.INVISIBLE);
        item.setOnClickListener(clickListener);
    }
}
