package com.vectras.as3.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vectras.as3.R;
import java.io.File;
import java.util.List;

public class GamesListAdapter extends RecyclerView.Adapter<GamesListAdapter.ViewHolder> {

    private final List<File> itemList;
    private final Context context;
    private final OnItemLongClickListener longClickListener;
    private final OnItemClickListener clickListener;

    public GamesListAdapter(
            Context context,
            List<File> items,
            OnItemLongClickListener longClickListener,
            OnItemClickListener clickListener) {
        this.context = context;
        this.itemList = items;
        this.longClickListener = longClickListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = itemList.get(position);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath() + "/PS3_GAME/ICON0.PNG");
        Bitmap bitmap2 = BitmapFactory.decodeFile(file.getAbsolutePath() + "/ICON0.PNG");

        if (new File(file.getAbsolutePath() + "/PS3_GAME/ICON0.PNG").exists()) {
            holder.imageView.setImageBitmap(bitmap);
        } else if (new File(file.getAbsolutePath() + "/ICON0.PNG").exists()) {
            holder.imageView.setImageBitmap(bitmap2);
        }

        String title = file.getName();
        holder.titleView.setText(title);

        holder.itemView.setOnLongClickListener(
                v -> {
                    if (longClickListener != null) {
                        longClickListener.onItemLongClick(file);
                        return true;
                    }
                    return false;
                });

        holder.itemView.setOnClickListener(
                v -> {
                    if (clickListener != null) {
                        clickListener.onItemClick(file);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateGamesList(List<File> newGamesList) {
        this.itemList.clear();
        this.itemList.addAll(newGamesList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleView = itemView.findViewById(R.id.titleView);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(File file);
    }

    public interface OnItemClickListener {
        void onItemClick(File file);
    }
}
