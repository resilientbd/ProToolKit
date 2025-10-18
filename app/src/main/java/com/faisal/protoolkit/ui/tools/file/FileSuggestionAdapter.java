package com.faisal.protoolkit.ui.tools.file;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faisal.protoolkit.databinding.ItemSuggestionBinding;
import com.faisal.protoolkit.domain.model.SuggestionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter showing non-destructive file management suggestions.
 */
public class FileSuggestionAdapter extends RecyclerView.Adapter<FileSuggestionAdapter.SuggestionViewHolder> {

    private final List<SuggestionItem> items = new ArrayList<>();

    public void submitList(@NonNull List<SuggestionItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSuggestionBinding binding = ItemSuggestionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SuggestionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {

        private final ItemSuggestionBinding binding;

        SuggestionViewHolder(@NonNull ItemSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull SuggestionItem item) {
            binding.suggestionIcon.setImageResource(item.getIconRes());
            
            if (item.usesResourceIds()) {
                // Use resource IDs
                binding.suggestionTitle.setText(item.getTitleRes());
                binding.suggestionDescription.setText(item.getDescriptionRes());
            } else {
                // Use direct strings
                binding.suggestionTitle.setText(item.getTitle());
                binding.suggestionDescription.setText(item.getDescription());
            }
        }
    }
}