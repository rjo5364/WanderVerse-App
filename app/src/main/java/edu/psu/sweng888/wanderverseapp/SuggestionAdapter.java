package edu.psu.sweng888.wanderverseapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    private List<PlaceSuggestion> suggestions;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(PlaceSuggestion suggestion);
    }

    public SuggestionAdapter(List<PlaceSuggestion> suggestions, OnItemClickListener onItemClickListener) {
        this.suggestions = suggestions;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        PlaceSuggestion suggestion = suggestions.get(position);
        holder.suggestionName.setText(suggestion.getName());
        holder.suggestionType.setText(suggestion.getType());

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(suggestion));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView suggestionName;
        TextView suggestionType;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestionName = itemView.findViewById(R.id.suggestion_name);
            suggestionType = itemView.findViewById(R.id.suggestion_type);
        }
    }
}