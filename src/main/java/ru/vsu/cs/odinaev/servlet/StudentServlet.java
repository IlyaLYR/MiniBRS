package ru.vsu.cs.odinaev.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.vsu.cs.odinaev.dto.student.StudentRequest;
import ru.vsu.cs.odinaev.model.Student;
import ru.vsu.cs.odinaev.service.StudentService;
import ru.vsu.cs.odinaev.servlet.utility.ResponseUtility;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/students/*")
public class StudentServlet extends HttpServlet {
    private Gson gson;
    private StudentService studentService;

    @Override
    public void init() {
        this.gson = new Gson();
        this.studentService = (StudentService) getServletContext().getAttribute("studentService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllStudents(resp);
            } else if (pathInfo.startsWith("/group/")) {
                handleGetStudentsByGroup(req, resp);
            } else {
                handleGetStudentById(req, resp);
            }
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);

        try {
            handleCreateStudent(req, resp);
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);

        try {
            handleUpdateStudent(req, resp);
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Internal server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);

        try {
            handleDeleteStudent(req, resp);
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Internal server error");
        }
    }

    private void handleGetAllStudents(HttpServletResponse resp) throws IOException {
        List<Student> students = studentService.getAllStudents();
        ResponseUtility.sendSuccess(resp, 200, students);
    }

    private void handleGetStudentById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String id = pathInfo.substring(1);

        try {
            UUID studentId = UUID.fromString(id);
            Student student = studentService.getStudentById(studentId);

            if (student != null) {
                ResponseUtility.sendSuccess(resp, 200, student);
            } else {
                ResponseUtility.handleError(resp, 404, "Student not found");
            }
        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        }
    }

    private void handleGetStudentsByGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String groupIdStr = pathInfo.substring("/group/".length()); //7

        try {
            UUID groupId = UUID.fromString(groupIdStr);
            List<Student> students = studentService.getStudentsByGroup(groupId);
            ResponseUtility.sendSuccess(resp, 200, students);
        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        }
    }

    private void handleCreateStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StudentRequest request = gson.fromJson(req.getReader(), StudentRequest.class);

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            ResponseUtility.handleError(resp, 400, "Student name is required");
            return;
        }

        if (request.getGroupId() == null) {
            ResponseUtility.handleError(resp, 400, "Group ID is required");
            return;
        }

        try {
            Student student = studentService.createStudent(request.getName(), request.getGroupId());
            ResponseUtility.sendSuccess(resp, 201, student);
        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        }
    }

    private void handleUpdateStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            ResponseUtility.handleError(resp, 404, "Student ID required");
            return;
        }

        String id = pathInfo.substring(1);

        try {
            UUID studentId = UUID.fromString(id);
            StudentRequest request = gson.fromJson(req.getReader(), StudentRequest.class);

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                ResponseUtility.handleError(resp, 400, "Student name is required");
                return;
            }

            if (request.getGroupId() == null) {
                ResponseUtility.handleError(resp, 400, "Group ID is required");
                return;
            }

            Student updatedStudent = studentService.updateStudent(studentId, request.getName(), request.getGroupId());
            ResponseUtility.sendSuccess(resp, 200, updatedStudent);

        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        }
    }

    private void handleDeleteStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            ResponseUtility.handleError(resp, 404, "Student ID required");
            return;
        }

        String id = pathInfo.substring(1);

        try {
            UUID studentId = UUID.fromString(id);
            Student student = studentService.getStudentById(studentId);

            if (student != null) {
                studentService.deleteStudent(studentId);
                ResponseUtility.sendSuccess(resp, 200, Map.of("message", "Student deleted successfully"));
            } else {
                ResponseUtility.handleError(resp, 404, "Student not found");
            }

        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        }
    }
}