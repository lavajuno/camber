package org.jmeifert.camber.data;

import org.jmeifert.camber.security.SHAutil;
import org.jmeifert.camber.util.ChatMap;
import org.jmeifert.camber.util.Format;
import java.util.Vector;

/**
 * Group is initialized with a name and password (which it then hashes) and contains a cache of recent messages.
 * It provides functionality related to adding and getting messages, as well as verifying user credentials.
 */
public class Group {
    private final int GROUP_MESSAGE_CACHE_SIZE = ChatMap.GROUP_MESSAGE_CACHE_SIZE;
    private final int MAX_MESSAGES_TO_OUTPUT = ChatMap.MAX_MESSAGES_TO_OUTPUT;
    private String hashedGroupPassword;
    private String groupName;
    private Vector<Message> messages = new Vector<>();

    /**
     * Creates a group with the given name and password.
     * @param groupName The group's name
     * @param groupPassword The group's password
     * @throws IllegalArgumentException Throws an IllegalArgumentException if the group parameters are invalid
     */
    public Group(String groupName, String groupPassword) throws IllegalArgumentException {
        if (!Format.isValidName(groupName) || !Format.isValidName(groupPassword)) {
            throw new IllegalArgumentException("Invalid group parameter(s).");
        }
        this.groupName = groupName;
        this.hashedGroupPassword = SHAutil.getHash(groupPassword);
    }

    /**
     * Adds a message to the group's message cache.
     * @param message Message to add
     */
    public synchronized void addMessage(Message message) {
        messages.add(message);
        if (messages.size() > GROUP_MESSAGE_CACHE_SIZE) {
            messages.remove(0);
        }
    }

    /**
     * Returns all the recent messages tagged with a specific topic.
     * @param topic Topic to return recent messages from
     * @return Recent messages tagged with specified topic
     */
    public String getMessages(String topic) {
        StringBuilder output = new StringBuilder();
        for (Message i : messages) {
            if (i.getTopic().equals(topic)) {
                output.append(i).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * @return All recent messages from the group
     */
    public String getMessages() {
        StringBuilder output = new StringBuilder();
        int outputCount = 0;
        for (Message i : messages) {
            output.append(i).append("\n");
            outputCount++;
            if(outputCount > MAX_MESSAGES_TO_OUTPUT) {
                return output.toString();
            }
        }
        return output.toString();
    }

    /**
     * @return The group's name
     */
    public String getName() {
        return groupName;
    }

    /**
     * Checks if a given password hash is valid.
     * @param hashedPassword Password hash to check
     * @return Returns true if password hashes match
     */
    public Boolean verifyPassword(String hashedPassword) {
        if (hashedGroupPassword.equals("")) {
            return true;
        }
        return hashedGroupPassword.equals(hashedPassword);
    }

    /**
     * @return Active topics in the Group
     */
    public String getTopics() {
        StringBuilder output = new StringBuilder();
        Vector<String> topics = new Vector<>();
        for (Message i : messages) {
            if (!topics.contains(i.getTopic())) {
                topics.add(i.getTopic());
            }
        }
        for(String i : topics) {
            output.append("'").append(i).append("'").append("\n");
        }
        return output.toString();
    }
}
