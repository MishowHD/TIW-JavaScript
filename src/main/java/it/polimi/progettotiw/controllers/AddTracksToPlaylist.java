package it.polimi.progettotiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import it.polimi.progettotiw.ConnectionHandler;
import it.polimi.progettotiw.dao.PlaylistDAO;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/AddTracksToPlaylist")
@MultipartConfig
public class AddTracksToPlaylist extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        connection = ConnectionHandler.getConnection(servletContext);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Assicuriamoci di leggere correttamente i parametri di form-data UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        int playlistId;
        try {
            playlistId = Integer.parseInt(request.getParameter("playlist_id"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"ID playlist non valido\"}");
            return;
        }

        // Legge i checkbox con nome "trackIds[]"
        String[] trackIdsArray = request.getParameterValues("trackIds[]");
        if (trackIdsArray == null || trackIdsArray.length == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Nessuna traccia selezionata\"}");
            return;
        }

        try {
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);
            for (String trackIdStr : trackIdsArray) {
                int trackId = Integer.parseInt(trackIdStr);
                playlistDAO.addTrackToPlaylist(playlistId, trackId);
            }

            // Rispondiamo con un JSON di conferma
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\":\"Tracce aggiunte con successo\"}");

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Formato ID traccia non valido\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Errore server: impossibile aggiungere le tracce\"}");
        }
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
