package sample.endless.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static javax.persistence.GenerationType.AUTO;

@Entity
@Table()
public class WrappedValue {

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "record_id", nullable = false, foreignKey = @ForeignKey(name = "FK_WRAPPED_1"))
    private Record record;

    @OneToOne(targetEntity = Value.class, cascade = CascadeType.ALL, optional = false, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "value_id", nullable = false, foreignKey = @ForeignKey(name = "FK_WRAPPED_2"))
    private Value value;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(final Record record) {
        this.record = record;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(final Value value) {
        this.value = value;
    }
}
