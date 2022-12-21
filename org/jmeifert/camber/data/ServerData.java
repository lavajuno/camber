package org.jmeifert.camber.data;

import org.jmeifert.camber.util.Format;

import java.util.Vector;

/**
 * Server passes an instance of ServerData to each ServerThread upon its instantiation.
 * ServerData contains Groups that ServerThreads can synchronously read from and write to.
 */
public class ServerData {
    private Vector<Group> groups;

    /**
     * Instantiates a ServerData.
     */
    public ServerData() {
        groups = new Vector<>();
    }

    /**
     * Creates a group.
     * @param name Group name
     * @param password Group password
     * @throws IllegalArgumentException If the name is invalid or already exists.
     */
    public synchronized void createGroup(String name, String password) throws IllegalArgumentException {
        if (!Format.isSafeAscii(name) || !Format.isSafeAscii(password)) {
            throw new IllegalArgumentException();
        }
        for (Group i : groups) {
            if (i.getName().equals(name)) {
                throw new IllegalArgumentException("Group already exists!");
            }
        }
        groups.add(new Group(name, password));
    }

    /**
     * Returns the group with the given name.
     * @param name Name of the group to return
     * @return The group with the given name
     */
    public Group getGroup(String name) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getName().equals(name)) {
                return groups.get(i);
            }
        }
        return null;
    }

}
