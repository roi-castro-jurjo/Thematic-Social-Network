package gal.usc.etse.grei.es.controller;

import gal.usc.etse.grei.es.controller.dto.FilterCondition;
import gal.usc.etse.grei.es.domain.*;
import gal.usc.etse.grei.es.domain.Assessment;
import gal.usc.etse.grei.es.repository.support.GenericFilterCriteriaBuilder;
import gal.usc.etse.grei.es.service.AssessmentService;
import gal.usc.etse.grei.es.service.AssessmentService;
import gal.usc.etse.grei.es.service.FilterBuilderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/comments")
@Tag(name = "Assessment API", description = "Assessment related operations")
@SecurityRequirement(name = "JWT")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final FilterBuilderService filterBuilderService;
    private final LinkRelationProvider linkRelationProvider;

    public AssessmentController(FilterBuilderService filterBuilderService, AssessmentService assessmentService, LinkRelationProvider linkRelationProvider) {
        this.filterBuilderService = filterBuilderService;
        this.assessmentService = assessmentService;
        this.linkRelationProvider = linkRelationProvider;
    }

    private void cleanAssessmentFields(Assessment assessment) {

    }


    /**
     * @param page      page number
     * @param size      size count
     * @param order    string orders
     * @param movie    string movie
     * @param user    string user
     * @return PageResponse<Assessment>
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("(#user != null and (hasRole('ROLE_ADMIN') or #user == principal or @friendshipServiceImpl.areFriends(#user, principal))) " +
            "or (isAuthenticated() and #user == null)")
    @Operation(
            operationId = "getAllAssessments",
            summary = "Get the list of all assessments.",
            description = "Get a paginated list of all assessments. Any logged user can access the list, " +
                    "but you must be a the own user, a friend of the user or an admin if you want to filter by user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Assessments details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    public ResponseEntity<PageResponse<Assessment>> getAllAssessments(
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

        Link self = linkTo(methodOn(AssessmentController.class).getAllAssessments(page, size, order, movie, user)).withSelfRel();
        Link first = linkTo(methodOn(AssessmentController.class).getAllAssessments(0, size, order, movie, user)).withRel(IanaLinkRelations.FIRST);
        Link next = linkTo(methodOn(AssessmentController.class).getAllAssessments(page + 1, size, order, movie, user)).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo(methodOn(AssessmentController.class).getAllAssessments(page - 1, size, order, movie, user)).withRel(IanaLinkRelations.PREVIOUS);
        Link last = linkTo(methodOn(AssessmentController.class).getAllAssessments(pg.getTotalPages() - 1, size, order, movie, user)).withRel(IanaLinkRelations.LAST);
        Link resource = linkTo(methodOn(AssessmentController.class).get(null)).withRel(linkRelationProvider.getItemResourceRelFor(Assessment.class));


        return ResponseEntity.ok()
                .headers(new HttpHeaders() {{
                    add(HttpHeaders.LINK, self.toString());
                    add(HttpHeaders.LINK, first.toString());
                    add(HttpHeaders.LINK, next.toString());
                    add(HttpHeaders.LINK, previous.toString());
                    add(HttpHeaders.LINK, last.toString());
                    add(HttpHeaders.LINK, resource.toString());
                }})
                .body(response);
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
    @PreAuthorize("#comment.user.email == principal")
    @Operation(
            operationId = "modifyAssessment",
            summary = "Edit a assessment",
            description = "Edit a assessment. Only the user can edit his own assessment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified assessment details.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessment not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad body",
                    content = @Content
            ),
    })
    public ResponseEntity<Assessment> updateComment(@PathVariable String commentId, @RequestBody Assessment comment) {
        Assessment updatedComment = assessmentService.updateComment(commentId, comment);
        if (updatedComment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("{commentId}")
    @Operation(
            operationId = "deleteAssessment",
            summary = "Delete a Assessment",
            description = "Delete an existing Assessment. To delete a user you must be the author user or and admin."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessment not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Assessment deleted",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        boolean isDeleted = assessmentService.deleteComment(commentId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "createAssessment",
            summary = "Create a new assessment",
            description = "Create a new assessment. Any logger user can create a assessment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Assessment details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Assessment already exists.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Assessment fields not valid or missing",
                    content = @Content
            ),
    })
    public ResponseEntity<Assessment> addComment(@RequestBody Assessment comment) {
        Assessment addedComment = assessmentService.addCommentToMovie(comment.getMovie().getId(), comment);

        if (addedComment == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(addedComment);

    }



}