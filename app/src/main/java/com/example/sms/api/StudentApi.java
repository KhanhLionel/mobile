package com.example.sms.api;

import com.example.sms.models.Grade;
import com.example.sms.models.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentApi {
    private DatabaseReference studentsRef;
    private DatabaseReference gradesRef;

    public StudentApi() {
        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        gradesRef = FirebaseDatabase.getInstance().getReference("Grades");
    }

    public void getAllStudents(ApiCallback<List<Student>> callback) {
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Student> students = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Student s = data.getValue(Student.class);
                    if (s != null) {
                        s.setId(data.getKey());
                        students.add(s);
                    }
                }
                callback.onSuccess(students);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void addStudent(Student student, ApiCallback<Void> callback) {
        String id = studentsRef.push().getKey();
        if (id != null) {
            student.setId(id);
            studentsRef.child(id).setValue(student)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onFailure(e));
        }
    }

    public void updateStudent(Student student, ApiCallback<Void> callback) {
        studentsRef.child(student.getId()).setValue(student)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteStudent(String studentId, ApiCallback<Void> callback) {
        studentsRef.child(studentId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getStudentGrades(String studentId, String subjectId, ApiCallback<Grade> callback) {
        gradesRef.child(studentId).child(subjectId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Grade g = snapshot.getValue(Grade.class);
                    callback.onSuccess(g);
                } else {
                    callback.onSuccess(new Grade(0, 0, 0)); // default grade
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void updateGrades(String studentId, String subjectId, Grade grade, ApiCallback<Void> callback) {
        gradesRef.child(studentId).child(subjectId).setValue(grade)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void recalculateStudentGpa(String studentId, ApiCallback<Void> callback) {
        DatabaseReference studentSubjectsRef = FirebaseDatabase.getInstance().getReference("Student_Subjects");
        studentSubjectsRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> subjectIds = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    subjectIds.add(data.getKey());
                }

                if (subjectIds.isEmpty()) {
                    updateGpaTong(studentId, 0.0, callback);
                    return;
                }

                gradesRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot gradesSnapshot) {
                        double totalGpa = 0.0;
                        int count = subjectIds.size();
                        
                        for (String subjId : subjectIds) {
                            if (gradesSnapshot.hasChild(subjId)) {
                                Grade g = gradesSnapshot.child(subjId).getValue(Grade.class);
                                if (g != null) {
                                    totalGpa += g.getGpaMon();
                                }
                            }
                        }
                        
                        double averageGpa = count > 0 ? totalGpa / count : 0.0;
                        updateGpaTong(studentId, averageGpa, callback);
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onFailure(error.toException());
                    }
                });
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }
    
    private void updateGpaTong(String studentId, double newGpa, ApiCallback<Void> callback) {
        studentsRef.child(studentId).child("gpaTong").setValue(newGpa)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e));
    }
}
