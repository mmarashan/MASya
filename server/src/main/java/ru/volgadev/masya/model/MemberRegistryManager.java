package ru.volgadev.masya.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
// TODO: database member register (or serialisation)
public class MemberRegistryManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberRegistryManager.class);

    // key - username, value - roomCode
    private final Map<String, Member> memberMap = new ConcurrentHashMap<>();

    public void addMember(String username){
        LOGGER.info("Add member: " + username);
        memberMap.put(username, new Member(username));
    }

    public boolean isMemberRegistred(String username){
        return memberMap.containsKey(username);
    }

    public boolean checkCredentials(String username, String password){
        // TODO: real check credentials
        if (username.equals(password)) return true;
        return false;
    }

    public void addMemberRoom(String username, String roomCode){
        if (!memberMap.containsKey(username)) addMember(username);
        memberMap.get(username).setRoomCode(roomCode);
    }

    /*
    * return roomCode
    * */
    public String getMemberRoom(String username){
        if (memberMap.containsKey(username)) return memberMap.get(username).getRoomCode();
        return null;
    }

    public boolean isMemberOnline(String username){
        if (!memberMap.containsKey(username)) return false;
        return memberMap.get(username).isOnline();
    }

    public void setMemberOnline(String username, boolean online){
        if (!memberMap.containsKey(username)) addMember(username);
        memberMap.get(username).setOnline(online);
    }


    public void addNewMessageToMemberBuffer(String username, MessageDTO m){
        if (!memberMap.containsKey(username)) addMember(username);
        memberMap.get(username).addNewMessageToBuffer(m);
    }

    public ArrayList<MessageDTO> getNewMessages(String username){
        if (memberMap.containsKey(username)) return memberMap.get(username).getNewMessages();
        return null;
    }

}
