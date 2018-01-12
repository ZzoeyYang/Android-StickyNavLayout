package online.sniper.widget.recycler;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ViewHolder的基类
 * <p/>
 * Created by wangpeihe on 2016/6/22.
 */
public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    protected RecyclerViewAdapter mAdapter;
    private SparseArray<View> mViews = new SparseArray<View>();
    private T mData;
    private int mPosition;

    public RecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onSetData(T data, int position);

    public final void setData(T data, int position) {
        mData = data;
        mPosition = position;
        onSetData(data, position);
    }

    public T getData() {
        return mData;
    }

    public int position() {
        return mPosition;
    }

    public void setAdapter(RecyclerViewAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * 返回对应view
     *
     * @param resId view resource Id
     * @param <V>   返回view的类型
     * @return 返回View
     */
    public <V extends View> V getView(@IdRes int resId) {
        View view = mViews.get(resId);
        if (view == null) {
            view = itemView.findViewById(resId);
            mViews.put(resId, view);
        }
        return (V) view;
    }

    /**
     * 给ImageView设置背景
     *
     * @param imgViewResId ImageView对应资源ID
     * @param imgResId     图片资源ID
     */
    public void setImageView(@IdRes int imgViewResId, @DrawableRes int imgResId) {
        ImageView view = getView(imgViewResId);
        view.setBackgroundResource(imgResId);
    }


    /**
     * 设置View的可见性
     *
     * @param resId   View的Resource ID
     * @param visible 是否可见
     */
    public void setVisible(@IdRes int resId, @VisibleType int visible) {
        View view = getView(resId);
        view.setVisibility(visible);
    }

    /**
     * 设置字体颜色
     *
     * @param resId     TextView ResId
     * @param textColor 字体颜色
     */
    public void setTextColor(@IdRes int resId, int textColor) {
        TextView textView = getView(resId);
        textView.setTextColor(textColor);
    }

    /**
     * 设置View的选中状态
     *
     * @param resId    View的Resource ID
     * @param selected 是不选中，如果为true, 则选中
     */
    public void setViewSelected(@IdRes int resId, boolean selected) {
        View view = getView(resId);
        view.setSelected(selected);
    }

    /**
     * 设置View的背景颜色
     *
     * @param resId View的Resource ID
     * @param color 颜色值
     */
    public void setViewBackground(@IdRes int resId, int color) {
        View view = getView(resId);
        view.setBackgroundColor(color);
    }

    /**
     * 设置文字
     *
     * @param resId TextView ResId
     * @param text  文字内容
     */
    public void setText(@IdRes int resId, String text) {
        TextView view = getView(resId);
        view.setText(text);
    }

    /**
     * 设置文字
     */
    public void setText(@IdRes int resId, Spanned text) {
        TextView view = getView(resId);
        view.setText(text);
    }

    /**
     * 设置HTML显示
     *
     * @param resId
     * @param text
     */
    public void setHtml(@IdRes int resId, String text) {
        TextView view = getView(resId);
        view.setText(Html.fromHtml(text));
    }

    /**
     * 设置文字
     *
     * @param resId    TextView ResId
     * @param txtResId String res id
     */
    public void setText(@IdRes int resId, @StringRes int txtResId) {
        TextView view = getView(resId);
        view.setText(txtResId);
    }

    /**
     * 设置View的点击事件
     *
     * @param resId           View的Resource ID
     * @param onClickListener 点击事件监听器
     */
    public void setViewOnClickListener(@IdRes int resId, View.OnClickListener onClickListener) {
        View view = getView(resId);
        view.setOnClickListener(onClickListener);
    }

    /**
     * 给整个Item设置点击事件
     *
     * @param onItemClickListener onClickListener
     */
    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        this.itemView.setOnClickListener(onItemClickListener);
    }

    /**
     * 当被长按选中时、或取消选中是执行
     *
     * @param selected 是否选中
     */
    public void onSelectedChanged(boolean selected) {

    }


    /**
     * 取每一个Item的View
     *
     * @return 返回整个Item
     */
    public View getItemView() {
        return itemView;
    }

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface VisibleType {

    }

    /**
     * 给ImageView src
     *
     * @param imgViewResId ImageView对应资源ID
     * @param imgResId     图片资源ID
     */
    public void setImageViewSrc(@IdRes int imgViewResId, @DrawableRes int imgResId) {
        ImageView view = getView(imgViewResId);
        view.setImageResource(imgResId);
    }

    public boolean isMovable() {
        return true;
    }
}
