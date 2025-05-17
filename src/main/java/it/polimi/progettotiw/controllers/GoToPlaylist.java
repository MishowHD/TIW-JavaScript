package it.polimi.progettotiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.progettotiw.ConnectionHandler;
import it.polimi.progettotiw.beans.*;
import it.polimi.progettotiw.dao.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@WebServlet("/GoToPlaylist")
public class GoToPlaylist extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        application = JakartaServletWebApplication.buildApplication(servletContext);
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        connection = ConnectionHandler.getConnection(servletContext);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("user");

        int playlistId = Integer.parseInt(request.getParameter("playlist_id"));
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 0;
        int blockSize = 5;

        try {
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);
            TrackDAO trackDAO = new TrackDAO(connection);

            // Recupera playlist e tracks ordinati
            Playlist playlist = playlistDAO.getPlaylistById(playlistId);
            List<Track> playlistTracks = trackDAO.getTracksByPlaylistOrdered(playlistId);

            // Suddivisione in blocchi
            List<List<Track>> blocks = new ArrayList<>();
            for (int i = 0; i < playlistTracks.size(); i += blockSize) {
                blocks.add(playlistTracks.subList(i, Math.min(i + blockSize, playlistTracks.size())));
            }

            // Tracks disponibili (escludendo quelli già presenti)
            List<Track> availableTracks = trackDAO.findByUserOrdered(user.getUsername());
            availableTracks.removeAll(playlistTracks);
            List<Track> currentTracks = blocks.isEmpty() ? new ArrayList<>() : blocks.get(page);

            WebContext ctx = new WebContext(application.buildExchange(request, response));
            ctx.setVariable("playlist", playlist);
            ctx.setVariable("currentTracks", currentTracks);
            ctx.setVariable("currentBlock", page);
            ctx.setVariable("hasNextBlock", page < blocks.size() - 1);
            ctx.setVariable("availableTracks", availableTracks);

            templateEngine.process("/playlistPage.html", ctx, response.getWriter());

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}