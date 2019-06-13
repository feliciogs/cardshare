package com.felicio.grupo.cardshare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private String current_user_id;
    private ProgressBar progressLoad;
    private Button new_card_btn;

    public BottomNavigationView mainBottomNav;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressLoad = findViewById(R.id.progressLoad);
        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("CardShare");

        if(mAuth.getCurrentUser() != null) {
            mainBottomNav = findViewById(R.id.mainBottonNav);

            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

            replaceFragment(homeFragment);

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.btn_bottom_home:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.btn_bottom_notification:
                            replaceFragment(notificationFragment);
                            return true;
                        case R.id.btn_bottom_account:
                            replaceFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            /*
            new_card_btn = findViewById(R.id.new_card_btn);
            new_card_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newCardIntent = new Intent(MainActivity.this, NewCardActivity.class);
                    startActivity(newCardIntent);
                }
            });
            */
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        progressLoad.setVisibility(View.VISIBLE);
        if(currentUser == null){
            redirectToLogin();
        }else{
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    }else{
                        String erro = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Erro: "+erro, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            replaceFragment(homeFragment);
        }
        progressLoad.setVisibility(View.INVISIBLE);
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_logout:
                logout();
                return true;

            case R.id.action_setting_btn:
                Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(setupIntent);
                return true;

            case R.id.action_newcard_btn:
                Intent newCardIntent = new Intent(MainActivity.this, NewCardActivity.class);
                startActivity(newCardIntent);
                return true;
            default:
                return false;
        }
    }

    private void logout() {
        mAuth.signOut();
        redirectToLogin();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();

    }
}
