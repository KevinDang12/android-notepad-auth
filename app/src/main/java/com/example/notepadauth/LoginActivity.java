package com.example.notepadauth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The layout for the Login Page for Google and Facebook
 */
public class LoginActivity extends AppCompatActivity {

    /** Configuration options for Google Sign-In. */
    GoogleSignInOptions gso;

    /** Client to perform Google Sign-In operations. */
    GoogleSignInClient gsc;

    /** Google Login Button */
    Button googleBtn;

    /** REST API Interface */
    private RetrofitInterface retrofitInterface;

    /** Progress Bar Spinner */
    private ProgressBar spinner;

    private final String BASE_URL = "http://notepad.kevindang12.com/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        spinner = findViewById(R.id.loadingBar);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        googleBtn = findViewById(R.id.google_login);

        googleBtn.setOnClickListener(view -> {
            spinner.setVisibility(View.VISIBLE);
            googleSignIn();
        });
    }

    /**
     * Sign-In functionality for Google
     */
    private void googleSignIn() {
        Call<Void> getStatus = retrofitInterface.getStatus();
        getStatus.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent signInIntent = gsc.getSignInIntent();
                    startActivityForResult(signInIntent, 1000);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Unable to sign in with Google. Please try again later.",
                        Toast.LENGTH_LONG).show();
                spinner.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                navigateToNotepad();
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Navigate to the Notepad Area
     */
    private void navigateToNotepad() {
        finish();
        Intent intent = new Intent(LoginActivity.this, NotepadActivity.class);
        startActivity(intent);
    }
}
