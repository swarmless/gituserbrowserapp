package swarmless.gituserbrowserapp.interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import swarmless.gituserbrowserapp.models.User;
/**
 * Created by Firas-PC on 06.08.2016.
 *
 *
 */

/**
 * GitHub client + getUsers call
 */

public interface GitHubClient {
    @GET("users?")
    Call<List<User>> users(
           @Query("since") int since

    );

}