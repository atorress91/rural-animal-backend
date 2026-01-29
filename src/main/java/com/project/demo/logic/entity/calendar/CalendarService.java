package com.project.demo.logic.entity.calendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CalendarService {

    private final TokenRefreshService tokenRefreshService;
    private static final String APPLICATION_NAME = "rural-animal";
    private static final String SERVICE_ACCOUNT_KEY_FILE = "/calendar-rural-animal.json";

    @Value("${google.calendar-id}")
    private String calendarId;

    @Autowired
    public CalendarService(TokenRefreshService tokenRefreshService) {
        this.tokenRefreshService = tokenRefreshService;
    }

    /**
     * Crea un evento en el calendario especificado utilizando los detalles proporcionados en el
     * objeto EventDTO. El evento se inserta en el servicio de calendario proporcionado y se ajusta
     * a la zona horaria de "America/Costa_Rica".
     *
     * @param calendarService el servicio de calendario que se utilizará para insertar el evento.
     * @param calendarId      el identificador del calendario donde el evento será añadido.
     * @param eventDTO        un objeto que contiene los detalles del evento a ser creado, incluyendo
     *                        título, descripción, tiempo de inicio y tiempo de fin.
     * @throws IOException si ocurre un error durante la inserción del evento en el calendario.
     */
    private void createEventInCalendar(Calendar calendarService, String calendarId, EventDTO eventDTO) throws IOException {
        Event event = new Event()
                .setSummary(eventDTO.getSummary())
                .setDescription(eventDTO.getDescription());

        ZonedDateTime startZoned = eventDTO.getStartTime().atZone(ZoneId.of("America/Costa_Rica"));
        ZonedDateTime endZoned = eventDTO.getEndTime().atZone(ZoneId.of("America/Costa_Rica"));

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(startZoned.toInstant().toEpochMilli()))
                .setTimeZone("America/Costa_Rica");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(endZoned.toInstant().toEpochMilli()))
                .setTimeZone("America/Costa_Rica");
        event.setEnd(end);

        calendarService.events().insert(calendarId, event).execute();
    }

    /**
     * Crea un evento en el calendario del usuario especificado.
     *
     * @param userId   El ID del usuario para el que se desea crear el evento en su calendario.
     * @param eventDTO Un objeto EventDTO que contiene la información del evento a crear.
     * @throws RuntimeException si ocurre un error al intentar crear el evento en el calendario del usuario.
     */
    public void createEventInUserCalendar(Long userId, EventDTO eventDTO) {
        try {
            String accessToken = tokenRefreshService.getValidAccessToken(userId);

            Calendar calendarService = new Calendar.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            createEventInCalendar(calendarService, "primary", eventDTO);
        } catch (IOException e) {
            log.error("Error creating event in user calendar", e);
            throw new RuntimeException("Error creating event in user calendar", e);
        }
    }

    /**
     * Crea un evento en el calendario del sistema utilizando las credenciales de servicio de Google.
     * Procesa el evento proporcionado por el objeto EventDTO y lo inserta en el calendario especificado.
     *
     * @param eventDTO objeto que contiene los detalles del evento que se va a crear en el calendario.
     *                 Incluye la información necesaria como título, fecha, hora y descripción del evento.
     * @throws RuntimeException si ocurre un error al leer el archivo de credenciales o al establecer
     *                          la conexión con los servicios de Google Calendar.
     */
    public void createEventInSystemCalendar(EventDTO eventDTO) {
        try {
            InputStream serviceAccountStream = getClass().getResourceAsStream(SERVICE_ACCOUNT_KEY_FILE);
            if (serviceAccountStream == null) {
                throw new IOException("Service account key file not found");
            }

            GoogleCredential credential = GoogleCredential.fromStream(serviceAccountStream)
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

            Calendar calendarService = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            createEventInCalendar(calendarService, calendarId, eventDTO);
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error creating event in system calendar", e);
            throw new RuntimeException("Error creating event in system calendar", e);
        }
    }

    /**
     * Recupera una lista de eventos del calendario para un rango de fechas específico.
     *
     * @param startDate La fecha y hora de inicio del rango para recuperar los eventos.
     * @param endDate   La fecha y hora de fin del rango para recuperar los eventos.
     * @return Una lista de objetos Event que representa los eventos del calendario dentro del rango especificado.
     * @throws RuntimeException Si ocurre un error al acceder a los eventos del calendario.
     */
    public List<Event> getEvents(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            InputStream serviceAccountStream = getClass().getResourceAsStream(SERVICE_ACCOUNT_KEY_FILE);

            if (serviceAccountStream == null) {
                throw new IOException("No se encontró el archivo de clave de la Cuenta de Servicio en la ruta especificada.");
            }

            GoogleCredential credential = GoogleCredential.fromStream(serviceAccountStream)
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

            Calendar calendarService = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            DateTime timeMin = new DateTime(startDate.toString() + "Z");
            DateTime timeMax = new DateTime(endDate.toString() + "Z");

            Events events = calendarService.events().list(calendarId)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setSingleEvents(true)
                    .setOrderBy("startTime")
                    .execute();

            return events.getItems();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error al obtener los eventos del calendario", e);
            throw new RuntimeException("Error al obtener los eventos del calendario", e);
        }
    }
}

