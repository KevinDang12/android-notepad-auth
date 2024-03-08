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
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Main Content Activity containing the Title and Notepad
 */
public class NotepadActivity extends AppCompatActivity {

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

    private final String BASE_URL = "http://notepad.kevindang12.com/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        setContentView(R.layout.notepad_activity);
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
            getUsers(firstName, lastName);
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
                                getUsers(firstName, lastName);

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

    /**
     * Get all the users from the backend server
     *
     * @param firstName The User's first name
     * @param lastName The User's last name
     */
    private void getUsers(String firstName, String lastName) {
        Call<List<UserId>> getNotes = retrofitInterface.getUsers();

        getNotes.enqueue(new Callback<List<UserId>>() {
            @Override
            public void onResponse(Call<List<UserId>> call, Response<List<UserId>> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    boolean foundUser = false;
                    for (UserId userId : response.body()) {
                        if (id.equals(userId.getId())) {
                            foundUser = true;
                            break;
                        }
                    }

                    if (!foundUser) {
                        addNewUser(firstName, lastName);
                    }
                    getUserById();
                }
            }

            @Override
            public void onFailure(Call<List<UserId>> call, Throwable t) {
                Log.e("Error", "An error occurred: " + t.getMessage());
                t.printStackTrace();
            }
        });
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
                Log.e("Error", "An error occurred: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    /**
     * Retrieve the User by their ID
     */
    private void getUserById() {
        Call<User> getNoteById = retrofitInterface.getUserById(id);
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
                Log.e("Error", "An error occurred: " + t.getMessage());
                t.printStackTrace();
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

    /**
     * Save the results of the title and notes to the backend server
     */
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
                Toast.makeText(NotepadActivity.this, "Your Notes are Saved!",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Error", "An error occurred: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    /**
     * Handle the sign out functionality for the notepad app
     */
    private void signOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                LoginManager.getInstance().logOut();
                startActivity(new Intent(NotepadActivity.this, LoginActivity.class));
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

    /**
     * Initialize the Title and Notepad area with text
     *
     * @param title The text for the Title
     * @param data The text for the Notepad
     */
    private void initView(String title, String data) {
        this.title = title;
        this.data = data;

        this.titleArea.setText(this.title);
        this.contentEditText.setText(this.data);
        this.contentEditText.fillScreen();

        ViewTreeObserver observer = this.contentEditText.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> NotepadActivity.this.contentEditText.fillScreen());
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
