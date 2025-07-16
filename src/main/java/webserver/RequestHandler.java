package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;


import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;


public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            header_and_length header = headerRead(in);
            String[] token = header.getHeader().split(" ");
            int length = header.getLength();

            String url = token[1];

            if(url.startsWith("/user/create")){
                String body = IOUtils.readData(header.getBufferedReader(), length);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);

                User user = new User(params.get("userId")
                        , params.get("password")
                        , params.get("name")
                        , params.get("email"));

                log.debug("User : {}", user);

                DataOutputStream dos = new DataOutputStream(out);
                byte[] response_body = Files.readAllBytes(new File("webapp" + "/index.html").toPath());
                response302Header(dos, response_body.length);
                responseBody(dos, response_body);
            }
            else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("webapp" + url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private header_and_length headerRead(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String header = reader.readLine();
        log.debug("HTTP request Header : {}", header);

        int length = 0;

        String line = ".";

        while (!"".equals(line)){
            line = reader.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("Content-Length")) {
                String[] contLength_str = line.split(":");
                length = Integer.parseInt(contLength_str[1].trim());
            }
            log.debug("HTTP request : {}",line);
        }

        return new header_and_length(length, header, reader);
    }

    static class header_and_length{
        int length;
        String header;
        BufferedReader reader;

        public header_and_length(int lenght, String header, BufferedReader reader){
            this.header = header;
            this.length = lenght;
            this.reader = reader;
        }

        public int getLength(){
            return length;
        }

        public String getHeader() {
            return header;
        }

        public BufferedReader getBufferedReader() {
            return reader;
        }
    }
}
