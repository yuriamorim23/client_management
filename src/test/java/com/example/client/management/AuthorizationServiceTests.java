package com.example.client.management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.client.management.configuration.TokenService;
import com.example.client.management.dto.AuthenticationDto;
import com.example.client.management.dto.LoginResponseDto;
import com.example.client.management.dto.RegisterDto;
import com.example.client.management.enums.UserRole;
import com.example.client.management.models.UserModel;
import com.example.client.management.repository.UserRepository;
import com.example.client.management.service.AuthorizationService;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private ApplicationContext context;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    public void loadUserByUsername_UserExists_ReturnsUser() {
        String email = "test@example.com";
        UserModel mockUser = new UserModel(email, "password", UserRole.USER);
        mockUser.setCreatedAt(new Date());
        when(userRepository.findByEmail(email)).thenReturn(mockUser);

        UserDetails returnedUser = authorizationService.loadUserByUsername(email);

        assertEquals(email, returnedUser.getUsername());
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void loadUserByUsername_UserNotFound_ThrowsException() {
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            authorizationService.loadUserByUsername(email);
        });
    }

    @Test
    public void login_ValidCredentials_ReturnsToken() {
        AuthenticationDto dto = new AuthenticationDto("test@example.com", "password");
        UserModel user = new UserModel("test@example.com", "password", UserRole.USER);
        user.setCreatedAt(new Date());
        String fakeToken = "fakeToken123";

        when(userRepository.findByEmail(dto.email())).thenReturn(user);
        when(context.getBean(AuthenticationManager.class)).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(tokenService.generateToken(user)).thenReturn(fakeToken);

        ResponseEntity<Object> response = authorizationService.login(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof LoginResponseDto);
        assertEquals(fakeToken, ((LoginResponseDto) response.getBody()).token());
    }

    @Test
    public void login_InvalidEmail_ReturnsEmailNotExist() {
        AuthenticationDto dto = new AuthenticationDto("wrong@example.com", "password");

        when(userRepository.findByEmail(dto.email())).thenReturn(null);

        ResponseEntity<Object> response = authorizationService.login(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Error: Email does not exist.", response.getBody());
    }

    @Test
    public void login_InvalidPassword_ReturnsUnauthorized() {
        AuthenticationDto dto = new AuthenticationDto("test@example.com", "wrongPassword");
        UserModel user = new UserModel("test@example.com", "password", UserRole.USER);

        when(userRepository.findByEmail(dto.email())).thenReturn(user);
        when(context.getBean(AuthenticationManager.class)).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<Object> response = authorizationService.login(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Error: password is wrong.", response.getBody());
    }

    @Test
    public void register_ValidData_ReturnsOk() {
        RegisterDto dto = new RegisterDto("new@example.com", "password", UserRole.USER);
        String token = "Bearer validToken123";

        when(tokenService.validateToken("validToken123")).thenReturn("email@example.com");
        when(userRepository.findByEmail(dto.email())).thenReturn(null);
        when(userRepository.save(any(UserModel.class))).thenReturn(new UserModel());

        ResponseEntity<Object> response = authorizationService.register(dto, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void register_UserExists_ReturnsBadRequest() {
        RegisterDto dto = new RegisterDto("existing@example.com", "password", UserRole.USER);
        String token = "Bearer validToken123";
        UserModel existingUser = new UserModel(dto.email(), "password", UserRole.USER);
        existingUser.setCreatedAt(new Date());

        when(tokenService.validateToken("validToken123")).thenReturn("email@example.com");
        when(userRepository.findByEmail(dto.email())).thenReturn(existingUser);

        ResponseEntity<Object> response = authorizationService.register(dto, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
    }
}