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

import java.util.Objects;

/**
 * AppCompatActivity containing main content.
 */
public class ContentMainActivity extends AppCompatActivity {

    /** Saved notes written by the user */
    private String data;

    /** Area for the user to write their notes */
    private NotePadArea contentEditText;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initialize views and perform other setup
        initView();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String id = acct.getId();
            String firstName = acct.getGivenName();
            String lastName = acct.getFamilyName();
            Log.d("debug", "Google");
            Log.d("debug", id);
            Log.d("debug", firstName);
            Log.d("debug", lastName);
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
                                String id = object.getString("id");
                                String firstName = object.getString("first_name");
                                String lastName = object.getString("last_name");
                                Log.d("debug", "Facebook");
                                Log.d("debug", id);
                                Log.d("debug", firstName);
                                Log.d("debug", lastName);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_button) {
//            this.saveData();
        }

        if (id == R.id.logout) {
            signOut();
        }

        return super.onOptionsItemSelected(item);
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

    private void initView() {
        this.contentEditText = findViewById(R.id.notepad_input);
        this.contentEditText.setText(data);
        this.contentEditText.fillScreen();

        NotePadTornPage contentImageView = findViewById(R.id.notepad_torn);
        contentImageView.setLineHeight(this.contentEditText.getLineHeight());

        ViewTreeObserver observer = this.contentEditText.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> ContentMainActivity.this.contentEditText.fillScreen());

        // Switch statement checking the value of the action event, if action is equal to ACTION_UP, show the soft
        // keyboard on screen else don't show the keyboard break
        View view = findViewById(android.R.id.content); // Or use a specific layout container id
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                showSoftKeyboard(contentEditText);
            }
            return true;
        });

        this.showSoftKeyboard(contentEditText);
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
    public String getNotes() {
        return Objects.requireNonNull(this.contentEditText.getText()).toString();
    }
}
