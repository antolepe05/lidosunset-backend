package lidosunset.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// Gestione centralizzata delle eccezioni per tutta l'applicazion

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Intercetta il fallimento del lock ottimistico
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLockingFailura(ObjectOptimisticLockingFailureException exception){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "L'attrezzatura è stata appena modificata o prenotata da un altro utente. Riprova."));
    }

    // Intercetta l'eccezione quando un'attrezzatura risulta già occupata nella fascia oraria richiesta
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }


     // Intercetta gli errori di validazione dei DTO (annotati con @Valid).
     // Restituisce una mappa con i campi che hanno fallito la validazione e i relativi messaggi d'errore.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Cattura qualsiasi altra eccezione non gestita esplicitamente sopra,
    // evitando che si propaghi come errore generico poco chiaro al frontend.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception exception){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server: " + exception.getMessage()));
    }
}
