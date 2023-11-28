package gal.usc.etse.grei.es.controller;

import gal.usc.etse.grei.es.controller.dto.FilterCondition;
import gal.usc.etse.grei.es.controller.dto.MovieDTO;
import gal.usc.etse.grei.es.domain.Assessment;
import gal.usc.etse.grei.es.domain.Cast;
import gal.usc.etse.grei.es.domain.Movie;
import gal.usc.etse.grei.es.domain.User;
import gal.usc.etse.grei.es.exception.NotFoundException;
import gal.usc.etse.grei.es.repository.support.GenericFilterCriteriaBuilder;
import gal.usc.etse.grei.es.service.AssessmentService;
import gal.usc.etse.grei.es.service.MovieService;
import gal.usc.etse.grei.es.service.FilterBuilderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/movies")
@Tag(name = "Movie API", description = "Movie related operations")
@SecurityRequirement(name = "JWT")
public class MovieController {

    private final MovieService movieService;
    private final FilterBuilderService filterBuilderService;
    private final LinkRelationProvider linkRelationProvider;

    public MovieController(MovieService movieService, FilterBuilderService filterBuilderService, LinkRelationProvider linkRelationProvider) {
        this.movieService = movieService;
        this.filterBuilderService = filterBuilderService;
        this.linkRelationProvider = linkRelationProvider;
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
     * @param genre    string genre
     * @param keyword    string keyword
     * @param credits    string credits
     * @param date    string date
     * @return PageResponse<Movie>
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAllMovies",
            summary = "Get the list of all movies.",
            description = "Get a paginated list of all movies. Any logged user can access this list."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movies details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Movie.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    public ResponseEntity<PageResponse<Movie>> getAllMovies(
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

        Link self = linkTo(methodOn(MovieController.class).getAllMovies(page, size, order, genre, keyword, credits, date)).withSelfRel();
        Link first = linkTo(methodOn(MovieController.class).getAllMovies(0, size, order, genre, keyword, credits, date)).withRel(IanaLinkRelations.FIRST);
        Link next = linkTo(methodOn(MovieController.class).getAllMovies(page + 1, size, order, genre, keyword, credits, date)).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo(methodOn(MovieController.class).getAllMovies(page - 1, size, order, genre, keyword, credits, date)).withRel(IanaLinkRelations.PREVIOUS);
        Link last = linkTo(methodOn(MovieController.class).getAllMovies(pg.getTotalPages() - 1, size, order, genre, keyword, credits, date)).withRel(IanaLinkRelations.LAST);
        Link resource = linkTo(methodOn(MovieController.class).get(null)).withRel(linkRelationProvider.getItemResourceRelFor(Movie.class));

        return ResponseEntity.ok()
                .headers(new HttpHeaders() {{
                    add(HttpHeaders.LINK, self.toString());
                    add(HttpHeaders.LINK, first.toString());
                    add(HttpHeaders.LINK, next.toString());
                    add(HttpHeaders.LINK, previous.toString());
                    add(HttpHeaders.LINK, last.toString());
                    add(HttpHeaders.LINK, resource.toString());
                }})
                .body(response);
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
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getOneMovie",
            summary = "Get a single movie details",
            description = "Get the details for a given movie. Any logged user can access these details."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movies details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Movie.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    ResponseEntity<Movie> get(@PathVariable("id") String id) {
        Optional<Movie> movie;
        try {
            movie = movieService.get(id);
            if (movie.isPresent()){
                Link self = linkTo(methodOn(MovieController.class).get(id)).withSelfRel();
                Link all = linkTo(methodOn(MovieController.class).getAllMovies(0, 20, null, null, null, null, null)).withRel(IanaLinkRelations.NEXT);
                return ResponseEntity.ok()
                        .headers(new HttpHeaders(){{
                            add(HttpHeaders.LINK, self.toString());
                            add(HttpHeaders.LINK, all.toString());
                        }})
                        .body(movie.get());
            }
            else {
                throw new NotFoundException("Movie not found.");
            }
        } catch (Exception e){
            return ResponseEntity.of(movieService.get(id));
        }
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

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            operationId = "createMovie",
            summary = "Create a new movie",
            description = "Create a new movie. Only an admin can create a movie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Movie.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Movie already exists.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad body",
                    content = @Content
            ),
    })
    public ResponseEntity<Object> addMovie(@RequestBody Movie movie){
        Movie response = movieService.postMovie(movie);
        if (response == null){
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", "Title is mandatory to POST.");

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        Link self = linkTo(methodOn(MovieController.class).addMovie(movie)).withSelfRel();
        Link all = linkTo(methodOn(MovieController.class).getAllMovies(0, 10, null, null, null, null, null)).withRel(IanaLinkRelations.NEXT);


        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();

        return ResponseEntity.created(location)
                .headers(new HttpHeaders(){{
                    add(HttpHeaders.LINK, self.toString());
                    add(HttpHeaders.LINK, all.toString());
                }})
                .body(response);
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            operationId = "deleteMovie",
            summary = "Delete a movie",
            description = "Delete a movie. Only an admin can delete a movie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Movie deleted",
                    content = @Content
            )
    })
    public ResponseEntity<?> deleteMovie(@PathVariable String id) {
        boolean isRemoved = movieService.deleteMovieById(id);

        Link all = linkTo(methodOn(MovieController.class).getAllMovies(0, 10, null, null, null, null, null)).withRel(IanaLinkRelations.NEXT);


        if (!isRemoved) {
            return ResponseEntity.notFound().header(HttpHeaders.LINK, all.toString()).build();
        }

        return ResponseEntity.noContent().header(HttpHeaders.LINK, all.toString()).build();
    }


}