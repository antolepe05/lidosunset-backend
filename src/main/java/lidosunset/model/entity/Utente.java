package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity //Specifica che è una classe nel DB
@Table(name="utente") //Specifica il nome della tabella nel DB
@Data     //Genera automaticamente getter, setter, toString, equals e hascCode
@Builder    // Permette di creare oggetti con il pattern Builder
@NoArgsConstructor       //Genera il costruttore senza parametri, obbligatorio per JPA.
@AllArgsConstructor      //Genera il costruttore con tutti i parametri
public class Utente implements UserDetails {

    @Id //Indica la chiave primaria della tabella
    @GeneratedValue(strategy = GenerationType.IDENTITY)      // Il valore viene generato automaticamente (auto-incrementa)
    private Long id;

    // Si usa unique=true in modo che non possano esserci due utenti con lo stesso user
    // e il campo non può essere null
    @Column(unique = true, nullable = false)
    private String username;

    // Si usa unique=true in modo che non possano esserci due utenti con la stessa email
    // e il campo non può essere null
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Enumerated(EnumType.STRING) salva il valore dell'enum come stringa nel DB.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
