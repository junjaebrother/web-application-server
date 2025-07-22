package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import webserver.domain.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


import static util.HttpRequestUtils.parseQueryString;

public class HttpRequest {
    private static final Logger log  = LoggerFactory.getLogger(HttpRequest.class);

    private Map<String, String> request = new HashMap<>();
    private Map<String, String> parameter;
    private RequestLine requestLine;

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String header = reader.readLine();
        if (header == null) {
            return;
        }

        requestLine = new RequestLine(header);
        String line = reader.readLine();

        while (!"".equals(line)){
            if (line == null) {
                break;
            }
            String[] parse_header = line.split(": ");
            request.put(parse_header[0].trim(), parse_header[1].trim());

            line = reader.readLine();
        }

        if (requestLine.getMethod().isPost()){
            String body = IOUtils.readData(reader, Integer.parseInt(request.get("Content-Length")));
            parameter = HttpRequestUtils.parseQueryString(body);
        }
        else {
            parameter = requestLine.getParams();
        }

    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String name) {
        return request.get(name);
    }

    public String getParameter(String name) {
        return parameter.get(name);
    }

    private String[] url_param_split(String url){
        return url.split("\\?");
    }

    private void processRequestLine(String requestLine) {
        log.debug("request line: {}", requestLine);
        String[] token = requestLine.split(" ");

        request.put("method", token[0]);

        if (request.get("method").equals("GET")){
            String[] url_param = url_param_split(token[1]);
            request.put("path",url_param[0]);
            parameter = parseQueryString(url_param[1]);
            return;
        }
        request.put("path", token[1]);
    }

    public boolean isLogin(String cookieValue) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieValue);
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }


}
