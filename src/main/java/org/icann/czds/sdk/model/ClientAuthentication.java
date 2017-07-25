package org.icann.czds.sdk.model;

import java.io.Serializable;

public class ClientAuthentication implements Serializable{

    private String userName;

    private String password;

    public ClientAuthentication(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "{userName:'" + userName + '\'' +
                ", password:'" + password + '\'' +
                '}';
    }
}
