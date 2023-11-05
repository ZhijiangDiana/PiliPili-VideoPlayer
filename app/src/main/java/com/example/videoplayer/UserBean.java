package com.example.videoplayer;

public class UserBean {
    String id;
    String userName;
    String userEmail;
    String password;
    String userNickName;
    int membership;
    String favorite;

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
