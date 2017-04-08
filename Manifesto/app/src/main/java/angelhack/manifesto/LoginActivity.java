package angelhack.manifesto;

import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private Activity mActivity;
    private TextInputLayout mEmailInput;
    private TextInputLayout mPasswordInput;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private ProgressDialog mDialog;
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
        // setting default screen to login.xml
        setContentView(R.layout.activity_login);

        mActivity = this;
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage("Please Wait");

        mEmailInput = (TextInputLayout) findViewById(R.id.login_email_input);
        mPasswordInput = (TextInputLayout) findViewById(R.id.login_password_input);

        Button registerScreen = (Button) findViewById(R.id.link_to_register);

        Button btn = (Button) findViewById(R.id.btnLogin);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();

                signin();
            }
        });

        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("FireBase", "onAuthStateChanged:signed_in:" + user.getUid());

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d("FireBase", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    private void signin() {
        String mail = mEmailInput.getEditText().getText().toString().trim();
        String password = mPasswordInput.getEditText().getText().toString().trim();
        boolean error = false;
        if (mail.equals("")) {
            mEmailInput.setError("Enter your email");
            error = true;
        } else {
            String arr[] = mail.split("@");
            if (arr.length != 2) {
                mEmailInput.setError("This doesn't look like an email id...");
                error = true;
            } else {
                mEmailInput.setError("");
            }
        }
        if (password.equals("")) {
            mPasswordInput.setError("Enter a password");
            error = true;
        } else {
            mPasswordInput.setError("");
        }
        if (!error) {
            mDialog.show();
            makeProfile(mail, password);
        }
    }

    private void makeProfile(final String mail, String password) {
        try {
            if (checkInternetConenction()) {
                Toast.makeText(getApplicationContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                mDialog.hide();
            } else {
                mAuth.signInWithEmailAndPassword(mail, password)
                        .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Log.d("Firebase", "signInWithEmail:onComplete:" + task.isSuccessful());
                                    startActivity(new Intent(mActivity, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w("Firebase", "signInWithEmail:failed", task.getException());
                                    mDialog.hide();
                                    Toast.makeText(mActivity, "Sign-in Failed. Please Check Username and Password",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        } catch (Exception e) {
            FirebaseCrash.log(e.getMessage().toString());
            e.printStackTrace();
            mDialog.hide();
            Toast.makeText(mActivity, "Unable to post data!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
            Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

}