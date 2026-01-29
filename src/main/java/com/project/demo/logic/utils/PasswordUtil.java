package com.project.demo.logic.utils;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class PasswordUtil {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_+=<>?";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateTemporalPassword() {
        // Crear una lista para asegurar que al menos un carácter de cada tipo esté incluido
        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        passwordChars.add(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        passwordChars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL_CHARACTERS.charAt(RANDOM.nextInt(SPECIAL_CHARACTERS.length())));

        // Completar el resto de la contraseña con caracteres aleatorios
        String allCharacters = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
        for (int i = 4; i < 9; i++) {
            passwordChars.add(allCharacters.charAt(RANDOM.nextInt(allCharacters.length())));
        }

        // Mezclar los caracteres para mayor aleatoriedad
        Collections.shuffle(passwordChars, RANDOM);

        // Convertir la lista de caracteres en una cadena
        StringBuilder password = new StringBuilder();
        for (Character c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}