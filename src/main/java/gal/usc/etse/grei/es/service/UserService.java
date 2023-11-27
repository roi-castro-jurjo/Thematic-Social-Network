package gal.usc.etse.grei.es.service;

import gal.usc.etse.grei.es.controller.dto.MovieDTO;
import gal.usc.etse.grei.es.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

public interface UserService {


    Optional<User> get(String id);


    /**
     * @param query custom query
     * @return list of User
     */
    List<User> getAll(Query query);

    /**
     * Get all custom paginate data for entity User
     *
     * @param query    custom query
     * @param pageable pageable param
     * @return Page of entity User
     */
    Page<User> getPage(Query query, Pageable pageable);

    User postUser(User user);

    User putUser(String id, User user);

    boolean deleteUserById(String id);

    Optional<User> getByName(String name);

    User addFriend(String userEmail, User friend);

    boolean removeFriend(String userEmail, String friendEmail);

}