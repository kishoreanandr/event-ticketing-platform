package com.eventticketing.event.config;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignJwtInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {

        ServletRequestAttributes attributes=
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes==null){
            return;
        }

        HttpServletRequest request=attributes.getRequest();

        String authorization=request.getHeader("Authorization");

        if(authorization!=null && authorization.startsWith("Bearer ")){
            requestTemplate.header("Authorization",authorization);
        }
    }
}
