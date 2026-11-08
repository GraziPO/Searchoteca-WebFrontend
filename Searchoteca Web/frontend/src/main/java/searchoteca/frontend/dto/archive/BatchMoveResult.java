package searchoteca.frontend.dto.archive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BatchMoveResult(String localCode, int moved, List<CopyLabel> labels, List<String> alreadyInLocation) {}
