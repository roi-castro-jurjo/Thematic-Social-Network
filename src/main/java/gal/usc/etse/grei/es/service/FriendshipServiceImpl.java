package gal.usc.etse.grei.es.service;

import gal.usc.etse.grei.es.domain.Date;
import gal.usc.etse.grei.es.domain.Friendship;
import gal.usc.etse.grei.es.domain.User;
import gal.usc.etse.grei.es.repository.FriendshipRepository;
import gal.usc.etse.grei.es.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        System.out.println(userEmail);
        System.out.println(friendEmail);

        Optional<User> userOptional = userRepository.findById(userEmail);
        if (!userOptional.isPresent()) {
            System.out.println("olaola");
            return null;
        }

        Optional<User> friendOptional = userRepository.findById(friendEmail);
        if (!friendOptional.isPresent()) {
            System.out.println("olaola222");
            return null;
        }

        System.out.println("AAAAAAAAAAAAAAAAA");

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
}
