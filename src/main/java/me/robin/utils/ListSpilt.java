package me.robin.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ListSpilt<T> {

    private List<T> originalList;

    private int index;

    public ListSpilt(List<T> originalList) {
        if (null == originalList || originalList.isEmpty()) {
            this.originalList = Collections.emptyList();
        } else if (originalList instanceof ArrayList) {
            this.originalList = originalList;
        } else {
            this.originalList = new ArrayList<>(originalList);
        }
    }

    public ListSpilt(Set<T> originalSet) {
        if (null == originalSet || originalSet.isEmpty()) {
            this.originalList = Collections.emptyList();
        } else {
            this.originalList = new ArrayList<>(originalSet);
        }
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

    public List<List<T>> spilt(int spilt) {
        List<List<T>> spiltList = new ArrayList<>();
        int p_idx = this.index;
        this.index = 0;
        while (this.hasNext()) {
            spiltList.add(this.next(spilt));
        }
        this.index = p_idx;
        return spiltList;
    }

    public static int taskBatch(int ori, int sub) {
        if (0 == ori) {
            return 0;
        }

        int mod = ori % sub;
        return ori / sub + (mod == 0 ? 0 : 1);
    }
}
