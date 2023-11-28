package gal.usc.etse.grei.es.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ServerErrorException extends RuntimeException{
    public ServerErrorException(String msg){
        super(msg);
    }
}
