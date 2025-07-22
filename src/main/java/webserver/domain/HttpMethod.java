package webserver.domain;

public enum HttpMethod {
    GET, POST;

    public boolean isPost() {
        return this == POST;
    }
}
