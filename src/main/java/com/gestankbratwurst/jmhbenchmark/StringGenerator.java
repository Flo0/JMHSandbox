package com.gestankbratwurst.jmhbenchmark;

import java.util.concurrent.ThreadLocalRandom;

public class StringGenerator {

  private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
  private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  public static String genString(int length) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < length; i++) {
      builder.append(CHARS.charAt(RANDOM.nextInt(0, CHARS.length())));
    }

    return builder.toString();
  }

}
