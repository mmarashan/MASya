package ru.volgadev.masya.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import ru.volgadev.masya.data.DaoApi;
import ru.volgadev.masya.data.model.Message;

import java.util.ArrayList;

@Component
class MessageSender {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private DaoApi daoApi;

    /*
     * send sessionId in JOIN message
     * */
    void sendJoinMessageToMember(String username, String roomCode){
        logger.info("Send JOIN to member "+ username);
        Message chatMessage = new Message();
        chatMessage.setType(Message.MessageType.JOIN);
        chatMessage.setReceiver(username);
        chatMessage.setTextContent(roomCode);
        messagingTemplate.convertAndSend("/message.new/"+username, chatMessage);
        logger.info("Send JOIN to member "+ username + " OK");
    }

    /*
     * send old messages from buffer
     * */
    void sendBufferMemberMessages(String sessionId){
        String username = daoApi.getMemberBySession(sessionId);
        ArrayList<Message> bufferMessages = daoApi.getNewMessages(username);
        if (bufferMessages!=null){
            logger.info("Send buffer messages for "+ username + "; count="+bufferMessages.size());
            for (Message m: bufferMessages){
                messagingTemplate.convertAndSend("/message.new/"+sessionId);
            }
        }
    }


    void sendMessage(Message message){
        String receiverUserame = message.getReceiver();
        if (receiverUserame == null){
            return;
        }
        // if receiver online - send message
        // else save message for next session
        if (daoApi.isMemberOnline(receiverUserame)){
            String roomCode = daoApi.getMemberRoom(receiverUserame);
            logger.info("Send: " + message.toString()+" to "+roomCode);
            messagingTemplate.convertAndSend("/message.new/" + roomCode, message);
        } else {
            logger.info("Add to buffer: " + message.toString());
            daoApi.addNewMessageToMemberBuffer(receiverUserame, message);
        }
    }


    void sendError(String receiverCode, String errorText){
        Message message = new Message();
        message.setReceiver(receiverCode);
        message.setTextContent(errorText);
        message.setType(Message.MessageType.ERROR);
        sendMessage(message);
    }

}
