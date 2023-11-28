package gal.usc.etse.grei.es.service;

import gal.usc.etse.grei.es.domain.Date;
import gal.usc.etse.grei.es.domain.Friendship;
import gal.usc.etse.grei.es.repository.FriendshipRepository;
import gal.usc.etse.grei.es.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface FriendshipService {
    Friendship createFriendship(String userEmail, String friendEmail);

    boolean removeFriendship(String userEmail, String friendEmail);

    boolean areFriends(String user1, String user2);
}
