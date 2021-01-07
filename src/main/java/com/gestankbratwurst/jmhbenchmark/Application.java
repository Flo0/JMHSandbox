package com.gestankbratwurst.jmhbenchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Application {

  private static final File JAR_FOLDER = new File(System.getProperty("user.dir"));
  private static boolean CONSOLE_LOG = false;

  public static void main(String[] args) throws RunnerException, FileNotFoundException {

    if (!(CONSOLE_LOG = args[0].equals("c"))) {
      System.out.println("Export location: " + JAR_FOLDER.toString());
    } else {
      System.out.println("Console log enabled.");
    }

    runBenchmark(CollectionsBenchmark.class);
    runBenchmark(IOBenchmark.class);
    runBenchmark(MandelbrotBenchmark.class);
  }

  private static void runBenchmark(Class<?> benchmarkClass) throws FileNotFoundException, RunnerException {
    PrintStream out = CONSOLE_LOG ? System.out : new PrintStream(new File(JAR_FOLDER, benchmarkClass.getSimpleName() + ".txt"));
    OutputFormat collectionsFormat = OutputFormatFactory.createFormatInstance(out, VerboseMode.NORMAL);

    Options opt = new OptionsBuilder()
            .include(benchmarkClass.getSimpleName())
            .build();
    new Runner(opt, collectionsFormat).run();
  }

}
