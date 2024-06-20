package in.softment.c0ns3nt;

import static java.sql.DriverManager.println;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import in.softment.c0ns3nt.Adapter.ConsentHistoryAdapter;
import in.softment.c0ns3nt.Model.HistoryModel;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;

public class ConsentHistoryActivity extends AppCompatActivity  {

    private TextView noConsentsAvailable;
    private ArrayList<HistoryModel> historyModels;
    private ConsentHistoryAdapter adapter; // This is your custom RecyclerView adapter
    private boolean isFromSettingsPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent_history);


        ImageView backView = findViewById(R.id.back);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        noConsentsAvailable = findViewById(R.id.noConsentAvailable);

        historyModels = new ArrayList<>();
        adapter = new ConsentHistoryAdapter(historyModels); // Initialize with your custom adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        isFromSettingsPage = getIntent().getBooleanExtra("isFromSettings",false);

        backView.setOnClickListener(v -> backBtnClicked());


        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backBtnClicked();
            }
        });
        getConsentHistory();
    }



    private void backBtnClicked() {
        if (isFromSettingsPage) {
            finish();
        } else {
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void getConsentHistory() {
        ProgressHud.show(this,"");
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("History")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                   ProgressHud.dialog.dismiss();

                    if (e != null) {
                        Services.showDialog(ConsentHistoryActivity.this,"ERROR",e.getMessage());
                        return;
                    }

                    historyModels.clear();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            HistoryModel model = document.toObject(HistoryModel.class);
                            historyModels.add(model);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    noConsentsAvailable.setVisibility(historyModels.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }


}
