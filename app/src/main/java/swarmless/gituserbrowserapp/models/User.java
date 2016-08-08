package swarmless.gituserbrowserapp.models;

/**
 * Created by Firas-PC on 05.08.2016.
 *
 *
 */

/**
 * User Model
 */
public class User {

    int id;
    String login;
    String avatar_url;
    String html_url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getLogin() {
        return login;
    }


    public String getHtml_url() {
        return html_url;
    }


    public String getAvatar_url() {
        return avatar_url;
    }



    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", html_url='" + html_url + '\'' +
                '}';
    }


}
