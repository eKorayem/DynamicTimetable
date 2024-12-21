package com.example.dynamictimetable;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class TimetableActivity extends AppCompatActivity {
    private TextView levelTextView, sectionTextView, dateTextView;
    private FirebaseAuth auth;
    private FirebaseFirestore fStore;
    private String userID;
    private String studentLevel;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        try {

            dateTextView = findViewById(R.id.dateTextView);
            levelTextView = findViewById(R.id.levelNumberTextView);
            sectionTextView = findViewById(R.id.sectionNumberTextView);

            auth = FirebaseAuth.getInstance();
            fStore = FirebaseFirestore.getInstance();

            // Set the current date
            String today = currentDay();
            dateTextView.setText(today);

            logoutButton = findViewById(R.id.logoutButton);
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    auth.signOut();
                    Toast.makeText(TimetableActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(TimetableActivity.this, MainActivity.class));
                }
            });

            if (auth.getCurrentUser() == null) {
                Log.e("TimetableActivity", "User is not logged in");
                return;
            }

            userID = auth.getCurrentUser().getUid();

            // Fetch user level and section
            DocumentReference userDocRef = fStore.collection("users").document(userID);
            userDocRef.addSnapshotListener(this, (value, error) -> {
                if (error != null) {
                    Log.e("TimetableActivity", "Error fetching user data.", error);
                    return;
                }

                if (value != null && value.exists()) {
                    try {
                        Long level = value.getLong("Level");
                        if (level != null) {
                            studentLevel = "Level_" + level;
                            levelTextView.setText("Level: " + level);

                            // Fetch lectures after studentLevel is determined
                            fetchLectures(studentLevel);
                        }

                        Long section = value.getLong("Section");
                        if (section != null) {
                            sectionTextView.setText("Section: " + section);
                        }
                    } catch (Exception e) {
                        Log.e("TimetableActivity", "Error processing user data.", e);
                    }
                } else {
                    Log.d("TimetableActivity", "User data not found.");
                }
            });
        } catch (Exception e) {
            Log.e("TimetableActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error loading timetable", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchLectures(String studentLevel) {
        DocumentReference lecturesDocRef = fStore.collection("Departments")
                .document("ICT")
                .collection(studentLevel)
                .document("Lectures");

        lecturesDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    Map<String, Object> data = doc.getData();
                    if (data != null) {
                        displayLectures(data);
                    }
                } else {
                    Log.d("TimetableActivity", "No lectures document exists");
                }
            } else {
                Log.e("TimetableActivity", "Failed to get lectures", task.getException());
            }
        });
    }

    private void displayLectures(Map<String, Object> data) {
        RelativeLayout lecturesLayout = findViewById(R.id.tasksLayout);
        if (lecturesLayout != null) {
            lecturesLayout.removeAllViews(); // Clear existing views
        }

        int previousTextViewId = -1;
        String today = currentDay();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> lectureMap = (Map<String, Object>) entry.getValue();
                if (today.equals(lectureMap.get("day"))) {
                    String courseName = String.valueOf(lectureMap.get("course_name"));

                    Object startTimeObj = lectureMap.get("start_time");
                    String startTime = (startTimeObj instanceof Timestamp) ?
                            formatTimestamp((Timestamp) startTimeObj) :
                            String.valueOf(startTimeObj);

                    Object endTimeObj = lectureMap.get("end_time");
                    String endTime = (endTimeObj instanceof Timestamp) ?
                            formatTimestamp((Timestamp) endTimeObj) :
                            String.valueOf(endTimeObj);

                    String hallNumber = String.valueOf(lectureMap.get("hall_number"));
                    String professorName = String.valueOf(lectureMap.get("professor_name"));

                    createLectureCard(
                            lecturesLayout,
                            previousTextViewId,
                            courseName,
                            startTime,
                            endTime,
                            hallNumber,
                            professorName
                    );

                    previousTextViewId = lecturesLayout.getChildAt(lecturesLayout.getChildCount() - 1).getId();
                }
            }
        }
    }

    private void createLectureCard(RelativeLayout parent, int previousId,
                                   String courseName, String startTime, String endTime,
                                   String hallNumber, String professorName) {
        try {
            TextView lectureCard = new TextView(this);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    convertDpToPx(120)
            );

            if (previousId == -1) {
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            } else {
                params.addRule(RelativeLayout.BELOW, previousId);
                params.topMargin = convertDpToPx(16);
            }

            int uniqueId = View.generateViewId();
            lectureCard.setId(uniqueId);

            lectureCard.setLayoutParams(params);
            lectureCard.setText(String.format("%s\nTime: %s - %s\nHall: %s\nProfessor: %s",
                    courseName, startTime, endTime, hallNumber, professorName));
            lectureCard.setTextSize(16);
            lectureCard.setTextColor(getResources().getColor(android.R.color.white));
            lectureCard.setPadding(convertDpToPx(8), convertDpToPx(8), convertDpToPx(8), convertDpToPx(8));
            lectureCard.setBackground(getResources().getDrawable(R.drawable.event_card_blue));

            parent.addView(lectureCard);
        } catch (Exception e) {
            Log.e("TimetableActivity", "Error creating lecture card", e);
        }
    }

    private String currentDay() {
        Calendar calendar = Calendar.getInstance();
        String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    private int convertDpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String formatTimestamp(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp.toDate());
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int amPm = calendar.get(Calendar.AM_PM);

        String period = (amPm == Calendar.AM) ? "AM" : "PM";
        if (hour == 0) {
            hour = 12;
        }

        return String.format("%02d:%02d %s", hour, minute, period);
    }
}
