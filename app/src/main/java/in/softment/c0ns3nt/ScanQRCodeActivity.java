package in.softment.c0ns3nt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import in.softment.c0ns3nt.Model.HistoryModel;
import in.softment.c0ns3nt.Model.UniqueModel;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.google.firebase.firestore.FirebaseFirestore;

public class ScanQRCodeActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private String myUniqueId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        myUniqueId = getIntent().getStringExtra("uniqueID");

        // Initialize the barcode scanner view
        barcodeView = findViewById(R.id.barcode_scanner);
        barcodeView.setStatusText("");
        barcodeView.decodeContinuous(barcodeCallback);
    }

    // Barcode scanning callback
    private final BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                // Handle the scanned QR code value
                barcodeView.pause(); // Pause scanning
                String scannedValue = result.getText();
                checkQRCode(scannedValue);
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume(); // Resume scanning when the activity is in focus
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause(); // Pause scanning when the activity is not in focus
    }

    private void checkQRCode(String value) {

        ProgressHud.show(this,"");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        db.collection("UniqueId").document(value).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            UniqueModel uniqueModel = documentSnapshot.toObject(UniqueModel.class);
                            HistoryModel historyModel = new HistoryModel();
                            historyModel.setUniqueId(value);
                            historyModel.setUserWhoScannedCode(auth.getCurrentUser().getUid());
                            historyModel.setDate(new Date());
                            historyModel.setUserWhichCodeScanned(uniqueModel.getUid());
                            historyModel.setStatus("pending");

                            WriteBatch batch = db.batch();

                            String id = db.collection("Users").document(uniqueModel.getUid()).collection("History").document().getId();
                            historyModel.setId(id);


                            DocumentReference historyRef = db.collection("Users").document(auth.getCurrentUser().getUid()).collection("History").document(id);
                            batch.set(historyRef, historyModel);


                                            HistoryModel historyModel1 = new HistoryModel();
                                            historyModel1.setUniqueId(myUniqueId);
                                            historyModel1.setUserWhoScannedCode(auth.getCurrentUser().getUid());
                                            historyModel1.setDate(new Date());
                                            historyModel1.setUserWhichCodeScanned(uniqueModel.getUid());
                                            historyModel1.setStatus("pending");
                                            historyModel1.setId(id);

                                            DocumentReference historyRef1 = db.collection("Users").document(uniqueModel.getUid()).collection("History").document(id);
                                            batch.set(historyRef1, historyModel1);

                                            batch.commit()
                                                    .addOnSuccessListener(aVoid -> {
                                                        ProgressHud.dialog.dismiss();

                                                        Intent intent = new Intent(ScanQRCodeActivity.this, FingerPrintUnlockActivity.class);
                                                        intent.putExtra("historyModel",historyModel);
                                                        startActivity(intent);


                                                    })
                                                    .addOnFailureListener(e -> {
                                                        ProgressHud.dialog.dismiss();
                                                        Services.showDialog(ScanQRCodeActivity.this,"ERROR",e.getMessage());
                                                    });

                        } catch (Exception e) {
                            ProgressHud.dialog.dismiss();
                            Services.showDialog(ScanQRCodeActivity.this,"ERROR",e.getMessage());
                        }
                    } else {
                        ProgressHud.dialog.dismiss();
                        Services.showDialog(ScanQRCodeActivity.this,"ERROR","Invalid QR Code");
                    }
                })
                .addOnFailureListener(e -> {
                    ProgressHud.dialog.dismiss();
                    Services.showDialog(ScanQRCodeActivity.this,"ERROR",e.getMessage());
                });
    }
}
