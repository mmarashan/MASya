package ru.volgadev.masya.model;

import java.util.List;

public class Member {

    // public user login
    private String userCode;

    // private system user room
    // nobody can't send message to user directly
    private String roomCode;

    // channels created by member, where only he can write
    private List<String> publishChannels;

    // subscribtion of this member - rooms and channels
    private List<String> subscribtions;
}
