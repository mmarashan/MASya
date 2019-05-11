package ru.volgadev.masya.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import ru.volgadev.masya.model.MemberRegistryManager;
import ru.volgadev.masya.model.MessageDTO;

@org.springframework.stereotype.Controller
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private SessionHolder sessionHolder;

    @Autowired
    private MemberRegistryManager memberRegistryManager;
    
    // redirect message
    @MessageMapping("/message.send")
    public void sendMessage(@Payload MessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Handle message: "+message.toString());

        String sessionId = headerAccessor.getSessionId();

        // TEMPORALLY instead submit: return message to sender
        // TODO: add submit
        String senderRoomCode = memberRegistryManager.getMemberRoom(message.getSender());
        logger.info("Send message to : "+senderRoomCode);
        messagingTemplate.convertAndSend("/message.new/"+senderRoomCode, message);

        String roomCode = memberRegistryManager.getMemberRoom(message.getReceiver());
        // String roomCode = message.getReceiver();
        messagingTemplate.convertAndSend("/message.new/"+roomCode, message);
    }




}
