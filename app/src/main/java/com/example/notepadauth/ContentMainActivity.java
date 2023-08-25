package com.example.notepadauth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Main Content Activity containing the Title and Notepad
 */
public class ContentMainActivity extends AppCompatActivity {

    /** Save title written by the user */
    private String title;

    /** Saved notes written by the user */
    private String data;

    /** The account id of the user */
    private String id;

    /** Area for the user to write their notes */
    private NotePadArea contentEditText;

    /** Area for the user to write their title */
    private TitleArea titleArea;

    /** Configuration options for Google Sign-In. */
    GoogleSignInOptions gso;

    /** Client to perform Google Sign-In operations. */
    GoogleSignInClient gsc;

    /** REST API Interface */
    private RetrofitInterface retrofitInterface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String BASE_URL = "http://192.168.50.201:5000";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        setContentView(R.layout.content_main);
        this.titleArea = findViewById(R.id.title);
        this.contentEditText = findViewById(R.id.notepad);

        NotePadTornPage contentImageView = findViewById(R.id.notepad_torn);
        contentImageView.setLineHeight(this.contentEditText.getLineHeight());

        // Switch statement checking the value of the action event, if action is equal to ACTION_UP, show the soft
        // keyboard on screen else don't show the keyboard break
        View view = findViewById(android.R.id.content);
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                showSoftKeyboard(contentEditText);
            }
            return true;
        });

        this.showSoftKeyboard(contentEditText);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            id = acct.getId();
            String firstName = acct.getGivenName();
            String lastName = acct.getFamilyName();
            Call<User[]> getNotes = retrofitInterface.getNotes();

            getNotes.enqueue(new Callback<User[]>() {
                @Override
                public void onResponse(Call<User[]> call, Response<User[]> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        boolean foundUser = false;
                        for (User note : response.body()) {
                            if (id.equals(note.getId())) {
                                foundUser = true;
                                break;
                            }
                        }

                        if (!foundUser) {
                            Log.d("debug", "Add new User");
                            addNewUser(firstName, lastName);
                        }

                        Log.d("debug", "Get User");
                        getNoteById();
                    }
                }

                @Override
                public void onFailure(Call<User[]> call, Throwable t) {
                    // App code
                }
            });
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            try {
                                id = object.getString("id");
                                String firstName = object.getString("first_name");
                                String lastName = object.getString("last_name");

                                Call<User[]> getNotes = retrofitInterface.getNotes();

                                getNotes.enqueue(new Callback<User[]>() {
                                    @Override
                                    public void onResponse(Call<User[]> call, Response<User[]> response) {
                                        if (response.isSuccessful()) {
                                            assert response.body() != null;
                                            boolean foundUser = false;
                                            for (User note : response.body()) {
                                                if (id.equals(note.getId())) {
                                                    foundUser = true;
                                                    break;
                                                }
                                            }
                                            if (!foundUser) {
                                                addNewUser(firstName, lastName);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<User[]> call, Throwable t) {

                                    }
                                });

                                Call<User> getNoteById = retrofitInterface.getNoteById(id);
                                getNoteById.enqueue(new Callback<User>() {
                                    @Override
                                    public void onResponse(Call<User> call, Response<User> response) {
                                        if (response.isSuccessful()) {
                                            assert response.body() != null;
                                            title = response.body().getTitle();
                                            data = response.body().getNote();
                                            initView(title, data);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<User> call, Throwable t) {

                                    }
                                });
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,first_name,last_name");
            request.setParameters(parameters);
            request.executeAsync();
        }
    }

    private void addNewUser(String firstName, String lastName) {
        HashMap<String, String> user = new HashMap<>();
        user.put("id", id);
        user.put("first_name", firstName);
        user.put("last_name", lastName);
        user.put("title", "");
        user.put("note", "Enter your notes here!");
        Call<Void> saveNotes = retrofitInterface.saveNotes(user);

        saveNotes.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // App code
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // App code
            }
        });
    }

    private void getNoteById() {
        Call<User> getNoteById = retrofitInterface.getNoteById(id);
        getNoteById.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    title = response.body().getTitle();
                    data = response.body().getNote();
                    initView(title, data);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // App code
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_button) {
            this.saveData();
        }

        if (id == R.id.logout) {
            signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        title = this.getNotesTitle();
        data = this.getNotes();

        HashMap<String, String> user = new HashMap<>();
        user.put("title", title);
        user.put("note", data);

        Call<Void> updateNotes = retrofitInterface.updateNotes(id, user);

        updateNotes.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(ContentMainActivity.this, "Your Notes are Saved!",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // App code
            }
        });
    }

    private void signOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                LoginManager.getInstance().logOut();
                startActivity(new Intent(ContentMainActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        gsc.signOut();
        LoginManager.getInstance().logOut();
    }

    private void initView(String title, String data) {
        this.title = title;
        this.data = data;

        this.titleArea.setText(this.title);
        this.contentEditText.setText(this.data);
        this.contentEditText.fillScreen();

        ViewTreeObserver observer = this.contentEditText.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> ContentMainActivity.this.contentEditText.fillScreen());
    }

    /**
     * Opens keyboard focus on the view.
     *
     * @param view View that the keyboard opens for.
     */
    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * Return the text that the user typed in.
     *
     * @return text from contentEditText.
     */
    private String getNotes() {
        return Objects.requireNonNull(this.contentEditText.getText()).toString();
    }

    /**
     * Return the title that the user typed in.
     *
     * @return text from the titleArea.
     */
    private String getNotesTitle() {
        return this.titleArea.getText().toString();
    }
}
