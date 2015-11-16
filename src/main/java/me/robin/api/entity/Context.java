package me.robin.api.entity;

import java.util.List;

/**
 * Created by Lubin.Xuan on 2015/10/12.
 * ie.
 */
public class Context {
    private String target;
    private List<Entity> entityList;

    public Context(List<Entity> entityList) {
        this.entityList = entityList;
    }

    public Context(String target, List<Entity> entityList) {
        this.target = target;
        this.entityList = entityList;
    }

    public String getTarget() {
        return target;
    }

    public List<Entity> getEntityList() {
        return entityList;
    }

    public boolean isEmpty() {
        return entityList.isEmpty();
    }
}
