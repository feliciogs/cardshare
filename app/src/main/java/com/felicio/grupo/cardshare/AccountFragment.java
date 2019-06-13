package com.felicio.grupo.cardshare;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private List<CardClass> card_list;
    private RecyclerView listCardsContacts;
    private String userID;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private CardRecyclerAdapter cardRecyclerAdapter;
    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        card_list = new ArrayList<>();
        listCardsContacts = view.findViewById(R.id.listCardsContacts);
        firebaseAuth = FirebaseAuth.getInstance();
        cardRecyclerAdapter = new CardRecyclerAdapter(card_list);
        listCardsContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        listCardsContacts.setAdapter(cardRecyclerAdapter);

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            userID = firebaseAuth.getCurrentUser().getUid();

            CollectionReference cardsRef = firebaseFirestore.collection(userID);
            Query query = cardsRef.orderBy("timestamp",Query.Direction.DESCENDING);

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if(documentSnapshots != null){
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                CardClass cardClass = doc.getDocument().toObject(CardClass.class);
                                cardClass.setCard_id(doc.getDocument().getId());
                                card_list.add(cardClass);
                                cardRecyclerAdapter.userCurrentID = userID;
                                cardRecyclerAdapter.refDelete = userID;
                                cardRecyclerAdapter.currentFragment = "AccountFragment";
                                cardRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }});
            // Inflate the layout for this fragment
        }

        return view;
    }

}
