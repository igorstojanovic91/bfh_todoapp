package controller.rest;

import model.UserException;
import model.UserManager;
import controller.rest.helper.JsonHelper;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Register a user via REST API.
 * Listens to "/api/users" path.
 *
 * @author Igor Stojanovic, Sabina Löffel, Christophe Leupi, Raphael Gerber
 * @version 1.0
 */
@WebServlet("/api/users")
public class UsersRestServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UsersRestServlet.class.getName());

    /**
     * Register a user.
     *
     * @param request  the request
     * @param response the response
     * @throws UnsupportedEncodingException if character encoding is not UTF-8
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        String contentType = request.getContentType();
        if (!contentType.equalsIgnoreCase(JsonHelper.CONTENT_TYPE)) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE); // unsupported content type
            LOGGER.warning(" - - - - Wrong content Type from Request: " + contentType + " - - - - ");

        } else {
            try {
                String body = request.getReader()
                    .lines()
                    .reduce("", (String::concat));
                Map<String, ?> map = JsonHelper.readJsonData(body);
                if (map != null && !map.isEmpty()) {
                    String name = (String) map.get("name");
                    String password = (String) map.get("password");
                    ServletContext servletContext = getServletContext();
                    UserManager userManager = UserManager.getInstance(servletContext);
                    try {
                        if (name != null && !name.isEmpty() && password != null && !password.isEmpty()) {
                            userManager.register(name, password);
                            userManager.writeData(servletContext);
                            response.setStatus(HttpServletResponse.SC_CREATED); // user registered
                            LOGGER.info(" - - - -  Response given - - - - ");
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // invalid user data
                            LOGGER.warning(" - - - - Resource not found: " + request.getPathInfo() + " - - - - ");
                        }
                    } catch (UserException e) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT); // a user with the same name already exists
                        LOGGER.warning(" - - - - user with same name exists  - - - - ");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // invalid user data
                    LOGGER.warning(" - - - - Invalid user data  - - - - ");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // invalid user data
                LOGGER.warning(" - - - - Invalid user data  - - - - ");
            }
        }
    }
}
