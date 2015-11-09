package me.robin.api.entity;

/**
 * Created by Lubin.Xuan on 2015/10/13.
 * ie.
 */
public class CombineEntity extends StringEntity{
    private String [] mappings;

    private String join = "";

    public CombineEntity(String key, String[] mappings) {
        super(key);
        this.mappings = mappings;
    }

    public CombineEntity(String key, String[] mappings, String join) {
        super(key);
        this.mappings = mappings;
        this.join = join;
    }

    public String[] getMappings() {
        return mappings;
    }

    public String getJoin() {
        return join;
    }
}
