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
import ru.volgadev.masya.state.MemberRegistry;
import ru.volgadev.masya.model.MessageDTO;
import ru.volgadev.masya.state.SessionHolder;

import java.util.ArrayList;

@Component
public class WebSocketEventController {

    private final String USERNAME_HEADER = "username";
    private final String PASSWORD_HEADER = "password";

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private SessionHolder sessionHolder;

    @Autowired
    private MemberRegistry memberRegistry;

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
            String username = (String) headerAccessor.getFirstNativeHeader(USERNAME_HEADER);
            String password = (String) headerAccessor.getFirstNativeHeader(PASSWORD_HEADER);
            if ((username!=null)&&(password!=null)&&(memberRegistry.checkCredentials(username, password))){
                logger.info("Success authorisation for "+username+"; sessionId = "+sessionId);
                // create room, registerSession and subscribe
                sessionHolder.registerMemberSession(sessionId, username);

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
        String username = sessionHolder.getMemberBySession(sessionId);
        logger.info("Success new web socket connection: " + sessionId + " " + username);
    }

    /*
    * send sessionId in JOIN message
    * */
    private void sendJoinMessageToMember(String username, String roomCode){
        logger.info("Send JOIN to member "+ username);
        MessageDTO chatMessage = new MessageDTO();
        chatMessage.setType(MessageDTO.MessageType.JOIN);
        chatMessage.setReceiver(username);
        chatMessage.setTextContent(roomCode);
        // TODO: temporally roomCode is sessionId. fix it
        memberRegistry.addMemberRoom(username, roomCode);
        messagingTemplate.convertAndSend("/message.new/"+username, chatMessage);
        logger.info("Send JOIN to member "+ username + " OK");
    }

    /*
     * send old messages from buffer
     * */
    private void sendBufferMemberMessages(String username){
        ArrayList<MessageDTO> bufferMessages = memberRegistry.getNewMessages(username);
        if (bufferMessages!=null){
            logger.info("Send buffer messages for "+ username + "; count="+bufferMessages.size());
            for (MessageDTO m: bufferMessages){
                messagingTemplate.convertAndSend("/message.new/"+username, m);
            }
        }
    }


    /*
    * check if online user and know his room
    * */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getFirstNativeHeader(USERNAME_HEADER);
        String sessionId = headerAccessor.getSessionId();

        if (username==null){
            return;
        }

        // TODO: check permission for subscription
        logger.info("Member "+ username+" subscribe on "+event.getMessage().getHeaders().get("simpDestination"));

        // for new member send JOIN message with roomCode and old messages from buffer
        if (!memberRegistry.isMemberOnline(username)) {
            sendBufferMemberMessages(username);
            sendJoinMessageToMember(username, sessionId);
            memberRegistry.setMemberOnline(username, true);
        };
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
        String username = sessionHolder.getMemberBySession(sessionId);
        logger.info("Member Disconnected : " + username);

        if(sessionId != null) {
            sessionHolder.closeSessionById(sessionId);
            if (username!=null) memberRegistry.setMemberOnline(username, false);
            logger.info("Session closed: " + sessionId);
        }
    }
}
