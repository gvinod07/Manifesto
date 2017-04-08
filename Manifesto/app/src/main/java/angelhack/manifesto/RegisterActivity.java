package angelhack.manifesto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.request.Requests;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends Activity {

    private TextInputLayout mNameInput;
    private TextInputLayout mMailInput;
    private TextInputLayout mPaswordInput;
    private TextInputLayout mAddressInput;
    private TextInputLayout mIdentityInput;
    private Spinner mIdentityType;

    private ProgressDialog mDialog;

    private Activity mActivity;

    private String UserUId;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.content_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mActivity = this;
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage("Please Wait");

        mNameInput = (TextInputLayout) findViewById(R.id.username_input_layout);
        mMailInput = (TextInputLayout) findViewById(R.id.email_input_layout);
        mPaswordInput = (TextInputLayout) findViewById(R.id.password_input_layout);
        mAddressInput = (TextInputLayout) findViewById(R.id.address_input_layout);
        mIdentityInput = (TextInputLayout) findViewById(R.id.identity_input_layout);
        mIdentityType = (Spinner) findViewById(R.id.spinner);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("FireBase", "onAuthStateChanged:signed_in:" + user.getUid());
                    UserUId = user.getUid();
                } else {
                    // User is signed out
                    Log.d("FireBase", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        Button register_btn = (Button) findViewById(R.id.btnRegister);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

    }

    private void signup() {
        String name = mNameInput.getEditText().getText().toString().trim();
        final String email = mMailInput.getEditText().getText().toString().trim();
        String password = mPaswordInput.getEditText().getText().toString().trim();
        String idType = mIdentityType.getSelectedItem().toString();
        String username = mNameInput.getEditText().getText().toString().trim();
        String idNumber = mIdentityInput.getEditText().getText().toString().trim();
        String address = mAddressInput.getEditText().getText().toString().trim();

        final User user = new User();
        user.Name = name;
        user.EMail = email;
        user.IdentityType = idType;
        user.IdentificationNumber = idNumber;
        user.Address = address;

        boolean error = false;
        if (name.equals("")) {
            mNameInput.setError("Enter your name");
            error = true;
        } else {
            mNameInput.setError("");
        }
        if (email.equals("")) {
            mMailInput.setError("Enter your email");
            error = true;
        } else {
            String arr[] = email.split("@");
            if (arr.length != 2) {
                mMailInput.setError("This doesn't look like an email id...");
                error = true;
            } else {
                mMailInput.setError("");
            }
        }

        if (password.equals("")) {
            mPaswordInput.setError("Enter a password");
            error = true;
        } else {
            mPaswordInput.setError("");
        }

        if (username.equals("")) {
            mNameInput.setError("Enter a name");
            error = true;
        } else {
            mNameInput.setError("");
        }
        name = mNameInput.getEditText().getText().toString().trim();

        if (!error) {
            mDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Log.d("FireBase", "createUserWithEmail:onComplete:" + task.isSuccessful());
                                Toast.makeText(mActivity, "Sign Up Complete", Toast.LENGTH_LONG).show();

                                user.uID = UserUId;
                                mDatabase.child("users").push().setValue(user);
                                mDialog.hide();

                                Intent intent = new Intent(mActivity, MainActivity.class);
                                startActivity(intent);
                            }

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.d("Firebase", "Unsuccesful");
                                Toast.makeText(mActivity, "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                            }
                        }
                        // ...
                    });
        }

    }
}