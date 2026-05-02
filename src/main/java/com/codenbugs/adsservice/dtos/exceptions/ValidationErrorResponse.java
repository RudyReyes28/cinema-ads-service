package com.codenbugs.adsservice.dtos.exceptions;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder
public class ValidationErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp;

    Integer status;

    String error;

    String message;

    String path;

    Map<String, String> fieldErrors;
}