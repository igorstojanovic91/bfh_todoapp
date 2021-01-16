<%--
  Created by IntelliJ IDEA.
  User: chleu
  Date: 28.12.2020
  Time: 10:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Todo Application • Todo List</title>
    <meta name="title" content="Todo Application • Todo List">
    <meta name="description" content="Todo Application • CAS Software Development (SD-HS20) • Gruppe 1">
    <meta name="author" content="Igor Stojanovic, Sabina Löffel, Christophe Leupi, Raphael Gerber">
    <meta name="keywords" content="berner fachhochschule, bfh, cas software development, sd-hs20, herbst 2020, igor stojanovic, sabina löffel, christophe leupi, raphael gerber, bern university of applied science." />

    <link rel="stylesheet" href="css/bulma.min.css">
    <link rel="stylesheet" href="css/styles.css">

    <!-- Favicons -->
    <link rel="apple-touch-icon" href="favicon/apple-touch-icon.png">
    <link rel="apple-touch-icon" sizes="180x180" href="favicon/apple-touch-icon-180x180.png">
    <meta name="apple-mobile-web-app-title" content="codeyard">
    <link rel="mask-icon" href="favicon/safari-pinned-tab.svg" color="#4c9ebf">
    <link rel="alternate icon" type="image/png" href="favicon/favicon.png">
    <link rel="icon" type="image/svg+xml" href="favicon/favicon.svg">
    <link rel="manifest" href="manifest.webmanifest">
    <meta name="msapplication-TileColor" content="#1d1e1f">
    <meta name="msapplication-config" content="favicon/browserconfig.xml">
    <meta name="theme-color" content="#1d1e1f">
    <link rel="icon" type="image/png" sizes="32x32" href="favicon/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="favicon/favicon-16x16.png">
    <meta name="thumbnail" content="favicon/thumb-150x150.png">
</head>

<body>
<section class="hero is-info is-bold">
    <div class="hero-head">
        <nav class="level is-mobile pt-5 pr-5">
            <div class="level-left"></div>
            <div class="level-right">
                <p class="level-item">${user.getUserName()}</p>
                <p class="level-item"><a href="logout" class="button">Logout</a></p>
            </div>
        </nav>
    </div>

    <div class="hero-body pt-5 pr-5 pb-6 pl-5">
        <div class="container has-text-centered">
            <svg height="32" viewBox="0 0 32 32" width="32" xmlns="http://www.w3.org/2000/svg"><g fill="#fff"><path d="m16 0c-8.83658065 0-16 7.16341935-16 16 0 8.8365806 7.16341935 16 16 16 8.8365806 0 16-7.1634194 16-16 0-8.83658065-7.1634194-16-16-16zm0 3.09677419c7.1310968 0 12.9032258 5.77103226 12.9032258 12.90322581 0 7.1310968-5.7710323 12.9032258-12.9032258 12.9032258-7.13109677 0-12.90322581-5.7710323-12.90322581-12.9032258 0-7.13109677 5.77103226-12.90322581 12.90322581-12.90322581"/><path d="m25.0454194 11.5010968-1.4539355-1.4656774c-.3010968-.30354843-.7912904-.30554843-1.0948387-.0043871l-9.1194839 9.0461935-3.8575484-3.8888387c-.30109677-.3035484-.79129032-.3055484-1.09483871-.0044516l-1.46574193 1.4539355c-.30354839.3010967-.30554839.7912903-.0043871 1.0949032l5.85683874 5.9042581c.3010967.3035483.7912903.3055483 1.0948387.0043871l11.1347742-11.0454194c.3034838-.3011613.3054193-.7913548.0043226-1.0949032z" stroke="#fff" stroke-width=".64"/></g></svg>
            <h1 class="title">Todo Application</h1>
        </div>
    </div>
</section>


<section class="section">
    <h1 class="title">Todo List</h1>

    <nav class="level is-mobile">
        <div class="level-left">
            <div class="level-item">
                <c:if test="${user.getTodos().size() == 0}">
                    <em class="subtitle is-4">Your Todo List is empty :-(</em>
                </c:if>

                <c:if test="${user.getTodos().size() > 0}">
                    <form action="todos" method="post">
                        <div class="field is-horizontal">
                            <div class="control has-icons-left">
                                <div class="select">
                                    <select name="category">
                                        <option value="">Category</option>
                                        <c:forEach var="category" items="${user.getDistinctCategories()}">
                                            <option value="${category}" <c:if test="${category.equals(categoryFilter)}">selected</c:if>>${category}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="icon is-small is-left">
                                    <i class="fas fa-filter"></i>
                                </div>
                            </div>
                            <div class="control ml-3">
                                <input type="submit" class="button is-light" value="Filter">
                            </div>
                            <c:if test="${(categoryFilter != null) && (!categoryFilter.isEmpty())}">
                                <div class="control ml-3">
                                    <a href="todos?category=" title="Delete Filter" class="button is-light"><span class="icon"><i class="fas fa-trash"></i></span></a>
                                </div>
                            </c:if>
                        </div>
                    </form>
                </c:if>
            </div>
        </div>

        <div class="level-right">
            <p class="level-item">
                <a href="todo" class="button is-success has-text-weight-bold">
                    <span class="icon is-small"><i class="fas fa-plus"></i></span>
                    <span>New Todo</span>
                </a>
            </p>
        </div>
    </nav>

    <c:if test="${user.getTodos().size() > 0}">
        <table class="table is-striped is-hoverable is-fullwidth">
            <thead>
            <tr>
                <th class="is-narrow">&nbsp;</th>
                <th class="has-text-left">Title</th>
                <th class="has-text-left">Category</th>
                <th class="has-text-left">Due Date</th>
                <th class="has-text-left">Action</th>
            </tr>
            </thead>
            <tbody>

            <c:forEach var="todo" items="${todos}">
                <c:choose>
                    <c:when test="${todo.isOverdue() == true}"><tr class="has-text-danger has-background-danger-light has-text-weight-bold"></c:when>
                    <c:when test="${todo.isCompleted() == true}"><tr class="done"></c:when>
                    <c:otherwise><tr></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${todo.isImportant() == true}">
                        <td class="is-narrow"><div class="icon has-text-danger"><i class="fas fa-exclamation"></i></div></td>
                    </c:when>
                    <c:otherwise>
                        <td class="is-narrow">&nbsp;</td>
                    </c:otherwise>
                </c:choose>

                <td><c:out value="${todo.getTitle()}" /></td>
                <td><c:out value="${todo.getCategory()}" /></td>
                <td><c:out value="${todo.getDueDate()}" /></td>

                <td>
                    <a href="todo?todoID=${todo.getTodoID()}" title="Edit Todo" class="button"><span class="icon"><i class="fas fa-pen"></i></span></a>
                    <a href="todo?todoID=${todo.getTodoID()}" title="Delete Todo" class="button"><span class="icon"><i class="fas fa-trash"></i></span></a>
                </td>
                </tr>
            </c:forEach>

            </tbody>
        </table>
    </c:if>
</section>

<footer class="footer">
    <div class="content has-text-centered">
        <p>
            &copy; 2021 by <strong>CAS SD-HS20 • Gruppe 1</strong>: Igor Stojanovic, Sabina L&ouml;ffel, Christophe Leupi &amp; Raphael Gerber.
        </p>
    </div>
</footer>

<script defer src="https://use.fontawesome.com/releases/v5.14.0/js/all.js"></script>
</body>
</html>