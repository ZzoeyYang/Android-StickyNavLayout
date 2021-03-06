package online.sniper.widget.list;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangpeihe on 2016/6/22.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ListViewHolder {
    int viewType() default 0;

    int layout();

    Class<? extends RecyclerListViewHolder> holder() default DefaultListViewHolder.class;
}
