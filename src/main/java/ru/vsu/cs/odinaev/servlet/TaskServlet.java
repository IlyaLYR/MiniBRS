package ru.vsu.cs.odinaev.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.model.TaskStatus;
import ru.vsu.cs.odinaev.service.TaskService;
import ru.vsu.cs.odinaev.servlet.utility.ResponseUtility;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/api/tasks/*")
public class TaskServlet extends HttpServlet {
    private TaskService taskService;

    @Override
    public void init() {
        this.taskService = (TaskService) getServletContext().getAttribute("taskService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/student/")) {
                handleGetStudentTasks(req, resp);
            } else {
                ResponseUtility.handleError(resp, 400, "Invalid endpoint. Use /api/tasks/student/{studentId}");
            }
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.endsWith("/mark")) {
                handleMarkTask(req, resp);
            } else if (pathInfo != null && pathInfo.endsWith("/reset")) {
                handleResetTask(req, resp);
            } else {
                ResponseUtility.handleError(resp, 400, "Invalid endpoint. Use /mark or /reset");
            }
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Internal server error");
        }
    }

    private void handleGetStudentTasks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String studentIdStr = pathInfo.substring("/student/".length());

        try {
            UUID studentId = UUID.fromString(studentIdStr);
            List<Task> tasks = taskService.getTasksByStudent(studentId);
            ResponseUtility.sendSuccess(resp, 200, tasks);
        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        }
    }

    private void handleMarkTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String taskIdStr = pathInfo.replace("/mark", "").substring(1);

        try {
            UUID taskId = UUID.fromString(taskIdStr);
            taskService.updateTaskStatus(taskId, TaskStatus.SUBMITTED);

            resp.setStatus(200);

        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        }
    }

    private void handleResetTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String taskIdStr = pathInfo.replace("/reset", "").substring(1);

        try {
            UUID taskId = UUID.fromString(taskIdStr);
            taskService.updateTaskStatus(taskId, TaskStatus.NOT_SUBMITTED);

            resp.setStatus(200);

        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        }
    }
}