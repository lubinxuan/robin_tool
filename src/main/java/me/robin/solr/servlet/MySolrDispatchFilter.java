package me.robin.solr.servlet;

import me.robin.solr.util.HBaseDataThreadUtil;
import org.apache.solr.servlet.SolrDispatchFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */
public class MySolrDispatchFilter extends SolrDispatchFilter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            super.doFilter(request, response, chain);
        } finally {
            HBaseDataThreadUtil.clear();
        }
    }
}
