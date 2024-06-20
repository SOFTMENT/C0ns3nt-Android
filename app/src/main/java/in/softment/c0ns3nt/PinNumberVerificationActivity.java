package in.softment.c0ns3nt;

import static com.facebook.appevents.codeless.internal.UnityReflection.sendMessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.softment.c0ns3nt.Model.HistoryModel;
import in.softment.c0ns3nt.Model.UserModel;
import in.softment.c0ns3nt.Util.Constants;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;

public class PinNumberVerificationActivity extends AppCompatActivity {

    private EditText number1, number2, number3, number4;
    private HistoryModel historyModel; // Define HistoryModel class according to your data structure

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_number_verification);

        historyModel = (HistoryModel) getIntent().getSerializableExtra("historyModel");

        // Initialize views
        number1 = findViewById(R.id.box1);
        number2 = findViewById(R.id.box2);
        number3 = findViewById(R.id.box3);
        number4 = findViewById(R.id.box4);
        Button confirmBtn = findViewById(R.id.confirm);
        ImageView backBtn = findViewById(R.id.back);

        // Set up back button
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Services.cancelConsent(PinNumberVerificationActivity.this,historyModel,false);
            }
        });

        // Set up confirm button
        confirmBtn.setOnClickListener(v -> onConfirmButtonClicked());

        // Focus on first PIN field
        number1.requestFocus();

        // Set up text change listeners for PIN fields
        setUpPinField(number1, number2);
        setUpPinField(number2, number3);
        setUpPinField(number3, number4);
        setUpPinField(number4, null);


    }

    private void setUpPinField(EditText currentField, EditText nextField) {
        currentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && nextField != null) {
                    nextField.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void onConfirmButtonClicked() {
        String sPin1 = number1.getText().toString();
        String sPin2 = number2.getText().toString();
        String sPin3 = number3.getText().toString();
        String sPin4 = number4.getText().toString();

        if (sPin1.isEmpty() || sPin2.isEmpty() || sPin3.isEmpty() || sPin4.isEmpty()) {
            showError("Enter Correct PIN");
            return;
        }

        String sFullPIN = sPin1 + sPin2 + sPin3 + sPin4;

        // Verify PIN logic here. Example:
        if (UserModel.data.pinNumber.equals(sFullPIN)) {
            ProgressHud.show(PinNumberVerificationActivity.this,"");
            Map map = new HashMap();
            map.put("status","verified");
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("History").document(historyModel.getId()).set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ProgressHud.dialog.dismiss();
                            if (task.isSuccessful()) {

                                Intent intent = new Intent(PinNumberVerificationActivity.this, WaitingActivity.class);
                                intent.putExtra("historyModel",historyModel);
                                startActivity(intent);
                            }
                            else {
                                Services.showDialog(PinNumberVerificationActivity.this,"ERROR",task.getException().getMessage());
                            }
                        }
                    });
        } else if (UserModel.data.emergencyPIN.equals(sFullPIN)){
            String googleMap = "https://maps.google.com/?q=" + UserModel.data.getLatitude() + "," + UserModel.data.getLongitude();
            String message = "Hello! This is an Emergency Alert from the app Black by C0ns3nt. " +
                    (UserModel.data.getFullName() != null ? UserModel.data.getFullName() : "Unknown") +
                    " has entered their EMERGENCY PIN at " + Services.convertDateToString(new Date()) +
                    " at this location " + Constants.address + ".\n\n" + googleMap;

            // Send messages to emergency contacts
            sendMessage(UserModel.data.getEmergencyPhoneNumber1(), message);
            sendMessage(UserModel.data.getEmergencyPhoneNumber2(), message);
            sendMessage(UserModel.data.getEmergencyPhoneNumber3(), message);

            updateFirestoreForEmergency();

            showEmergencyAlert();
        }
        else {
            showError("Incorrect PIN Number");
        }

        // Add logic for emergency PIN and other functionalities
    }


    public void sendMessage(String phoneNumber, String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = "https://softment.in/consent/Twilio/send_sms.php";
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String urlParameters = "number=" + URLEncoder.encode(phoneNumber, "UTF-8") +
                            "&message=" + URLEncoder.encode(message, "UTF-8");

                    // Send post request
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    int responseCode = con.getResponseCode();
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void updateFirestoreForEmergency() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String historyDocumentId = historyModel.getId(); // Implement this to get the history model ID

        Map<String, Object> data = new HashMap<>();
        data.put("status", "deniedbyemergency");

        db.collection("Users").document(currentUserId).collection("History")
                .document(historyDocumentId).set(data, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid;


                            if (historyModel.getUserWhoScannedCode().equals(UserModel.data.getUid())) {
                                uid = historyModel.getUserWhichCodeScanned();

                            } else {
                                uid = historyModel.getUserWhoScannedCode();

                            }

                            FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                            DocumentReference docRef = db1.collection("Users")
                                    .document(uid)
                                    .collection("History")
                                    .document(historyModel.getId());

                            docRef.set(Collections.singletonMap("status", "deniedbyother"), SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Handle success (e.g., navigate to another activity)
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Services.showDialog(PinNumberVerificationActivity.this,"ERROR",e.getMessage());
                                        }
                                    });
                        }
                    else {
                        Services.showDialog(PinNumberVerificationActivity.this,"ERROR",task.getException().getMessage());
                    }

                });
    }

    private void showEmergencyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Emergency PIN Activated")
                .setMessage("We have sent your current location to all your emergency contact numbers.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Services.cancelConsent(PinNumberVerificationActivity.this,historyModel,true);
                    }
                })
                .show();
    }
    private void showError(String errorMessage) {
        Services.showDialog(this,"ERROR",errorMessage);
    }
}
