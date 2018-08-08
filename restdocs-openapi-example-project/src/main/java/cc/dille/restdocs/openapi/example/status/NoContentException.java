package cc.dille.restdocs.openapi.example.status;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
    public class NoContentException extends RuntimeException {
}
