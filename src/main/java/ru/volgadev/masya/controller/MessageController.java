package ru.volgadev.masya.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import ru.volgadev.masya.data.DaoApi;
import ru.volgadev.masya.data.dao.MemberRegistry;
import ru.volgadev.masya.data.model.Message;
import ru.volgadev.masya.data.dao.SessionHolder;

@Controller
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private DaoApi daoApi;

    @Autowired
    private MessageSender messageSender;
    
    // handle input message
    @MessageMapping("/message.send")
    public void handle(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        LOGGER.info("Handle message: "+message.toString());

        if (Message.MessageType.MESSAGE.equals(message.getType())) handeSend(message, headerAccessor);
        if (Message.MessageType.SUBMIT.equals(message.getType())) handleSubmit(message, headerAccessor);
        if (Message.MessageType.LEAVE.equals(message.getType())) handleLeave(message, headerAccessor);

    }

    private void handeSend(@Payload Message message, SimpMessageHeaderAccessor headerAccessor){

        String sessionId = headerAccessor.getSessionId();

        // validate field
        if (message.getSender()==null) {
            LOGGER.info("Bad message. Sender is empty: "+message.toString());
            return;
        }
        if (message.getReceiver()==null) {
            messageSender.sendError(message.getSender(), "Receiver is empty!");
            return;
        }

        // TODO: save message to db as cost option :)
        messageSender.sendMessage(message);
    }

    // receiver send submit message to sender
    // TODO: save unsubmited messages and repeat after
    // NOW: redirect it
    private void handleSubmit(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        LOGGER.info("Handle submit message: " + message.toString());
        if (message.getTag()==null){
            messageSender.sendError(message.getSender(), "Message TAG is empty!");
            return;
        }
        messageSender.sendMessage(message);
    }

    // close session and set offline
    private void handleLeave(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        daoApi.closeSession(headerAccessor.getSessionId());
        LOGGER.info("Session closed: " + sessionId);

    }

}
