package util;

public class HeaderUtils {
    public static String[] splitHeader(String header){

        String[] token = header.split(" ");

        return token;
    }
}
