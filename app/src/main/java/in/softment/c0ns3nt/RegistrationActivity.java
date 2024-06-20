package in.softment.c0ns3nt;

import android.Manifest;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import in.softment.c0ns3nt.Model.UserModel;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;

public class RegistrationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        EditText emailAddress = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);

        findViewById(R.id.tos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://c0ns3nt.com/index.php/terms-of-service/"));
                startActivity(browserIntent);
            }
        });

        //BACK
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.signUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String sEmail = emailAddress.getText().toString().trim();
                String sPassword = password.getText().toString().trim();
                if (sEmail.isEmpty()) {
                    Services.showCenterToast(RegistrationActivity.this, "Enter Email Address");
                } else if (sPassword.isEmpty()) {
                    Services.showCenterToast(RegistrationActivity.this, "Enter Password");
                } else {
                    ProgressHud.show(RegistrationActivity.this, "Creating Account...");
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            ProgressHud.dialog.dismiss();
                            if (task.isSuccessful()) {
                                UserModel userModel = new UserModel();
                                userModel.email = sEmail;
                                userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                userModel.registredAt = new Date();
                                userModel.regiType = "custom";

                                Services.addUserDataOnServer(RegistrationActivity.this, userModel);
                            } else {

                                Services.showDialog(RegistrationActivity.this, "ERROR", task.getException().getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        });

        findViewById(R.id.signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }
}
