package com.project.demo.rest.veterinaryAppointment;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.veterinaryAppointment.AvailabilityDto;
import com.project.demo.logic.entity.veterinaryAppointment.CreateAppointmentDto;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentDto;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentService;
import com.project.demo.logic.utils.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/veterinary_appointments")
public class VeterinaryAppointmentRestController {

    private final VeterinaryAppointmentService veterinaryAppointmentService;

    @Autowired
    private EmailService emailService;

    public VeterinaryAppointmentRestController(VeterinaryAppointmentService veterinaryAppointmentService) {
        this.veterinaryAppointmentService = veterinaryAppointmentService;
    }

    /**
     * Recupera la lista de citas de un usuario autenticado.
     * <p>
     * Este método maneja el mapeo GET para obtener una lista paginada de citas veterinarias
     * del usuario actualmente autenticado. Los resultados pueden ser paginados utilizando
     * los parámetros de consulta 'page' y 'size'.
     *
     * @param page    el número de página de la solicitud de paginación, empezando desde 1.
     *                Valor predeterminado es 1.
     * @param size    el número de elementos por página. Valor predeterminado es 10.
     * @param request la solicitud HTTP actual que contiene datos de solicitud y metadatos.
     * @return un objeto ResponseEntity que contiene la lista de citas de usuario en el cuerpo de la
     * respuesta y el estado HTTP correspondiente. Si las citas se obtienen
     * con éxito, se devuelve un código de estado HTTP 200 (OK). Si ocurre un error,
     * se devuelve un código de estado HTTP adecuado (por ejemplo, 404 o 500) junto con un
     * mensaje de error específico.
     */
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserAppointments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            TblUser user = (TblUser) authentication.getPrincipal();
            Page<VeterinaryAppointmentDto> appointments = veterinaryAppointmentService.getUserAppointments(user.getId(), PageRequest.of(page - 1, size));

            Meta meta = new Meta(
                    request.getMethod(),
                    request.getRequestURL().toString()
            );

            return new GlobalResponseHandler().handleResponse(
                    "Appointments retrieved successfully",
                    appointments.getContent(),
                    HttpStatus.OK,
                    meta
            );

        } catch (ResponseStatusException e) {
            return new GlobalResponseHandler().handleResponse(
                    e.getMessage(),
                    null,
                    HttpStatus.NOT_FOUND,
                    new Meta(request.getMethod(), request.getRequestURL().toString())
            );
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse(
                    "Error retrieving appointments",
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new Meta(request.getMethod(), request.getRequestURL().toString())
            );
        }
    }

    /**
     * Obtiene las fechas disponibles para citas veterinarias dentro de un rango de fechas especificado.
     *
     * @param startDate fecha y hora de inicio del rango a consultar, en formato ISO DATE_TIME.
     * @param endDate   fecha y hora de fin del rango a consultar, en formato ISO DATE_TIME.
     * @param request   objeto HttpServletRequest que contiene detalles de la solicitud HTTP actual.
     * @return ResponseEntity con la lista de fechas disponibles para citas y un código de estado HTTP.
     */
    @GetMapping("/availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAvailableDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request
    ) {
        try {

            List<AvailabilityDto> availabilities = veterinaryAppointmentService.getAvailableDates(startDate, endDate);

            Meta meta = new Meta(
                    request.getMethod(),
                    request.getRequestURL().toString()
            );

            return new GlobalResponseHandler().handleResponse(
                    "Available dates retrieved successfully",
                    availabilities,
                    HttpStatus.OK,
                    meta
            );

        } catch (ResponseStatusException e) {
            return new GlobalResponseHandler().handleResponse(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND,
                    request
            );
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse(
                    "Error retrieving available dates",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    request
            );
        }
    }

    /**
     * Crea una nueva cita veterinaria para el usuario autenticado.
     *
     * @param appointmentDTO objeto que contiene la información necesaria para crear la cita.
     * @param request        objeto HttpServletRequest que contiene detalles de la solicitud HTTP actual.
     * @return ResponseEntity con el resultado de la operación, que incluye la cita creada y un código de estado HTTP.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createAppointment(
            @RequestBody CreateAppointmentDto appointmentDTO,
            HttpServletRequest request
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            TblUser user = (TblUser) authentication.getPrincipal();

            VeterinaryAppointmentDto createdAppointment =
                    veterinaryAppointmentService.createAppointment(appointmentDTO, user.getId());

            emailService.notificateAppointment(createdAppointment, user.getId());

            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());

            return new GlobalResponseHandler().handleResponse(
                    "Appointment created successfully",
                    createdAppointment,
                    HttpStatus.OK,
                    meta
            );

        } catch (ResponseStatusException e) {
            return new GlobalResponseHandler().handleResponse(
                    e.getMessage(),
                    null,
                    HttpStatus.BAD_REQUEST,
                    new Meta(request.getMethod(), request.getRequestURL().toString())
            );
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse(
                    "Error creating appointment: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new Meta(request.getMethod(), request.getRequestURL().toString())
            );
        }
    }

    /**
     * Recupera todas las citas veterinarias en un DTO.
     *
     * @param request objeto HttpServletRequest que contiene detalles de la solicitud HTTP actual.
     * @return ResponseEntity con la lista de todas las citas y un código de estado HTTP.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getAllAppointments(HttpServletRequest request) {
        try {
            List<VeterinaryAppointmentDto> appointments = veterinaryAppointmentService.getAll();

            Meta meta = new Meta(
                    request.getMethod(),
                    request.getRequestURL().toString()
            );

            return new GlobalResponseHandler().handleResponse(
                    "All appointments retrieved successfully",
                    appointments,
                    HttpStatus.OK,
                    meta
            );

        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse(
                    "Error retrieving all appointments",
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new Meta(request.getMethod(), request.getRequestURL().toString())
            );
        }
    }
}
