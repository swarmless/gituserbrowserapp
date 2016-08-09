package swarmless.gituserbrowserapp;

import android.app.Application;

/**
 * Created by Firas-PC on 05.08.2016.
 *
 *
 */
public class App extends Application {

    public static final class Const {

        //URL for the GitHub Api
        public static final String API_BASE_URL = "https://api.github.com/";

        // Error codes
        public static final int RESPONSE_CODE_UNAUTHORIZED = 401; // Unauthorized access
    }
}
