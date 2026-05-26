package org.example;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting test...");

        // students
        Student s1 = new Student("Khrystyna", "Kuz", 20);
        Student s2 = new Student("Lilia", "Kotyk", 21);

        // student group
        Group group = new Group("KN-301");

        group.addStudent(s1);
        group.addStudent(s2);

        // print
        System.out.println("Group: " + group.getGroupName());
        System.out.println("Total students in group: " + group.getStudents().size());
    }
}