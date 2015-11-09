package me.robin.solr.handler.component;

import me.robin.solr.highlight.MyDefaultSolrHighlighter;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.HighlightComponent;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.highlight.SolrHighlighter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */
public abstract class MyHighlightComponent extends HighlightComponent {

    private static final Logger logger = LoggerFactory.getLogger(MyHighlightComponent.class);

    public static final String HIGH_LIGHT_FIELDS = "HighlightFields";
    public static final String DEFAULT_HIGH_LIGHT_FIELDS = "defaultHighlightFields";

    private PluginInfo info;

    @Override
    public void init(PluginInfo info) {
        super.init(info);
        this.info = info;
    }

    @Override
    public void inform(SolrCore core) {
        List<PluginInfo> children = info.getChildren("highlighting");
        SolrHighlighter highlighter;
        if (children.isEmpty()) {
            PluginInfo pluginInfo = core.getSolrConfig().getPluginInfo(SolrHighlighter.class.getName()); //TODO deprecated configuration remove later
            if (pluginInfo != null) {
                highlighter = core.createInitInstance(pluginInfo, SolrHighlighter.class, null, MyDefaultSolrHighlighter.class.getName());
            } else {
                DefaultSolrHighlighter defHighlighter = new MyDefaultSolrHighlighter(core);
                defHighlighter.init(PluginInfo.EMPTY_INFO);
                highlighter = defHighlighter;
            }
        } else {
            highlighter = core.createInitInstance(children.get(0), SolrHighlighter.class, null, MyDefaultSolrHighlighter.class.getName());
        }
        try {
            Field field = HighlightComponent.class.getDeclaredField("highlighter");
            field.setAccessible(true);
            field.set(this, highlighter);
            field.setAccessible(false);
            logger.info("高亮组件!!!{}", highlighter);
        } catch (Throwable e) {
            logger.error("设置高亮组件异常!!!! 系统将退出", e);
            System.exit(-1);
        }
    }
}
