package com.example.client.management.configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.client.management.dto.LoginResponseDto;
import com.example.client.management.models.UserModel;

@Service
public class TokenService {

	private String secret = "secret";

	public LoginResponseDto generateToken(UserModel userModel) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Date expiresAt = getExpirationDate();
            String token = JWT.create().withIssuer("auth").withSubject(userModel.getEmail())
                    .withExpiresAt(expiresAt).sign(algorithm);
            return new LoginResponseDto(token, expiresAt.getTime());
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }


	public String validateToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);

			return JWT.require(algorithm).withIssuer("auth").build().verify(token).getSubject();
		}

		catch (JWTVerificationException exception) {
			return "";
		}
	}

	private Date getExpirationDate() {
		return Date.from(LocalDateTime.now().plusMinutes(1).atZone(ZoneId.systemDefault()).toInstant());
	}
}