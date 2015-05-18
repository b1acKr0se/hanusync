package io.wyrmise.hanusync;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mIdView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView no_internet;
    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        setContentView(R.layout.activity_login);
        setTheme(R.style.White);
        // Set up the login form.
        mIdView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    new InternetCheckingAsyncTask().execute();
                    return true;
                }
                return false;
            }
        });

        no_internet = (TextView) findViewById(R.id.no_internet);
        retry = (Button) findViewById(R.id.reconnectBtn);
        retry.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                new ReconnectAsyncTask().execute();
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new InternetCheckingAsyncTask().execute();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

    }

    public void checkForInternet(boolean check) {
        if (check) {
            mLoginFormView.setVisibility(View.VISIBLE);
            no_internet.setVisibility(TextView.GONE);
            retry.setVisibility(Button.GONE);
        } else {
            mLoginFormView.setVisibility(View.GONE);
            no_internet.setVisibility(TextView.VISIBLE);
            retry.setVisibility(Button.VISIBLE);
        }
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mIdView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String id = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(id)) {
            mIdView.setError(getString(R.string.error_field_required));
            focusView = mIdView;
            cancel = true;
        } else if (!isIDValid(id)) {
            mIdView.setError(getString(R.string.error_invalid_email));
            focusView = mIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(id, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isIDValid(String id) {
        return id.matches("[0-9]+");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mId;
        private final String mPassword;
        private Map<String, String> cookies = new HashMap<String, String>();

        protected void onPreExecute() {
            mProgressView.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.GONE);
        }

        UserLoginTask(String email, String password) {
            mId = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection.Response loginForm = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/login/index.php")
                        .data("username", mId)
                        .data("password", mPassword)
                        .method(Connection.Method.POST)
                        .execute();

                cookies = loginForm.cookies();

                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/my/")
                        .cookies(cookies)
                        .get();

                Thread.sleep(2000);

                if (document.title().contains("Faculty of IT - HANU :: Portal: Login to the site"))
                    return false;
                return true;

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                myIntent.putExtra("id", mId);
                myIntent.putExtra("cookies", (java.io.Serializable) cookies);
                LoginActivity.this.startActivity(myIntent);

            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }

    private class InternetCheckingAsyncTask extends AsyncTask<Void, Void, Boolean> {

        protected void onPreExecute() {
            mProgressView.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (isOnline())
                return true;
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                attemptLogin();
            } else {
                mProgressView.setVisibility(View.GONE);
                mLoginFormView.setVisibility(View.GONE);
                checkForInternet(false);
            }
        }

    }

    private class ReconnectAsyncTask extends AsyncTask<Void, Void, Boolean> {

        protected void onPreExecute() {
            mProgressView.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.GONE);
            no_internet.setVisibility(TextView.GONE);
            retry.setVisibility(Button.GONE);
            Toast.makeText(getApplicationContext(),"Reconnecting, please wait",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (isOnline())
                return true;
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                checkForInternet(true);
                mProgressView.setVisibility(View.GONE);
                mLoginFormView.setVisibility(View.VISIBLE);
            } else {
                mProgressView.setVisibility(View.GONE);
                mLoginFormView.setVisibility(View.GONE);
                checkForInternet(false);
            }
        }

    }
}



