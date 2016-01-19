package me.robin.mybatis;

/**
 * Created by Lubin.Xuan on 2015/2/14.
 * ie.
 */
public interface BatchMapper<T> {
    public int insert(T t, Object... objects);

    public int update(T t, Object... objects);
}
