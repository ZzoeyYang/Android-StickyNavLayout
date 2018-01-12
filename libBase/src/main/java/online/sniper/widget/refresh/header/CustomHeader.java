package online.sniper.widget.refresh.header;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import online.sniper.R;
import online.sniper.widget.refresh.CoolRefreshView;
import online.sniper.widget.refresh.PullHeader;

/**
 * Created by liushengwen on 2017/4/17.
 */
public class CustomHeader implements PullHeader {
    private ImageView imageView;
    private ImageView progressImageView;
    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;
    private Animation mOperatingRotateAnim;
    private int mRotateAniTime = 150;
    private View headerView;
    private int backgroundColor = Color.parseColor("#ffffff");

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        if (headerView != null) {
            headerView.setBackgroundColor(color);
        }
    }

    @Override
    public View createHeaderView(CoolRefreshView refreshView) {
        Context context = refreshView.getContext();
        headerView = LayoutInflater.from(context).inflate(R.layout.coolrefreshview_customheader, refreshView, false);
        imageView = (ImageView) headerView.findViewById(R.id.coolrefresh_custom_header_imageView);
        progressImageView = (ImageView) headerView.findViewById(R.id.coolrefresh_custom_header_progress_imageView);

        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(mRotateAniTime);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(mRotateAniTime);
        mReverseFlipAnimation.setFillAfter(true);

        mOperatingRotateAnim = new RotateAnimation(0, 359, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mOperatingRotateAnim.setInterpolator(new LinearInterpolator());
        mOperatingRotateAnim.setDuration(400);
        mOperatingRotateAnim.setRepeatCount(-1);
        mOperatingRotateAnim.setFillAfter(true);

        progressImageView.setAnimation(mOperatingRotateAnim);
        imageView.setAnimation(mFlipAnimation);
        headerView.setBackgroundColor(backgroundColor);
        return headerView;
    }

    @Override
    public void onPullBegin(CoolRefreshView refreshView) {
        progressImageView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        isDownArrow = true;
    }

    private boolean isDownArrow = true;

    @Override
    public void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance) {
        int offsetToRefresh = getConfig().offsetToRefresh(refreshView, headerView);
        if (status == CoolRefreshView.PULL_STATUS_TOUCH_MOVE) {
            if (currentDistance < offsetToRefresh) {
                if (!isDownArrow) {
                    imageView.clearAnimation();
                    imageView.startAnimation(mReverseFlipAnimation);
                    isDownArrow = true;
                }
            } else {
                if (isDownArrow) {
                    imageView.clearAnimation();
                    imageView.startAnimation(mFlipAnimation);
                    isDownArrow = false;
                }
            }
        }
    }

    @Override
    public void onRefreshing(CoolRefreshView refreshView) {
        imageView.clearAnimation();
        imageView.setVisibility(View.GONE);
        progressImageView.setVisibility(View.VISIBLE);
        progressImageView.clearAnimation();
        progressImageView.startAnimation(mOperatingRotateAnim);
    }

    @Override
    public void onReset(CoolRefreshView refreshView, boolean pullRelease) {
        imageView.setVisibility(View.GONE);
        progressImageView.setVisibility(View.GONE);
        imageView.clearAnimation();
        progressImageView.clearAnimation();
    }

    @Override
    public void onPullRefreshComplete(CoolRefreshView refreshView) {
        imageView.setVisibility(View.GONE);
        progressImageView.setVisibility(View.GONE);
        imageView.clearAnimation();
        progressImageView.clearAnimation();
    }

    private Resources getResources() {
        return headerView.getResources();
    }


    @Override
    public Config getConfig() {
        return config;
    }

    private DefaultConfig config = new DefaultConfig() {
        @Override
        public int offsetToRefresh(CoolRefreshView refreshView, View headerView) {
            return (int) (headerView.getMeasuredHeight() / 3 * 1.2f);
        }

        @Override
        public int offsetToKeepHeaderWhileLoading(CoolRefreshView refreshView, View headerView) {
            return headerView.getMeasuredHeight() / 3;
        }

        @Override
        public int totalDistance(CoolRefreshView refreshView, View headerView) {
            return headerView.getMeasuredHeight();
        }
    };
}
