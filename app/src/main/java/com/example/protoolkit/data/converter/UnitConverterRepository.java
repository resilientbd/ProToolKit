package com.example.protoolkit.data.converter;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Handles unit conversion logic.
 */
public class UnitConverterRepository {

    private static final Map<String, Double> LENGTH_CONVERSION = new HashMap<>();
    private static final Map<String, Double> WEIGHT_CONVERSION = new HashMap<>();
    private static final Map<String, Double> AREA_CONVERSION = new HashMap<>();

    static {
        // Length factors relative to meters.
        LENGTH_CONVERSION.put("meter", 1.0);
        LENGTH_CONVERSION.put("kilometer", 1000.0);
        LENGTH_CONVERSION.put("foot", 0.3048);
        LENGTH_CONVERSION.put("yard", 0.9144);
        LENGTH_CONVERSION.put("mile", 1609.34);

        // Weight factors relative to kilograms.
        WEIGHT_CONVERSION.put("kilogram", 1.0);
        WEIGHT_CONVERSION.put("gram", 0.001);
        WEIGHT_CONVERSION.put("pound", 0.453592);
        WEIGHT_CONVERSION.put("ounce", 0.0283495);

        // Area factors relative to square meters.
        AREA_CONVERSION.put("square_meter", 1.0);
        AREA_CONVERSION.put("square_kilometer", 1_000_000.0);
        AREA_CONVERSION.put("square_foot", 0.092903);
        AREA_CONVERSION.put("square_yard", 0.836127);
        AREA_CONVERSION.put("acre", 4046.86);
    }

    public double convertLength(double value, @NonNull String from, @NonNull String to) {
        return convert(value, from, to, LENGTH_CONVERSION);
    }

    public double convertWeight(double value, @NonNull String from, @NonNull String to) {
        return convert(value, from, to, WEIGHT_CONVERSION);
    }

    public double convertArea(double value, @NonNull String from, @NonNull String to) {
        return convert(value, from, to, AREA_CONVERSION);
    }

    public double convertTemperature(double value, @NonNull String from, @NonNull String to) {
        String source = from.toLowerCase(Locale.US);
        String target = to.toLowerCase(Locale.US);
        double celsius;
        switch (source) {
            case "celsius":
                celsius = value;
                break;
            case "fahrenheit":
                celsius = (value - 32) * 5 / 9;
                break;
            case "kelvin":
                celsius = value - 273.15;
                break;
            default:
                throw new IllegalArgumentException("Unsupported temperature unit: " + from);
        }

        switch (target) {
            case "celsius":
                return celsius;
            case "fahrenheit":
                return (celsius * 9 / 5) + 32;
            case "kelvin":
                return celsius + 273.15;
            default:
                throw new IllegalArgumentException("Unsupported temperature unit: " + to);
        }
    }

    private double convert(double value, @NonNull String from, @NonNull String to, @NonNull Map<String, Double> factors) {
        Double fromFactor = factors.get(from);
        Double toFactor = factors.get(to);
        if (fromFactor == null || toFactor == null) {
            throw new IllegalArgumentException("Unsupported unit conversion: " + from + " -> " + to);
        }
        double baseValue = value * fromFactor;
        return baseValue / toFactor;
    }
}
