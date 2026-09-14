package searchoteca.frontend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Department {
    private Long id;

    private String departCode;
    private String departName;
    private String departDesc;

    public Department() {}
}
