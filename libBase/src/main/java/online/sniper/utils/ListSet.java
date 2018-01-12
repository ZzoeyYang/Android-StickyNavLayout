package online.sniper.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * List和Set的结合体
 * <p>
 * Created by wangpeihe on 2017/5/2.
 */
public class ListSet<V> {
    private List<V> mList = new ArrayList<>();
    private Set<V> mSet = new HashSet<>();

    public ListSet() {
    }

    public synchronized void clear() {
        mList.clear();
        mSet.clear();
    }

    public synchronized void add(V v) {
        mList.add(v);
        mSet.add(v);
    }

    public synchronized boolean contains(V v) {
        return mSet.contains(v);
    }

    public synchronized void remove(V v) {
        mList.remove(v);
        mSet.remove(v);
    }

    public synchronized V getByPosition(int position) {
        return mList.get(position);
    }

    public synchronized List<V> list() {
        return Collections.unmodifiableList(mList);
    }

    public synchronized Set<V> set() {
        return Collections.unmodifiableSet(mSet);
    }

    public synchronized boolean isEmpty() {
        return mList.isEmpty();
    }

    public synchronized int size() {
        return mList.size();
    }
}
