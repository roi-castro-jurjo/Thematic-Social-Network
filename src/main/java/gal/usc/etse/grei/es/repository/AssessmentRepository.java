package gal.usc.etse.grei.es.repository;

import gal.usc.etse.grei.es.domain.Assessment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface AssessmentRepository extends ResourceRepository<Assessment, String> {
    List<Assessment> findByMovie_Id(String Movie_id);

    List<Assessment> findByUser_Email(String User_Email);

}