package me.robin.solr.manager;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2016/2/22.
 */
public class TestHttpRequestInterceptor implements HttpRequestInterceptor {
    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        System.out.println();
    }
}
