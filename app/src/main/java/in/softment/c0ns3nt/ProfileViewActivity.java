package in.softment.c0ns3nt;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
// ... Other necessary imports

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import in.softment.c0ns3nt.Model.UserModel;
import in.softment.c0ns3nt.Util.Constants;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;
// ... Other necessary imports

public class ProfileViewActivity extends AppCompatActivity {

    private ImageView backView, profileImage, editProfileBtn, goPremiumImage;
    private TextView name;
    private TextView email;
    private TextView goPremiumTitle;
    private TextView logout;
    private View consentHistory, deleteAccountBtn, goPremiumView, termsOfService, privacy, inviteFriends, rateApp, helpCentre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        // Initialize views
        backView = findViewById(R.id.back);
        profileImage = findViewById(R.id.profilePic);
        editProfileBtn = findViewById(R.id.edit);
        goPremiumImage = findViewById(R.id.membershipImage);
        name = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        TextView version = findViewById(R.id.version);
        goPremiumTitle = findViewById(R.id.membershipType);
        logout = findViewById(R.id.logout);
        consentHistory = findViewById(R.id.consent_history);
        deleteAccountBtn = findViewById(R.id.delete_account);
        goPremiumView = findViewById(R.id.goPremium);
        termsOfService = findViewById(R.id.termsOfUse);
        privacy = findViewById(R.id.privacyPolicy);
        inviteFriends = findViewById(R.id.share_app);
        rateApp = findViewById(R.id.rate_app);
        helpCentre = findViewById(R.id.help_center);

        try {
            PackageManager packageManager = this.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            version.setText(String.valueOf(versionCode));
            // Use the version code as needed
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Handle the exception
        }

        // Set click listeners
        setClickListeners();

        // Load user data and other initializations
        loadUserData();
    }

    private void setClickListeners() {
        backView.setOnClickListener(v -> finish());
        editProfileBtn.setOnClickListener(v -> editProfile());
        rateApp.setOnClickListener(v -> rateApp());
        inviteFriends.setOnClickListener(v -> inviteFriends());
        termsOfService.setOnClickListener(v -> redirectToTermsOfService());
        privacy.setOnClickListener(v -> redirectToPrivacyPolicy());
        logout.setOnClickListener(v -> logout());
        helpCentre.setOnClickListener(v -> helpCentre());
        goPremiumView.setOnClickListener(v -> goPremium());
        consentHistory.setOnClickListener(v -> consentHistory());
        deleteAccountBtn.setOnClickListener(v -> deleteAccount());

        updateMembershipUI();
    }
    private void updateMembershipUI() {
        if (!Constants.membershipType.equals("FREE")) {
            int daysLeft = membershipDaysLeft(new Date(), Constants.expireDate) + 1;
            if (daysLeft > 1) {
                goPremiumTitle.setText(daysLeft + " Days Left");
            } else {
                goPremiumTitle.setText(daysLeft + " Day Left");
            }
            goPremiumImage.setImageResource(R.drawable.clock); // Assuming you have a clock image in your drawables
        } else {
            goPremiumImage.setImageResource(R.drawable.crown); // Assuming you have a crown image in your drawables
            goPremiumTitle.setText("Go Premium");
        }
    }

    private int membershipDaysLeft(Date currentDate, Date expireDate) {
        // Implement the logic to calculate the number of days left
        // This is a placeholder and needs to be replaced with actual implementation
        long difference = expireDate.getTime() - currentDate.getTime();
        return (int) (difference / (1000 * 60 * 60 * 24));
    }

    private void loadUserData() {
       name.setText(UserModel.data.getFullName());
       email.setText(UserModel.data.getEmail());
       if (!UserModel.data.getProfilePic().isEmpty()) {
           Glide.with(this).load(UserModel.data.getProfilePic()).placeholder(R.drawable.profileplaceholder).into(profileImage);
       }
    }

    private void editProfile() {
       startActivity(new Intent(this,EditProfileViewController.class));
    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void inviteFriends() {
        String someText = "Check Out C0ns3nt App Now.";
        String url = "https://apps.apple.com/us/app/C0ns3nt/id6445856285?ls=1&mt=8";

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, someText + " " + url);
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void redirectToTermsOfService() {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://c0ns3nt.com/index.php/terms-of-service/"));
        startActivity(browserIntent);

    }

    private void redirectToPrivacyPolicy() {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://c0ns3nt.com/index.php/terms-of-service/"));
        startActivity(browserIntent);

    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("LOGOUT");
        builder.setMessage("Are you sure you want to logout?");

        // Logout Button
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Services.logout(ProfileViewActivity.this);
            }
        });

        // Cancel Button
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void helpCentre() {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "c0ns3ntapp@gmail.com" });
        startActivity(Intent.createChooser(intent, ""));


    }

    private void goPremium() {
        if (Constants.membershipType.equals("FREE")) {
            startActivity(new Intent(this, MembershipActivity.class));
        }
    }

    private void consentHistory() {
        Intent intent = new Intent(this, ConsentHistoryActivity.class);
        intent.putExtra("isFromSettings",true);
        startActivity(intent);
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileViewActivity.this);
        builder.setTitle("DELETE ACCOUNT");
        builder.setMessage("Are you sure you want to delete your account?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth auth = FirebaseAuth.getInstance();

                if (auth.getCurrentUser() != null) {
                    String userId = auth.getCurrentUser().getUid();
                    ProgressHud.show(ProfileViewActivity.this, "Deleting Account...");

                    auth.getCurrentUser().delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseFirestore.getInstance().collection("Users").document(userId).delete().addOnCompleteListener(task1 -> {
                                ProgressHud.dialog.dismiss();
                                if (task1.isSuccessful()) {
                                    logout();
                                } else {
                                    Services.showDialog(ProfileViewActivity.this, "ERROR", task1.getException().getLocalizedMessage());
                                }
                            });
                        } else {
                            ProgressHud.dialog.dismiss();
                            showReLoginRequiredDialog();
                        }

                    });
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showReLoginRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileViewActivity.this);
        builder.setTitle("Re-Login Required");
        builder.setMessage("Delete account requires re-verification. Please login and try again.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
