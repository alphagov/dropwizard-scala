package uk.gov.gds.microservice.test.dropwizard.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class URIMaker {

    public static URI formatUri(String format, Object... args) {
        return URI.create(String.format(format, args));
    }

    public static URI toUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot convert URL to URI: " + url, e);
        }
    }

    public static URI toUri(String protocol, String host, int port, String uri) {
        try {
            return toUri(new URL(protocol, host, port, uri));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL for URI: " + uri, e);
        }
    }
}
