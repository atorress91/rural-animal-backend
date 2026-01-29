package com.project.demo.rest.user;

import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.user.UserService;
import com.project.demo.logic.utils.EmailService;

import com.project.demo.logic.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserRestController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblDirectionRepository tblDirectionRepository;

    @Autowired
    private TblRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<TblUser> usersPage = userRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(usersPage.getTotalPages());
        meta.setTotalElements(usersPage.getTotalElements());
        meta.setPageNumber(usersPage.getNumber() + 1);
        meta.setPageSize(usersPage.getSize());

        return new GlobalResponseHandler().handleResponse("User retrieved successfully",
                usersPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody TblUser user, HttpServletRequest request) {
        // Validar nombre
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre no puede estar vacío o ser nulo.");
        }

        // Validar primer apellido
        if (user.getLastName1() == null || user.getLastName1().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El primer apellido no puede estar vacío o ser nulo.");
        }

        // Validar email
        if (!isValidEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("El correo ingresado no tiene un formato válido.");
        }

        // Ver si email existe
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este correo ya existe");
        }

        // Validar contraseña
        if (!isValidPassword(user.getPassword())) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres, incluir una letra mayúscula, " +
                    "una letra minúscula, un número y un carácter especial.");
        }

        // Validar edad
        if (!isValidAge(user.getBirthDate())) {
            return ResponseEntity.badRequest().body("Debes ser mayor de 18 años y menor de 100 años para registrarte.");
        }

        // Validar y formatear la identificación costarricense
        if (user.getIdentification() == null || !user.getIdentification().matches("^[1-7][0-9]{8}$")) {
            return ResponseEntity.badRequest().body("La identificación debe iniciar con un número entre 1 y 7 y tener 9 dígitos.");
        }

        // Validación del teléfono
        if (user.getPhoneNumber() == null || !user.getPhoneNumber().matches("^[2,4,6,7,8]\\d{7}$")) {
            return ResponseEntity.badRequest().body("El número de teléfono debe tener exactamente 8 dígitos y contar con formato costarricense.");
        }


        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Validar rol
        Optional<TblRole> optionalRole = roleRepository.findByTitle(user.getRole().getTitle());
        if (optionalRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found");
        }
        user.setRole(optionalRole.get());

        // Verifica que la dirección no sea nula
        if (user.getDirection() != null) {
            // Crea la dirección
            TblDirection tblDirection = new TblDirection();
            tblDirection.setDistrict(user.getDirection().getDistrict());
            tblDirection.setCanton(user.getDirection().getCanton());
            tblDirection.setProvince(user.getDirection().getProvince());
            tblDirection.setOtherDetails(user.getDirection().getOtherDetails());

            // Guarda la dirección en la base de datos
            TblDirection newDirection = tblDirectionRepository.save(tblDirection);
            user.setDirection(newDirection); // Asigna la dirección guardada al usuario
        } else {
            // Maneja el caso donde la dirección es nula si es necesario
            return ResponseEntity.badRequest().body("La dirección no puede ser nula.");
        }

        userRepository.save(user);

        return new GlobalResponseHandler().handleResponse("User added successfully",
                user, HttpStatus.OK, request);
    }


    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody TblUser user, HttpServletRequest request) {
        Optional<TblUser> foundOrder = userRepository.findById(userId);
        if(foundOrder.isPresent()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return new GlobalResponseHandler().handleResponse("TblUser updated successfully",
                    user, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("TblUser id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<?> patchUser(@PathVariable Long userId, @RequestBody TblUser user, HttpServletRequest request) {
        Optional<TblUser> foundUser = userRepository.findById(userId);

        if (foundUser.isPresent()) {
            TblUser existingUser = foundUser.get();

            // Validación de nombre
            if (user.getName() != null && user.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre no puede estar vacío.");
            }

            // Actualización del nombre
            if (user.getName() != null) {
                existingUser.setName(user.getName());
            }

            // Actualización del estado
            if (user.getState() != null) {
                existingUser.setState(user.getState());
            }

            // Validación de primer apellido
            if (user.getLastName1() != null && user.getLastName1().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El primer apellido no puede estar vacío.");
            }

            // Actualización de primer apellido
            if (user.getLastName1() != null) {
                existingUser.setLastName1(user.getLastName1());
            }

            // Actualización de segundo apellido
            if (user.getLastName2() != null) {
                existingUser.setLastName2(user.getLastName2());
            }

            // Actualización de vco
            if (user.getVco() != null) {
                existingUser.setVco(user.getVco());
            }

            // Validación y actualización del correo
            if (user.getEmail() != null) {
                if (!isValidEmail(user.getEmail())) {
                    return ResponseEntity.badRequest().body("El correo ingresado no tiene un formato válido.");
                }
                existingUser.setEmail(user.getEmail());
            }

            // Validación y actualización de la contraseña
            if (user.getPassword() != null) {
                if (!isValidPassword(user.getPassword())) {
                    return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres, incluir una letra mayúscula, " +
                            "una letra minúscula, un número y un carácter especial.");
                }
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            // Actualización del número de teléfono
            if (user.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(user.getPhoneNumber());
            }

            // Actualización de la identificación
            if (user.getIdentification() != null) {
                if (!user.getIdentification().matches("^[1-7][0-9]{8}$")) {
                    return ResponseEntity.badRequest().body("La identificación debe iniciar con un número entre 1 y 7 y tener 9 dígitos.");
                }
                existingUser.setIdentification(user.getIdentification());
            }

            // Validación de la fecha de nacimiento
            if (user.getBirthDate() != null) {
                if (!isValidAge(user.getBirthDate())) {
                    return ResponseEntity.badRequest().body("Debes ser mayor de 18 años y menor de 100 años para registrarte.");
                }
                existingUser.setBirthDate(user.getBirthDate());
            }

            // Actualización del número de teléfono
            if (user.getPhoneNumber() != null) {
                if (!user.getPhoneNumber().matches("^[2,4,6,7,8]\\d{7}$")) {
                    return ResponseEntity.badRequest().body("El número de teléfono debe tener exactamente 8 dígitos y contar con formato costarricense");
                }
                existingUser.setPhoneNumber(user.getPhoneNumber());
            }


            // Actualización de la dirección
            if (user.getDirection() != null) {
                TblDirection updatedDirection = user.getDirection();
                TblDirection currentDirection = existingUser.getDirection();

                if (currentDirection != null) {
                    // Solo actualizamos si los campos han cambiado
                    if (!currentDirection.equals(updatedDirection)) {
                        currentDirection.setDistrict(updatedDirection.getDistrict());
                        currentDirection.setCanton(updatedDirection.getCanton());
                        currentDirection.setProvince(updatedDirection.getProvince());
                        currentDirection.setOtherDetails(updatedDirection.getOtherDetails());
                        tblDirectionRepository.save(currentDirection);
                    }
                } else {
                    // Si no hay dirección, crear una nueva
                    TblDirection tblDirection = new TblDirection();
                    tblDirection.setDistrict(updatedDirection.getDistrict());
                    tblDirection.setCanton(updatedDirection.getCanton());
                    tblDirection.setProvince(updatedDirection.getProvince());
                    tblDirection.setOtherDetails(updatedDirection.getOtherDetails());
                    TblDirection savedDirection = tblDirectionRepository.save(tblDirection);
                    existingUser.setDirection(savedDirection);
                }
            }

            // Guardamos el usuario actualizado
            userRepository.save(existingUser);
            return new GlobalResponseHandler().handleResponse("Información actualizada con éxito", existingUser, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("El usuario con ID " + userId + " no se encontró", HttpStatus.NOT_FOUND, request);
        }
    }


    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        Optional<TblUser> foundOrder = userRepository.findById(userId);
        if(foundOrder.isPresent()) {
            userRepository.deleteById(userId);
            return new GlobalResponseHandler().handleResponse("TblUser deleted successfully",
                    foundOrder.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Order id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateEmail(@RequestBody TblUser user, HttpServletRequest request) {
        try {
            Optional<TblUser> foundUser = userRepository.findByEmail(user.getEmail());

            if (foundUser.isPresent()) {
                emailService.authenticateEmail(foundUser.get());

                return new GlobalResponseHandler().handleResponse("Correo enviado satisfactoriamente",
                        foundUser, HttpStatus.OK, request);
            } else {
                return new GlobalResponseHandler().handleResponse("Correo electrónico invalido",
                        HttpStatus.NOT_FOUND, request);
            }
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Error: " + e.getMessage(),
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/emailConfirm")
    public ResponseEntity<?> EmailConfirm(@RequestBody long id, HttpServletRequest request) {
        try {
            Optional<TblUser> foundUser = userRepository.findById(id);

            if (foundUser.isPresent()) {
                TblUser user = foundUser.get();
                user.setState("Active");
                userRepository.save(user);

                return new GlobalResponseHandler().handleResponse("Perfil verificado satisfactoriamente",
                        foundUser, HttpStatus.OK, request);
            } else {
                return new GlobalResponseHandler().handleResponse("Id de usuario " + id + " no encontrado",
                        HttpStatus.NOT_FOUND, request);
            }
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Error: " + e.getMessage(),
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody String userEmail, HttpServletRequest request) {
        try {
            Optional<TblUser> foundUser = userRepository.findByEmail(userEmail);

            if (foundUser.isPresent()) {
                TblUser user = foundUser.get();
                String tempPassword = PasswordUtil.generateTemporalPassword();
                user.setPassword(passwordEncoder.encode(tempPassword));
                userRepository.save(user);

                emailService.restorePassword(user, tempPassword);

                return new GlobalResponseHandler().handleResponse("Correo enviado satisfactoriamente",
                        foundUser, HttpStatus.OK, request);
            } else {
                return new GlobalResponseHandler().handleResponse("Correo de usuario invalido",
                        HttpStatus.NOT_FOUND, request);
            }
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Error: " + e.getMessage(),
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public TblUser authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (TblUser) authentication.getPrincipal();
    }

    /**
     * Endpoint para filtrar usuarios por palabra clave
     *
     * @param keyword Palabra clave
     * @return Lista de usuarios filtrados
     */
    @GetMapping("/filter")
    public ResponseEntity<?> filterUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<TblUser> usersPage = userRepository.findUsersByKeyword(keyword, pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(usersPage.getTotalPages());
        meta.setTotalElements(usersPage.getTotalElements());
        meta.setPageNumber(usersPage.getNumber() + 1);
        meta.setPageSize(usersPage.getSize());

        return new GlobalResponseHandler().handleResponse("User retrieved successfully",
                usersPage.getContent(), HttpStatus.OK, meta);
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^(?!\\.|-)[a-zA-Z0-9._-]+@[a-zA-Z0-9]+(\\.[a-zA-Z]{2,6})+$";
        return email != null && email.matches(emailRegex);
    }

    public boolean isValidPassword(String password) {
        // Expresión regular para validar la contraseña
        String passwordRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
        return password != null && password.matches(passwordRegex);
    }

    public boolean isValidAge(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age >= 18 && age <= 99;
    }

}