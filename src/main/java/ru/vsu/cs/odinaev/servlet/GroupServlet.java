package ru.vsu.cs.odinaev.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.vsu.cs.odinaev.dto.group.GroupRequest;
import ru.vsu.cs.odinaev.model.Group;
import ru.vsu.cs.odinaev.service.GroupService;
import ru.vsu.cs.odinaev.servlet.utility.ResponseUtility;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/groups/*")
public class GroupServlet extends HttpServlet {
    private Gson gson;
    private GroupService groupService;

    @Override
    public void init() {
        this.gson = new Gson();
        this.groupService = (GroupService) getServletContext().getAttribute("groupService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAllGroups(resp);
        } else {
            handleGetGroupById(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);
        handleCreateGroup(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);
        handleUpdateGroup(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtility.setupResponse(resp);
        handleDeleteGroup(req, resp);
    }

    private void handleGetAllGroups(HttpServletResponse resp) throws IOException {
        try {
            List<Group> groups = groupService.getAllGroups();
            ResponseUtility.sendSuccess(resp, 200, groups);
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Failed to retrieve groups");
        }
    }

    private void handleGetGroupById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String id = pathInfo.substring(1);

        try {
            UUID groupId = UUID.fromString(id);
            Group group = groupService.getGroupById(groupId);

            if (group != null) {
                ResponseUtility.sendSuccess(resp, 200, group);
            } else {
                ResponseUtility.handleError(resp, 404, "Group not found");
            }
        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Failed to retrieve group");
        }
    }

    private void handleCreateGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            GroupRequest request = gson.fromJson(req.getReader(), GroupRequest.class);

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                ResponseUtility.handleError(resp, 400, "Group name is required");
                return;
            }

            Group group = groupService.createGroup(request.getName(), request.getCourse());
            ResponseUtility.sendSuccess(resp, 201, group);
        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, e.getMessage());
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Failed to create group");
        }
    }

    private void handleUpdateGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            ResponseUtility.handleError(resp, 400, "Group ID required");
            return;
        }

        String id = pathInfo.substring(1);

        try {
            UUID groupId = UUID.fromString(id);
            GroupRequest request = gson.fromJson(req.getReader(), GroupRequest.class);

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                ResponseUtility.handleError(resp, 400, "Group name is required");
                return;
            }

            Group updatedGroup = groupService.updateGroup(groupId, request.getName(), request.getCourse());
            ResponseUtility.sendSuccess(resp, 200, updatedGroup);

        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Failed to update group");
        }
    }

    private void handleDeleteGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            ResponseUtility.handleError(resp, 400, "Group ID required");
            return;
        }

        String id = pathInfo.substring(1);

        try {
            UUID groupId = UUID.fromString(id);
            Group group = groupService.getGroupById(groupId);

            if (group != null) {
                groupService.deleteGroup(groupId);
                ResponseUtility.sendSuccess(resp, 200, Map.of("message", "Group deleted successfully"));
            } else {
                ResponseUtility.handleError(resp, 404, "Group not found");
            }

        } catch (IllegalArgumentException e) {
            ResponseUtility.handleError(resp, 400, "Invalid UUID format");
        } catch (Exception e) {
            ResponseUtility.handleError(resp, 500, "Failed to delete group");
        }
    }
}