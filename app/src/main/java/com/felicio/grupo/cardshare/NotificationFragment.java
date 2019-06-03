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
public class NotificationFragment extends Fragment {

    private RecyclerView cardTodos_list_view;
    private List<CardClass> card_list;
    private String userID;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private CardRecyclerAdapter cardRecyclerAdapter;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        card_list = new ArrayList<>();
        cardTodos_list_view = view.findViewById(R.id.cardTodos_list_view);
        firebaseAuth = FirebaseAuth.getInstance();
        cardRecyclerAdapter = new CardRecyclerAdapter(card_list);
        cardTodos_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        cardTodos_list_view.setAdapter(cardRecyclerAdapter);

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            userID = firebaseAuth.getCurrentUser().getUid();

            CollectionReference cardsRef = firebaseFirestore.collection("Cards");
            Query query = cardsRef.orderBy("timestamp",Query.Direction.DESCENDING);

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if(documentSnapshots != null){
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                CardClass cardClass = doc.getDocument().toObject(CardClass.class);
                                if(!cardClass.getUser_id().equals(userID)){
                                    card_list.add(cardClass);
                                    cardRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                }
            }});
            // Inflate the layout for this fragment
        }

        return view;
    }

}
