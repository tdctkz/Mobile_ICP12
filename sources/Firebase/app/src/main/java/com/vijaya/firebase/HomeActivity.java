package com.vijaya.firebase;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private TextView txtDetails;
    private EditText inputName, inputPhone;
    private Button btnSave;
    private Button btnLogout;
    private Button btnDelete;
    private ProgressBar progressBar;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String userId;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Displaying toolbar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        txtDetails = (TextView) findViewById(R.id.txt_user);
        inputName = (EditText) findViewById(R.id.name);
        inputPhone = (EditText) findViewById(R.id.phone);
        btnSave = (Button) findViewById(R.id.btn_save);
        //new variables added here. Logout, delete, and progress bar
        btnLogout = (Button) findViewById(R.id.btn_logout);
        btnDelete = (Button) findViewById(R.id.btn_delete);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        firebaseAuth = firebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();





        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");

        // store app title to 'app_title' node
        mFirebaseInstance.getReference("app_title").setValue("Realtime Database");

        // app_title change listener
        mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "App title updated");

                String appTitle = dataSnapshot.getValue(String.class);

                // update toolbar title
                getSupportActionBar().setTitle(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });

        // Save / update the user
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String phone = inputPhone.getText().toString();

                // Check for already existed userId
                if (TextUtils.isEmpty(userId)) {
                    createUser(name, phone);
                } else {
                    updateUser(name, phone);
                }
            }
        });

        toggleButton();

        /**TODO ADDING LOG OUT FUNCTIONALITY HERE
         */
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                //new intent created
                Intent intent = new Intent ( HomeActivity.this, LoginActivity.class);
                //adding flags
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //begin task
                startActivity(intent);
            }

        });
        /**TODO ADDING DELETE FUNCTINALITY HERE
         *
         */

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creating dialog box to notify user of what is happening
                AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                //provides a prompt to user to make sure they know what they are doing
                dialog.setTitle(getString(R.string.delete_prompt_title));
                dialog.setMessage(getString(R.string.delete_prompt_message));

                /**IF USER SAYS THEY WISH TO DELETE THEIR ACCOUNT, DELETE INFO FROM DB AND GO TO HOME PAGE
                 *
                 */
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //show progress bar
                        //progressBar.setVisibility(View.VISIBLE);
                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //remove progress bar
                                    //progressBar.setVisibility(View.GONE);
                                    //if the deletion works, tell the user the account
                                    // has been deleted.
                                    Log.d(TAG, "User account has been deleted.");
                                    Toast.makeText(HomeActivity.this,"Account " +
                                            "has been deleted.", Toast.LENGTH_LONG).show();

                                    //new intent created
                                    Intent intent = new Intent ( HomeActivity.
                                            this, LoginActivity.class);
                                    //adding flags
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    //start intent
                                    startActivity(intent);

                                }else{
                                    Toast.makeText(HomeActivity.this,task.getException()
                                            .getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });

                /**NEGATIVE BUTTON. IF USER SAYS THEY DO NOT WISH TO DELETE THEIR ACCOUNT, DO NOTHING
                 *
                 */
                dialog.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        //removes dialog box and goes back to the same page
                        dialogInterface.dismiss();

                    }
                });

                //this section will show the dialog
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
                //deleteUser();
            }

        });
    }

    // Changing button text
    private void toggleButton() {
        if (TextUtils.isEmpty(userId)) {
            btnSave.setText("Save");
        } else {
            btnSave.setText("Update");
        }
    }

    /**
     * Creating new user node under 'users'
     */
    private void createUser(String name, String phone) {
        // TODO
        // In real apps this userId should be fetched
        // by implementing firebase auth
        if (TextUtils.isEmpty(userId)) {
            userId = mFirebaseDatabase.push().getKey();
        }
        User user = new User(name, phone);
        mFirebaseDatabase.child(userId).setValue(user);
        addUserChangeListener();
    }

    /**
     * User data change listener
     */
    private void addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!");
                    return;
                }
                Log.e(TAG, "User data is changed!" + user.name + ", " + user.phone);

                // Display newly updated name and phone
                txtDetails.setText(user.name + ", " + user.phone);

                // clear edit text
                inputName.setText("");
                inputPhone.setText("");
                toggleButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });
    }

    private void updateUser(String name, String phone) {
        // updating the user via child nodes
        if (!TextUtils.isEmpty(name))
            mFirebaseDatabase.child(userId).child("name").setValue(name);

        if (!TextUtils.isEmpty(phone))
            mFirebaseDatabase.child(userId).child("phone").setValue(phone);
    }

    /**TODO COMPLETED HERE DELETES USER ACCOUNT FROM DB
     */
    private void deleteUser(){

    }
}