package com.felicio.grupo.cardshare;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CardRecyclerAdapter extends RecyclerView.Adapter<CardRecyclerAdapter.ViewHolder> {

    public List<CardClass> card_list;
    public Context context;
    public String userCurrentID;
    public String user_id;

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

        String cargo_data = card_list.get(i).getCargo();
        viewHolder.setCargoText(cargo_data);

        String contact_data = card_list.get(i).getContact();
        viewHolder.setContactText(contact_data);

        String email_data = card_list.get(i).getEmail();
        viewHolder.setEmailText(email_data);

        String endereco_data = card_list.get(i).getEndereco();
        viewHolder.setEnderecoText(endereco_data);

        String image_url = card_list.get(i).getImage_url();
        viewHolder.setBlogImage(image_url);

        user_id = card_list.get(i).getUser_id();
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
        private TextView descView,cargoView,contactView,emailView,enderecoView;
        private TextView cardDate;
        private ImageView cardImageView;

        private TextView user_name;
        private CircleImageView user_image;

        private Bitmap bitmap;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CardView savingLayout = mView.findViewById(R.id.main_card_list);
                    File file = saveBitMap(mView.getContext(), savingLayout);
                    if (file != null) {
                        Log.i("TAG", "Drawing saved to the gallery!");
                    } else {
                        Log.i("TAG", "Oops! Image could not be saved.");
                    }

                    Toast.makeText(mView.getContext(), "Teste", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(mView.getContext());
                    View dView = LayoutInflater.from(mView.getContext()).inflate(R.layout.dialog_qrcode,null);
                    ImageView imageQR = dView.findViewById(R.id.imageQR);
                    imageQR.setImageBitmap(bitmap);

                    mBuilder.setView(dView);
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                }
            });

        }

        public void setCargoText(String text){
            cargoView = mView.findViewById(R.id.card_cargo);
            cargoView.setText(text);
        }

        public void setContactText(String text){
            contactView = mView.findViewById(R.id.card_contact);
            contactView.setText(text);
        }

        public void setEmailText(String text){
            emailView = mView.findViewById(R.id.card_email);
            emailView.setText(text);
        }

        public void setEnderecoText(String text){
            enderecoView= mView.findViewById(R.id.card_endereco);
            enderecoView.setText(text);
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

        private File saveBitMap(Context context, View drawView){
            File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Logicchip");

            String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
            File pictureFile = new File(filename);
            bitmap = getBitmapFromView(drawView);
            try {
                pictureFile.createNewFile();
                FileOutputStream oStream = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
                oStream.flush();
                oStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("TAG", "There was an issue saving the image.");
            }
            scanGallery( context,pictureFile.getAbsolutePath());
            return pictureFile;
        }

        private Bitmap getBitmapFromView(View view) {
            //Define a bitmap with the same size as the view
            Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
            //Bind a canvas to it
            Canvas canvas = new Canvas(returnedBitmap);
            //Get the view's background
            Drawable bgDrawable =view.getBackground();
            if (bgDrawable!=null) {
                //has background drawable, then draw it on the canvas
                bgDrawable.draw(canvas);
            }   else{
                //does not have background drawable, then draw white background on the canvas
                canvas.drawColor(Color.WHITE);
            }
            // draw the view on the canvas
            view.draw(canvas);
            //return the bitmap
            return returnedBitmap;
        }

        private void scanGallery(Context cntx, String path) {
            try {
                MediaScannerConnection.scanFile(cntx, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("TAG", "There was an issue scanning gallery.");
            }
        }

        public void setDescText(String text){
            descView = mView.findViewById(R.id.card_desc);
            descView.setText(text);
        }

    }
}
