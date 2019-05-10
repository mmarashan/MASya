package ru.volgadev.masya.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import ru.volgadev.masya.model.MemberRegistryManager;
import ru.volgadev.masya.model.Message;

@org.springframework.stereotype.Controller
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    // redirect message
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Message message) {
        logger.debug("Redirect message: "+message.toString());

        // TEMPORALLY instead submit: return message to sender
        String senderRoomCode = MemberRegistryManager.getMemberRoom(message.getSender());
        messagingTemplate.convertAndSend("/chat.room/"+senderRoomCode, message);

        String roomCode = MemberRegistryManager.getMemberRoom(message.getReceiver());
        messagingTemplate.convertAndSend("/chat.room/"+roomCode, message);
    }



    @MessageMapping("/chat.addUser")
    public void addUser(@Payload Message message,
                               SimpMessageHeaderAccessor headerAccessor) {
        logger.debug("Register user session: "+message.toString());
        
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        messagingTemplate.convertAndSend("/chat.room/"+message.getSender(), message);
    }

}
