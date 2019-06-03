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
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CardRecyclerAdapter extends RecyclerView.Adapter<CardRecyclerAdapter.ViewHolder> {

    public List<CardClass> card_list;
    public Context context;

    public FirebaseFirestore firebaseFirestore;

    public CardRecyclerAdapter(List<CardClass> card_list){
        this.card_list = card_list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_list_item, viewGroup, false);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        String desc_data = card_list.get(i).getDesc();
        viewHolder.setDescText(desc_data);

        String contact_data = card_list.get(i).getContact();
        viewHolder.setContactText(contact_data);

        String image_url = card_list.get(i).getImage_url();
        viewHolder.setBlogImage(image_url);

        String user_id = card_list.get(i).getUser_id();
        //viewHolder.setUserId(user_id);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    viewHolder.setUserData(userName,userImage);
                }
            }
        });

        long milliseconds = card_list.get(i).getTimestamp().getTime();
        String dateString = DateFormat.format("dd/MM/yyyy", new Date(milliseconds)).toString();
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

        private TextView user_name;
        private CircleImageView user_image;

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

            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.color.common_google_signin_btn_text_light_default);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(downloadUri).into(cardImageView);
        }

        public void setTime(String date){
            cardDate = mView.findViewById(R.id.card_user_data);
            cardDate.setText(date);
        }
        public void setUserData(String name, String image){
            user_name = mView.findViewById(R.id.card_user_name);
            user_image = mView.findViewById(R.id.card_user_image);

            user_name.setText(name);

            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.list_cardprofile);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(image).into(user_image);

        }
    }


}
