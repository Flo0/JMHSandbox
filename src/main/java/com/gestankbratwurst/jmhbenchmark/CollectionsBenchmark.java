package com.gestankbratwurst.jmhbenchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms3G", "-Xmx3G"})
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class CollectionsBenchmark {

  private static final String MISS_ELEMENT = "-&& MISS &&-";

  @Param({"10000"})
  private int elements;

  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  private final List<String> elementList = new ArrayList<>();

  private final List<Collection<String>> testCollections = new ArrayList<>();

  public CollectionsBenchmark() {
    List<String> stringArrayList = new ArrayList<>();
    testCollections.add(stringArrayList);

    List<String> stringLinkedList = new LinkedList<>();
    testCollections.add(stringLinkedList);

    Set<String> stringHashSet = new HashSet<>();
    testCollections.add(stringHashSet);

    Set<String> stringLinkedHashSet = new LinkedHashSet<>();
    testCollections.add(stringLinkedHashSet);

    Set<String> stringTreeSet = new TreeSet<>();
    testCollections.add(stringTreeSet);
  }

  @Setup
  public void setup() {
    for (int i = 0; i < elements; i++) {
      String element = StringGenerator.genString(20);
      elementList.add(element);
    }

    for (Collection<String> collection : testCollections) {
      collection.addAll(elementList);
    }
  }

  @TearDown
  public void teardown() {
    for (Collection<String> collection : testCollections) {
      collection.clear();
    }
  }

  @Benchmark
  public void containsElementHit(Blackhole bh) {
    String randomElement = getRandomElementHit();

    for (Collection<String> collection : testCollections) {
      boolean containing = collection.contains(randomElement);
      bh.consume(containing);
    }
  }

  @Benchmark
  public void containsElementMiss(Blackhole bh) {
    for (Collection<String> collection : testCollections) {
      boolean containing = collection.contains(MISS_ELEMENT);
      bh.consume(containing);
    }
  }

  @Benchmark
  public void streamConcat(Blackhole bh) {
    for (Collection<String> collection : testCollections) {
      String bulk = collection.stream().collect(Collectors.joining(","));
      bh.consume(bulk);
    }
  }

  @Benchmark
  public void parallelStreamConcat(Blackhole bh) {
    for (Collection<String> collection : testCollections) {
      String bulk = collection.parallelStream().collect(Collectors.joining(","));
      bh.consume(bulk);
    }
  }

  @Benchmark
  public void arrayConversion(Blackhole bh) {
    for (Collection<String> collection : testCollections) {
      Object[] arr = collection.toArray();
      bh.consume(arr);
    }
  }

  private String getRandomElementHit() {
    return elementList.get(random.nextInt(0, elements));
  }

}
