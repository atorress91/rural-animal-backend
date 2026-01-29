package com.project.demo.logic.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormatterUtil {

    public static String formatDate(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}