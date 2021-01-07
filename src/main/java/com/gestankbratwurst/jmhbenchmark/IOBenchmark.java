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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms3G", "-Xmx3G"})
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class IOBenchmark {

  @Param({"1000"})
  private int lineCount;
  @Param({"50"})
  private int charsPerLine;

  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  private final List<String> lines = new ArrayList<>();
  private byte[] rawData;

  public IOBenchmark() {

  }

  private Path createTempFile() {
    try {
      return Files.createTempFile(StringGenerator.genString(10), ".tmp");
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Setup
  public void setup() {
    for (int i = 0; i < lineCount; i++) {
      String element = StringGenerator.genString(charsPerLine);
      lines.add(element);
    }

    rawData = String.join("/n", lines).getBytes();
  }

  @TearDown
  public void teardown() {
    lines.clear();
    rawData = null;
  }

  @Benchmark
  public void writeLinesUnbuffered(Blackhole bh) throws IOException {
    Path tempFile = createTempFile();
    OutputStream outputStream = new FileOutputStream(tempFile.toFile());
    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
      for (String line : lines) {
        writer.write(line + "/n");
      }
    }
  }

  @Benchmark
  public void writeRawUnbuffered(Blackhole bh) throws IOException {
    Path tempFile = createTempFile();
    try (OutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
      outputStream.write(rawData);
    }
  }

  @Benchmark
  public void writeLinesBuffered(Blackhole bh) throws IOException {
    Path tempFile = createTempFile();
    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile.toFile()));
    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
      for (String line : lines) {
        writer.write(line + "/n");
      }
    }
  }

  @Benchmark
  public void writeRawBuffered(Blackhole bh) throws IOException {
    Path tempFile = createTempFile();
    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile.toFile()))) {
      outputStream.write(rawData);
    }
  }

  @Benchmark
  public void writeLinesAPI(Blackhole bh) throws IOException {
    Path tempFile = createTempFile();
    Files.write(tempFile, lines);
  }

  @Benchmark
  public void writeRawAPI(Blackhole bh) throws IOException {
    Path tempFile = createTempFile();
    Files.write(tempFile, rawData);
  }

}
