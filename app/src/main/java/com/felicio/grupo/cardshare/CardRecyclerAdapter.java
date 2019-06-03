package com.felicio.grupo.cardshare;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.List;

public class CardRecyclerAdapter extends RecyclerView.Adapter<CardRecyclerAdapter.ViewHolder> {

    public List<CardClass> card_list;
    public Context context;

    public CardRecyclerAdapter(List<CardClass> card_list){
        this.card_list = card_list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_list_item, viewGroup, false);
        context = viewGroup.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String desc_data = card_list.get(i).getDesc();
        viewHolder.setDescText(desc_data);

        String contact_data = card_list.get(i).getContact();
        viewHolder.setContactText(contact_data);

        String image_url = card_list.get(i).getImage_url();
        viewHolder.setBlogImage(image_url);

        String user_id = card_list.get(i).getUser_id();
        //viewHolder.setUserId(user_id);

        long milliseconds = card_list.get(i).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
        viewHolder.setTime(dateString);
    }

    @Override
    public int getItemCount() {
        return card_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private TextView contactView;
        private TextView cardDate;
        private ImageView cardImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescText(String text){
            descView = mView.findViewById(R.id.card_desc);
            descView.setText(text);
        }

        public void setContactText(String text){
            contactView = mView.findViewById(R.id.card_contact);
            contactView.setText(text);
        }

        public void setBlogImage(String downloadUri){
            cardImageView = mView.findViewById(R.id.card_image);
            Glide.with(context).load(downloadUri).into(cardImageView);
        }

        public void setTime(String date){
            cardDate = mView.findViewById(R.id.card_user_data);
            cardDate.setText(date);
        }
    }


}
