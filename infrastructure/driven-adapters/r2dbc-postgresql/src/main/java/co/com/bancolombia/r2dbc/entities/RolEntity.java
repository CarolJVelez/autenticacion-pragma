package co.com.bancolombia.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("roles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RolEntity {

    @Id
    @Column("role_id")
    private Long id;
    @Column("name")
    private String name;
    @Column("description")
    private String description;
}
