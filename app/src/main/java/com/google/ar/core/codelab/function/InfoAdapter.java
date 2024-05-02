package com.google.ar.core.codelab.function;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.function.InfoModel;

import java.util.List;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {

    private final List<InfoModel> infoList;

    public InfoAdapter(List<InfoModel> infoList) {
        this.infoList = infoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.infomation_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        InfoModel info = infoList.get(position);
        holder.informationTitle.setText(info.getTitle());

        holder.expandableContent.setVisibility(info.isExpanded() ? View.VISIBLE : View.GONE);

        holder.waveorconnectbtn.setImageResource(info.isExpanded() ? R.drawable.ic_arrowup : R.drawable.ic_dropdown);

        holder.waveorconnectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = info.isExpanded();
                info.setExpanded(!isExpanded);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView informationTitle;
        ImageButton waveorconnectbtn;
        View expandableContent;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            informationTitle = itemView.findViewById(R.id.Information1);
            waveorconnectbtn = itemView.findViewById(R.id.waveorconnectbtn);
            expandableContent = itemView.findViewById(R.id.tagview); // Assuming this is the content to expand/collapse
            cardView = itemView.findViewById(R.id.profile_row_cardview);
        }
    }
}
