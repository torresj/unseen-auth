package com.torresj.unseenauth.config;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class CustomErrorAttributesConfig extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        Throwable error = super.getError(webRequest);
        try{
            errorAttributes.put("reason",((ResponseStatusException)error).getReason());
        }catch(Exception e){
            log.warn("Exception is not ResponseStatusException");
        }

        return errorAttributes;
    }
}