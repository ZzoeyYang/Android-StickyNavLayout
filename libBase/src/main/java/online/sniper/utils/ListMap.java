package online.sniper.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List和Map的结合体
 * <p>
 * Created by wangpeihe on 2017/5/2.
 */
public class ListMap<K, V> {
    private List<V> mList = new ArrayList<>();
    private Map<K, V> mMap = new HashMap<>();

    public ListMap() {
    }

    public synchronized void clear() {
        mList.clear();
        mMap.clear();
    }

    public synchronized void add(K k, V v) {
        mList.add(v);
        mMap.put(k, v);
    }

    public synchronized boolean containsKey(K k) {
        return mMap.containsKey(k);
    }

    public synchronized V getByPosition(int position) {
        return mList.get(position);
    }

    public synchronized V getByKey(K k) {
        return mMap.get(k);
    }

    public synchronized List<V> list() {
        return Collections.unmodifiableList(mList);
    }

    public synchronized Map<K, V> map() {
        return Collections.unmodifiableMap(mMap);
    }

    public synchronized boolean isEmpty() {
        return mList.isEmpty();
    }

    public synchronized int size() {
        return mList.size();
    }
}
