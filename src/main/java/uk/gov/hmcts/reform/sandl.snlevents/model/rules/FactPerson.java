package uk.gov.hmcts.reform.sandl.snlevents.model.rules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactPerson implements Serializable {
    private String id;
    private String name;
}
