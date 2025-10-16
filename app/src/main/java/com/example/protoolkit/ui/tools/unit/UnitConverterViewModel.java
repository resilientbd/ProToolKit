package com.example.protoolkit.ui.tools.unit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.protoolkit.data.converter.UnitConverterRepository;
import com.example.protoolkit.ui.base.BaseViewModel;
import com.example.protoolkit.util.FormatUtils;

/**
 * Handles state and conversions for UnitConverterFragment.
 */
public class UnitConverterViewModel extends BaseViewModel {

    public enum Category {
        LENGTH,
        WEIGHT,
        TEMPERATURE,
        AREA
    }

    private static final String[] LENGTH_UNITS = {"meter", "kilometer", "foot", "yard", "mile"};
    private static final String[] WEIGHT_UNITS = {"kilogram", "gram", "pound", "ounce"};
    private static final String[] TEMPERATURE_UNITS = {"celsius", "fahrenheit", "kelvin"};
    private static final String[] AREA_UNITS = {"square_meter", "square_kilometer", "square_foot", "square_yard", "acre"};

    private final UnitConverterRepository repository;
    private final MutableLiveData<Category> currentCategory = new MutableLiveData<>(Category.LENGTH);
    private final MutableLiveData<String> resultText = new MutableLiveData<>("");
    private String inputValue = "";
    private int fromIndex = 0;
    private int toIndex = 1;

    public UnitConverterViewModel(@NonNull UnitConverterRepository repository) {
        this.repository = repository;
    }

    public LiveData<Category> getCurrentCategory() {
        return currentCategory;
    }

    public LiveData<String> getResultText() {
        return resultText;
    }

    public String[] getUnitsFor(Category category) {
        switch (category) {
            case WEIGHT:
                return WEIGHT_UNITS.clone();
            case TEMPERATURE:
                return TEMPERATURE_UNITS.clone();
            case AREA:
                return AREA_UNITS.clone();
            case LENGTH:
            default:
                return LENGTH_UNITS.clone();
        }
    }

    public void setCategory(@NonNull Category category) {
        currentCategory.setValue(category);
        fromIndex = 0;
        toIndex = category == Category.TEMPERATURE ? 1 : Math.min(1, getUnitsFor(category).length - 1);
        recalculate();
    }

    public void setFromIndex(int index) {
        fromIndex = index;
        recalculate();
    }

    public void setToIndex(int index) {
        toIndex = index;
        recalculate();
    }

    public void updateInput(@NonNull String value) {
        inputValue = value;
        recalculate();
    }

    public void swapUnits() {
        int temp = fromIndex;
        fromIndex = toIndex;
        toIndex = temp;
        recalculate();
    }

    private void recalculate() {
        if (inputValue == null || inputValue.trim().isEmpty()) {
            resultText.postValue("");
            return;
        }
        try {
            double parsed = Double.parseDouble(inputValue);
            String[] units = getUnitsFor(currentCategory.getValue() == null ? Category.LENGTH : currentCategory.getValue());
            if (fromIndex < 0 || fromIndex >= units.length || toIndex < 0 || toIndex >= units.length) {
                resultText.postValue("");
                return;
            }
            String fromUnit = units[fromIndex];
            String toUnit = units[toIndex];
            double result;
            Category category = currentCategory.getValue() == null ? Category.LENGTH : currentCategory.getValue();
            switch (category) {
                case WEIGHT:
                    result = repository.convertWeight(parsed, fromUnit, toUnit);
                    break;
                case TEMPERATURE:
                    result = repository.convertTemperature(parsed, fromUnit, toUnit);
                    break;
                case AREA:
                    result = repository.convertArea(parsed, fromUnit, toUnit);
                    break;
                case LENGTH:
                default:
                    result = repository.convertLength(parsed, fromUnit, toUnit);
                    break;
            }
            resultText.postValue(FormatUtils.formatNumber(result));
        } catch (NumberFormatException exception) {
            resultText.postValue("");
        }
    }
}
