package gal.usc.etse.grei.es.service;

import gal.usc.etse.grei.es.domain.Assessment;
import gal.usc.etse.grei.es.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface AssessmentService {

    Optional<Assessment> get(String id);


    /**
     * @param query custom query
     * @return list of User
     */
    List<Assessment> getAll(Query query);

    /**
     * Get all custom paginate data for entity User
     *
     * @param query    custom query
     * @param pageable pageable param
     * @return Page of entity User
     */
    Page<Assessment> getPage(Query query, Pageable pageable);
    List<Assessment> getCommentsByMovie(String movieId);

    List<Assessment> getCommentsByUser(String userId);

    Assessment addCommentToMovie(String movieId, Assessment comment);

    Assessment updateComment(String commentId, Assessment comment);

    boolean deleteComment(String commentId);

}
