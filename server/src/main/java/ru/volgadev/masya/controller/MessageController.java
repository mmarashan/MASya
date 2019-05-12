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

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private SessionHolder sessionHolder;

    @Autowired
    private MemberRegistryManager memberRegistryManager;
    
    // redirect message
    @MessageMapping("/message.send")
    public void sendMessage(@Payload MessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        LOGGER.info("Handle message: "+message.toString());

        String sessionId = headerAccessor.getSessionId();

        // TEMPORALLY instead submit: return message to sender
        // TODO: return submit 3
        // TODO: set timestamp 2
        String senderRoomCode = memberRegistryManager.getMemberRoom(message.getSender());
        LOGGER.info("Send message to : "+senderRoomCode);
        messagingTemplate.convertAndSend("/message.new/"+senderRoomCode, message);

        String receiverUserame = message.getReceiver();
        // if receiver online - send message
        // else save message for next session
        if (memberRegistryManager.isMemberOnline(receiverUserame)){
            String roomCode = memberRegistryManager.getMemberRoom(receiverUserame);
            messagingTemplate.convertAndSend("/message.new/" + roomCode, message);
        } else {
            memberRegistryManager.addNewMessageToMemberBuffer(receiverUserame, message);
        }
    }




}
