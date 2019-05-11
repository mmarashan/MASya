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
import ru.volgadev.masya.model.MemberRegistryManager;
import ru.volgadev.masya.model.Message;

@Component
public class WebSocketEventListener {

    private final String USERNAME_HEADER = "username";
    private final String PASSWORD_HEADER = "password";

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private SessionHolder sessionHolder;

    /*
        handle connect - check credentials
    */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        logger.info("Handle connect");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("sessionId = "+sessionId);
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            String username = (String) headerAccessor.getFirstNativeHeader(USERNAME_HEADER);
            String password = (String) headerAccessor.getFirstNativeHeader(PASSWORD_HEADER);
            if (MemberRegistryManager.checkCredentials(username, password)){
                logger.info("Success authorisation for "+username+"; sessionId = "+sessionId);
                // create room, registerSession and subscribe
                sessionHolder.registerUserSession(sessionId, username);
                return;
            } else {
                logger.info("Unsuccess authorisation for "+username+". Close session "+sessionId);
                sessionHolder.closeSessionById(sessionId);
                return;
            }
        }
        logger.info("Success authorisation. Close session "+sessionId);
        sessionHolder.closeSessionById(sessionId);

    }

    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        logger.info("Success a new web socket connection: " + sessionId);
    }


    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getFirstNativeHeader(USERNAME_HEADER);
        String sessionId = headerAccessor.getSessionId();
        logger.info("User "+ username+" subscribe on ...");
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("Unsubscribe listener");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = sessionHolder.getUserBySession(sessionId);
        logger.info("User Disconnected : " + username);

        if(sessionId != null) {
            sessionHolder.closeSessionById(sessionId);
            logger.info("Session closed: " + sessionId);

            // TODO: remove in only delete session
            Message chatMessage = new Message();
            chatMessage.setType(Message.MessageType.LEAVE);
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend("/chat.room/"+username, chatMessage);
        }
    }
}
