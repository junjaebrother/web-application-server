package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static util.HttpRequestUtils.parseQueryString;

public class HttpRequest {

    Map<String, String> request = new HashMap<>();
    Map<String, String> parameter;

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String header = reader.readLine();
        String[] token = header.split(" ");

        request.put("method", token[0]);

        if (request.get("method").equals("GET")){
            String[] url_param = url_param_split(token[1]);
            request.put("path",url_param[0]);
            parameter = parseQueryString(url_param[1]);

        }
        else {
            request.put("path", token[1]);
        }

        String line = reader.readLine();

        while (!"".equals(line)){

            if (line == null) {
                break;
            }
            String[] parse_header = line.split(": ");
            request.put(parse_header[0].trim(), parse_header[1].trim());

            line = reader.readLine();
        }

        if (request.get("method").equals("POST")){
            String body = IOUtils.readData(reader, Integer.parseInt(request.get("Content-Length")));
            parameter = HttpRequestUtils.parseQueryString(body);
        }
        line = reader.readLine();
    }

    public String getMethod() {
        return request.get("method");
    }

    public String getPath() {
        return request.get("path");
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


}
