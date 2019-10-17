package ru.volgadev.masya.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;
import ru.volgadev.masya.data.DaoApi;
import ru.volgadev.masya.data.dao.MemberRegistry;
import ru.volgadev.masya.data.dao.SessionHolder;

@Component
public class WebSocketEventController {

    private final String USERNAME_HEADER = "username";
    private final String PASSWORD_HEADER = "password";

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private DaoApi daoApi;

    @Autowired
    private MessageSender messageSender;

    /*
        handle connect - check credentials
        if OK - register session
        esle - close session
    */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        logger.info("Handle connect");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("sessionId = "+sessionId);
        logger.info("headers: "+ headerAccessor.getMessageHeaders().toString());
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            String username = headerAccessor.getFirstNativeHeader(USERNAME_HEADER);
            String password = headerAccessor.getFirstNativeHeader(PASSWORD_HEADER);
            if ((username!=null)&&(password!=null)&&(daoApi.checkCredentials(username, password))){
                logger.info("Success authorisation for "+username+"; sessionId = "+sessionId);
                // create room, registerSession and subscribe
                daoApi.registerMemberSession(sessionId, username);

                return;
            } else {
                logger.info("Unsuccess authorisation for "+username+". Close session "+sessionId);
                daoApi.closeSession(sessionId);
                return;
            }
        }
        logger.info("Unsuccess authorisation. Close session "+sessionId);
        daoApi.closeSession(sessionId);

    }

    /*
    * handle connected
    * */
    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        String username = daoApi.getMemberBySession(sessionId);
        logger.info("Success new web socket connection: " + sessionId + " " + username);
    }


    /*
    * check if online user and know his room
    * */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = daoApi.getMemberBySession(sessionId);

        if (username==null){
            return;
        }

        if (!event.getMessage().getHeaders().containsKey("simpDestination")){
            logger.debug("Error message for subscribing! Empty simpDestination");
        }

        boolean joinToPrivateRoom = event.getMessage().getHeaders().get("simpDestination").toString().endsWith(sessionId);

        // TODO: check permission for subscription
        logger.info("Member "+ username+" subscribe on "+event.getMessage().getHeaders().get("simpDestination"));


        // если пользователь подключился к приватной комнате, отправляем ему сообщение из буффера
        // иначе отправляем сообщение с данными о приватной комнате (временно не проверяем, онлайн пользователь или нет
        // TODO: temporally roomCode is sessionId. fix it
        if (joinToPrivateRoom){
            logger.info("Member joined to private room");
            messageSender.sendBufferMemberMessages(sessionId);
            daoApi.authMember(username);
        } else {
            if (!daoApi.isMemberOnline(username)) {
                messageSender.sendJoinMessageToMember(username, sessionId);
                daoApi.addMemberRoom(username, sessionId);
            }
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getFirstNativeHeader(USERNAME_HEADER);
        // String sessionId = headerAccessor.getSessionId();
        logger.info("Member "+ username+" unsubscribe from "+event.getMessage().getHeaders().get("simpDestination"));
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = daoApi.getMemberBySession(sessionId);
        logger.info("Member Disconnected : " + username);

        if(sessionId != null) daoApi.closeSession(sessionId);
        logger.info("Session closed: " +username + " " + sessionId);
    }
}
