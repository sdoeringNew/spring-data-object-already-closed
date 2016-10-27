package sample.endless.repositories;

import sample.endless.entities.Record;
import sample.endless.entities.Value;
import sample.endless.entities.WrappedValue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

@RunWith(SpringRunner.class)
@SpringBootTest()
public class RecordRepositoryTest {

    static final int NUMBER_OF_RECORDS = 20000;

    @Autowired
    RecordRepository recordRepository;

    @Test
    public void readAll_the_object_is_already_closed() throws ExecutionException, InterruptedException {
        // setup
        System.out.println("Prepare all data.");

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

        // when
        System.out.println("Do the test.");

        final AtomicBoolean breaker = new AtomicBoolean(false);
        final BlockingDeque<Long> blockingDeque = new LinkedBlockingDeque<>(100);
        final ExecutorService creator = Executors.newSingleThreadExecutor();
        final Future<?> creatorFuture = creator.submit(() -> {
            try (final Stream<Record> stream = recordRepository.readAllByIdNotNull()) {
                stream.forEach(record -> {
                    try {
                        if (record.getId() % 250 == 0) {
                            System.out.println("Handling Record No: " + record.getId());
                        }
                        final long sum = record.getWrappedValues().stream()
                            .mapToLong(wrapped -> Long.valueOf(new String(wrapped.getValue().getValue())))
                            .sum();
                        blockingDeque.putLast(sum);
                    } catch (final InterruptedException e) {
                        throw new RuntimeException("Creator: Test thread interrupted.");
                    } catch (final Exception e) {
                        throw new RuntimeException("Creator: Unexpected error: " + e.getMessage(), e);
                    }
                });
                // all records have been processed, set the breaking value which will finish the other executor
                breaker.set(true);
            }
        });

        final AtomicLong counter = new AtomicLong();
        final ExecutorService calculator = Executors.newSingleThreadExecutor();
        final Future<?> calculatorFuture = calculator.submit(() -> {
            try {
                while (true) {
                    final Long sum = blockingDeque.poll(10L, TimeUnit.MILLISECONDS);
                    if (sum == null) {
                        if (breaker.get() == true) {
                            break; // there will be no more sums to add to the counter
                        }
                        continue; // wait for next result to add
                    }

                    // simulate long running calculation
                    Thread.sleep(25L);
                    counter.addAndGet(sum);
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException("Calculator: Test thread interrupted.");
            } catch (final Exception e) {
                throw new RuntimeException("Calculator: Unexpected error: " + e.getMessage(), e);
            }
        });

        // wait for the threads to finish
        creatorFuture.get();
        calculatorFuture.get();

        // then
        Assert.assertEquals(15L * NUMBER_OF_RECORDS, counter.get());

        // cleanup
        creator.shutdown();
        calculator.shutdown();
    }
}
