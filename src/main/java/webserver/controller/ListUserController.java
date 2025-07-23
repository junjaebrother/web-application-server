package webserver.controller;

import db.DataBase;
import model.HttpSession;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Collection;

public class ListUserController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        if (isLogined(request.getSession())){
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

            response.forwardBody(sb.toString());
        }
        else{
            response.sendRedirect("/user/login.html");
        }
    }

    private static boolean isLogined(HttpSession session) {
        Object user = session.getAttribute("user");
        if(user == null) {
            return false;
        }
        return true;
    }
}