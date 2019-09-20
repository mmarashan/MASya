package ru.volgadev.masya.model;

import java.util.ArrayList;
import java.util.List;

public class Member {

    // public member login
    private String username;

    // private system user room
    // nobody can't send message to user directly
    private String roomCode;

    private ArrayList<Message> messageBuffer;

    // channels created by member, where only he can write
    private List<String> publishChannels;

    // subscribtion of this member - rooms and channels
    private List<String> subscribtions;

    private boolean isOnline = false;

    public Member(String username) {
        this.username = username;
        this.messageBuffer = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void addNewMessageToBuffer(Message m){
        messageBuffer.add(m);
    }

    public ArrayList<Message> getNewMessages(){
        if (messageBuffer.size() == 0) return null;
        ArrayList<Message> listClone = new ArrayList<>(messageBuffer);
        messageBuffer.clear();
        return listClone;
    }
}
