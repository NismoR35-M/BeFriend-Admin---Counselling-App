package com.group.projectAdmin.notification;

@SuppressWarnings("unused")
public class Token {
    String token;
    public Token(String token){
        this.token = token;
    }
    public Token(){

    }
    public String getToken(){return token;}
    public void setToken(String token){this.token = token;}
}
