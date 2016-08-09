package swarmless.gituserbrowserapp.activites;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import swarmless.gituserbrowserapp.App;
import swarmless.gituserbrowserapp.R;
import swarmless.gituserbrowserapp.adapters.UserAdapter;
import swarmless.gituserbrowserapp.interfaces.GitHubClient;
import swarmless.gituserbrowserapp.adapters.EndlessRecyclerOnScrollListener;
import swarmless.gituserbrowserapp.models.User;
import swarmless.gituserbrowserapp.utils.Utils;
import swarmless.gituserbrowserapp.webService.ServiceGenerator;

import static swarmless.gituserbrowserapp.utils.Utils.isAndroid6;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int lastId = 1;
    Toolbar toolbar;
    GitHubClient client;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout userListEmpty;
    private UserAdapter userAdapter;
    protected RecyclerView.LayoutManager layoutManager;
    private Button retryButton;
    private android.app.AlertDialog loginDataDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.title_main_activity);
        }


        // initialise http client
        client = ServiceGenerator.createService(GitHubClient.class, Utils.getPrefs(this, Utils.PREFS_LOGIN_USERNAME_KEY, ""), Utils.getPrefs(this, Utils.PREFS_LOGIN_PASSWORD_KEY, ""));


        // showEnterAuthenticationDialog();
        if (isAndroid6()) {
              // no permissions needed - only internet permission
        }


        retryButton = (Button) findViewById(R.id.retry_button);
        userListEmpty = (LinearLayout) findViewById(R.id.users_list_empty);

        // recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setAddDuration(200);
        defaultItemAnimator.setRemoveDuration(100);
        recyclerView.setItemAnimator(defaultItemAnimator);
        recyclerView.setHasFixedSize(true);
        userAdapter = new UserAdapter(this, new ArrayList<User>());
        recyclerView.setAdapter(userAdapter);
        recyclerView.animate().translationY(300); // a little animation


        //SwipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUsers(0, true);  // swipe to refresh

            }
        });



        fetchUsers(0, true);// get users ..
    }


    @Override
    protected void onResume() {
        super.onResume();
       //refresh list everytime
    }

    /**
     * initialize and starts Asynchronous  to get users
     *
     * @param lastId the Id of the last User in dataset - used as starting point to next page - page size are left  default(30 records)
     * @param refresh true if list is being refreshed
     */
    private void fetchUsers(int lastId, final boolean refresh) {

        if (refresh) resetOnScrollListener(); // in case of refresh, reset the onScrollistener

        Log.d(TAG, "calling since:" + lastId + " refresh " + refresh);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        GitHubClient gitHubClient = ServiceGenerator.createService(GitHubClient.class);
        Call<List<User>> call = gitHubClient.users(lastId);
        call.enqueue(new Callback<List<User>>() {

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {

                Log.d(TAG, response.message() + "\n" + response.code() + "\n" + response.headers().get("link")); // just for control - the app handels the pagination itself ..


                if (response.isSuccessful()) {

                    if (response.body().isEmpty() && userAdapter.isEmpty()) {
                        hideRetryButton();
                        showNoUsersText();

                    } else if (userAdapter.isEmpty() || refresh) {
                        userAdapter.clear();
                        userAdapter.addAll(response.body());
                        userAdapter.addItem(null);
                        recyclerView.animate().translationY(0).setDuration(300);
                        hideNoUsersText();
                        hideRetryButton();

                    } else {
                        int lastSize = userAdapter.getItemCount() - 1;
                        userAdapter.addAll(response.body());
                        userAdapter.removerItem(lastSize);
                        userAdapter.addItem(null);
                        hideNoUsersText();
                        hideRetryButton();

                    }

                } else {
                    // error response, no access to resource?

                    switch (response.code()) {

                        case App.Const.RESPONSE_CODE_UNAUTHORIZED:

                            showEnterAuthenticationDialog();  // asks the user to enter correct authentication
                            break;

                        default:
                            showErrorFetchingDataDialog(response.message());
                            break;

                    }


                }

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);

                if (isConnectionAvailable()) {
                    showErrorFetchingDataDialog(t.getMessage());
                } else {
                    showNoInternetDialog();
                }

            }
        });
    }


    private boolean isConnectionAvailable() {
        return Utils.isConnectionAvailable(this);
    }


    /**
     *
     * resets the scrollListener
     */
    private void resetOnScrollListener() {

        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener((LinearLayoutManager) layoutManager) {
            @Override
            public void onLoadMore() {
                fetchUsers(userAdapter.getLastId(), false); // get users since the "id" of the last user in adapter.dataset

            }
        });
    }


    /**
     * opens the url in browser
     *
     * @param html_url link to user page
     */
    public void openUserPageInBrowser(String html_url) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(html_url));
        startActivity(browserIntent);

    }


    /**
     * dialog telling user that this user has no valid url
     */
    public void showUserPageUrlIsNotValid() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.user_page_url_not_valid));
        builder.setMessage(R.string.user_page_url_is_not_valid_cannot_redirect);

        builder.setNegativeButton(getResources().getString(R.string.ok), null);
        builder.show();

    }

    /**
     * dialog is shown if the application has no internet
     */
    private void showNoInternetDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.no_connection));
        builder.setMessage(R.string.this_application_needs_internet_connect_and_retry);


        builder.setPositiveButton(getResources().getString(R.string.ok),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showRetryButton();
            }
        });
        builder.show();

    }


    /**
     * text shown if after successful http request the users list is empty
     */
    private void showNoUsersText() {

        if (userListEmpty.getVisibility() == View.GONE) {
            userListEmpty.setVisibility(View.VISIBLE);
        }


    }

    /**
     * text hidden if after successful http request the users list is not empty
     */
    private void hideNoUsersText() {

        if (userListEmpty.getVisibility() == View.VISIBLE) {
            userListEmpty.setVisibility(View.GONE);
        }

    }


    /**
     * Dialog shown if the http cal returned an error
     * error message will be shown
     *
     * @param errorMessage String errorMessage
     */
    private void showErrorFetchingDataDialog(String errorMessage) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.error));


        builder.setMessage(getResources().getString(R.string.an_error_occurred_while_fetching_data) + "\n" +
                "\n" + getResources().getString(R.string.error_message) + "\n" +
                errorMessage + "\n"
        );

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showRetryButton();
            }
        });
        builder.show();

    }


    /**
     * shows the retry button if http call was not successful
     */
    private void showRetryButton() {

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchUsers(lastId, true);
            }
        });

        if (userAdapter.isEmpty() && retryButton.getVisibility() == View.GONE) {
            retryButton.setVisibility(View.VISIBLE);
        }
        hideNoUsersText();

    }

    /**
     * hides retry button
     */

    private void hideRetryButton() {

        if (retryButton.getVisibility() == View.VISIBLE) {
            retryButton.setVisibility(View.GONE);
        }
    }



    /**
     * Shows a dialog in order to enter username and password for authentication with the GitHubApi - entered username and password are stored in shared preferences
     * Dialog makes sure that both fields are not empty.
     *
     */
    private void showEnterAuthenticationDialog() {


        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.login);
        dialogBuilder.setCancelable(false);

        final TextView tv_username = new TextView(this);
        final EditText input_username = new EditText(this);
        final TextView tv_password = new TextView(this);
        final EditText input_password = new EditText(this);


        tv_username.setText(R.string.username);
        //input.setSingleLine();
        input_username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        String actualUsernameValue = Utils.getPrefs(this, Utils.PREFS_LOGIN_USERNAME_KEY, "");
        if (actualUsernameValue != null && !actualUsernameValue.equals("")) {
            input_username.setText(actualUsernameValue);
        }

        tv_password.setText(R.string.password);
        input_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        tv_username.setLayoutParams(lp);
        tv_username.setPadding(10, 10, 10, 10);

        input_username.setLayoutParams(lp);

        tv_password.setLayoutParams(lp);
        tv_password.setPadding(10, 40, 10, 10);

        input_password.setLayoutParams(lp);

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(llp);
        ll.setPadding(80, 60, 80, 60);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(tv_username);
        ll.addView(input_username);
        ll.addView(tv_password);
        ll.addView(input_password);

        dialogBuilder.setView(ll);

        dialogBuilder.setPositiveButton(R.string.retry, null);
        loginDataDialog = dialogBuilder.create();

        loginDataDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btn = loginDataDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                btn.setEnabled(true);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newUsernameValue = input_username.getText().toString();
                        String newPasswordValue = input_password.getText().toString();

                        if (newUsernameValue.trim().matches("") || newPasswordValue.trim().matches("")) {

                            if (newPasswordValue.trim().matches("")) {
                                input_password.setError(getResources().getString(R.string.enter_password));
                                input_password.requestFocus();
                                input_password.selectAll();
                            }
                            if (newUsernameValue.trim().matches("")) {

                                input_username.setError(getResources().getString(R.string.enter_username));
                                input_username.requestFocus();
                                input_username.selectAll();
                            }
                        } else {

                            saveUsername(newUsernameValue);
                            savePassword(newPasswordValue);
                            resetClientAndFetUsers(newUsernameValue, newPasswordValue);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });


        loginDataDialog.show();
        input_username.requestFocus();


    }

    /**
     * resets the client with the new values ..
     *
     * @param newUsernameValue String Username to be saved in Prefs
     * @param newPasswordValue String Password to be saved in Prefs
     */
    private void resetClientAndFetUsers(String newUsernameValue, String newPasswordValue) {
        client = ServiceGenerator.createService(GitHubClient.class, newUsernameValue, newPasswordValue);
        fetchUsers(0, true);
    }

    /**
     * saves username in shared prefs
     *
     * @param newUsernameValue String username
     */

    private void saveUsername(String newUsernameValue) {
        Utils.savePrefs(this, Utils.PREFS_LOGIN_USERNAME_KEY, newUsernameValue);
    }

    /**
     * saves password in shared prefs
     *
     * @param newPasswordValue String password
     */
    private void savePassword(String newPasswordValue) {
        Utils.savePrefs(this, Utils.PREFS_LOGIN_PASSWORD_KEY, newPasswordValue);
    }


}
