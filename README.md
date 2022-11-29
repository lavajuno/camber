# camber
Simple, encrypted ad-hoc group messaging.

## about
Camber fits the needs of a group that needs to spin up a temporary password-protected chat room with no external
services or hardware. It doesn't need an internet connection or any dependencies, and is cross-platform. Its source
code is small, organized, and documented, and its user interface is easy to use.

## usage
Camber requires a JRE that supports Java 11 (default-jre works on most Debian-based distros). To run it:
 - Client: "java -jar camber.jar [port]"
 - Server: "java -jar camberserver.jar [hostname] [port]"

Camber's default port is 7450, and the default hostname is 127.0.0.1 (localhost).

## security notice
Camber is a work-in-progress. Currently, communication between server and client is encrypted, but the server's 
identity is not persistent and thus cannot be fully verified. THIS WILL BE FIXED IN THE NEAR FUTURE.
#### Does this mean I'm vulnerable to man-in-the-middle attacks if I use Camber right now?
>Possibly. However, for one to work, someone on your local network has to dislike you a LOT (enough to go research some 
>random college project that you found and write a man-in-the-middle attack against it). If this sounds like you, you 
>may want to open an honest conversation with them about why this is the case, or wait until it is updated.

## roadmap
At this point, it's a little early to start pinning down exact functionality, however I will list some nice-to-haves
that I hope to implement in the future. This list is sorted by priority.
 - Server identity verification (WIP) - Saving and verification of server RSA public keys by the client
 - Persistent configuration (WIP) - Saving of hostnames, ports, and group logins by client. 
Saving of groups and port by server.
 - Reworked message buffers - Per-topic instead of per-group message buffers
 - Server commands - Create and destroy groups on the server console
 - JavaFX GUI application - A more polished user interface
 - Server-controlled user accounts & permissions - Access to groups controlled by user permissions
 - Ability to send media - Images, audio, etc. with a reasonable size limit

## licensing
Camber is (and always will be) free and open-source software. It is released under the GNU General Public License v3.
