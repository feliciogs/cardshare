package com.felicio.grupo.cardshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewCardActivity extends AppCompatActivity {

    private Toolbar newCardToolbar;
    private ImageView newCardImage;
    private EditText newCardName,newCardContact,newCardCargo,newCardEmail,newCardEndereco;
    private TextView text_cardTitle;
    private Button newCardBtn;
    private ProgressBar newCardProgress;

    private Uri cardImageURI = null;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        newCardToolbar = findViewById(R.id.new_card_toolbar);
        setSupportActionBar(newCardToolbar);
        getSupportActionBar().setTitle("Criar um novo cartão");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newCardName = findViewById(R.id.new_card_desc);
        newCardCargo = findViewById(R.id.new_card_cargo);
        newCardContact = findViewById(R.id.new_card_contact);
        newCardEmail = findViewById(R.id.new_card_email);
        newCardEndereco = findViewById(R.id.new_card_endereco);
        text_cardTitle = findViewById(R.id.text_cardTitle);

        newCardImage = findViewById(R.id.new_card_image);
        newCardBtn = findViewById(R.id.card_btn);
        newCardProgress = findViewById(R.id.new_card_progress);

        newCardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewCardActivity.this);
            }
        });

        newCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newCardName.getText().toString();
                final String cargo = newCardCargo.getText().toString();
                final String contact = newCardContact.getText().toString();
                final String email = newCardEmail.getText().toString();
                final String endereco = newCardEndereco.getText().toString();

                if(!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(cargo) && !TextUtils.isEmpty(contact) && !TextUtils.isEmpty(email) && cardImageURI != null){
                    final String randomName = UUID.randomUUID().toString();

                    newCardProgress.setVisibility(View.VISIBLE);

                    StorageReference filePath = storageReference.child("card_images").child(randomName +".jpg");
                    filePath.putFile(cardImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            final String downloadUri = task.getResult().getDownloadUrl().toString();
                            if(task.isSuccessful()){
                                File newImageFile = new File(cardImageURI.getPath());
                                try {
                                    compressedImageFile = new Compressor(NewCardActivity.this)
                                            .setMaxHeight(720)
                                            .setMaxWidth(720)
                                            .setQuality(50)
                                            .compressToBitmap(newImageFile);


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("card_images/thumbs").child(randomName +".jpg").putBytes(thumbData);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String downloadthumbURI = taskSnapshot.getDownloadUrl().toString();

                                        Map<String,Object> cardMap = new HashMap<>();
                                        cardMap.put("image_url",downloadUri);
                                        cardMap.put("image_thumb",downloadthumbURI);
                                        cardMap.put("desc",desc);
                                        cardMap.put("contact",contact);
                                        cardMap.put("cargo",cargo);
                                        cardMap.put("email",email);
                                        cardMap.put("endereco",endereco);
                                        cardMap.put("user_id",current_user_id);
                                        cardMap.put("timestamp",FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Cards").add(cardMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){

                                                    Toast.makeText(NewCardActivity.this, "Cartão criado com sucesso!", Toast.LENGTH_SHORT).show();
                                                    Intent mainIntent = new Intent(NewCardActivity.this,MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                }else{

                                                }
                                                newCardProgress.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                            }else{
                                newCardProgress.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
                }else{
                    Toast.makeText(NewCardActivity.this, "Por favor preencha os campos e selecione uma imagem para o cartão.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cardImageURI = result.getUri();
                newCardImage.setImageURI(cardImageURI);
                text_cardTitle.setVisibility(View.INVISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
