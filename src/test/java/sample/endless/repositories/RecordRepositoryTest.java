package sample.endless.repositories;

import sample.endless.entities.Record;
import sample.endless.entities.Value;
import sample.endless.entities.WrappedValue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class RecordRepositoryTest {

    static final int NUMBER_OF_RECORDS = 100000;

    @Autowired
    RecordRepository recordRepository;

    @Test
    @Transactional
    public void readAll_the_object_is_already_closed() {
        // setup
        System.out.println("Prepare all data.");
        prepareAllData();

        // when
        System.out.println("Do the test.");
        final AtomicLong counter = new AtomicLong();

        // -- SHOULD NOT FAIL
        try (final Stream<Record> stream = recordRepository.readAllByIdNotNull()) {
            stream.forEach(record -> {
                if (record.getId() % 1000L == 0L) {
                    System.out.println("Handling Record No: " + record.getId());
                }
                final long sum = record.getWrappedValues().stream()
                    .mapToLong(wrapped -> Long.valueOf(new String(wrapped.getValue().getValue())))
                    .sum();
                counter.addAndGet(sum);
            });
        }

        // then
        Assert.assertThat(15L * NUMBER_OF_RECORDS, is(counter.get()));
    }

    private void prepareAllData() {
        final List<Record> records = IntStream.rangeClosed(1, NUMBER_OF_RECORDS).mapToObj(ignored -> {
            final Record record = new Record();
            final Set<WrappedValue> wrappedValues = IntStream.rangeClosed(1, 5).mapToObj(it -> {
                final Value value = new Value();
                value.setValue(String.valueOf(it).getBytes());
                final WrappedValue wrappedValue = new WrappedValue();
                wrappedValue.setRecord(record);
                wrappedValue.setValue(value);
                return wrappedValue;
            }).collect(Collectors.toSet());
            record.setWrappedValues(wrappedValues);
            return record;
        }).collect(Collectors.toList());
        recordRepository.save(records);
    }
}
