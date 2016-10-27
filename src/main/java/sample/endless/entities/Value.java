package sample.endless.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import static javax.persistence.GenerationType.AUTO;

@Entity
@Table()
public class Value {

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;

    @Lob
    private byte[] value;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(final byte[] value) {
        this.value = value;
    }
}
