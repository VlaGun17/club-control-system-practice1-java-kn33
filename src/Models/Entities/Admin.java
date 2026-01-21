package Entities;

public class Admin extends BaseEntity {

    private final String login;
    private final String password;
    private final String email;

    public Admin(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    @Override
    public String toString() {
        return "Admin{" +
              "login='" + login + '\'' +
              ", password='" + password + '\'' +
              ", email='" + email + '\'' +
              '}';
    }
}
