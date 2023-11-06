package com.example.videoplayer;

public class UserBean {
    private String id;
    private String userName;
    private String userEmail;
    private String password;
    private String userNickName;
    private int membership;
    private String favorite;

    public UserBean(String id, String userName, String userEmail,
                    String password, String userNickName, int membership, String favorite) {
        this.id = id;
        this.userName = userName;
        this.userEmail = userEmail;
        this.password = password;
        this.userNickName = userNickName;
        this.membership = membership;
        this.favorite = favorite;
    }

    public UserBean() {}

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getPassword() {
        return password;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public int getMembership() {
        return membership;
    }

    public String getFavorite() {
        return favorite;
    }
}
