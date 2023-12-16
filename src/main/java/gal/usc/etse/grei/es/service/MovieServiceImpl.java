package gal.usc.etse.grei.es.service;

import gal.usc.etse.grei.es.controller.dto.MovieDTO;
import gal.usc.etse.grei.es.domain.Movie;
import gal.usc.etse.grei.es.repository.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;


import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Service
public class MovieServiceImpl implements MovieService{
    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public MovieDTO toDTO(Movie movie){
        return new MovieDTO(movie.getId(), movie.getTitle(), movie.getOverview(), movie.getGenres(), movie.getReleaseDate(), movie.getResources());
    }

    public Optional<Movie> get(String id) {
        return movieRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Movie> getAll(Query query) {
        return movieRepository.findAll(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Movie> getPage(Query query, Pageable pageable) {
        return movieRepository.findAll(query, pageable);
    }

    private String generateUniqueId() {
        Random random = new Random();
        String id;
        do {
            id = String.format("%06d", random.nextInt(999999));
        } while (movieRepository.findById(id).isPresent());

        return id;
    }

    @Override
    public Movie postMovie(Movie movie){
        System.out.println(movie);
        if (movie.getTitle() == null || Objects.equals(movie.getTitle(), "")){
            System.out.println("Title is mandatory to POST.");
            return null;
        }

        movie.setId(generateUniqueId());
        while (movieRepository.findById(movie.getId()).isPresent()) {
            movie.setId(generateUniqueId());
        }

        return movieRepository.save(movie);
    }


    public void updateMovieAttributes(Movie movieToUpdate, Movie movie) {
        Class<?> clazz = movie.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // Permite el acceso a campos privados
            try {
                Object value = field.get(movie);
                if (value != null && !field.getName().equals("id")) { // Ignora el campo 'id'
                    Field fieldToUpdate = clazz.getDeclaredField(field.getName());
                    fieldToUpdate.setAccessible(true);
                    fieldToUpdate.set(movieToUpdate, value);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Movie putMovie(String id, Movie movie){
        Optional<Movie> movieOptional = movieRepository.findById(id);
        if (movieOptional.isPresent()) {
            Movie movieToUpdate = movieOptional.get();
            updateMovieAttributes(movieToUpdate, movie);

            return movieRepository.save(movieToUpdate);
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteMovieById(String id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
