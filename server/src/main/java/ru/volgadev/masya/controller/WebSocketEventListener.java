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
import ru.volgadev.masya.model.MessageDTO;

@Component
public class WebSocketEventListener {

    private final String USERNAME_HEADER = "username";
    private final String PASSWORD_HEADER = "password";

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private SessionHolder sessionHolder;

    @Autowired
    private MemberRegistryManager memberRegistryManager;

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
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            String username = (String) headerAccessor.getFirstNativeHeader(USERNAME_HEADER);
            String password = (String) headerAccessor.getFirstNativeHeader(PASSWORD_HEADER);
            if (memberRegistryManager.checkCredentials(username, password)){
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
        logger.info("Unsuccess authorisation. Close session "+sessionId);
        sessionHolder.closeSessionById(sessionId);

    }

    /*
    * handle connected
    * */
    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        String username = sessionHolder.getUserBySession(sessionId);
        logger.info("Success new web socket connection: " + sessionId + " " + username);
    }

    /*
    * send sessionId in JOIN message
    * */
    private void sendJoinMessageToMember(String memberCode, String roomCode){
        logger.info("Send JOIN to member "+ memberCode);
        MessageDTO chatMessage = new MessageDTO();
        chatMessage.setType(MessageDTO.MessageType.JOIN);
        chatMessage.setReceiver(memberCode);
        chatMessage.setContent(roomCode);
        // TODO: temporally roomCode is sessionId. fix it
        memberRegistryManager.addMemberRoom(memberCode, roomCode);
        messagingTemplate.convertAndSend("/message.new/"+memberCode, chatMessage);
        logger.info("Send JOIN to member "+ memberCode + " OK");
    }


    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getFirstNativeHeader(USERNAME_HEADER);
        String sessionId = headerAccessor.getSessionId();
        // TODO: check permission for subscription
        logger.info("User "+ username+" subscribe on "+event.getMessage().getHeaders().get("simpDestination"));

        // for new member send JOIN message with roomCode
        if (!memberRegistryManager.isMemberRegistred(username)) sendJoinMessageToMember(username, sessionId);
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
        }
    }
}
