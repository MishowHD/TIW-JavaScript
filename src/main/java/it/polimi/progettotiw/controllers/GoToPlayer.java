package it.polimi.progettotiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import it.polimi.progettotiw.beans.Track;
import it.polimi.progettotiw.dao.TrackDAO;
import it.polimi.progettotiw.ConnectionHandler;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@WebServlet("/GoToPlayer")
public class GoToPlayer extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        connection = ConnectionHandler.getConnection(servletContext);

        // Configuro Thymeleaf
        application = JakartaServletWebApplication.buildApplication(servletContext);
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 1. Ottengo e valido track_id
        String trackIdParam = request.getParameter("track_id");
        if (trackIdParam == null || trackIdParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing track_id parameter");
            return;
        }

        int trackId;
        try {
            trackId = Integer.parseInt(trackIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid track_id format");
            return;
        }

        // 2. Prelevo dal DB
        TrackDAO trackDAO = new TrackDAO(connection);
        Track track;
        try {
            track = trackDAO.findById(trackId);
        } catch (SQLException e) {
            log("Database access error in GoToPlayer", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
            return;
        }

        // 3. Controllo esistenza
        if (track == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Track not found");
            return;
        }

        // 4. Process Thymeleaf e invio al client
        WebContext ctx = new WebContext(application.buildExchange(request, response));
        ctx.setVariable("track", track);
        // Se il template si trova in /WEB-INF/templates/playerPage.html
        templateEngine.process("playerPage", ctx, response.getWriter());
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            log("Failed to close database connection in GoToPlayer", e);
        }
    }
}
