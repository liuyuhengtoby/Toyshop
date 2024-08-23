package mypkg;

public class User {

    private int id;
    private String username;
    private String phonenumber;
    private String email;
    private String address;

    // Constructor
    public User(int id, String username, String phonenumber, String email, String address) {
        this.id = id;
        this.username = username;
        this.phonenumber = phonenumber;
        this.email = email;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
