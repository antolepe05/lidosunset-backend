package lidosunset.model.dto;

import lombok.Data;

// DTO per la richiesta di registrazione. Contiene i dati inviati dal client nel body della POST /api/auth/register.
@Data
public class RegisterRequest {

    private String email;
    private String username;
    private String password;

    // Necessari per creare anche l'anagrafica Cliente collegata all'account
    private String nome;
    private String cognome;
    private String telefono;
}