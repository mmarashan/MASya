package ru.volgadev.masya.data.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.volgadev.masya.data.model.Member;
import ru.volgadev.masya.data.model.Message;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
// TODO: database member register (or serialisation)
public class MemberRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberRegistry.class);

    // key - username, value - roomCode
    private final Map<String, Member> memberMap = new ConcurrentHashMap<>();

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


    public void addNewMessageToMemberBuffer(String username, Message m){
        if (!memberMap.containsKey(username)) addMember(username);
        memberMap.get(username).addNewMessageToBuffer(m);
    }

    public ArrayList<Message> getNewMessages(String username){
        if (memberMap.containsKey(username)) return memberMap.get(username).getNewMessages();
        return null;
    }

    private void addMember(String username){
        LOGGER.info("Add member: " + username);
        memberMap.put(username, new Member(username));
    }

}
