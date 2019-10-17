package ru.volgadev.masya.data.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHolder.class);

    // TOD0: key sessionId is too small, it can create collision. fix it!

    // key - sessionId, value - usercode
    // for check user connect status
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    // key - sessionId, value - session (WS session create before username define
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

//    public WebSocketSession getSession(String sessionId){
//        LOGGER.debug("Return session sessionId = "+sessionId);
//        if (sessionMap.containsKey(sessionId)) return sessionMap.get(sessionId);
//        else return null;
//    }

    public void registerSession(WebSocketSession session) {
        LOGGER.info("Register new session: "+session.getId());
        sessionMap.put(session.getId(), session);
    }

    public void closeSession(WebSocketSession session){
        closeSessionById(session.getId());
    }

    public void closeSessionById(String sessionId){
        if (sessionMap.containsKey(sessionId)) {
            try {
                sessionMap.get(sessionId).close();
            } catch (IOException e) {
                LOGGER.error("Error session close: "+e.toString());
            }
            sessionMap.remove(sessionId);
            sessionUserMap.remove(sessionId);
        }
    }

    public String getMemberBySession(String sessionId){
        if (sessionUserMap.containsKey(sessionId)) return sessionUserMap.get(sessionId);
        return null;
    }


    public void registerMemberSession(String sessionId, String usercode){
        if (sessionMap.containsKey(sessionId)) sessionUserMap.put(sessionId, usercode);
    }

}