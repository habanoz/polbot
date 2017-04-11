package com.cf.client;

import java.io.IOException;
import java.util.List;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;

/**
 * @author David
 */
public class HTTPClient {
    public String postHttp(String url, List<NameValuePair> params, List<NameValuePair> headers) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        post.getEntity().toString();

        if (headers != null) {
            for (NameValuePair header : headers) {
                post.addHeader(header.getName(), header.getValue());
            }
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        if (System.getProperty("http.proxyHost") != null && !System.getProperty("http.proxyHost").isEmpty()) {

            HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"),
                    Integer.parseInt(System.getProperty("http.proxyPort")), "http");

            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(routePlanner);
        }

        HttpClient httpClient = httpClientBuilder.build();

        HttpResponse response = httpClient.execute(post);


        HttpEntity entity = response.getEntity();
        if (entity != null) {
            return EntityUtils.toString(entity);

        }
        return null;
    }

    public String getHttp(String url, List<NameValuePair> headers) throws IOException {
        HttpRequestBase request = new HttpGet(url);

        if (headers != null) {
            for (NameValuePair header : headers) {
                request.addHeader(header.getName(), header.getValue());
            }
        }
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        if (System.getProperty("http.proxyHost") != null && !System.getProperty("http.proxyHost").isEmpty()) {

            HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"),
                    Integer.parseInt(System.getProperty("http.proxyPort")), "http");

            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(routePlanner);
        }

        HttpClient httpClient = httpClientBuilder.build();
        HttpResponse response = httpClient.execute(request);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            return EntityUtils.toString(entity);

        }
        return null;
    }
}
