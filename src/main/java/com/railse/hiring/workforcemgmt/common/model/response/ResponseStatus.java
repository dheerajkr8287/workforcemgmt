package com.railse.hiring.workforcemgmt.common.model.response;

import lombok.Data;

@Data
public class ResponseStatus {
    private int code;
    private String message;

    public ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}