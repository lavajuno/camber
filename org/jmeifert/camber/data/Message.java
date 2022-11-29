package org.jmeifert.camber.data;

import org.jmeifert.camber.util.Format;
import java.util.Date;

/**
 * Message represents a message sent by a user. It includes information about the message's contents,
 * what group it was sent in, its topic, and when it was sent.
 */
public class Message {
    private String body;
    private Date date;
    private String user;
    private String topic;

    /**
     * Instantiates a Message.
     * @param topic The topic the message is in
     * @param user The user this message was sent by
     * @param body The body of this message
     */
    public Message(String topic, String user, String body) {
        if (!Format.isValidName(topic) || !Format.isValidName(user) || !Format.isValidMessage(body)) {
            throw new IllegalArgumentException("Invalid message parameter(s).");
        }
        this.topic = topic;
        this.user = user;
        this.body = body;
        this.date = new Date();
    }

    /**
     * @return The contents of this message.
     */
    public String getBody() {
        return body;
    }

    /**
     * @return date The date this message was sent.
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return user The user this message was sent by.
     */
    public String getUser() { return user; }

    /**
     * @return channel The topic this message is tagged with.
     */
    public String getTopic() { return topic; }

    @Override
    public String toString() {
        return "{" + topic + "} (" + date.toString() + ") [" + user + "]: " + body;
    }
}
