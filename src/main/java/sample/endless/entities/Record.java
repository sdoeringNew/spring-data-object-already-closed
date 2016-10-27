package sample.endless.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import static javax.persistence.GenerationType.AUTO;

@Entity
@Table()
public class Record {

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;

    @OneToMany(targetEntity = WrappedValue.class, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "record", fetch = FetchType.LAZY)
    private Set<WrappedValue> wrappedValues;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Set<WrappedValue> getWrappedValues() {
        return wrappedValues;
    }

    public void setWrappedValues(final Set<WrappedValue> wrappedValues) {
        this.wrappedValues = wrappedValues;
    }
}
