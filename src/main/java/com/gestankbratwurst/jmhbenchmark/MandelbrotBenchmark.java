package com.gestankbratwurst.jmhbenchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms3G", "-Xmx3G"})
@Warmup(iterations = 4, batchSize = 2)
@Measurement(iterations = 10, batchSize = 2)
public class MandelbrotBenchmark {

  private static final int IMAGE_WIDTH = 1024;
  private static final int IMAGE_HEIGHT = 1024;
  private static final double PLANE_REAL_MIN = -2D;
  private static final double PLANE_REAL_MAX = 0.75D;
  private static final double PLANE_IMAG_MIN = -1.2D;
  private static final double PLANE_IMAG_MAX = 1.2D;

  @Param({"50"})
  private int iterationDepth;

  private final Complex[][] complexMatrix = new Complex[IMAGE_WIDTH][IMAGE_HEIGHT];
  private final int[][] pixelMatrix = new int[IMAGE_WIDTH][IMAGE_HEIGHT];

  private char currentPrefix = 'X';

  @Setup(value = Level.Invocation)
  public void setup() {
    fillComplexMatrix();
  }

  @TearDown(value = Level.Invocation)
  public void cleanup() {
    saveImage();
    clearPixelMatrix();
  }

  private void clearPixelMatrix() {
    for (int x = 0; x < IMAGE_WIDTH; x++) {
      for (int y = 0; y < IMAGE_HEIGHT; y++) {
        pixelMatrix[x][y] = 0;
      }
    }
  }

  private void fillComplexMatrix() {
    double xStep = (PLANE_REAL_MAX - PLANE_REAL_MIN) / IMAGE_WIDTH;
    double yStep = (PLANE_IMAG_MAX - PLANE_IMAG_MIN) / IMAGE_HEIGHT;
    double currentX = PLANE_REAL_MIN;
    double currentY = PLANE_IMAG_MIN;
    for (int x = 0; x < IMAGE_WIDTH; x++) {
      for (int y = 0; y < IMAGE_HEIGHT; y++) {
        Complex complex = new Complex(currentX, currentY);
        complexMatrix[x][y] = complex;
        currentY += yStep;
      }
      currentX += xStep;
      currentY = PLANE_IMAG_MIN;
    }
  }

  private void saveImage() {
    BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    DataBuffer buffer = image.getRaster().getDataBuffer();
    IntUnaryOperator scaleOperand = val -> (int) (255D / iterationDepth * val);
    for (int i = 0; i < IMAGE_WIDTH * IMAGE_HEIGHT; i++) {
      int value = pixelMatrix[i % IMAGE_HEIGHT][i / IMAGE_WIDTH];
      value = scaleOperand.applyAsInt(value);
      int c = new Color(value, value, value).getRGB();
      buffer.setElem(i, c);
    }

    try {
      ImageIO.write(image, "png", new File(currentPrefix + "_Mandelbrot_" + iterationDepth + ".png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Benchmark
  public void generateMandelbrot() {
    for (int x = 0; x < IMAGE_WIDTH; x++) {
      for (int y = 0; y < IMAGE_HEIGHT; y++) {
        pixelMatrix[x][y] = calcMandelbrotAtPos(x, y);
      }
    }
    currentPrefix = 'S';
  }

  @Benchmark
  public void generateMandelbrotParallel() {
    CountDownLatch latch = new CountDownLatch(IMAGE_WIDTH);
    for (int x = 0; x < IMAGE_WIDTH; x++) {
      final int cx = x;
      CompletableFuture.runAsync(() -> {
        for (int y = 0; y < IMAGE_HEIGHT; y++) {
          pixelMatrix[cx][y] = calcMandelbrotAtPos(cx, y);
        }
      }).thenRun(latch::countDown);
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    currentPrefix = 'P';
  }

  @Benchmark
  public void generateMandelbrotScheduler() {
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    CountDownLatch latch = new CountDownLatch(IMAGE_WIDTH);
    for (int x = 0; x < IMAGE_WIDTH; x++) {
      final int cx = x;
      executor.execute(() -> {
        for (int y = 0; y < IMAGE_HEIGHT; y++) {
          pixelMatrix[cx][y] = calcMandelbrotAtPos(cx, y);
        }
        latch.countDown();
      });
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return;
    }
    currentPrefix = 'E';
  }

  private int calcMandelbrotAtPos(int x, int y) {
    Complex z0 = complexMatrix[x][y];
    Complex z = z0;
    for (int t = 0; t < iterationDepth; t++) {
      if (z.abs() > 2.0) {
        return t;
      }
      z = z.times(z).plus(z0);
    }
    return iterationDepth;
  }

}
