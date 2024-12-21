package com.example.dynamictimetable;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText emailEditText, passwordEditText, collageIdEditText;
    String userID;
    int selectedLevel, selectedSection;
    FirebaseFirestore fstore;


    Spinner levelSpinner;
    int[] levels;
    Integer[] levelArray;
    ArrayAdapter<Integer> levelAdapter;

    Spinner sectionSpinner;
    int[] sections;
    Integer[] sectionArray;
    ArrayAdapter<Integer> sectionAdapter;

    private Button registerButton;
    private ImageView goBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ImageView backButton = findViewById(R.id.backButton_register);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Optional: Closes RegisterActivity so it doesn't remain in the back stack
            }
        });

        // Levels drop menue
        levelSpinner = findViewById(R.id.levelSpinner_register);
        levels = getResources().getIntArray(R.array.levels);
        levelArray = Arrays.stream(levels).boxed().toArray(Integer[]::new);
        levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levelArray);

        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(levelAdapter);

        // Sections Drop menue
        sectionSpinner = findViewById(R.id.sectionSpinner_register);
        sections = getResources().getIntArray(R.array.sections);
        sectionArray = Arrays.stream(sections).boxed().toArray(Integer[]::new);
        sectionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sectionArray);

        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(sectionAdapter);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        // User data
        emailEditText = findViewById(R.id.email_login);
        passwordEditText = findViewById(R.id.password_login);
        collageIdEditText = findViewById(R.id.collageId_register);


        registerButton = findViewById(R.id.btn_login);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String collage_id = collageIdEditText.getText().toString().trim();
                selectedLevel = (int) levelSpinner.getSelectedItem();
                selectedSection = (int) sectionSpinner.getSelectedItem();
                // Validate user input (e.g., check for empty fields, email format)
                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
                    Toast.makeText(RegisterActivity.this, "Empty Credentials!", Toast.LENGTH_SHORT).show();
                else if(password.length() < 8)
                    Toast.makeText(RegisterActivity.this, "Password is too short", Toast.LENGTH_SHORT).show();
                else registerUser(email, password, collage_id, selectedLevel, selectedSection);
            }
        });


    }

    private void registerUser(String email, String password, String collage_id, int level, int section) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    userID = auth.getCurrentUser().getUid();
                    DocumentReference dr = fstore.collection("users").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("CollageID", collage_id);
                    user.put("Level", level);
                    user.put("Section", section);
                    dr.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(RegisterActivity.this, "Registering user success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, TimetableActivity.class));
                            finish();
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this, "Registering failed!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}