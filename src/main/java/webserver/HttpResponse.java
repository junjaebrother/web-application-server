package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private DataOutputStream dos;
    private String status;
    private Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream out){
        this.dos = new DataOutputStream(out);
    }

    public void forward(String file) throws IOException {
        byte[] body = Files.readAllBytes(new File("webapp" + file).toPath());
        response200Header(body.length);
        processHeaders();
        responseBody(body);
    }

    public void sendRedirect(String file) {
        status = "HTTP/1.1 302 OK \r\n";
        headers.put("Location: ", file+"\r\n");
        processHeaders();
    }

    public void addHeader(String key, String value) {
        headers.put(key + ": ", value + "\r\n");
    }

    private void response200Header(int lengthOfBodyContent) {
        status = "HTTP/1.1 200 OK \r\n";
        headers.put("Content-Type: ", "text/html;charset=utf-8\r\n");
        headers.put("Content-Length: ", lengthOfBodyContent + "\r\n");
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHeaders() {
        try {
            dos.writeBytes(status);
            for (Map.Entry<String, String> header : headers.entrySet()){
                dos.writeBytes(header.getKey() + header.getValue());
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
