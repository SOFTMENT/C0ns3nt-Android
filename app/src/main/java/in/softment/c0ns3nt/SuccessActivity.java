package in.softment.c0ns3nt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        LottieAnimationView animation = findViewById(R.id.animationView);


        animation.playAnimation();

        // Delay for 3 seconds before transitioning
        new Handler().postDelayed(() -> {
            // Intent to start another activity after 3 seconds
            Intent intent = new Intent(SuccessActivity.this, ConsentHistoryActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
