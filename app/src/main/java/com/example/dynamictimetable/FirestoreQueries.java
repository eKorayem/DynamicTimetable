package com.example.dynamictimetable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirestoreQueries {
    private FirebaseFirestore db;

    public FirestoreQueries() {
        db = FirebaseFirestore.getInstance();
    }

    public void getLecturesForUser(String userLevel, OnLecturesLoadedListener listener) {
        db.collection("Departments")
                .document("ICT")
                .collection(userLevel)
                .document("Lectures")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Course> courses = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> lectures = documentSnapshot.getData();
                        if (lectures != null) {
                            for (Map.Entry<String, Object> entry : lectures.entrySet()) {
                                Map<String, Object> courseData = (Map<String, Object>) entry.getValue();
                                Course course = new Course(
                                        entry.getKey(),
                                        (String) courseData.get("course_name"),
                                        (String) courseData.get("start_date"),
                                        (String) courseData.get("end_date")
                                );
                                courses.add(course);
                            }
                        }
                    }
                    listener.onLecturesLoaded(courses);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void getSectionsForUser(String userLevel, String userSection, OnSectionsLoadedListener listener) {
        db.collection("Departments")
                .document("ICT")
                .collection(userLevel)
                .document("Sections")
                .collection(userSection)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Lab> labs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Map<String, Object> labData = document.getData();
                        Lab lab = new Lab(
                                document.getId(),
                                (String) labData.get("lab_name"),
                                (String) labData.get("start_date"),
                                (String) labData.get("end_date")
                        );
                        labs.add(lab);
                    }
                    listener.onSectionsLoaded(labs);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Model classes
    public static class Course {
        public String subjectName;
        public String courseName;
        public String startDate;
        public String endDate;

        public Course(String subjectName, String courseName, String startDate, String endDate) {
            this.subjectName = subjectName;
            this.courseName = courseName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Getters
        public String getSubjectName() {
            return subjectName;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        // Setters
        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    public static class Lab {
        public String labId;
        public String labName;
        public String startDate;
        public String endDate;

        public Lab(String labId, String labName, String startDate, String endDate) {
            this.labId = labId;
            this.labName = labName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Getters
        public String getLabId() {
            return labId;
        }

        public String getLabName() {
            return labName;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        // Setters
        public void setLabId(String labId) {
            this.labId = labId;
        }

        public void setLabName(String labName) {
            this.labName = labName;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    // Callback interfaces
    public interface OnLecturesLoadedListener {
        void onLecturesLoaded(List<Course> courses);
        void onError(String errorMessage);
    }

    public interface OnSectionsLoadedListener {
        void onSectionsLoaded(List<Lab> labs);
        void onError(String errorMessage);
    }
}