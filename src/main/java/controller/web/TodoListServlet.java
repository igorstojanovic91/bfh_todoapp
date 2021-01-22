package controller.web;

import model.User;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/todos")
public class TodoListServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(TodoListServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        RequestDispatcher view;
        if (user == null) {
            response.reset();
            response.sendRedirect("login");
            LOGGER.info(" - - - - User not logged in  - - - - ");
        } else {
            try {
                request.setAttribute("todos", user.getTodos());
                view = request.getRequestDispatcher("todos.jsp");
                view.forward(request, response);
                LOGGER.info(" - - - - Getting users todo list  - - - - ");
            } catch (ServletException e) {
                e.printStackTrace();
                view = request.getRequestDispatcher("errors.jsp");
                view.forward(request, response);
                LOGGER.severe(" - - - - Error occurred: " + e.getMessage() + " - - - - ");
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String category = request.getParameter("category");
        String status = request.getParameter("status");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        RequestDispatcher view;
        if (user == null) {
            response.reset();
            response.sendRedirect("login");
            LOGGER.info(" - - - - User not logged in  - - - - ");
        } else {
            request.setAttribute("todos", user.getTodos(category, status));
            request.setAttribute("categoryFilter", category);
            request.setAttribute("statusFilter", status);
            boolean listIsFiltered = ((category != null && !category.isEmpty()) || (status != null && !status.isEmpty())) ? true : false;
            request.setAttribute("listIsFiltered", listIsFiltered);
            try {
                view = request.getRequestDispatcher("todos.jsp");
                view.forward(request, response);
                LOGGER.info(" - - - - Filtering user list  - - - - ");
            } catch (ServletException | IOException e) {
                e.printStackTrace();
                view = request.getRequestDispatcher("errors.jsp");
                view.forward(request, response);
                LOGGER.severe(" - - - - Error occurred: " + e.getMessage() + " - - - - ");
            }
        }
    }
}
