package com.nguyent.cncfapiservice.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseApi {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String statusText;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int statusNumber;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, String> errors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object data;
}
