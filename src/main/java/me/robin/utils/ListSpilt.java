package me.robin.utils;

import java.util.ArrayList;
import java.util.List;

public class ListSpilt<T> {
    private List<T> originalList;

    private int index;

    public ListSpilt(List<T> originalList) {
        this.originalList = null == originalList ? new ArrayList<>() : originalList;
    }

    public List<T> next(int i) {
        int end = i + index;
        if (end > originalList.size()) {
            end = originalList.size();
        }
        int _start = index;
        index = end;
        return originalList.subList(_start, end);
    }

    public boolean hasNext() {
        return index < originalList.size();
    }
}
