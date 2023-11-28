package gal.usc.etse.grei.es.controller;


import gal.usc.etse.grei.es.controller.dto.FilterCondition;
import gal.usc.etse.grei.es.domain.User;
import gal.usc.etse.grei.es.exception.NotFoundException;
import gal.usc.etse.grei.es.repository.support.GenericFilterCriteriaBuilder;
import gal.usc.etse.grei.es.service.UserService;
import gal.usc.etse.grei.es.service.FilterBuilderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/users")
@Tag(name = "User API", description = "User related operations")
@SecurityRequirement(name = "JWT")
public class UserController {

    private final UserService userService;
    private final FilterBuilderService filterBuilderService;
    private final LinkRelationProvider linkRelationProvider;


    public UserController(UserService userService, FilterBuilderService filterBuilderService, LinkRelationProvider linkRelationProvider) {
        this.userService = userService;
        this.filterBuilderService = filterBuilderService;
        this.linkRelationProvider = linkRelationProvider;
    }

    private void cleanUserFields(User user) {
        user.setEmail(null);
        //user.setFriends(null);
    }


    /**
     * @param page      page number
     * @param size      size count
     * @param order     string order
     * @param name      string name
     * @param email     string email
     * @return PageResponse<User>
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAllUsers",
            summary = "Get a list of all users",
            description = "Get a paginated list of all users. Any logged user can access this list."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
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
    public ResponseEntity<PageResponse<User>> getAllUsers(
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

        Link self = linkTo(methodOn(UserController.class).getAllUsers(page, size, order, name, email)).withSelfRel();
        Link first = linkTo(methodOn(UserController.class).getAllUsers(0, size, order, name, email)).withRel(IanaLinkRelations.FIRST);
        Link next = linkTo(methodOn(UserController.class).getAllUsers(page + 1, size, order, name, email)).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo(methodOn(UserController.class).getAllUsers(page - 1, size, order, name, email)).withRel(IanaLinkRelations.PREVIOUS);
        Link last = linkTo(methodOn(UserController.class).getAllUsers(pg.getTotalPages() - 1, size, order, name, email)).withRel(IanaLinkRelations.LAST);
        Link resource = linkTo(methodOn(UserController.class).get(null)).withRel(linkRelationProvider.getItemResourceRelFor(User.class));

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




    /**
     * @param id      string email
     * @return User
     */
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
                    description = "User details",
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
                Link all = linkTo(methodOn(UserController.class).getAllUsers(0, 20, null, null, null)).withRel(IanaLinkRelations.NEXT);
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



    /**
     * @return User
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "createUser",
            summary = "Create a new user",
            description = "Create a new user. Any user can create a user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User already exists.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User fields not valid or missing",
                    content = @Content
            ),
    })
    public ResponseEntity<Object> addUser(@RequestBody User user){
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

        Link self = linkTo(methodOn(UserController.class).addUser(user)).withSelfRel();
        Link all = linkTo(methodOn(UserController.class). getAllUsers(0, 10, null, null, null)).withRel(IanaLinkRelations.NEXT);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{email}")
                .buildAndExpand(response.getEmail()).toUri();

        return ResponseEntity.created(location)
                .headers(new HttpHeaders(){{
                    add(HttpHeaders.LINK, self.toString());
                    add(HttpHeaders.LINK, all.toString());
                }})
                .body(response);
    }

    /**
     * @param email      string email
     * @return User
     */
    @PostMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "createUser",
            summary = "Create a new user",
            description = "Create a new user. Any user can create a user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User already exists.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User fields not valid or missing",
                    content = @Content
            ),
    })
    public ResponseEntity<Object> addUser(@PathVariable String email, @RequestBody User user){
        user.setEmail(email);
        User response = userService.postUser(user);
        if (response == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            String message;

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

        Link self = linkTo(methodOn(UserController.class).addUser(user)).withSelfRel();
        Link all = linkTo(methodOn(UserController.class).getAllUsers(0, 10, null, null, null)).withRel(IanaLinkRelations.NEXT);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{email}")
                .buildAndExpand(response.getEmail()).toUri();

        return ResponseEntity.created(location)
                .headers(new HttpHeaders(){{
                    add(HttpHeaders.LINK, self.toString());
                    add(HttpHeaders.LINK, all.toString());
                }})
                .body(response);
    }




    /**
     * @param id      string email
     * @return User
     */
    @PutMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#id == principal")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified user details.",
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
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad body",
                    content = @Content
            ),
    })
    public ResponseEntity<Object> modifyUser(@PathVariable String id, @RequestBody User user){
        User updatedUser = userService.putUser(id, user);
        if (updatedUser == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", "Modifying email or birthday is not allowed, or user not found.");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else {

            Link self = linkTo(methodOn(UserController.class).addUser(user)).withSelfRel();
            Link all = linkTo(methodOn(UserController.class).getAllUsers(0, 10, null, null, null)).withRel(IanaLinkRelations.NEXT);

            return ResponseEntity.ok()
                    .headers(new HttpHeaders(){{
                        add(HttpHeaders.LINK, self.toString());
                        add(HttpHeaders.LINK, all.toString());
                    }})
                    .body(updatedUser);
        }
    }


    /**
     * @param id      string email
     */
    @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#id == principal")
    @Operation(
            operationId = "deleteUser",
            summary = "Delete a user",
            description = "Delete an existing user. To delete a user you must be the requested user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted",
                    content = @Content
            )
    })
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        boolean isRemoved = userService.deleteUserById(id);

        Link all = linkTo(methodOn(UserController.class).getAllUsers(0, 10, null, null, null)).withRel(IanaLinkRelations.NEXT);

        if (!isRemoved) {
            return ResponseEntity.notFound().header(HttpHeaders.LINK, all.toString()).build();
        }

        return ResponseEntity.noContent().header(HttpHeaders.LINK, all.toString()).build();
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