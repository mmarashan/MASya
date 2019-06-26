package model;

import com.google.gson.Gson;

import java.time.Instant;

public class MessageDTO {
    private MessageType type;
    private String textContent;
    private String sender;
    private String receiver;
    private String tag;

    private long timestampUTC = Instant.now().getEpochSecond();


    public enum MessageType {
        MESSAGE,
        SUBMIT,
        JOIN, // server output message to member with roomCode
        LEAVE,
        ERROR // server output message to member with error
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // only getter - sender can't set timestamp
    public long getTimestampUTC() {
        return timestampUTC;
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static MessageDTO fromJson(String jsonStr){
        Gson gson = new Gson();
        MessageDTO m = gson.fromJson(jsonStr, MessageDTO.class);
        return m;
    }

    @Override
    public String toString() {
        return "{" +
                "\"type\":\"" + type + '\"' +
                ", \"textContent\":\"" + textContent + '\"' +
                ", \"sender\":\"" + sender + '\"' +
                ", \"receiver\":\"" + receiver + '\"' +
                ", \"tag\":\"" + tag + '\"' +
                ", \"timestampUTC\":\"" + timestampUTC + '\"' +
                '}';
    }
}
