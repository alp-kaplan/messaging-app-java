import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Message class represents a message object containing details about the sender, receiver, content, and timestamp.
 */
public class Message implements Serializable {
    private String sender;
    private String receiver;
    private String content;
    private Timestamp timestamp;

    /**
     * Constructs a new Message object.
     *
     * @param sender    The username of the sender.
     * @param receiver  The username of the receiver.
     * @param content   The content of the message.
     * @param timestamp The timestamp when the message was sent.
     */
    public Message(String sender, String receiver, String content, Timestamp timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Returns the username of the sender.
     *
     * @return The sender's username.
     */
    public String getSender() { return sender; }

    /**
     * Returns the username of the receiver.
     *
     * @return The receiver's username.
     */
    public String getReceiver() { return receiver; }

    /**
     * Returns the content of the message.
     *
     * @return The content of the message.
     */
    public String getContent() { return content; }

    /**
     * Returns the timestamp of the message.
     *
     * @return The timestamp of the message.
     */
    public Timestamp getTimestamp() { return timestamp; }

    /**
     * Returns a string representation of the Message object.
     *
     * @return A string in the format "sender:::receiver:::content:::timestamp".
     */
    @Override
    public String toString() {
        return sender + ":::" + receiver + ":::" + content + ":::" + timestamp;
    }

}
