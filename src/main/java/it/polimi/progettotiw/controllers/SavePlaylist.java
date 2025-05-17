package it.polimi.progettotiw.controllers;

import it.polimi.progettotiw.ConnectionHandler;
import it.polimi.progettotiw.beans.User;
import it.polimi.progettotiw.dao.PlaylistDAO;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/SavePlaylist")
public class SavePlaylist extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Connection connection;

    @Override
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        // Thymeleaf
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(ctx);
        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(application);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setSuffix(".html");
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        // DB
        connection = ConnectionHandler.getConnection(ctx);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // L’utente è già autenticato dal filter
        User user = (User) request.getSession().getAttribute("user");

        String title = request.getParameter("title");
        String[] trackIdsParam = request.getParameterValues("trackIds");

        if (title == null || title.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Titolo mancante");
            return;
        }

        List<Integer> trackIds = (trackIdsParam == null)
                ? List.of()
                : Arrays.stream(trackIdsParam)
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        try {
            new PlaylistDAO(connection)
                    .createPlaylistWithTracks(title, user.getUsername(), trackIds);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Errore salvataggio playlist");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/GoToHome");
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException ignore) {}
    }
}
