package com.faisal.protoolkit.ui.tools.document;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faisal.protoolkit.R;

import java.util.List;

/**
 * Adapter for displaying scanned document pages in the original document scanner
 */
public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {
    private List<DocumentItem> documents;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public DocumentAdapter(List<DocumentItem> documents, OnItemClickListener listener) {
        this.documents = documents;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document_scanned, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentItem document = documents.get(position);
        holder.bind(document, position);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public void updateList(List<DocumentItem> newDocuments) {
        this.documents = newDocuments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView titleText;
        private ImageView deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.document_image);
            titleText = itemView.findViewById(R.id.document_title);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }

        public void bind(DocumentItem document, int position) {
            titleText.setText(document.getTitle());
            imageView.setImageBitmap(document.getBitmap());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            });

            deleteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            });
        }
    }
}