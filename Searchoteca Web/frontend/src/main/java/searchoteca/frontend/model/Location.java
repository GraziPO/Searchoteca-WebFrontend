package searchoteca.frontend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location {
    private Long id;

    private String localCode;
    private String localName;
    private String localDesc;
    private String departCode;

    public Location() {}
}
