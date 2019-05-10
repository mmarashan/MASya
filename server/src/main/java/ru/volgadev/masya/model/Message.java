package ru.volgadev.masya.model;

public class Message {
    private MessageType type;
    private String content;
    private ContentType contentType;
    private String sender;
    private String receiver;
    private String tag;

    public enum ContentType {
        TEXT
    }

    public enum MessageType {
        MESSAGE,
        SUBMIT,
        JOIN,
        LEAVE
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", contentType=" + contentType +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }
}
