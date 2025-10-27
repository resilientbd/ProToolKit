package com.faisal.protoolkit.ui.tools.document.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.data.entities.PageEntity;
import com.faisal.protoolkit.ui.tools.document.DocumentPageEditActivity;
import com.faisal.protoolkit.ui.tools.document.viewmodels.DocumentDetailViewModel;
import java.io.File;

public class PageAdapter extends ListAdapter<PageEntity, PageAdapter.PageViewHolder> {
    private final DocumentDetailViewModel viewModel;
    private final OnPageClickListener listener;
    private final OnPageDeleteListener deleteListener;

    public interface OnPageClickListener {
        void onPageClick(PageEntity page);
    }

    public interface OnPageDeleteListener {
        void onPageDelete(PageEntity page);
    }

    public PageAdapter(DocumentDetailViewModel viewModel, OnPageClickListener listener, 
                      OnPageDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.viewModel = viewModel;
        this.listener = listener;
        this.deleteListener = deleteListener;
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
                       oldItem.uri_original.equals(newItem.uri_original) &&
                       oldItem.updated_at == newItem.updated_at;
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
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            indexTextView = itemView.findViewById(R.id.index_text_view);
            editButton = itemView.findViewById(R.id.btn_edit_page);
            deleteButton = itemView.findViewById(R.id.btn_delete_page);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPageClick(getItem(position));
                }
            });

            editButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PageEntity page = getItem(position);
                    // Start the edit activity
                    Context context = itemView.getContext();
                    Intent intent = new Intent(context, DocumentPageEditActivity.class);
                    intent.putExtra("page_id", page.id);
                    context.startActivity(intent);
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    deleteListener.onPageDelete(getItem(position));
                }
            });
        }

        void bind(PageEntity page) {
            indexTextView.setText("Page " + (page.index + 1)); // Display with "Page" prefix
            
            // Load thumbnail from the original URI
            if (page.uri_original != null && !page.uri_original.isEmpty()) {
                // For local files, we can use Glide to load them
                Glide.with(itemView.getContext())
                        .load(new File(page.uri_original))
                        .placeholder(R.drawable.placeholder_page) // Use a placeholder while loading
                        .error(R.drawable.placeholder_page) // Use error drawable if loading fails
                        .into(thumbnailImageView);
            } else {
                // Set a default placeholder if no URI is available
                thumbnailImageView.setImageResource(R.drawable.placeholder_page);
            }
        }
    }
}