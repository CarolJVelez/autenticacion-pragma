package co.com.bancolombia.model.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Login {

    private String email;
    private String password;

}
