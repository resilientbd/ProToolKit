package com.faisal.protoolkit.ui.tools.document;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faisal.protoolkit.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying scanned document items in a RecyclerView
 */
public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    private List<DocumentItem> documents;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public DocumentAdapter(List<DocumentItem> documents, OnDeleteClickListener listener) {
        this.documents = documents;
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        DocumentItem document = documents.get(position);
        
        holder.title.setText(document.getTitle());
        
        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(document.getTimestamp());
        holder.subtitle.setText(formattedDate);
        
        // Set thumbnail
        holder.thumbnail.setImageBitmap(document.getBitmap());
        
        // Set delete button click listener
        holder.deleteBtn.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title, subtitle;
        ImageView deleteBtn;

        DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.document_thumb);
            title = itemView.findViewById(R.id.document_title);
            subtitle = itemView.findViewById(R.id.document_subtitle);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}