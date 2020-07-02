package com.nightdream.ttpassenger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ViewHolder extends FirebaseRecyclerAdapter<requestGetterSetter, ViewHolder.PastViewHolder> {

    String id, uID;
    //Firebase
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    Context context;

    public ViewHolder(@NonNull FirebaseRecyclerOptions<requestGetterSetter> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull PastViewHolder pastViewHolder, int i, @NonNull requestGetterSetter requestGetterSetter) {
        pastViewHolder.clatView.setText(requestGetterSetter.getClat());
        pastViewHolder.clngView.setText(requestGetterSetter.getClng());
        pastViewHolder.dlatView.setText(requestGetterSetter.getDlat());
        pastViewHolder.dlngView.setText(requestGetterSetter.getDlng());
        pastViewHolder.clocationView.setText(requestGetterSetter.getcLocation());
        pastViewHolder.dlocationView.setText(requestGetterSetter.getdLocation());
        id = this.getRef(i).getKey();
    }

    @NonNull
    @Override
    public PastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
        return new PastViewHolder(view);
    }

    class PastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView clatView, clngView, dlatView, dlngView, clocationView, dlocationView;

        public PastViewHolder(@NonNull View itemView) {
            super(itemView);
            clatView = itemView.findViewById(R.id.cLat);
            clngView = itemView.findViewById(R.id.cLng);
            dlatView = itemView.findViewById(R.id.dLat);
            dlngView = itemView.findViewById(R.id.dLng);
            clocationView = itemView.findViewById(R.id.cLocation);
            dlocationView = itemView.findViewById(R.id.dLocation);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            database = FirebaseDatabase.getInstance();
            reference = database.getReference();
            mAuth = FirebaseAuth.getInstance();
            uID = mAuth.getCurrentUser().getUid();

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            transaction(v);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            Toast.makeText(v.getContext(), "Too bad...", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setMessage("Would you like to accept this ride request?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    private void transaction(View v) {
        final HashMap<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("driver", uID);
        sessionMap.put("status", "waiting");

        reference.child("rideTransaction").child(id).updateChildren(sessionMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    startQrCode();
                }
            }
        });
    }

    private void startQrCode() {
        Intent intent = new Intent(context, QrCodeHandler.class);
        intent.putExtra("keyId", id);
        context.startActivity(intent);
    }

}
