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
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
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

public class EditProfileViewController extends AppCompatActivity {

    private RoundedImageView profile_picture;
    private Uri resultUri = null;
    private EditText firstName, lastName, username, phoneNumber, pinNumber, emergencyContact1,emergencyContact2,emergencyContact3,emergencyPin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_view_controller);


        profile_picture = findViewById(R.id.profileImage);

        if (!UserModel.data.getProfilePic().isEmpty()) {
            Glide.with(this).load(UserModel.data.getProfilePic()).placeholder(R.drawable.profileplaceholder).into(profile_picture);
        }

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);

        String[] parts = UserModel.data.getFullName().split(" ");

        String sfirstName = "";
        String slastName = "";

        if (parts.length > 1) {
            sfirstName = parts[0]; // First part is the first name
            slastName = parts[1]; // Second part is the last name
        } else if (parts.length == 1) {
            sfirstName = parts[0]; // Only one part, assume it as first name
        }

        firstName.setText(sfirstName);
        lastName.setText(slastName);


        username = findViewById(R.id.username);
        username.setText(UserModel.data.getUsername());

       firstName.setEnabled(false);
        lastName.setEnabled(false);
        username.setEnabled(false);

        phoneNumber = findViewById(R.id.phone);
        phoneNumber.setText(UserModel.data.getPhoneNumber());

        pinNumber = findViewById(R.id.pinNumber);
        pinNumber.setText(UserModel.data.getPinNumber());

        emergencyContact1 = findViewById(R.id.emergency1);
        emergencyContact1.setText(UserModel.data.getEmergencyPhoneNumber1());

        emergencyContact2 = findViewById(R.id.emergency2);
        emergencyContact2.setText(UserModel.data.getEmergencyPhoneNumber2());

        emergencyContact3 = findViewById(R.id.emergency3);
        emergencyContact3.setText(UserModel.data.getEmergencyPhoneNumber3());

        emergencyPin = findViewById(R.id.emergencyPin);
        emergencyPin.setText(UserModel.data.getEmergencyPIN());

        findViewById(R.id.uploadBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(EditProfileViewController.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        requestReadStoragePermission();
                    } else {
                        ShowFileChooser();
                    }

                }
                else  {

                    if (ContextCompat.checkSelfPermission(EditProfileViewController.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                finish();
            }
        });

        findViewById(R.id.createProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sPhone = phoneNumber.getText().toString();
                String sPinNumber = pinNumber.getText().toString();
                String sEmergency1 = emergencyContact1.getText().toString();
                String sEmergency2 = emergencyContact2.getText().toString();
                String sEmergency3 = emergencyContact3.getText().toString();
                String sEmergencyPin = emergencyPin.getText().toString();


                if (sPhone.isEmpty()) {
                    Services.showCenterToast(EditProfileViewController.this,"Enter Phone Number");
                }
                else if (sPinNumber.isEmpty()) {
                    Services.showCenterToast(EditProfileViewController.this,"Enter Pin Number");
                }
                else if (sPinNumber.length()<4) {
                    Services.showCenterToast(EditProfileViewController.this,"PIN Number Must Be 4 Character Long.");
                }
                else if (sEmergency1.isEmpty()) {
                    Services.showCenterToast(EditProfileViewController.this,"Enter Emergency Number 1");
                }
                else if (sEmergencyPin.isEmpty()) {
                    Services.showCenterToast(EditProfileViewController.this,"Enter Emergency PIN");
                }
                else {
                    ProgressHud.show(EditProfileViewController.this, "");


                                    UserModel.data.phoneNumber = sPinNumber;
                                    UserModel.data.pinNumber = sPinNumber;
                                    UserModel.data.emergencyPhoneNumber1 = sEmergency1;
                                    UserModel.data.emergencyPhoneNumber2 = sEmergency2;
                                    UserModel.data.emergencyPhoneNumber3 = sEmergency3;
                                    UserModel.data.emergencyPIN = sEmergencyPin;
                                    if (resultUri != null) {
                                        uploadImageOnFirebase(UserModel.data);
                                    }
                                    else {
                                        ProgressHud.show(EditProfileViewController.this,"");
                                        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(UserModel.data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ProgressHud.dialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    Services.showCenterToast(EditProfileViewController.this,"Updated");
                                                   new Handler().postDelayed(new Runnable() {
                                                       @Override
                                                       public void run() {
                                                           finish();
                                                       }
                                                   },1200);
                                                }
                                                else {
                                                    Services.showDialog(EditProfileViewController.this,"ERROR",task.getException().getMessage());
                                                }
                                            }
                                        });
                                    }
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
                                ActivityCompat.requestPermissions(EditProfileViewController.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 123);
                            }
                            else {
                                ActivityCompat.requestPermissions(EditProfileViewController.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
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
        ProgressHud.show(EditProfileViewController.this,"Wait...");
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
                            Intent intent = new Intent(EditProfileViewController.this, MembershipActivity.class);
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
