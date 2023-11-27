package gal.usc.etse.grei.es.repository;


import gal.usc.etse.grei.es.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends ResourceRepository<User, String> {
    public Optional<User> findByName(String name);
}
