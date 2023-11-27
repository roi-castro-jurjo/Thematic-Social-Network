package gal.usc.etse.grei.es.controller.dto;

import gal.usc.etse.grei.es.domain.Date;
import gal.usc.etse.grei.es.domain.Resource;

import java.util.List;

public class MovieDTO {
    private String id;
    private String title;
    private String overview;
    private List<String> genres;
    private Date releaseDate;
    private List<Resource> resources;

    public MovieDTO(String id, String title, String overview, List<String> genres, Date releaseDate, List<Resource> resources) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.genres = genres;
        this.releaseDate = releaseDate;
        this.resources = resources;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
