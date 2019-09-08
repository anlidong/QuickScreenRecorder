package com.quickquick.screenrecorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.quickquick.screenrecorder.adapter.holder.ViewHolder;

import java.util.List;

public class SimpleAdapter<T> extends RecyclerView.Adapter<ViewHolder>{

    private List<T> list;

    private int layoutId;

    private int brId;

    private OnItemClickListener listener;

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SimpleAdapter(List<T> list, int layoutId, int brId) {
        this.list = list;
        this.layoutId = layoutId;
        this.brId = brId;
    }

    public SimpleAdapter(int layoutId, int brId) {
        this.layoutId = layoutId;
        this.brId = brId;
    }

    public void setList(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(binding.getRoot());
        viewHolder.setBinding(binding);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.getBinding().setVariable(brId, list.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onItemClick(holder, v, position);
                }
            }
        });
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ViewHolder holder, View v, int position);
    }
}
