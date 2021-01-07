package com.gestankbratwurst.jmhbenchmark;

import java.util.Objects;

public class Complex {
  private final double real;
  private final double imag;

  public Complex(double real, double imag) {
    this.real = real;
    this.imag = imag;
  }

  public String toString() {
    if (imag == 0) return real + "";
    if (real == 0) return imag + "i";
    if (imag < 0) return real + " - " + (-imag) + "i";
    return real + " + " + imag + "i";
  }

  public double abs() {
    return Math.hypot(real, imag);
  }

  public Complex plus(Complex b) {
    Complex a = this;
    double real = a.real + b.real;
    double imag = a.imag + b.imag;
    return new Complex(real, imag);
  }

  public Complex minus(Complex b) {
    Complex a = this;
    double real = a.real - b.real;
    double imag = a.imag - b.imag;
    return new Complex(real, imag);
  }

  public Complex times(Complex b) {
    Complex a = this;
    double real = a.real * b.real - a.imag * b.imag;
    double imag = a.real * b.imag + a.imag * b.real;
    return new Complex(real, imag);
  }

  public Complex multiply(double alpha) {
    return new Complex(alpha * real, alpha * imag);
  }

  public Complex conjugate() {
    return new Complex(real, -imag);
  }

  public Complex reciprocal() {
    double scale = real * real + imag * imag;
    return new Complex(real / scale, -imag / scale);
  }

  public double getReal() {
    return real;
  }

  public double getImag() {
    return imag;
  }

  public Complex divides(Complex b) {
    Complex a = this;
    return a.times(b.reciprocal());
  }

  public boolean equals(Object x) {
    if (x == null) return false;
    if (this.getClass() != x.getClass()) return false;
    Complex that = (Complex) x;
    return (this.real == that.real) && (this.imag == that.imag);
  }

  public int hashCode() {
    return Objects.hash(real, imag);
  }
}
