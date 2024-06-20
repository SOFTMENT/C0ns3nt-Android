package in.softment.c0ns3nt;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.IOException;
import java.util.Objects;

import in.softment.c0ns3nt.Model.UserModel;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;

public class CreateProfileActivity extends AppCompatActivity {

    private RoundedImageView profile_picture;
    private Uri resultUri = null;
    private EditText firstName,lastName, username, phoneNumber, pinNumber, emergencyContact1,emergencyContact2,emergencyContact3,emergencyPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);


        profile_picture = findViewById(R.id.profileImage);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        username = findViewById(R.id.username);
        phoneNumber = findViewById(R.id.phone);
        pinNumber = findViewById(R.id.pinNumber);
        emergencyContact1 = findViewById(R.id.emergency1);
        emergencyContact2 = findViewById(R.id.emergency2);
        emergencyContact3 = findViewById(R.id.emergency3);
        emergencyPin = findViewById(R.id.emergencyPin);

        findViewById(R.id.uploadBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(CreateProfileActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        requestReadStoragePermission();
                    } else {
                        ShowFileChooser();
                    }

                }
                else  {

                    if (ContextCompat.checkSelfPermission(CreateProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        requestReadStoragePermission();
                    } else {
                        ShowFileChooser();
                    }
                }


            }
        });

            findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Services.logout(CreateProfileActivity.this);
            }
        });

        findViewById(R.id.createProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sFirstName = firstName.getText().toString();
                String sLastName = firstName.getText().toString();
                String sUsername = username.getText().toString();
                String sPhone = phoneNumber.getText().toString();
                String sPinNumber = pinNumber.getText().toString();
                String sEmergency1 = emergencyContact1.getText().toString();
                String sEmergency2 = emergencyContact2.getText().toString();
                String sEmergency3 = emergencyContact3.getText().toString();
                String sEmergencyPin = emergencyPin.getText().toString();

                if (resultUri == null) {
                    Services.showCenterToast(CreateProfileActivity.this,"Upload Profile Picture");
                }
                else if (sFirstName.isEmpty()) {
                    Services.showCenterToast(CreateProfileActivity.this,"Enter First Name");
                }
                else if (sLastName.isEmpty()) {
                    Services.showCenterToast(CreateProfileActivity.this,"Enter Last Name");
                }
                else if (sUsername.isEmpty()) {
                    Services.showCenterToast(CreateProfileActivity.this,"Enter Username");
                }
                else if (sPhone.isEmpty()) {
                    Services.showCenterToast(CreateProfileActivity.this,"Enter Phone Number");
                }
                else if (sPinNumber.isEmpty()) {
                    Services.showCenterToast(CreateProfileActivity.this,"Enter Pin Number");
                }
                else if (sPinNumber.length()<4) {
                    Services.showCenterToast(CreateProfileActivity.this,"PIN Number Must Be 4 Character Long.");
                }
                else if (sEmergency1.isEmpty()) {
                    Services.showCenterToast(CreateProfileActivity.this,"Enter Emergency Number 1");
                }
                else if (sEmergencyPin.isEmpty()) {
                    Services.showCenterToast(CreateProfileActivity.this,"Enter Emergency PIN");
                }
                else {
                    ProgressHud.show(CreateProfileActivity.this, "");
                    FirebaseFirestore.getInstance().collection("Users").whereEqualTo("username",sUsername).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    ProgressHud.dialog.dismiss();
                                    Services.showDialog(CreateProfileActivity.this,"ERROR","Username already exists");
                                }
                                else {
                                    UserModel.data.fullName = sFirstName + " "+ sLastName;
                                    UserModel.data.username = sUsername;
                                    UserModel.data.phoneNumber = sPinNumber;
                                    UserModel.data.pinNumber = sPinNumber;
                                    UserModel.data.emergencyPhoneNumber1 = sEmergency1;
                                    UserModel.data.emergencyPhoneNumber2 = sEmergency2;
                                    UserModel.data.emergencyPhoneNumber3 = sEmergency3;
                                    UserModel.data.emergencyPIN = sEmergencyPin;

                                    uploadImageOnFirebase(UserModel.data);
                                }
                            }

                        }
                    });
                }

            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                requestReadStoragePermission();
            }

        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                requestReadStoragePermission();
            }
        }


    }

    private void requestReadStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission is needed to access photo from your external storage.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Prompt the user once explanation has been shown
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ActivityCompat.requestPermissions(CreateProfileActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 123);
                            }
                            else {
                                ActivityCompat.requestPermissions(CreateProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                            }
                        }
                    })
                    .create()
                    .show();
        } else {
            // No explanation needed; request the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 123);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 123: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ShowFileChooser();
                } else {
                    // Permission denied
                    // Handle the feature without the permission or disable it
                }
                return;
            }

            // Other 'case' lines to check for other permissions this app might request
        }
    }
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri filepath = data.getData();
                        resultUri = filepath;
                        Bitmap bitmap = null;
                        try {

                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                            profile_picture.setImageBitmap(bitmap);


                        } catch (IOException ignored) {

                        }

                    }
                }
            });




    public void ShowFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        someActivityResultLauncher.launch(intent);

    }





    private void uploadImageOnFirebase(UserModel userModel) {
        ProgressHud.show(CreateProfileActivity.this,"Wait...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ProfilePicture").child(userModel.getUid()+ ".png");
        UploadTask uploadTask = storageReference.putFile(resultUri);
        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    ProgressHud.dialog.dismiss();
                    throw Objects.requireNonNull(task.getException());
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {

                    userModel.profilePic = String.valueOf(task.getResult());

                }

                FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(userModel, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ProgressHud.dialog.dismiss();
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(CreateProfileActivity.this, MembershipActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });


            }
        });
    }
}
