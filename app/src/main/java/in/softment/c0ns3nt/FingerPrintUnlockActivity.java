package in.softment.c0ns3nt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

import in.softment.c0ns3nt.Model.HistoryModel;
import in.softment.c0ns3nt.Util.Services;

public class FingerPrintUnlockActivity extends AppCompatActivity {

    private TextView authTitle;
    private ImageView authImage;
    private HistoryModel historyModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_unlock);

        historyModel = (HistoryModel) getIntent().getSerializableExtra("historyModel");

        authTitle = findViewById(R.id.heading);
        authImage = findViewById(R.id.image);
        ImageView backBtn = findViewById(R.id.back);
        Button startVerificationBtn = findViewById(R.id.startVerification);

        startVerificationBtn.setOnClickListener(view -> startBiometricAuthentication());
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Services.cancelConsent(FingerPrintUnlockActivity.this,historyModel,false);
            }
        });

        updateUIBasedOnBiometricAvailability();
    }

    private void startBiometricAuthentication() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(FingerPrintUnlockActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent intent = new Intent(FingerPrintUnlockActivity.this,PinNumberVerificationActivity.class);
                intent.putExtra("historyModel",historyModel);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Services.showDialog(FingerPrintUnlockActivity.this,"ERROR", String.valueOf(errString));
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Services.showDialog(FingerPrintUnlockActivity.this,"Authentication failed","You could not be verified, Please try again.");
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void updateUIBasedOnBiometricAvailability() {
        BiometricType biometricType = getBiometricType();
        switch (biometricType) {
            case FACE:
                authTitle.setText("Face Unlock");
                authImage.setImageResource(R.drawable.face);
                break;
            case FINGERPRINT:
                authTitle.setText("Fingerprint Unlock");
                authImage.setImageResource(R.drawable.fingerprint);
                // Set fingerprint image
                break;
            case NONE:

                break;
        }
    }

    private BiometricType getBiometricType() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {// This means that a biometric sensor is available and the user is enrolled.
            return (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) ? BiometricType.FINGERPRINT : BiometricType.FACE;
        }
        return BiometricType.NONE;
    }

    enum BiometricType {
        NONE,

        FINGERPRINT,
        FACE
    }
}
