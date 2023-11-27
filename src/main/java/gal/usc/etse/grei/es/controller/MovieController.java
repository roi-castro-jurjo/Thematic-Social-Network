package gal.usc.etse.grei.es.controller;

import gal.usc.etse.grei.es.controller.dto.FilterCondition;
import gal.usc.etse.grei.es.controller.dto.MovieDTO;
import gal.usc.etse.grei.es.domain.Assessment;
import gal.usc.etse.grei.es.domain.Cast;
import gal.usc.etse.grei.es.domain.Movie;
import gal.usc.etse.grei.es.repository.support.GenericFilterCriteriaBuilder;
import gal.usc.etse.grei.es.service.AssessmentService;
import gal.usc.etse.grei.es.service.MovieService;
import gal.usc.etse.grei.es.service.FilterBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;
    private final FilterBuilderService filterBuilderService;
    private final AssessmentService assessmentService;

    public MovieController(MovieService movieService, FilterBuilderService filterBuilderService, AssessmentService assessmentService) {
        this.movieService = movieService;
        this.filterBuilderService = filterBuilderService;
        this.assessmentService = assessmentService;
    }

    private void cleanMovieFields(Movie movie) {
        movie.setTagline(null);
        movie.setCollection(null);
        movie.setKeywords(null);
        movie.setProducers(null);
        movie.setCast(null);
        movie.setCrew(null);
        movie.setBudget(null);
        movie.setStatus(null);
        movie.setRuntime(null);
        movie.setRevenue(null);
    }


    /**
     * @param page      page number
     * @param size      size count
     * @param order    string orders
     * @return PageResponse<Movie>
     */
    @GetMapping()
    public ResponseEntity<PageResponse<Movie>> getSearchCriteriaPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "credits", required = false) String credits,
            @RequestParam(value = "date", required = false) String date){

        PageResponse<Movie> response = new PageResponse<>();

        Pageable pageable = filterBuilderService.getPageable(size, page, order);
        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        String filter = "";
        String creditsFilter = null;

        if (genre != null){
            genre =  genre.substring(0,1).toUpperCase() + genre.substring(1);
            filter += "genres|jn|" + genre + "&";
        }

        if (keyword != null){
            filter += "keywords|jn|" + keyword + "&";
        }

        if (date != null){
            String[] dateDivided = date.split("-");
            filter += "releaseDate.day|eq|" + dateDivided[0] + "&releaseDate.month|eq|" + dateDivided[1] + "&releaseDate.year|eq|" + dateDivided[2] + "&";
        }

        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filter);
        List<FilterCondition> orConditions = filterBuilderService.createFilterCondition(null);

        Query query = filterCriteriaBuilder.addCondition(andConditions, orConditions);
        Page<Movie> pg = movieService.getPage(query, pageable);
        for (Movie movie : pg) {
            cleanMovieFields(movie);
        }
        response.setPageStats(pg, pg.getContent());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/genres/{genre}")
    public ResponseEntity<PageResponse<Movie>> getMoviesByGenre(
            @PathVariable("genre") String genre,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "order", required = false) String order) {

        PageResponse<Movie> response = new PageResponse<>();

        Pageable pageable = filterBuilderService.getPageable(size, page, order);
        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        String filterAnd = "genres|like|" + genre;
        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filterAnd);

        Query query = filterCriteriaBuilder.addCondition(andConditions, null);

        Page<Movie> pg = movieService.getPage(query, pageable);

        for (Movie movie : pg) {
            cleanMovieFields(movie);
        }

        response.setPageStats(pg, pg.getContent());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/keywords/{keyword}")
    public ResponseEntity<PageResponse<Movie>> getMoviesByKeyword(
            @PathVariable("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "order", required = false) String order) {

        PageResponse<Movie> response = new PageResponse<>();

        Pageable pageable = filterBuilderService.getPageable(size, page, order);
        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        String filterAnd = "keywords|like|" + keyword;
        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filterAnd);

        Query query = filterCriteriaBuilder.addCondition(andConditions, null);

        Page<Movie> pg = movieService.getPage(query, pageable);

        for (Movie movie : pg) {
            cleanMovieFields(movie);
        }

        response.setPageStats(pg, pg.getContent());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }




    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        ResponseEntity<Movie> get(@PathVariable("id") String id) {
            return ResponseEntity.of(movieService.get(id));
        }


    /**
     * @param filterOr  string filter or conditions
     * @param filterAnd string filter and conditions
     * @return list of Movie
     */

    @GetMapping(value = "/notPaged")
    public ResponseEntity<List<Movie>> getAllSearchCriteria(
            @RequestParam(value = "filterOr", required = false) String filterOr,
            @RequestParam(value = "filterAnd", required = false) String filterAnd) {

        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filterAnd);
        List<FilterCondition> orConditions = filterBuilderService.createFilterCondition(filterOr);

        Query query = filterCriteriaBuilder.addCondition(andConditions, orConditions);
        List<Movie> movies = movieService.getAll(query);

        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Object> addMovie(@RequestBody Movie movie){
        Movie response = movieService.postMovie(movie);
        if (response == null){
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", "Title is mandatory to POST.");

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }



    @PutMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> modifyMovie(@PathVariable String id, @RequestBody Movie movie){
        Movie updatedMovie = movieService.putMovie(id, movie);
        if (updatedMovie == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(updatedMovie);
        }
    }

    @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteMovie(@PathVariable String id) {
        boolean isRemoved = movieService.deleteMovieById(id);

        if (!isRemoved) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }


}