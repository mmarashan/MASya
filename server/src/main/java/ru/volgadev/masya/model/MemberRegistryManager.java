package ru.volgadev.masya.model;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemberRegistryManager {

    // key - memberCode, value - roomCode
    private final Map<String, String> memberRoom = new ConcurrentHashMap<>();

    public String addUser(){
        return "";
    }

    public boolean checkCredentials(String username, String password){
        // TODO: real check credentials
        if (username.equals(password)) return true;
        return false;
    }

    public void addMemberRoom(String memberCode, String roomCode){
        memberRoom.put(memberCode, roomCode);
    }

    // TODO: database registerSession with user credentials, generate session roomCode
    /*
    * return roomCode
    * */
    public String getMemberRoom(String memberCode){
        if (memberRoom.containsKey(memberCode)) return memberRoom.get(memberCode);
        return null;
    }

    public boolean isMemberRegistred(String memberCode){
        return memberRoom.containsKey(memberCode);
    }

}
