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
import com.faisal.protoolkit.data.entities.DocumentEntity;
import com.faisal.protoolkit.util.FileManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;

public class DocumentAdapter extends ListAdapter<DocumentEntity, DocumentAdapter.DocumentViewHolder> {
    private final OnDocumentClickListener listener;
    private final FileManager fileManager;

    public interface OnDocumentClickListener {
        void onDocumentClick(DocumentEntity document);
    }

    public DocumentAdapter(OnDocumentClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        // Initialize FileManager with context - for now we'll pass a temporary file manager
        // In a real implementation, this would come from the fragment
        this.fileManager = null; // Will be handled differently
    }

    private static final DiffUtil.ItemCallback<DocumentEntity> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<DocumentEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull DocumentEntity oldItem, @NonNull DocumentEntity newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull DocumentEntity oldItem, @NonNull DocumentEntity newItem) {
                return oldItem.title.equals(newItem.title) && 
                       oldItem.page_count == newItem.page_count && 
                       oldItem.updated_at == newItem.updated_at;
            }
        };

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        DocumentEntity document = getItem(position);
        holder.bind(document);
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailImageView;
        private final TextView titleTextView;
        private final TextView pageCountTextView;
        private final TextView updatedAtTextView;

        DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            pageCountTextView = itemView.findViewById(R.id.page_count_text_view);
            updatedAtTextView = itemView.findViewById(R.id.updated_at_text_view);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDocumentClick(getItem(position));
                }
            });
        }

        void bind(DocumentEntity document) {
            titleTextView.setText(document.title);
            pageCountTextView.setText(itemView.getContext().getString(R.string.page_count, document.page_count));
            updatedAtTextView.setText(formatTimestamp(document.updated_at));
            
            // For thumbnail, try to load the cover page (first page) if available
            loadThumbnail(document);
        }
        
        private void loadThumbnail(DocumentEntity document) {
            try {
                // Create a file manager instance for this context
                FileManager fm = new FileManager(itemView.getContext());
                
                // Get the cover page (first page) for thumbnail
                File coverPageFile = fm.getOriginalImageFile(document.id, document.cover_page_index);
                
                if (coverPageFile.exists()) {
                    // Load and set the bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(coverPageFile.getAbsolutePath());
                    if (bitmap != null) {
                        thumbnailImageView.setImageBitmap(bitmap);
                    } else {
                        // Set a placeholder if bitmap couldn't be loaded
                        thumbnailImageView.setImageResource(R.drawable.preview_background);
                    }
                } else {
                    // Set a default placeholder
                    thumbnailImageView.setImageResource(R.drawable.preview_background);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Set a default placeholder on error
                thumbnailImageView.setImageResource(R.drawable.preview_background);
            }
        }
        
        private String formatTimestamp(long timestamp) {
            // Format the timestamp for display
            return android.text.format.DateUtils.formatDateTime(
                itemView.getContext(),
                timestamp,
                android.text.format.DateUtils.FORMAT_SHOW_DATE | android.text.format.DateUtils.FORMAT_SHOW_TIME
            );
        }
    }
}