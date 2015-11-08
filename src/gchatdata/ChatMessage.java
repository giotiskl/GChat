package gchatdata;

import java.io.Serializable;

/**
 * This class represents messages sent between client
 * and server in the chat application. A ChatMessage
 * object consists of two parts, the username and the
 * actual message
 *
 * It implements Serializable interface as such
 * objects constantly travel through streams.
 */
public class ChatMessage implements Serializable
{
    private static final long serialVersionUID = 857661769746528913L;

    private String username;
    private String message;

    /**
     * Public constructor of ChatMessage object
     *
     * @param username is the username of the client
     * @param message is the actual message to be sent over
     */
    public ChatMessage(String username, String message)
    {
        this.username = username;
        this.message = message;
    }

    //Getter-Setter methods for
    // username and message fields
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * Overridden toString variation
     *
     * @return username + message concatenated as one string if username field has a value
     *         otherwise return simple the message
     */
    @Override
    public String toString()
    {
        return username.length() > 0 ? (username + ": " + message) : (message);
    }
}
