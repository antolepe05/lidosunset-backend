package lidosunset.model.dto;
import lombok.Data;

// DTO per la richiesta di login. Contiene le credenziali inviate dal client nel body della POST /api/auth/login
@Data
public class LoginRequest {

    private String username;
    private String password;
}
