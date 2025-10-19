package com.faisal.protoolkit.ui.tools.document.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.data.entities.PageEntity;
import com.faisal.protoolkit.ui.tools.document.viewmodels.DocumentDetailViewModel;

public class PageAdapter extends ListAdapter<PageEntity, PageAdapter.PageViewHolder> {
    private final DocumentDetailViewModel viewModel;
    private final OnPageClickListener listener;

    public interface OnPageClickListener {
        void onPageClick(PageEntity page);
    }

    public PageAdapter(DocumentDetailViewModel viewModel, OnPageClickListener listener) {
        super(DIFF_CALLBACK);
        this.viewModel = viewModel;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<PageEntity> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<PageEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull PageEntity oldItem, @NonNull PageEntity newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PageEntity oldItem, @NonNull PageEntity newItem) {
                return oldItem.index == newItem.index && 
                       oldItem.uri_original.equals(newItem.uri_original);
            }
        };

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        PageEntity page = getItem(position);
        holder.bind(page);
    }

    class PageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailImageView;
        private final TextView indexTextView;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            indexTextView = itemView.findViewById(R.id.index_text_view);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPageClick(getItem(position));
                }
            });
        }

        void bind(PageEntity page) {
            indexTextView.setText(String.valueOf(page.index + 1)); // Display 1-based index
            
            // Load thumbnail - in a real implementation, you would load the page thumbnail
            // GlideApp.with(itemView.getContext())
            //         .load(page.uri_original)
            //         .placeholder(R.drawable.placeholder_page)
            //         .into(thumbnailImageView);
        }
    }
}