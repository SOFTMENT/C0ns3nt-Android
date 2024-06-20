package in.softment.c0ns3nt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import in.softment.c0ns3nt.Model.HistoryModel;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;

public class WaitingActivity extends AppCompatActivity {

    private HistoryModel historyModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        Button cancelBtn = findViewById(R.id.cancel);
        LottieAnimationView animationView = findViewById(R.id.animationView);

        // Assuming historyModel is passed through Intent
        historyModel = (HistoryModel) getIntent().getSerializableExtra("historyModel");

        if (historyModel == null) {
            finish();
            return;
        }


        animationView.playAnimation();

        cancelBtn.setOnClickListener(v -> cancelAction());

        checkIsConsentStatus();
    }

    private void checkIsConsentStatus() {
        String uid;
        if (historyModel.getUserWhoScannedCode().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            uid = historyModel.getUserWhichCodeScanned();
        } else {
            uid = historyModel.getUserWhoScannedCode();
        }

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users").document(uid).collection("History").document(historyModel.getId());
        docRef.addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                HistoryModel updatedHistoryModel = snapshot.toObject(HistoryModel.class);
                if (updatedHistoryModel != null && updatedHistoryModel.getStatus().contains("denied")) {

                    ProgressHud.show(WaitingActivity.this,"");
                    Map map = new HashMap();
                    map.put("status","deniedbyother");
                    FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .collection("History").document(historyModel.getId()).set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    ProgressHud.dialog.dismiss();
                                    if (task.isSuccessful()) {

                                        Intent intent = new Intent(WaitingActivity.this, FailedActivity.class);
                                        intent.putExtra("historyModel",historyModel);
                                        startActivity(intent);
                                    }
                                    else {
                                        Services.showDialog(WaitingActivity.this,"ERROR",task.getException().getMessage());
                                    }
                                }
                            });
                } else if ("verified".equals(updatedHistoryModel.getStatus())) {
                    startActivity(new Intent(WaitingActivity.this,SuccessActivity.class));
                }
            }
        });
    }

    private void cancelAction() {
        ProgressHud.show(this,"");

        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String historyModelId =  historyModel.getId();

        FirebaseFirestore.getInstance().collection("Users").document(currentUserUID).collection("History").document(historyModelId)
                .update("status", "denied")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid;
                        if (historyModel.getUserWhoScannedCode().equals(currentUserUID)) {
                            uid = historyModel.getUserWhichCodeScanned();
                        } else {
                            uid = historyModel.getUserWhoScannedCode();
                        }

                        FirebaseFirestore.getInstance().collection("Users").document(uid).collection("History").document(historyModelId)
                                .update("status", "deniedbyother")
                                .addOnCompleteListener(task1 -> {
                                   ProgressHud.dialog.dismiss();
                                    if (task1.isSuccessful()) {
                                        Services.cancelConsent(WaitingActivity.this,historyModel,false);
                                    }
                                });
                    } else {
                        ProgressHud.dialog.dismiss();
                    }
                });
    }

}
