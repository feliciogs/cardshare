package com.felicio.grupo.cardshare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email;
    private EditText reg_pass;
    private EditText reg_confirm_pass;
    private Button reg_btn;
    private Button reg_login_btn;
    private ProgressBar reg_progess;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        reg_email = (EditText) findViewById(R.id.reg_Email);
        reg_pass = (EditText) findViewById(R.id.reg_pass);
        reg_confirm_pass = (EditText) findViewById(R.id.reg_confirm_pass);
        reg_btn = (Button) findViewById(R.id.reg_btn);
        reg_login_btn = (Button) findViewById(R.id.reg_login_btn);
        reg_progess = (ProgressBar) findViewById(R.id.reg_progress);

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = reg_email.getText().toString();
                String pass = reg_pass.getText().toString();
                String confirmPass = reg_confirm_pass.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) & !TextUtils.isEmpty(confirmPass)){
                    if(pass.equals(confirmPass)){

                        reg_progess.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();

                                }else{
                                    String erro = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Erro: "+erro, Toast.LENGTH_SHORT).show();
                                }
                                reg_progess.setVisibility(View.INVISIBLE);
                            }
                        });
                    }else{
                        Toast.makeText(RegisterActivity.this, "Os campos de Senha e Confirmar Senha estão diferentes!", Toast.LENGTH_SHORT).show();
                    }

                }


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
