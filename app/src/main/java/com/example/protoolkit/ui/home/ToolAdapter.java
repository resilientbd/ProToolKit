package com.example.protoolkit.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protoolkit.databinding.ItemToolBinding;
import com.example.protoolkit.domain.model.ToolItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying tool cards in a grid.
 */
public class ToolAdapter extends RecyclerView.Adapter<ToolAdapter.ToolViewHolder> {

    public interface OnToolClickListener {
        void onToolClick(@NonNull ToolItem item);
    }

    private final List<ToolItem> items = new ArrayList<>();
    private final OnToolClickListener listener;

    public ToolAdapter(@NonNull OnToolClickListener listener) {
        this.listener = listener;
    }

    public void submitList(@NonNull List<ToolItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemToolBinding binding = ItemToolBinding.inflate(inflater, parent, false);
        return new ToolViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ToolViewHolder extends RecyclerView.ViewHolder {

        private final ItemToolBinding binding;

        ToolViewHolder(@NonNull ItemToolBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull ToolItem item) {
            binding.toolTitle.setText(item.getTitleRes());
            binding.toolIcon.setImageResource(item.getIconRes());
            binding.getRoot().setContentDescription(binding.getRoot().getContext()
                    .getString(com.example.protoolkit.R.string.content_description_tool_icon,
                            binding.toolTitle.getText()));
            binding.getRoot().setOnClickListener(v -> listener.onToolClick(item));
        }
    }
}
