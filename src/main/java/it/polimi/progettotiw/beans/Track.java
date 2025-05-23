package it.polimi.progettotiw.beans;

import java.io.Serializable;
import java.util.Objects;

public class Track implements Serializable {
    private int track_id;
    private String title;
    private String file_path;
    private Album album;
    private String genre_name;
    private String username;

    public int getTrack_id() {
        return track_id;
    }

    public void setTrack_id(int trackId) {
        this.track_id = trackId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return file_path;
    }

    public void setFilePath(String filePath) {
        this.file_path = filePath;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getGenreName() {
        return genre_name;
    }

    public void setGenreName(String genreName) {
        this.genre_name = genreName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return track_id == track.track_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(track_id);
    }

}