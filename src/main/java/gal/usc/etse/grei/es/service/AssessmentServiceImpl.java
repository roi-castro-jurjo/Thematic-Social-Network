package gal.usc.etse.grei.es.service;

import gal.usc.etse.grei.es.domain.Assessment;
import gal.usc.etse.grei.es.domain.Movie;
import gal.usc.etse.grei.es.domain.User;
import gal.usc.etse.grei.es.repository.AssessmentRepository;
import gal.usc.etse.grei.es.repository.MovieRepository;
import gal.usc.etse.grei.es.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssessmentServiceImpl implements AssessmentService{
    private final AssessmentRepository assessmentRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;


    public AssessmentServiceImpl(AssessmentRepository assessmentRepository, MovieRepository movieRepository, UserRepository userRepository) {
        this.assessmentRepository = assessmentRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    public Optional<Assessment> get(String id) {
        return assessmentRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Assessment> getAll(Query query) {
        return assessmentRepository.findAll(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Assessment> getPage(Query query, Pageable pageable) {
        return assessmentRepository.findAll(query, pageable);
    }

    @Override
    public List<Assessment> getCommentsByMovie(String movieId) {
        return assessmentRepository.findByMovie_Id(movieId);
    }

    @Override
    public List<Assessment> getCommentsByUser(String userId) {
        // Implementa la lógica para obtener todos los comentarios hechos por un usuario
        return assessmentRepository.findByUser_Email(userId);
    }

    @Override
    public Assessment addCommentToMovie(String movieId, Assessment comment) {
        Optional<Movie> movie = movieRepository.findById(movieId);
        if (!movie.isPresent()) {
            return null;
        }

        if (comment.getUser() == null || userRepository.findById(comment.getUser().getEmail()).isEmpty()) {
            return null;
        }

        comment.setMovie(movie.get());

        return assessmentRepository.save(comment);
    }

    @Override
    public Assessment updateComment(String commentId, Assessment comment) {
        Optional<Assessment> existingCommentOptional = assessmentRepository.findById(commentId);
        if (!existingCommentOptional.isPresent()) {
            return null;
        }

        Assessment existingComment = existingCommentOptional.get();

        existingComment.setRating(comment.getRating());
        existingComment.setComment(comment.getComment());

        return assessmentRepository.save(existingComment);
    }

    @Override
    public boolean deleteComment(String commentId) {
        if (!assessmentRepository.existsById(commentId)) {
            return false;
        }
        assessmentRepository.deleteById(commentId);
        return true;
    }

}
