package ru.volgadev.masya.model;

public class MemberRegistryManager {

    public static String addUser(){
        return "";
    }

    public static boolean checkCredentials(String username, String password){
        // TODO: add check credentials
        if (username.equals(password)) return true;
        return false;
    }

    // TODO: database registerSession with user credentials, generate session roomCode
    /*
    * return roomCode
    * */
    public static String getMemberRoom(String userCode){
        return userCode;
    }

}
