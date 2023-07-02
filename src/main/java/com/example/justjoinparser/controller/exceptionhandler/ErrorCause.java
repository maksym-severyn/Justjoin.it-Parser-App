package com.example.justjoinparser.controller.exceptionhandler;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorCause {
    private List<String> cause;
}
