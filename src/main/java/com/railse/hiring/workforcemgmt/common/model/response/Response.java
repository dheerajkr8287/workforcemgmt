// Response.java
package com.railse.hiring.workforcemgmt.common.model.response;

import lombok.Data;

@Data
public class Response<T> {
    private T data;
    private Object metadata;
    private ResponseStatus status;

    public Response() {
        this.status = new ResponseStatus(200, "Success");
    }

    public Response(T data) {
        this.data = data;
        this.status = new ResponseStatus(200, "Success");
    }

    public Response(T data, Object metadata, ResponseStatus status) {
        this.data = data;
        this.metadata = metadata;
        this.status = status;
    }
}
