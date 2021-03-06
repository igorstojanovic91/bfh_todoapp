package controller.rest;

import model.Todo;
import model.User;
import model.UserManager;
import controller.rest.helper.JsonHelper;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Read and manipulate todo items via REST API.
 * Listens to "/api/todos/*" path.
 *
 * @author Igor Stojanovic, Sabina Löffel, Christophe Leupi, Raphael Gerber
 * @version 1.0
 */
@WebServlet("/api/todos/*")
public class TodosRestServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(TodosRestServlet.class.getName());

    /**
     * Returns a single todo item if an id is present otherwise returns a list with todo items.
     * Filters the list by categories if the category query parameter is present.
     *
     * @param request  the request
     * @param response the response
     * @throws IOException is thrown when the response couldn't be written
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String acceptType = request.getHeader("Accept");
        request.setCharacterEncoding("UTF-8");

        if (!acceptType.equalsIgnoreCase(JsonHelper.CONTENT_TYPE)) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            LOGGER.warning(" - - - - Wrong content Type from Request: " + acceptType + " - - - - ");
        } else {
            String category = request.getParameter("category");
            ServletContext servletContext = getServletContext();
            UserManager userManager = UserManager.getInstance(servletContext);
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && !pathInfo.isEmpty()) {
                // todos/{id}
                try {
                    int todoID = Integer.parseInt(pathInfo.split("/")[1]);
                    User user = userManager.getUser((Integer) request.getAttribute("userID"));
                    Todo todo = user.getTodo(todoID);
                    if (todo != null) {
                        String json = JsonHelper.writeTodoJsonData(todo);
                        writeResponse(response, json, HttpServletResponse.SC_OK);
                        LOGGER.info(" - - - -  Response given - - - - ");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        LOGGER.warning(" - - - - Resource not found: " + request.getPathInfo() + " - - - - ");
                    }
                } catch (Exception exception) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    LOGGER.warning(" - - - - Resource not found: " + request.getPathInfo() + " - - - - ");
                }
            } else {
                // todos without path parameter
                User user = userManager.getUser((Integer) request.getAttribute("userID"));
                String json = JsonHelper.writeTodoJsonData(user.getTodos(category));
                writeResponse(response, json, HttpServletResponse.SC_OK);
                LOGGER.info(" - - - -  Response given - - - - ");
            }
        }
    }

    /**
     * Adds a todo.
     *
     * @param request  the request
     * @param response the response
     * @throws UnsupportedEncodingException if character encoding is not UTF-8
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        String contentType = request.getContentType();
        request.setCharacterEncoding("UTF-8");
        String acceptType = request.getHeader("Accept");

        if (!contentType.equalsIgnoreCase(JsonHelper.CONTENT_TYPE)) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            LOGGER.warning(" - - - - Wrong content Type from Request: " + contentType + " - - - - ");
        } else if (!acceptType.equalsIgnoreCase(JsonHelper.CONTENT_TYPE)) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            LOGGER.warning(" - - - - Wrong Accept Type from Request: " + acceptType + " - - - - ");
        } else {
            ServletContext servletContext = getServletContext();
            UserManager userManager = UserManager.getInstance(servletContext);
            try {
                String body = request.getReader()
                    .lines()
                    .reduce("", (String::concat));
                Map<String, ?> map = JsonHelper.readJsonData(body);
                if ((map != null && !map.isEmpty())
                    && (map.containsKey("title") && !map.get("title").toString().isEmpty())) {
                    String title = (String) map.get("title");
                    addNewTodo(request, response, servletContext, userManager, map, title);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    LOGGER.warning(" - - - - Bad request: " + request.getPathInfo() + " - - - - ");
                }
            } catch (Exception exception) {
                System.out.println("Exception: " + exception.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.warning(" - - - - Invalid Todo data  - - - - ");
            }
        }
    }

    /**
     * Updates a todo.
     * If there's an id in the request body, it must match the id from the path.
     *
     * @param request  the request
     * @param response the response
     * @throws IOException is thrown when the response couldn't be written
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contentType = request.getContentType();
        request.setCharacterEncoding("UTF-8");
        if (!contentType.equalsIgnoreCase(JsonHelper.CONTENT_TYPE)) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            LOGGER.warning(" - - - - Wrong Content Type from Request: " + contentType + " - - - - ");
        } else {
            ServletContext servletContext = getServletContext();
            UserManager userManager = UserManager.getInstance(servletContext);
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && !pathInfo.isEmpty()) {
                try {
                    int todoIDPath = Integer.parseInt(pathInfo.split("/")[1]);
                    User user = userManager.getUser((Integer) request.getAttribute("userID"));
                    Todo todo = user.getTodo(todoIDPath);
                    if (todo != null) {
                        String body = request.getReader()
                            .lines()
                            .reduce("", (String::concat));
                        Map<String, ?> map = JsonHelper.readJsonData(body);
                        if (map != null && !map.isEmpty()) {
                            Integer todoIDBody = (Integer) map.get("id");
                            // if there's an id in the body, it must match the id from the path
                            if (todoIDBody == null || todoIDPath == todoIDBody) {
                                if (map.get("title") != null && !((String) map.get("title")).isEmpty()) {
                                    updateTodo(response, servletContext, userManager, user, todo, map);
                                } else {
                                    writeResponse(response, "", HttpServletResponse.SC_BAD_REQUEST);
                                    LOGGER.warning(" - - - - Invalid Todo data: no title set - - - - ");
                                }
                            } else {
                                writeResponse(response, "", HttpServletResponse.SC_BAD_REQUEST);
                                LOGGER.warning(" - - - - Invalid Todo data: todo id's (body and path) do not match - - - - ");
                            }
                        } else {
                            writeResponse(response, "", HttpServletResponse.SC_BAD_REQUEST);
                            LOGGER.warning(" - - - - Invalid Todo data: body was not readable or empty - - - - ");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        LOGGER.warning(" - - - - Invalid Todo data: Todo not found - - - - ");
                    }

                } catch (Exception exception) {
                    writeResponse(response, "", HttpServletResponse.SC_BAD_REQUEST);
                    LOGGER.warning(" - - - - Invalid Todo data - - - - ");
                }
            } else {
                writeResponse(response, "", HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.warning(" - - - - Invalid Todo data: invalid path - - - - ");
            }
        }

    }


    /**
     * Remove a todo.
     *
     * @param request  the request
     * @param response the response
     * @throws IOException is thrown when the response couldn't be written
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        ServletContext servletContext = getServletContext();
        UserManager userManager = UserManager.getInstance(servletContext);
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty()) {
            // todos/{id}
            try {
                Integer todoID = Integer.parseInt(pathInfo.split("/")[1]);
                User user = userManager.getUser((Integer) request.getAttribute("userID"));
                Todo todo = user.getTodo(todoID);
                if (todo != null) {
                    user.deleteTodo(todo);
                    userManager.writeData(servletContext);
                    writeResponse(response, "", HttpServletResponse.SC_NO_CONTENT);
                    LOGGER.info(" - - - - Todo removed id: " + todo.getTodoID() + "  - - - - ");
                } else {
                    writeResponse(response, "", HttpServletResponse.SC_NOT_FOUND);
                    LOGGER.warning(" - - - - Todo not found: " + pathInfo + "  - - - - ");
                }
            } catch (Exception e) {
                writeResponse(response, "", HttpServletResponse.SC_NOT_FOUND);
                LOGGER.warning(" - - - - Todo not found: " + pathInfo + "  - - - - ");
            }
        } else {
            writeResponse(response, "", HttpServletResponse.SC_NOT_FOUND);
            LOGGER.warning(" - - - - Todo not found: " + pathInfo + "  - - - - ");
        }
    }

    /**
     * Writes a json response
     *
     * @param response     - the servlets HttpServletResponse object
     * @param responseBody - the text to be written in the response (previously parsed from json)
     * @param status       - Status code to be sent
     * @throws IOException is thrown when the response couldn't be written
     */
    private void writeResponse(HttpServletResponse response, String responseBody, Integer status) throws IOException {
        response.setStatus(status);
        response.setContentType(JsonHelper.CONTENT_TYPE);
        response.setCharacterEncoding(JsonHelper.ENCODING);
        PrintWriter out = response.getWriter();
        out.print(responseBody);
        out.flush();
    }

    private void updateTodo(HttpServletResponse response, ServletContext servletContext, UserManager userManager, User user, Todo todo, Map<String, ?> map) throws IOException {
        String title = (String) map.get("title");
        String category = (String) map.get("category");
        String dueDate = (String) map.get("dueDate");

        LocalDate date = (dueDate != null && !dueDate.isEmpty()) ? LocalDate.parse(dueDate) : LocalDate.MIN;
        todo.setTitle(title);
        if (category != null && !category.isEmpty()) {
            todo.setCategory(category);
        }
        if (!date.isEqual(LocalDate.MIN)) {
            todo.setDueDate(date);
        }
        if (map.get("important") != null) {
            boolean isImportant = (boolean) map.get("important");
            todo.setImportant(isImportant);
        }
        if (map.get("completed") != null) {
            boolean isCompleted = (boolean) map.get("completed");
            todo.setCompleted(isCompleted);
        }
        user.updateTodo(todo);
        userManager.writeData(servletContext);
        writeResponse(response, "todoId", HttpServletResponse.SC_NO_CONTENT);
        LOGGER.info(" - - - - todo updated: " + todo.getTodoID() + " - - - - ");
    }

    private void addNewTodo(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, UserManager userManager, Map<String, ?> map, String title) throws IOException {
        String category = (map.get("category") != null) ? (String) map.get("category") : "";
        String dueDate = (String) map.get("dueDate");
        boolean isImportant = map.get("important") != null && (boolean) map.get("important");
        boolean isCompleted = map.get("completed") != null && (boolean) map.get("completed");
        LocalDate date = (dueDate != null && !dueDate.isEmpty()) ? LocalDate.parse(dueDate) : null;

        User user = userManager.getUser((Integer) request.getAttribute("userID"));
        Todo todo = new Todo(title, category, date, isImportant, isCompleted);
        String todoId = todo.getTodoID().toString();
        user.addTodo(todo);

        userManager.writeData(servletContext);
        writeResponse(response, todoId, HttpServletResponse.SC_CREATED);
        LOGGER.info(" - - - - Todo with ID: " + todoId + " created  - - - - ");
    }
}
