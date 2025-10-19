package com.faisal.protoolkit.ui.tools.document.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.data.entities.FolderEntity;

public class FolderAdapter extends ListAdapter<FolderEntity, FolderAdapter.FolderViewHolder> {
    private final OnFolderClickListener listener;

    public interface OnFolderClickListener {
        void onFolderClick(FolderEntity folder);
    }

    public FolderAdapter(OnFolderClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FolderEntity> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<FolderEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull FolderEntity oldItem, @NonNull FolderEntity newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull FolderEntity oldItem, @NonNull FolderEntity newItem) {
                return oldItem.name.equals(newItem.name);
            }
        };

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FolderEntity folder = getItem(position);
        holder.bind(folder);
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;

        FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onFolderClick(getItem(position));
                }
            });
        }

        void bind(FolderEntity folder) {
            nameTextView.setText(folder.name);
        }
    }
}