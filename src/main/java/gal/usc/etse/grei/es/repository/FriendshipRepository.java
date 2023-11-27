package gal.usc.etse.grei.es.repository;

import gal.usc.etse.grei.es.domain.Friendship;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends ResourceRepository<Friendship, String>{
    Optional<Friendship> findByUserAndFriend(String user, String friend);
}
