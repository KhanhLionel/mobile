package com.example.sms.api;

import com.example.sms.models.Subject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class SubjectApi {
    private DatabaseReference subjectsRef;
    private DatabaseReference studentSubjectsRef; // Node: Student_Subjects -> studentId -> [subjectId: true]

    public SubjectApi() {
        subjectsRef = FirebaseDatabase.getInstance().getReference("Subjects");
        studentSubjectsRef = FirebaseDatabase.getInstance().getReference("Student_Subjects");
    }

    public void getAllSubjects(ApiCallback<List<Subject>> callback) {
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Subject> subjects = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Subject s = data.getValue(Subject.class);
                    if (s != null) {
                        s.setId(data.getKey());
                        subjects.add(s);
                    }
                }
                callback.onSuccess(subjects);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void addSubject(Subject subject, ApiCallback<Void> callback) {
        String id = subjectsRef.push().getKey();
        if (id != null) {
            subject.setId(id);
            subjectsRef.child(id).setValue(subject)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onFailure(e));
        }
    }

    public void addStudentToSubject(String studentId, Subject newSubject, ApiCallback<Void> callback) {
        // 1. Kiểm tra trùng lịch trước khi thêm
        studentSubjectsRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> currentSubjectIds = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    currentSubjectIds.add(data.getKey());
                }

                if (currentSubjectIds.isEmpty()) {
                    // Sinh viên chưa học môn nào, map luôn
                    doMapStudentSubject(studentId, newSubject.getId(), callback);
                    return;
                }

                // Lấy thông tin các môn hiện tại để so sánh ca học
                checkConflictAndAdd(studentId, newSubject, currentSubjectIds, callback);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    private void checkConflictAndAdd(String studentId, Subject newSubject, List<String> currentSubjectIds, ApiCallback<Void> callback) {
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean conflict = false;
                String conflictSubjectName = "";

                for (String subjId : currentSubjectIds) {
                    DataSnapshot subjSnap = snapshot.child(subjId);
                    if (subjSnap.exists()) {
                        Subject s = subjSnap.getValue(Subject.class);
                        if (s != null && s.getCaHoc() != null && newSubject.getCaHoc() != null) {
                            String existingTime = normalizeTime(s.getCaHoc());
                            String newTime = normalizeTime(newSubject.getCaHoc());
                            if (existingTime.equals(newTime)) {
                                conflict = true;
                                conflictSubjectName = s.getTenMonHoc();
                                break;
                            }
                        }
                    }
                }

                if (conflict) {
                    callback.onFailure(new Exception("Trùng lịch với môn: " + conflictSubjectName));
                } else {
                    doMapStudentSubject(studentId, newSubject.getId(), callback);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    private String normalizeTime(String input) {
        if (input == null) return "";
        // Remove accents
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        // Remove spaces and make lowercase
        return normalized.replaceAll("\\s+", "").toLowerCase();
    }

    private void doMapStudentSubject(String studentId, String subjectId, ApiCallback<Void> callback) {
        studentSubjectsRef.child(studentId).child(subjectId).setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void updateSubject(Subject subject, ApiCallback<Void> callback) {
        subjectsRef.child(subject.getId()).setValue(subject)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteSubject(String subjectId, ApiCallback<Void> callback) {
        subjectsRef.child(subjectId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void removeStudentFromSubject(String studentId, String subjectId, ApiCallback<Void> callback) {
        studentSubjectsRef.child(studentId).child(subjectId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getStudentsForSubject(String subjectId, StudentApi studentApi, ApiCallback<List<com.example.sms.models.Student>> callback) {
        studentSubjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> studentIds = new ArrayList<>();
                for (DataSnapshot studentData : snapshot.getChildren()) {
                    if (studentData.hasChild(subjectId)) {
                        studentIds.add(studentData.getKey());
                    }
                }
                
                if (studentIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                studentApi.getAllStudents(new ApiCallback<List<com.example.sms.models.Student>>() {
                    @Override
                    public void onSuccess(List<com.example.sms.models.Student> allStudents) {
                        List<com.example.sms.models.Student> enrolled = new ArrayList<>();
                        for (com.example.sms.models.Student s : allStudents) {
                            if (studentIds.contains(s.getId())) {
                                enrolled.add(s);
                            }
                        }
                        callback.onSuccess(enrolled);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void getSubjectsForStudent(String studentId, ApiCallback<List<Subject>> callback) {
        studentSubjectsRef.child(studentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> subjectIds = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    subjectIds.add(data.getKey());
                }

                if (subjectIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                getAllSubjects(new ApiCallback<List<Subject>>() {
                    @Override
                    public void onSuccess(List<Subject> allSubjects) {
                        List<Subject> enrolled = new ArrayList<>();
                        for (Subject s : allSubjects) {
                            if (subjectIds.contains(s.getId())) {
                                enrolled.add(s);
                            }
                        }
                        callback.onSuccess(enrolled);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }
}
