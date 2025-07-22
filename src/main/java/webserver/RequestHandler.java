package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;


import db.DataBase;
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
            HttpRequest request = new HttpRequest(in);
            String path = request.getPath();

            if(path.equals("/user/create")){
                User user = new User(request.getParameter("userId")
                        , request.getParameter("password")
                        , request.getParameter("name")
                        , request.getParameter("email"));

                log.debug("User : {}", user);

                DataBase.addUser(user);

                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html");
            }
            else if(path.equals("/user/login")){
                boolean loginSuccess = false;

                User user = DataBase.findUserById(request.getParameter("userId"));
                if (user != null && user.getPassword().equals(request.getParameter("password"))) {
                    loginSuccess = true;
                }
                DataOutputStream dos = new DataOutputStream(out);

                loginResponse302Header(dos, loginSuccess);
            }
            else if(path.equals("/user/list")){
                if (request.isLogin(request.getHeader("Cookie"))){
                    Collection<User> users = DataBase.findAll();  // findAll 호출

                    StringBuilder sb = new StringBuilder();
                    sb.append("<!DOCTYPE html>");
                    sb.append("<html>");
                    sb.append("<head><meta charset='UTF-8'><title>User List</title></head>");
                    sb.append("<body>");
                    sb.append("<h1>User List</h1>");
                    sb.append("<table border='1'>");
                    sb.append("<tr><th>UserId</th><th>Name</th><th>Email</th></tr>");

                    for (User user : users) {
                        sb.append("<tr>");
                        sb.append("<td>").append(user.getUserId()).append("</td>");
                        sb.append("<td>").append(user.getName()).append("</td>");
                        sb.append("<td>").append(user.getEmail()).append("</td>");
                        sb.append("</tr>");
                    }

                    sb.append("</table>");
                    sb.append("</body>");
                    sb.append("</html>");

                    byte[] body = sb.toString().getBytes();
                    DataOutputStream dos = new DataOutputStream(out);
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }
                else{
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos, "/user/login.html");
                }

            }
            else if(path.endsWith(".css")){
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("webapp" + path).toPath());
                css200Header(dos, body.length);
                responseBody(dos, body);
            }
            else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("webapp" + path).toPath());
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

    private void css200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loginResponse302Header(DataOutputStream dos, boolean loginSuccess) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            if (loginSuccess){
                dos.writeBytes("Location: /index.html\r\n");
            }
            else {
                dos.writeBytes("Location: /user/login_failed.html\r\n");
            }
            setCookie(dos, loginSuccess);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: " + url +"\r\n");
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
        String cookies = "";

        while (!"".equals(line)){
            line = reader.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("Content-Length")) {
                String[] contLength_str = line.split(":");
                length = Integer.parseInt(contLength_str[1].trim());
            }
            if (line.startsWith("Cookie")) {
                String[] cookie = line.split(":");
                cookies = cookie[1];
            }
            log.debug("HTTP request : {}",line);
        }

        return new header_and_length(length, header, reader, cookies);
    }

    static class header_and_length{
        int length;
        String header;
        BufferedReader reader;
        String cookies;

        public header_and_length(int lenght, String header, BufferedReader reader, String cookies){
            this.header = header;
            this.length = lenght;
            this.reader = reader;
            this.cookies = cookies;
        }

        public int getLength(){
            return length;
        }

        public String getHeader() {
            return header;
        }

    }

    public void setCookie(DataOutputStream dos, boolean loginSuccess) throws IOException {
        if (loginSuccess){
            dos.writeBytes("Set-Cookie: logined=true\r\n");
        }
        else{
            dos.writeBytes("Set-Cookie: logined=false\r\n");
        }
    }
}
