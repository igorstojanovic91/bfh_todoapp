package model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import model.helper.XmlHelper;

import javax.servlet.ServletContext;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * The User Manager is responsible for the registration and authentication of a user.
 *
 * @author Igor Stojanovic, Sabina Löffel, Christophe Leupi, Raphael Gerber
 * @version 1.0
 */
@JacksonXmlRootElement(localName = "root")
public class UserManager {

    private static final Set<User> users = new HashSet<>();

    private static UserManager instance;

    private UserManager() {
    }

    /**
     * Instantiates a UserManager as a singleton and sets the static counters
     * of the Todo and User classes to the highest values.
     *
     * @param servletContext the context of a ServletContext
     * @return an instance of UserManager
     */
    public static UserManager getInstance(ServletContext servletContext) {
        try {
            if (UserManager.instance == null) {
                UserManager.instance = loadData(servletContext);
                setCounters();
            }
            return UserManager.instance;
        } catch (Exception e) {
            UserManager.instance = new UserManager();
            return UserManager.instance;
        }
    }

    /**
     * Sets the static counters of the Todo and User classes to the highest values
     * When loading the Data.xml, the highest assigned todoID and userID are determined and stored in the static counters.
     * When adding new todos, the todoCounter is increased. This way it can be ensured that each todo and user
     * have unique ID's.
     */
    private static void setCounters() {
        Integer highestUserID = 0;
        Integer highestTodoID = 0;

        for (User user : UserManager.instance.getUsers()) {
            if (highestUserID < user.getUserID()) {
                highestUserID = user.getUserID();
            }
            for (Todo todo : user.getTodos()) {
                if (highestTodoID < todo.getTodoID()) {
                    highestTodoID = todo.getTodoID();
                }
            }
        }
        User.setUserCounter(++highestUserID);
        Todo.setTodoCounter(++highestTodoID);
    }

    /**
     * Registers a new user.
     *
     * @param userName the userName
     * @param password the password
     * @throws UserException if userName is already registered
     */
    public void register(String userName, String password) throws UserException {
        if (isNotRegistered(userName)) {
            User newUser = new User(userName, password);
            users.add(newUser);
        } else {
            throw new UserException("Username is not unique!");
        }
    }

    /**
     * Authenticates a user.
     *
     * @param userName the userName
     * @param password the password
     * @return a User object if authentication was successful
     * @throws UserException if the user does not exist or the passwords did not match
     */
    public User authenticate(String userName, String password) throws UserException {
        if (isNotRegistered(userName)) {
            throw new UserException("User does not exist!");
        } else {
            for (User thisUser : users) {
                if (thisUser.getUserName().equalsIgnoreCase(userName) && thisUser.getPassword().equals(password)) {
                    return thisUser;
                }
            }
            throw new UserException("Password does not match!");
        }
    }

    /**
     * Indicates whether a userName is already registered.
     *
     * @param userName the userName to check
     * @return true if userName is not yet registered, false otherwise
     */
    private boolean isNotRegistered(String userName) {
        for (User user : users) {
            if (user.getUserName().equalsIgnoreCase(userName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns all registered users.
     *
     * @return a set with all registered users
     */
    @JacksonXmlElementWrapper(localName = "users")
    @JacksonXmlProperty(localName = "user")
    public Set<User> getUsers() {
        return users;
    }

    /**
     * Returns the User associated with userID
     *
     * @param userID id of the user to return
     * @return a User object
     */
    public User getUser(int userID) {
        Optional<User> user = users.stream()
            .filter(u -> u.getUserID() == userID)
            .findFirst();
        return user.orElse(null);
    }

    /**
     * Loads a predefined set of users.
     */
    private static UserManager loadData(ServletContext servletContext) {
        return XmlHelper.readXmlData(servletContext);
    }

    public void writeData(ServletContext servletContext) {
        XmlHelper.writeXmlData(this, servletContext);
    }
}
