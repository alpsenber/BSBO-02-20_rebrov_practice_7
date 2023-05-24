package ru.mirea.rebrov.firebaseauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import ru.mirea.rebrov.firebaseauth.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        binding.button3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signIn(binding.editTextTextPersonName.getText().toString(), binding.editTextTextPersonName2.getText().toString());
            }
        });
        binding.button4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signOut();
            }
        });
        binding.button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createAccount(binding.editTextTextPersonName.getText().toString(), binding.editTextTextPersonName2.getText().toString());
            }
        });
        binding.button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendEmailVerification();
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user)
    {
        if (user != null) {
            binding.textView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail(), user.isEmailVerified()));
            binding.textView2.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            binding.editTextTextPersonName.setVisibility(View.GONE);
            binding.editTextTextPersonName2.setVisibility(View.GONE);
            binding.button.setVisibility(View.GONE);
            binding.button2.setVisibility(View.VISIBLE);
            binding.button2.setEnabled(!user.isEmailVerified());
            binding.button3.setVisibility(View.GONE);
            binding.button4.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.textView.setText(R.string.signed_out);
            binding.textView2.setText(null);
            binding.editTextTextPersonName.setVisibility(View.VISIBLE);
            binding.editTextTextPersonName2.setVisibility(View.VISIBLE);
            binding.button.setVisibility(View.VISIBLE);
            binding.button2.setVisibility(View.GONE);
            binding.button3.setVisibility(View.VISIBLE);
            binding.button4.setVisibility(View.GONE);
        }
    }

    private void createAccount(String email, String password)
    {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm())
        {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else
                        {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private boolean validateForm()
    {
        if(binding.editTextTextPersonName2.getText().toString().length() <6)
        {
            return false;
        }
        return !TextUtils.isEmpty(binding.editTextTextPersonName.getText().toString()) && android.util.Patterns.EMAIL_ADDRESS.matcher(binding.editTextTextPersonName.getText().toString()).matches();
    }

    private void signIn(String email, String password)
    {
        Log.d(TAG, "signIn:" + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else
                        {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        if (!task.isSuccessful())
                        {
                            binding.textView.setText(R.string.auth_failed);
                        }
                    }
                });
    }

    private void signOut()
    {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification()
    {
        binding.button2.setEnabled(false);
        final FirebaseUser user = mAuth.getCurrentUser();
        Objects.requireNonNull(user).sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        binding.button2.setEnabled(true);
                        if (task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(MainActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}