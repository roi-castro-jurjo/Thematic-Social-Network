package gal.usc.etse.grei.es.controller;

import gal.usc.etse.grei.es.controller.dto.FilterCondition;
import gal.usc.etse.grei.es.domain.Assessment;
import gal.usc.etse.grei.es.domain.Cast;
import gal.usc.etse.grei.es.domain.Assessment;
import gal.usc.etse.grei.es.repository.support.GenericFilterCriteriaBuilder;
import gal.usc.etse.grei.es.service.AssessmentService;
import gal.usc.etse.grei.es.service.AssessmentService;
import gal.usc.etse.grei.es.service.FilterBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final FilterBuilderService filterBuilderService;

    public AssessmentController(FilterBuilderService filterBuilderService, AssessmentService assessmentService) {
        this.filterBuilderService = filterBuilderService;
        this.assessmentService = assessmentService;
    }

    private void cleanAssessmentFields(Assessment assessment) {

    }


    /**
     * @param page      page number
     * @param size      size count
     * @param order    string orders
     * @return PageResponse<Assessment>
     */
    @GetMapping()
    public ResponseEntity<PageResponse<Assessment>> getSearchCriteriaPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "movie", required = false) String movie,
            @RequestParam(value = "user", required = false) String user) {

        PageResponse<Assessment> response = new PageResponse<>();

        Pageable pageable = filterBuilderService.getPageable(size, page, order);
        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        String filter = "";

        if (movie != null){
            filter += "movie._id|jn|" + movie + "&";
        }

        if (user != null){
            filter += "user._id|jn|" + user + "&";
        }

        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filter);

        Query query = filterCriteriaBuilder.addCondition(andConditions, null);
        Page<Assessment> pg = assessmentService.getPage(query, pageable);
        for (Assessment assessment : pg) {
            cleanAssessmentFields(assessment);
        }
        response.setPageStats(pg, pg.getContent());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Assessment> get(@PathVariable("id") String id) {
        return ResponseEntity.of(assessmentService.get(id));
    }



    /**
     * @param filterOr  string filter or conditions
     * @param filterAnd string filter and conditions
     * @return list of Assessment
     */

    @GetMapping(value = "/notPaged")
    public ResponseEntity<List<Assessment>> getAllSearchCriteria(
            @RequestParam(value = "filterOr", required = false) String filterOr,
            @RequestParam(value = "filterAnd", required = false) String filterAnd) {

        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filterAnd);
        List<FilterCondition> orConditions = filterBuilderService.createFilterCondition(filterOr);

        Query query = filterCriteriaBuilder.addCondition(andConditions, orConditions);
        List<Assessment> assessments = assessmentService.getAll(query);

        return new ResponseEntity<>(assessments, HttpStatus.OK);
    }


    @PutMapping("{commentId}")
    public ResponseEntity<Assessment> updateComment(@PathVariable String commentId, @RequestBody Assessment comment) {
        Assessment updatedComment = assessmentService.updateComment(commentId, comment);
        if (updatedComment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        boolean isDeleted = assessmentService.deleteComment(commentId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }



}