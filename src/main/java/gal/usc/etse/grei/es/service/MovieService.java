package gal.usc.etse.grei.es.service;


import gal.usc.etse.grei.es.controller.dto.MovieDTO;
import gal.usc.etse.grei.es.domain.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

public interface MovieService {

    MovieDTO toDTO(Movie movie);

    Optional<Movie> get(String id);


    /**
     * @param query custom query
     * @return list of Employee
     */
    List<Movie> getAll(Query query);

    /**
     * Get all custom paginate data for entity Employee
     *
     * @param query    custom query
     * @param pageable pageable param
     * @return Page of entity Employee
     */
    Page<Movie> getPage(Query query, Pageable pageable);

    Movie postMovie(Movie movie);

    Movie putMovie(String id, Movie movie);

    public boolean deleteMovieById(String id);

}