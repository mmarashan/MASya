package ru.volgadev.masya.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import ru.volgadev.masya.state.MemberRegistry;
import ru.volgadev.masya.model.MessageDTO;
import ru.volgadev.masya.state.SessionHolder;

@org.springframework.stereotype.Controller
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private SessionHolder sessionHolder;

    @Autowired
    private MemberRegistry memberRegistry;
    
    // handle input message
    @MessageMapping("/message.send")
    public void handle(@Payload MessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        LOGGER.info("Handle message: "+message.toString());

        if (MessageDTO.MessageType.MESSAGE.equals(message.getType())) handeSend(message, headerAccessor);
        if (MessageDTO.MessageType.SUBMIT.equals(message.getType())) handleSubmit(message, headerAccessor);
        if (MessageDTO.MessageType.LEAVE.equals(message.getType())) handleLeave(message, headerAccessor);

    }

    void handeSend(@Payload MessageDTO message, SimpMessageHeaderAccessor headerAccessor){

        String sessionId = headerAccessor.getSessionId();

        // validate field
        if (message.getSender()==null) {
            LOGGER.info("Bad message. Sender is empty: "+message.toString());
            return;
        }
        if (message.getReceiver()==null) {
            sendError(message.getSender(), "Receiver is empty!");
            return;
        }

        // TODO: save message to db as cost option :)
        sendMessage(message);
    }

    // receiver send submit message to sender
    // TODO: save unsubmited messages and repeat after
    // NOW: redirect it
    void handleSubmit(@Payload MessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        LOGGER.info("Handle submit message: " + message.toString());
        if (message.getTag()==null){
            sendError(message.getSender(), "Message TAG is empty!");
            return;
        }
        sendMessage(message);
    }

    // close session and set offline
    void handleLeave(@Payload MessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        LOGGER.info("Handle message: " + message.toString());

        String sessionId = headerAccessor.getSessionId();
        sessionHolder.closeSessionById(sessionId);
        memberRegistry.setMemberOnline(message.getSender(), false);
        LOGGER.info("Session closed: " + sessionId);

    }


    void sendMessage(MessageDTO message){
        String receiverUserame = message.getReceiver();
        if (receiverUserame == null){
            return;
        }
        // if receiver online - send message
        // else save message for next session
        if (memberRegistry.isMemberOnline(receiverUserame)){
            String roomCode = memberRegistry.getMemberRoom(receiverUserame);
            LOGGER.info("Send: " + message.toString());
            messagingTemplate.convertAndSend("/message.new/" + roomCode, message);
        } else {
            LOGGER.info("Add to buffer: " + message.toString());
            memberRegistry.addNewMessageToMemberBuffer(receiverUserame, message);
        }
    }


    void sendError(String receiverCode, String errorText){
        MessageDTO message = new MessageDTO();
        message.setReceiver(receiverCode);
        message.setTextContent(errorText);
        message.setType(MessageDTO.MessageType.ERROR);
        sendMessage(message);
    }


}
