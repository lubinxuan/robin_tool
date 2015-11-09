package me.robin.solr.handler;

import me.robin.solr.handler.component.AfterHighlightComponent;
import me.robin.solr.handler.component.HBaseDataFetchComponent;
import me.robin.solr.handler.component.PreHighlightComponent;
import org.apache.solr.handler.component.*;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */
public class MySearchHandler extends SearchHandler {

    @Override
    protected List<String> getDefaultComponents() {
        ArrayList<String> names = new ArrayList<>(6);
        names.add(QueryComponent.COMPONENT_NAME);
        names.add(FacetComponent.COMPONENT_NAME);
        names.add(MoreLikeThisComponent.COMPONENT_NAME);
        names.add(PreHighlightComponent.COMPONENT_NAME);
        names.add(HBaseDataFetchComponent.COMPONENT_NAME);
        names.add(AfterHighlightComponent.COMPONENT_NAME);
        names.add(StatsComponent.COMPONENT_NAME);
        names.add(DebugComponent.COMPONENT_NAME);
        names.add(ExpandComponent.COMPONENT_NAME);
        return names;
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        super.handleRequestBody(req, rsp);
    }
}
