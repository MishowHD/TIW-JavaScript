package it.polimi.progettotiw.dao;

import it.polimi.progettotiw.beans.Album;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {
    private final Connection con;

    public AlbumDAO(Connection connection) {
        this.con = connection;
    }

    public void create(String title, String performer, int publicationyear, String path, String username) throws SQLException {
        String query = " INSERT into Albums (title, performer, publication_year, image, username) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, title);
            ps.setString(2, performer);
            ps.setInt(3, publicationyear);
            ps.setString(4, path);
            ps.setString(5, username);
            ps.executeUpdate();
        }
    }

    public List<Album> getAlbumOfUser(String username) throws SQLException {
        String query = "SELECT album_id, title, performer, publication_year, image FROM Albums WHERE username = ?";
        List<Album> albums = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Album a = new Album();
                    a.setAlbumId(rs.getInt("album_id"));
                    a.setTitle(rs.getString("title"));
                    a.setPerformer(rs.getString("performer"));
                    a.setPublicationYear(rs.getInt("publication_year"));
                    a.setImage(rs.getString("image"));
                    albums.add(a);
                }
            }
        }
        return albums;
    }

    public boolean isOwnedBy(int albumId, String currentUser) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt "
                + "FROM Albums "
                + "WHERE album_id = ? AND username = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, albumId);
            ps.setString(2, currentUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
                return false;
            }
        }
    }
}
