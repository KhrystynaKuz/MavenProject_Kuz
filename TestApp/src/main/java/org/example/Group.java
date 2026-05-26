package org.example;

import java.util.ArrayList;
import java.util.List;

// group of students
public class Group {

    private String groupName;
    private List<Student> students;

    public Group(String groupName) {
        this.groupName = groupName;
        this.students = new ArrayList<>();
    }

    // add a new student to the group
    public void addStudent(Student student) {
        this.students.add(student);
    }

    public String getGroupName() {
        return groupName;
    }

    public List<Student> getStudents() {
        return students;
    }
}
