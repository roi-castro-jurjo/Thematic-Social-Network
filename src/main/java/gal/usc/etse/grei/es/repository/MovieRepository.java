package gal.usc.etse.grei.es.repository;

import gal.usc.etse.grei.es.domain.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface MovieRepository extends ResourceRepository<Movie, String> {

}
