package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    @Id
    private String id;
    private String type;
    private String severity;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProblemReference> references;
}
