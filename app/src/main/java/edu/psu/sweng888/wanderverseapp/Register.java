package edu.psu.sweng888.wanderverseapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName, editTextAge, editTextPreference;
    TextView textView;
    Button buttonRegistration;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore fstore;
    String userID;

    @Override
    public void onStart() {
        super.onStart();
        // Checks if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFirstName = findViewById(R.id.f_name);
        editTextLastName = findViewById(R.id.l_name);
        editTextAge = findViewById(R.id.age);
        editTextPreference = findViewById(R.id.t_pref);
        buttonRegistration = findViewById(R.id.button_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonRegistration.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                progressBar.setVisibility(View.VISIBLE);
                String email, password, fName, lName, age, preference;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                fName = String.valueOf(editTextFirstName.getText());
                lName = String.valueOf(editTextLastName.getText());
                age = String.valueOf(editTextAge.getText());
                preference = String.valueOf(editTextPreference.getText());

                // null checks etc
                if (TextUtils.isEmpty(fName)) {
                    Toast.makeText(Register.this, "Enter your first name", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(lName)) {
                    Toast.makeText(Register.this, "Enter your last name", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter a non-empty email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter a non-empty password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(Register.this, "Your password must be 6 characters or longer", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(age)) {
                    Toast.makeText(Register.this, "Enter your age", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(preference)) {
                    Toast.makeText(Register.this, "Enter your activity preference", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Registration Completed Succesfully",
                                            Toast.LENGTH_SHORT).show();

                                    // Retrieves user ID and store additional user details in Firestore
                                    userID = mAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = fstore.collection("users").document(userID);

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("fName", fName);
                                    user.put("lName", lName);
                                    user.put("email", email);
                                    user.put("age", age);
                                    user.put("preference", preference);

                                    // Stores user details in Firestore
                                    documentReference.set(user).addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Register.this, "User Profile Created", Toast.LENGTH_SHORT).show();
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(Register.this, "Error! Profile Creation Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });


                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();

                                }
                                else {
                                    // If sign-in fails, displays a message to the user.
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthUserCollisionException e) {
                                        // Email already in use
                                        Toast.makeText(Register.this, "Unable To Create Account: This email is already registered.", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        // Handle other error/excp
                                        Toast.makeText(Register.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });

            }
        });
        };
    }
