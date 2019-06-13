package com.felicio.grupo.cardshare;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CardRecyclerAdapter extends RecyclerView.Adapter<CardRecyclerAdapter.ViewHolder> {

    public List<CardClass> card_list;
    public Context context;
    public String userCurrentID;
    public String user_id;
    public String refDelete;

    public FirebaseFirestore firebaseFirestore;
    public String currentFragment;

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
        String cardID_data = card_list.get(i).getCard_id();
        viewHolder.setCardIDText(cardID_data);

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

        if(card_list.get(i).getTimestamp() != null){
            long milliseconds = card_list.get(i).getTimestamp().getTime();
            String dateString = DateFormat.format("dd/MM/yyyy", new Date(milliseconds)).toString();
            viewHolder.setTime(dateString);
        }
    }

    @Override
    public int getItemCount() {
        return card_list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView descView,cargoView,contactView,emailView,enderecoView,cardDate,user_name,cardID;
        private TextView addImageView,addDescView,addCargoView,addContactView,addEmailView,addEnderecoView;
        private ImageView cardImageView;

        private Button btn_savecard_contact;

        private CircleImageView user_image;
        private Bitmap bitmap;
        private Bitmap bitmapQR;
        public final static int QRcodeWidth = 500 ;

        private Uri qrImageURI = null;
        private StorageReference storageReference;
        private FirebaseAuth firebaseAuth;

        private String current_user_id;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            btn_savecard_contact = mView.findViewById(R.id.btn_savecard_contact);
            user_name = mView.findViewById(R.id.card_user_name);
            user_image = mView.findViewById(R.id.card_user_image);
            cardDate = mView.findViewById(R.id.card_user_data);

            if(currentFragment.equals("NotificationFragment")){
                btn_savecard_contact.setVisibility(View.VISIBLE);
            }else{
                user_image.setVisibility(View.INVISIBLE);
                user_name.setVisibility(View.INVISIBLE);
                cardDate.setVisibility(View.INVISIBLE);
            }

            storageReference = FirebaseStorage.getInstance().getReference();
            firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            current_user_id = firebaseAuth.getCurrentUser().getUid();

            btn_savecard_contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addDescView = mView.findViewById(R.id.card_desc);
                    addImageView = mView.findViewById(R.id.card_imageURI);
                    addCargoView = mView.findViewById(R.id.card_cargo);
                    addContactView = mView.findViewById(R.id.card_contact);
                    addEmailView = mView.findViewById(R.id.card_email);
                    addEnderecoView = mView.findViewById(R.id.card_endereco);

                    final String desc = addDescView.getText().toString();
                    final String downloadURI = addImageView.getText().toString();
                    final String colectionID = current_user_id;
                    final String cardCargo = addCargoView.getText().toString();
                    final String cardContact = addContactView.getText().toString();
                    final String cardEmail = addEmailView.getText().toString();
                    final String cardEndereco = addEnderecoView.getText().toString();

                    Map<String, Object> addCardMap = new HashMap<>();
                    addCardMap.put("image_url",downloadURI);
                    addCardMap.put("image_thumb",downloadURI);
                    addCardMap.put("desc",desc);
                    addCardMap.put("contact",cardContact);
                    addCardMap.put("cargo",cardCargo);
                    addCardMap.put("email",cardEmail);
                    addCardMap.put("endereco",cardEndereco);
                    addCardMap.put("user_id",colectionID);
                    addCardMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection(colectionID).add(addCardMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(mView.getContext(), "Cartão adicionado aos seus contatos com sucesso!", Toast.LENGTH_SHORT).show();
                                btn_savecard_contact.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                }
            });

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (user_id.equals(current_user_id)) {
                        final String[] shareLink = new String[1];
                        final String randomName = UUID.randomUUID().toString();
                        CardView savingLayout = mView.findViewById(R.id.main_card_list);
                        File file = saveBitMap(mView.getContext(), savingLayout);
                        if (file != null) {
                            Log.i("TAG", "Drawing saved to the gallery!");
                        } else {
                            Log.i("TAG", "Oops! Image could not be saved.");
                        }

                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(mView.getContext());
                        final View dView = LayoutInflater.from(mView.getContext()).inflate(R.layout.dialog_qrcode, null);
                        final ImageView imageQR = dView.findViewById(R.id.imageQR);
                        final Button btn_share = dView.findViewById(R.id.btn_share);
                        final ProgressBar loarQRProgress = dView.findViewById(R.id.loarQRProgress);
                        final Button deleteCard = dView.findViewById(R.id.btn_deletecard);

                        qrImageURI = bitmapToUriConverter(bitmap);

                        loarQRProgress.setVisibility(View.VISIBLE);

                        StorageReference filePath = storageReference.child("cards_qrcode").child(randomName + ".jpg");
                        filePath.putFile(qrImageURI).addOnCompleteListener(
                                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        final String downloadQRImage;
                                        if (task.isSuccessful()) {
                                            downloadQRImage = task.getResult().getDownloadUrl().toString();
                                            shareLink[0] = downloadQRImage;
                                            try {
                                                bitmapQR = TextToImageEncode(downloadQRImage);
                                                imageQR.setImageBitmap(bitmapQR);
                                                loarQRProgress.setVisibility(View.INVISIBLE);
                                            } catch (WriterException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });

                        mBuilder.setView(dView);
                        final AlertDialog dialog = mBuilder.create();
                        dialog.show();

                        btn_share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra(Intent.EXTRA_TEXT, shareLink[0]);
                                    intent.setType("text/plain");
                                    context.startActivity(Intent.createChooser(intent, "Enviar cartão via"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        deleteCard.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String card_id = getCardIDText();
                                DocumentReference docRef = firebaseFirestore.collection(refDelete).document(card_id);
                                docRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(dView.getContext(), "Cartão excluido com sucesso!", Toast.LENGTH_SHORT).show();
                                            redirectToMain();
                                        } else {
                                            Toast.makeText(dView.getContext(), "Erro ao tentar excluir o cartão.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        });
                    }
                }
            });

        }

        private void redirectToMain(){
            Intent mainIntent = new Intent(mView.getContext(), MainActivity.class);
            mView.getContext().startActivity(mainIntent);
        }
        public void setCardIDText(String text){
            cardID = mView.findViewById(R.id.cardID);
            cardID.setText(text);
        }

        public String getCardIDText(){
            cardID = mView.findViewById(R.id.cardID);
            return cardID.getText().toString();
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
            addImageView = mView.findViewById(R.id.card_imageURI);
            addImageView.setText(downloadUri);
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.color.common_google_signin_btn_text_light_default);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(downloadUri).into(cardImageView);
        }
        public void setTime(String date){
            cardDate = mView.findViewById(R.id.card_user_data);
            cardDate.setText(date);
        }

        public void setDescText(String text){
            descView = mView.findViewById(R.id.card_desc);
            descView.setText(text);
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

        private Bitmap TextToImageEncode(String Value) throws WriterException {
            BitMatrix bitMatrix;
            try {
                bitMatrix = new MultiFormatWriter().encode(
                        Value,
                        BarcodeFormat.DATA_MATRIX.QR_CODE,
                        QRcodeWidth, QRcodeWidth, null
                );

            } catch (IllegalArgumentException Illegalargumentexception) {

                return null;
            }
            int bitMatrixWidth = bitMatrix.getWidth();

            int bitMatrixHeight = bitMatrix.getHeight();

            int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];
            int colorBlack = ContextCompat.getColor(context, R.color.black);
            int colorWhite = ContextCompat.getColor(context, R.color.white);

            for (int y = 0; y < bitMatrixHeight; y++) {
                int offset = y * bitMatrixWidth;

                for (int x = 0; x < bitMatrixWidth; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? colorBlack : colorWhite;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

            bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
            return bitmap;
        }

        public Uri bitmapToUriConverter(Bitmap mBitmap) {
            Uri uri = null;
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, 200, 200);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 500, 300,
                        true);
                File file = new File(mView.getContext().getFilesDir(), "Image"
                        + new Random().nextInt() + ".jpeg");
                FileOutputStream out = mView.getContext().openFileOutput(file.getName(),
                        Context.MODE_PRIVATE);
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                //get absolute path
                String realPath = file.getAbsolutePath();
                File f = new File(realPath);
                uri = Uri.fromFile(f);

            } catch (Exception e) {
                Log.e("Your Error Message", e.getMessage());
            }
            return uri;
        }


        public  int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

    }
}
