package ru.vsu.cs.odinaev.service;

import ru.vsu.cs.odinaev.model.Group;
import ru.vsu.cs.odinaev.repository.GroupRepository;

import java.util.List;
import java.util.UUID;

public interface IGroupService {
    Group createGroup(String name, int courseNumber);
    void deleteGroup(UUID groupId);
    List<Group> getAllGroups();
    Group getGroupById(UUID groupId);
    Group updateGroup(UUID groupId, String newName, Integer newCourseNumber);
    boolean groupExists(UUID groupId);
    List<Object[]> getGroupReport(UUID groupId);
}