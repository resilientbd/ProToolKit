package com.example.protoolkit.data.converter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Basic unit tests covering core conversions.
 */
public class UnitConverterRepositoryTest {

    private static final double DELTA = 0.001;
    private UnitConverterRepository repository;

    @Before
    public void setUp() {
        repository = new UnitConverterRepository();
    }

    @Test
    public void convertMetersToFeet() {
        double result = repository.convertLength(1.0, "meter", "foot");
        assertEquals(3.2808, result, DELTA);
    }

    @Test
    public void convertFeetToMeters() {
        double result = repository.convertLength(3.28084, "foot", "meter");
        assertEquals(1.0, result, DELTA);
    }

    @Test
    public void convertKilogramsToPounds() {
        double result = repository.convertWeight(1.0, "kilogram", "pound");
        assertEquals(2.20462, result, DELTA);
    }

    @Test
    public void convertPoundsToKilograms() {
        double result = repository.convertWeight(2.20462, "pound", "kilogram");
        assertEquals(1.0, result, DELTA);
    }

    @Test
    public void convertCelsiusToFahrenheit() {
        double result = repository.convertTemperature(100.0, "celsius", "fahrenheit");
        assertEquals(212.0, result, DELTA);
    }

    @Test
    public void convertFahrenheitToCelsius() {
        double result = repository.convertTemperature(32.0, "fahrenheit", "celsius");
        assertEquals(0.0, result, DELTA);
    }
}
