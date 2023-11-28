package gal.usc.etse.grei.es.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/login")
@CrossOrigin
@Tag(name = "Authentication API", description = "Authentication operations")
public class AuthController {
    @PostMapping()
    @Operation(
            operationId = "login",
            summary = "Login",
            description = "Login with username and password to obtain a JWT token."
    )
    public void login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User and password for authentication",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"email\": \"test@test.com\", \"password\": \"test\"}"
                            )
                    )
            )
            @RequestBody Map<String, String> userpass
    ) {}
}