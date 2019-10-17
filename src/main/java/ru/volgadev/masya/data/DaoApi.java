package ru.volgadev.masya.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.volgadev.masya.data.dao.MemberRegistry;
import ru.volgadev.masya.data.dao.SessionHolder;
import ru.volgadev.masya.data.model.Message;

import java.util.ArrayList;

/**
 * Оборачивает логику работы с данными по пользователям и сессиям
 * обеспечивает связанность данных в SessionHolder и MemberRegistry
 * @author mmarashan
 */
@Component
public class DaoApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DaoApi.class);

    @Autowired
    private SessionHolder sessionHolder;

    @Autowired
    private MemberRegistry memberRegistry;

    /**
     * Сначала регистрируется фактическая сессия
     * @param session
     */
    public void registerSession(WebSocketSession session) {
        sessionHolder.registerSession(session);
    }

    /**
     * Потом пользователь авторизуется и сохраняется связка пользователь-сессия
     * @param sessionId
     * @param usercode
     */
    public void registerMemberSession(String sessionId, String usercode){
        sessionHolder.registerMemberSession(sessionId, usercode);
        memberRegistry.setMemberOnline(usercode, true);
    }

    public boolean checkCredentials(String username, String password){
        return memberRegistry.checkCredentials(username, password);
    }

    /**
     * Пометить пользователя как авторизованного
     * @param usercode
     */
    public void authMember(String usercode){
        memberRegistry.setMemberOnline(usercode, true);
    }

    public String getMemberBySession(String sessionId){
        return sessionHolder.getMemberBySession(sessionId);
    }

    public String getMemberRoom(String username){
        return memberRegistry.getMemberRoom(username);
    }

    public boolean isMemberOnline(String username){
        return memberRegistry.isMemberOnline(username);
    }

    /**
     * Пользователю выдается ключ комнаты, на которую он подписывается чтобы принимать входящие сообщения
     * @param username
     * @param roomCode
     */
    public void addMemberRoom(String username, String roomCode){
        memberRegistry.addMemberRoom(username, roomCode);
    }

    public void closeSession(WebSocketSession session){
        closeSession(session.getId());
    }

    public void closeSession(String sessionId){
        sessionHolder.closeSessionById(sessionId);
        String usercode = getMemberBySession(sessionId);
        if (usercode!=null) memberRegistry.setMemberOnline(getMemberBySession(sessionId), false);
    }


    // TODO: хранение буфера сообщений в отдельный DAO (или в MessageSender)
    /**
     * Добавляет в буфер сообщения пользователю в ожидание
     * @param username
     * @param m
     */
    public void addNewMessageToMemberBuffer(String username, Message m){
        memberRegistry.addNewMessageToMemberBuffer(username, m);
    }

    public ArrayList<Message> getNewMessages(String username){
        return memberRegistry.getNewMessages(username);
    }
}
