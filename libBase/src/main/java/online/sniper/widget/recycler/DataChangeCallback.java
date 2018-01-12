package online.sniper.widget.recycler;

/**
 * Created by knero on 4/10/17.
 */

public interface DataChangeCallback<T> {
    void update(T oldData, T newData);
}
