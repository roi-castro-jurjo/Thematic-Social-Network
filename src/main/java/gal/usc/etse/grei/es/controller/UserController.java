package gal.usc.etse.grei.es.controller;


import gal.usc.etse.grei.es.controller.dto.FilterCondition;
import gal.usc.etse.grei.es.domain.Assessment;
import gal.usc.etse.grei.es.domain.Cast;
import gal.usc.etse.grei.es.domain.User;
import gal.usc.etse.grei.es.exception.NotFoundException;
import gal.usc.etse.grei.es.exception.ServerErrorException;
import gal.usc.etse.grei.es.repository.support.GenericFilterCriteriaBuilder;
import gal.usc.etse.grei.es.service.AssessmentService;
import gal.usc.etse.grei.es.service.UserService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/users")
@Tag(name = "User API", description = "User related operations")
@SecurityRequirement(name = "JWT")
public class UserController {

    private final UserService userService;
    private final FilterBuilderService filterBuilderService;

    public UserController(UserService userService, FilterBuilderService filterBuilderService, AssessmentService assessmentService) {
        this.userService = userService;
        this.filterBuilderService = filterBuilderService;
    }

    private void cleanUserFields(User user) {
        user.setEmail(null);
        //user.setFriends(null);
    }


    /**
     * @param page      page number
     * @param size      size count
     * @param order    string orders
     * @return PageResponse<User>
     */
    @GetMapping()
    public ResponseEntity<PageResponse<User>> getSearchCriteriaPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email) {

        PageResponse<User> response = new PageResponse<>();

        Pageable pageable = filterBuilderService.getPageable(size, page, order);
        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        String filter = "";

        if (name != null){
            filter += "name|like|" + name + "&";
        }

        if (email != null){
            filter += "email|like|" + email + "&";
        }

        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filter);

        Query query = filterCriteriaBuilder.addCondition(andConditions, null);
        Page<User> pg = userService.getPage(query, pageable);
        for (User user : pg) {
            cleanUserFields(user);
        }
        response.setPageStats(pg, pg.getContent());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == principal or @friendshipServiceImpl.areFriends(#id, principal)")
    @Operation(
            operationId = "getOneUser",
            summary = "Get a single user details",
            description = "Get the details for a given user. To see the user details " +
                    "you must be the requested user, his friend, or have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
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
    })
    ResponseEntity<User> get(@PathVariable("id") String id) {
        Optional<User> user;
        try {
            user = userService.get(id);
            if (user.isPresent()){
                Link self = linkTo(methodOn(UserController.class).get(id)).withSelfRel();
                Link all = linkTo(methodOn(UserController.class).getSearchCriteriaPage(0, 20, null, null, null)).withRel(IanaLinkRelations.NEXT);
                return ResponseEntity.ok()
                        .headers(new HttpHeaders(){{
                            add(HttpHeaders.LINK, self.toString());
                            add(HttpHeaders.LINK, all.toString());
                        }})
                        .body(user.get());
            }
            else {
                throw new NotFoundException("User not found.");
            }
        } catch (Exception e){
            return ResponseEntity.of(userService.get(id));
        }
    }

    @GetMapping(path = "user/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponse<User>> getByName(@PathVariable("name") String name,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "20") int size,
                                   @RequestParam(value = "order", required = false) String order) {

        PageResponse<User> response = new PageResponse<>();

        Pageable pageable = filterBuilderService.getPageable(size, page, order);
        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        String filterAnd = "name|eq|" + name;
        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filterAnd);

        Query query = filterCriteriaBuilder.addCondition(andConditions, null);

        Page<User> pg = userService.getPage(query, pageable);
        for (User user : pg) {
            cleanUserFields(user);
        }
        response.setPageStats(pg, pg.getContent());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * @param filterOr  string filter or conditions
     * @param filterAnd string filter and conditions
     * @return list of User
     */

    @GetMapping(value = "/notPaged")
    public ResponseEntity<List<User>> getAllSearchCriteria(
            @RequestParam(value = "filterOr", required = false) String filterOr,
            @RequestParam(value = "filterAnd", required = false) String filterAnd) {

        GenericFilterCriteriaBuilder filterCriteriaBuilder = new GenericFilterCriteriaBuilder();

        List<FilterCondition> andConditions = filterBuilderService.createFilterCondition(filterAnd);
        List<FilterCondition> orConditions = filterBuilderService.createFilterCondition(filterOr);

        Query query = filterCriteriaBuilder.addCondition(andConditions, orConditions);
        List<User> users = userService.getAll(query);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Object> addUser(@RequestBody User user){
        System.out.println(user.getBirthday());
        User response = userService.postUser(user);
        if (response == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            String message = "";

            if(user.getName() == null || user.getName().isEmpty()) {
                message = "Name is mandatory to POST.";
            } else if(user.getEmail() == null || user.getEmail().isEmpty()) {
                message = "Email is mandatory to POST.";
            } else if(user.getBirthday() == null) {
                message = "Birthday is mandatory to POST.";
            }

            body.put("message", message);
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{email}")
                .buildAndExpand(response.getEmail()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addUser(@PathVariable String email, @RequestBody User user){
        user.setEmail(email);
        User response = userService.postUser(user);
        if (response == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            String message = "";

            if(user.getName() == null || user.getName().isEmpty()) {
                message = "Name is mandatory to POST.";
            } else if(user.getEmail() == null || user.getEmail().isEmpty()) {
                message = "Email is mandatory to POST.";
            } else if(user.getBirthday() == null) {
                message = "Birthday is mandatory to POST.";
            } else {
                message = "User already exists.";
            }

            body.put("message", message);
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{email}")
                .buildAndExpand(response.getEmail()).toUri();
        return ResponseEntity.created(location).body(response);
    }





    @PutMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> modifyUser(@PathVariable String id, @RequestBody User user){
        User updatedUser = userService.putUser(id, user);
        if (updatedUser == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", "Modifying email or birthday is not allowed, or user not found.");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(updatedUser);
        }
    }

    @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        boolean isRemoved = userService.deleteUserById(id);

        if (!isRemoved) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "{email}/friends", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addFriend(@PathVariable String email, @RequestBody User friend) {
        User updatedUser = userService.addFriend(email, friend);
        if (updatedUser == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", "User not found or friend could not be added.");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(updatedUser);
        }
    }

    @DeleteMapping(path = "{email}/friends/{friendEmail}")
    public ResponseEntity<Object> removeFriend(@PathVariable String email, @PathVariable String friendEmail) {
        boolean isRemoved = userService.removeFriend(email, friendEmail);
        if (isRemoved) {
            return ResponseEntity.noContent().build();
        } else {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", "Friend not found or could not be removed.");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
    }



}