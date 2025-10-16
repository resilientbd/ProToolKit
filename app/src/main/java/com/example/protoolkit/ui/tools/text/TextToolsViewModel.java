package com.example.protoolkit.ui.tools.text;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.protoolkit.data.text.TextToolsRepository;
import com.example.protoolkit.ui.base.BaseViewModel;

/**
 * Manages transformations for Text Tools.
 */
public class TextToolsViewModel extends BaseViewModel {

    private final TextToolsRepository repository;
    private final MutableLiveData<String> inputText = new MutableLiveData<>("");
    private final MutableLiveData<String> transformedText = new MutableLiveData<>("");
    private final MutableLiveData<Integer> wordCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> characterCount = new MutableLiveData<>(0);

    public TextToolsViewModel(@NonNull TextToolsRepository repository) {
        this.repository = repository;
    }

    public LiveData<String> getInputText() {
        return inputText;
    }

    public LiveData<String> getTransformedText() {
        return transformedText;
    }

    public LiveData<Integer> getWordCount() {
        return wordCount;
    }

    public LiveData<Integer> getCharacterCount() {
        return characterCount;
    }

    public void updateInput(@NonNull String newInput) {
        inputText.setValue(newInput);
        transformedText.setValue(newInput);
        updateCounts(newInput);
    }

    private void updateCounts(@NonNull String text) {
        wordCount.setValue(repository.countWords(text));
        characterCount.setValue(repository.countCharacters(text));
    }

    public void transformUppercase() {
        String current = inputText.getValue();
        if (current == null) {
            return;
        }
        String result = repository.toUpperCase(current);
        transformedText.setValue(result);
        updateCounts(result);
    }

    public void transformLowercase() {
        String current = inputText.getValue();
        if (current == null) {
            return;
        }
        String result = repository.toLowerCase(current);
        transformedText.setValue(result);
        updateCounts(result);
    }

    public void transformTitleCase() {
        String current = inputText.getValue();
        if (current == null) {
            return;
        }
        String result = repository.toTitleCase(current);
        transformedText.setValue(result);
        updateCounts(result);
    }
}
