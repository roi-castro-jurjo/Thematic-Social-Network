package gal.usc.etse.grei.es.controller;

import gal.usc.etse.grei.es.domain.Friendship;
import gal.usc.etse.grei.es.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/friendships")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @PostMapping(path = "/{userEmail}")
    public ResponseEntity<Object> createFriendship(@PathVariable String userEmail, @RequestBody String friendEmail) {
        Friendship friendship = friendshipService.createFriendship(userEmail, friendEmail);
        if (friendship == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", "User not found, friend not found, or friendship already exists.");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(friendship);
        }
    }

    @DeleteMapping(path = "/{userEmail}")
    public ResponseEntity<Object> removeFriendship(@PathVariable String userEmail, @RequestBody String friendEmail) {
        boolean isRemoved = friendshipService.removeFriendship(userEmail, friendEmail);
        if (isRemoved) {
            return ResponseEntity.noContent().build();
        } else {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", "Friendship not found or could not be removed.");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
    }
}

