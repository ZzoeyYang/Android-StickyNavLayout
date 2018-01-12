package online.sniper.widget.recycler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangpeihe on 2016/6/22.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewHolder {

    int viewType() default RecyclerViewAdapter.DEFAULT_ITEM_TYPE;

    int layout();

    Class<? extends RecyclerViewHolder> holder() default DefaultViewHolder.class;
}
