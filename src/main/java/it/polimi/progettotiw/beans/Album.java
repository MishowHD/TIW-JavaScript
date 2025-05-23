package it.polimi.progettotiw.beans;

public class Album {
    private int albumId;
    private String title;
    private String performer;
    private int publicationYear;
    private String image;
    private String username;

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getPublicationYear() {  // "Y" maiuscola
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getPerformer() {
        return performer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String path) {
        this.image = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
