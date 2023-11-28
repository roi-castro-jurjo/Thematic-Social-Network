package gal.usc.etse.grei.es.service;

import gal.usc.etse.grei.es.domain.Date;
import gal.usc.etse.grei.es.domain.Friendship;
import gal.usc.etse.grei.es.domain.User;
import gal.usc.etse.grei.es.repository.FriendshipRepository;
import gal.usc.etse.grei.es.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class FriendshipServiceImpl implements FriendshipService{
    private final FriendshipRepository friendshipRepository;

    private final UserRepository userRepository;

    public FriendshipServiceImpl(FriendshipRepository friendshipRepository, UserRepository userRepository){
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Friendship createFriendship(String userEmail, String friendEmail) {

        Optional<User> userOptional = userRepository.findById(userEmail);
        if (!userOptional.isPresent()) {
            return null;
        }

        Optional<User> friendOptional = userRepository.findById(friendEmail);
        if (!friendOptional.isPresent()) {
            return null;
        }

        if (friendshipRepository.findByUserAndFriend(userEmail, friendEmail).isPresent()) {
            return null;
        }

        Friendship friendship = new Friendship();
        friendship.setUser(userEmail);
        friendship.setFriend(friendEmail);
        friendship.setConfirmed(false);
        friendship.setSince(Date.today());

        return friendshipRepository.save(friendship);
    }

    @Override
    @Transactional
    public boolean removeFriendship(String userEmail, String friendEmail) {
        Optional<Friendship> friendship = friendshipRepository.findByUserAndFriend(userEmail, friendEmail);
        if (friendship.isPresent()) {
            friendshipRepository.delete(friendship.get());
            friendshipRepository.findByUserAndFriend(friendEmail, userEmail)
                    .ifPresent(friendshipRepository::delete);
            return true;
        }
        return false;
    }

    @Override
    public boolean areFriends(String user1, String user2){
        Optional<Friendship> friendship1 = friendshipRepository.findByUserAndFriend(user1, user2);
        if (friendship1.isPresent() && friendship1.get().getConfirmed()) {
            return true;
        }

        Optional<Friendship> friendship2 = friendshipRepository.findByUserAndFriend(user2, user1);
        return friendship2.isPresent() && friendship2.get().getConfirmed();

    }
}
