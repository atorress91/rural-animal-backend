package com.project.demo.rest.notificate;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.notification.NotificationRepository;
import com.project.demo.logic.entity.notification.TblNotification;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationRestController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getAllByUserId(
        @PathVariable Long userId,
        HttpServletRequest request) {
        Optional<TblUser> foundUser = userRepository.findById(userId);
        if(foundUser.isPresent()) {
            List<TblNotification> notificationsPage = notificationRepository.findActiveByUserId(userId);
            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());

            return new GlobalResponseHandler().handleResponse("Notification retrieved successfully",
                    notificationsPage, HttpStatus.OK, meta);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> addNotificationToUser(@PathVariable Long userId, @RequestBody TblNotification notification, HttpServletRequest request) {
        Optional<TblUser> foundUser = userRepository.findById(userId);
        if(foundUser.isPresent()) {
            notification.setUser(foundUser.get());
            TblNotification savedNotification = notificationRepository.save(notification);
            return new GlobalResponseHandler().handleResponse("Notification created successfully",
                    savedNotification, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<?> updateNotification(@PathVariable Long notificationId, @RequestBody TblNotification notification, HttpServletRequest request) {
        Optional<TblNotification> foundOrder = notificationRepository.findById(notificationId);
        if(foundOrder.isPresent()) {
            notification.setId(foundOrder.get().getId());
            notification.setUser(foundOrder.get().getUser());
            notificationRepository.save(notification);
            return new GlobalResponseHandler().handleResponse("Notification updated successfully",
                    notification, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Notification id " + notificationId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<?> patchNotification(@PathVariable Long notificationId, @RequestBody TblNotification notification, HttpServletRequest request) {

        Optional<TblNotification> foundNotification = notificationRepository.findById(notificationId);
        if(foundNotification.isPresent()) {
            if(notification.getState() != null) foundNotification.get().setState(notification.getState());
            if(notification.getPublication() != null) foundNotification.get().setPublication(notification.getPublication());
            if(notification.getTitle() != null) foundNotification.get().setTitle(notification.getTitle());
            if(notification.getDescription() != null) foundNotification.get().setDescription(notification.getDescription());
            notificationRepository.save(foundNotification.get());
            return new GlobalResponseHandler().handleResponse("Notification updated successfully",
                    foundNotification.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Notification id " + notificationId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId, HttpServletRequest request) {
        Optional<TblNotification> foundNotification = notificationRepository.findById(notificationId);

        if (foundNotification.isPresent()) {
            notificationRepository.deleteById(notificationId);
            return new GlobalResponseHandler().handleResponse("Notification deleted successfully",
                    HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Notification id " + notificationId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
