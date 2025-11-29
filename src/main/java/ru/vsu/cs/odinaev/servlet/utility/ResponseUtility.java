package ru.vsu.cs.odinaev.servlet.utility;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class ResponseUtility {
    static Gson gson = new Gson();
    public static void setupResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }

    public static void sendSuccess(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(data));
    }

    public static void handleError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(Map.of("error", message)));
    }
}
