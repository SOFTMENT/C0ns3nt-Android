package in.softment.c0ns3nt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class FailedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failed);

        TextView subheading = findViewById(R.id.subtitle);
        TextView consentDenied = findViewById(R.id.title);
        LottieAnimationView animation = findViewById(R.id.animationView);
        animation.animate();
        animation.playAnimation();

        // Assuming 'response' is passed through intent
        String response = getIntent().getStringExtra("RESPONSE");
        if (response != null && response.equals("deniedbyemergency")) {
            consentDenied.setText("");
            subheading.setText("Consent is experiencing technical difficulties.");
        }

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(FailedActivity.this, ConsentHistoryActivity.class); // Replace with the correct activity
            startActivity(intent);



        }, 3000);
    }
}
