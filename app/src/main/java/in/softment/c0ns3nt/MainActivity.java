package in.softment.c0ns3nt;


import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationRequestCompat;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import in.softment.c0ns3nt.Model.HistoryModel;
import in.softment.c0ns3nt.Model.UniqueModel;
import in.softment.c0ns3nt.Model.UserModel;
import in.softment.c0ns3nt.Util.Constants;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private RelativeLayout consentDialog;
    HistoryModel historyModel;

    private TextView hiUsername, date, tierName, totalUniqueId, uniqueIdNumber, consentRequestMessage;
    private ImageView profilePic, qrImage;

    private LinearLayout refreshUniqueIDBtn;
    private AppCompatButton startConsentBtn, noBtn, yesBtn;
    private UniqueModel uniqueModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase and location services


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI components
        consentDialog = findViewById(R.id.dialog);
        hiUsername = findViewById(R.id.fullName);
        date = findViewById(R.id.date);
        date.setText(formatDate(new Date()));
        tierName = findViewById(R.id.tier);
        totalUniqueId = findViewById(R.id.uniqueId);
        uniqueIdNumber = findViewById(R.id.uniqueIdNumbeer);
        consentRequestMessage = findViewById(R.id.dialogMessage);
        profilePic = findViewById(R.id.profile);
        qrImage = findViewById(R.id.qr);
        startConsentBtn = findViewById(R.id.startConsentBtn);
        refreshUniqueIDBtn = findViewById(R.id.refreshUniqueId);
        noBtn = findViewById(R.id.buttonNo);
        yesBtn = findViewById(R.id.buttonYes);


        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileViewActivity.class));
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request for permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 129);
        }
        // Setup UI and listeners
        setupUI(UserModel.data);
    }

    private void checkPendingConsent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users")
                .document(currentUserId)
                .collection("History")
                .orderBy("date", Query.Direction.DESCENDING)
                .whereEqualTo("status", "pending")
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Handle the error
                            return;
                        }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            DocumentSnapshot document = snapshots.getDocuments().get(0);
                            historyModel = document.toObject(HistoryModel.class);

                            String uid;
                            if (historyModel.userWhoScannedCode.equals(currentUserId)) {
                                uid = historyModel.userWhichCodeScanned;
                            } else {
                                uid = historyModel.userWhoScannedCode;
                            }

                            getUserDataByUid(uid, new UserDataCallback() {
                                @Override
                                public void onUserDataReceived(UserModel userModel, String errorMessage) {
                                    if (userModel != null) {
                                        // Update UI based on userModel
                                        consentDialog.setVisibility(View.VISIBLE); // Assuming you have a View named consentRequestView
                                        consentRequestMessage.setText("Do you agree to consensual interaction with " + userModel.getFullName() + "?");
                                    }
                                }
                            });
                        } else {
                            consentDialog.setVisibility(View.GONE);
                        }
                    }
                });
    }

    public String formatDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("EEEE dd, MMMM");

        // Set the timezone to UTC
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Change the timezone to the current timezone
        df.setTimeZone(TimeZone.getDefault());

        return df.format(date);
    }

    interface UserDataCallback {
        void onUserDataReceived(UserModel userModel, String errorMessage);
    }

    // Implement the getUserDataById method
    private void getUserDataByUid(String uid, final UserDataCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    UserModel userModel = document.toObject(UserModel.class);
                    callback.onUserDataReceived(userModel, null);
                } else {
                    callback.onUserDataReceived(null, "Not Found");
                }
            } else {
                if (task.getException() != null) {
                    callback.onUserDataReceived(null, task.getException().getMessage());
                } else {
                    callback.onUserDataReceived(null, "Error fetching document");
                }
            }
        });
    }

    private void setupUI(UserModel user) {
        // Load user data from Firebase or local preferences

        checkPendingConsent();

        switch (Constants.membershipType) {
            case "BLACK":
                updateUniqueId(0);
                refreshUniqueIDBtn.setVisibility(View.VISIBLE);
                tierName.setText("BLACK TIER");
                totalUniqueId.setText("Unlimited Unique ID");
                break;
            case "PLATINUM":
                int position = user.codePosition;
                if (position > 4) {
                    updateUniqueId(position % 5);
                } else {
                    updateUniqueId(0);
                }
                refreshUniqueIDBtn.setVisibility(View.VISIBLE);
                tierName.setText("PLATINUM TIER");
                totalUniqueId.setText("5 Unique ID");
                break;
            case "GOLD":
                updateUniqueId(0);
                refreshUniqueIDBtn.setVisibility(View.GONE);
                tierName.setText("GOLD TIER");
                totalUniqueId.setText("1 Unique ID");
                break;
            default:
                updateUniqueId(0);
                refreshUniqueIDBtn.setVisibility(View.GONE);
                tierName.setText("FREE TIER");
                totalUniqueId.setText("1 Unique ID");
                break;
        }

        hiUsername.setText("Hi " + user.getUsername());
        Glide.with(this).load(user.profilePic).placeholder(R.drawable.profileplaceholder).into(profilePic);

        // Set up button listeners
        startConsentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle start consent button click
                startConsent();
            }
        });

        refreshUniqueIDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle refresh unique ID button click
                refreshUniqueId();
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle 'no' button click
                handleNoAction();
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle 'yes' button click
                handleYesAction();
            }
        });

        // Request location updates
        requestLocationUpdates();
    }

    private void updateUniqueId(int position) {
        ProgressHud.show(this, "");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UniqueId")
                .whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    ProgressHud.dialog.dismiss();

                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!task.getResult().isEmpty() && task.getResult().getDocuments().size() > position) {
                             uniqueModel = task.getResult().getDocuments().get(position).toObject(UniqueModel.class);
                            if (uniqueModel != null) {
                                uniqueIdNumber.setText("Unique ID - " + (uniqueModel.uniqeId != null ? uniqueModel.uniqeId : "123"));
                                qrImage.setImageBitmap(generateQrCode(uniqueModel.uniqeId));
                            }
                        }
                    } else {
                        if (task.getException() != null) {
                            Services.showDialog(MainActivity.this, "ERROR", task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    public Bitmap generateQrCode(String code) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.QR_CODE, 900, 900);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace(); // Handle the exception
            return null;
        }
    }

    private void startConsent() {
        Intent intent = new Intent(MainActivity.this, ScanQRCodeActivity.class);
        intent.putExtra("uniqueID",uniqueModel.uniqeId);
        startActivity(intent);

    }

    private void refreshUniqueId() {
        UserModel userModel = UserModel.data;



        if (Constants.membershipType.equals("PLATINUM")) {
            int position = userModel.getCodePosition();
            if (position > 4) {
                position++;
                userModel.setCodePosition(position);
                FirebaseFirestore.getInstance().collection("Users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("codePosition", FieldValue.increment(1));
                updateUniqueId(position % 5);

            }
            else {
                generateNew();
            }
        }
        else {
            generateNew();
        }



    }

    public void generateNew(){
        ProgressHud.show(this, "");

        uniqueModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        uniqueModel.uniqeId = Services.generateUniqueCode();
        uniqueModel.date = new Date();

        uniqueIdNumber.setText("Unique ID - " + uniqueModel.uniqeId);
        Bitmap qrCodeBitmap = generateQrCode(uniqueModel.uniqeId); // Use the method from the previous response
        qrImage.setImageBitmap(qrCodeBitmap);

        FirebaseFirestore.getInstance().collection("UniqueId")
                .document(uniqueModel.uniqeId)
                .set(uniqueModel)
                .addOnCompleteListener(task -> {
                    ProgressHud.dialog.dismiss();
                    if (task.isSuccessful()) {
                        Services.showCenterToast(MainActivity.this, "Unique Id Refreshed");
                        if (Constants.membershipType.equals("PLATINUM")) {
                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .update("codePosition", FieldValue.increment(1));
                        }
                    } else {
                        if (task.getException() != null) {
                            Services.showDialog(MainActivity.this, "ERROR", task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    private void handleNoAction() {
        if (historyModel != null) {
            ProgressHud.show(MainActivity.this, "");

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Users").document(currentUserId).collection("History").document(historyModel.id)
                    .update("status", "denied")
                    .addOnCompleteListener(task -> {
                        ProgressHud.dialog.dismiss();
                        consentDialog.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            String uid = historyModel.userWhoScannedCode.equals(currentUserId)
                                    ? historyModel.userWhichCodeScanned
                                    : historyModel.userWhoScannedCode;

                            db.collection("Users").document(uid).collection("History").document(historyModel.id)
                                    .update("status", "deniedbyother");
                        }
                    });
        }
    }

    private void handleYesAction() {
        if (historyModel != null) {

            Intent intent = new Intent(MainActivity.this, FingerPrintUnlockActivity.class);
            intent.putExtra("historyModel", historyModel);
            startActivity(intent);
        }
    }

    private void requestLocationUpdates() {
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if necessary
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {
            // Start location updates
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(LocationRequestCompat.QUALITY_HIGH_ACCURACY, 100000)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize location requests here.
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if (locationResult != null) {
                                    Location location = locationResult.getLastLocation();
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    UserModel.data.latitude = latitude;
                                    UserModel.data.longitude = longitude;

                                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                    List<Address> addresses;
                                    try {
                                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                        if (addresses != null && !addresses.isEmpty()) {
                                            Address address = addresses.get(0);
                                            String completeAddress = address.getSubLocality() + " " +
                                                    address.getThoroughfare() + ", " +
                                                    address.getPostalCode() + ", " +
                                                    address.getLocality();

                                            // Set the address in your constants or user model
                                            Constants.address = completeAddress;

                                            // Generate geohash
                                            String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));
                                            UserModel.data.setGeoHash(hash);

                                            // Update Firestore
                                            FirebaseFirestore.getInstance().collection("Users")
                                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .set(UserModel.data, SetOptions.merge());
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        // Handle the exception
                                    }
                                }
                            }
                        },
                        Looper.getMainLooper());
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, 123);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Handle the error.
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                // The user agreed to make required location settings changes
                startLocationUpdates();
            }  // The user chose not to make required location settings changes

        }
    }
}
