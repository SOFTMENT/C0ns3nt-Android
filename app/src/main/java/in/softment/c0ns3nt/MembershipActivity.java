package in.softment.c0ns3nt;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.Purchase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Package;
import com.revenuecat.purchases.PurchaseParams;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.PurchaseCallback;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;
import com.revenuecat.purchases.models.StoreTransaction;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import in.softment.c0ns3nt.Model.UniqueModel;
import in.softment.c0ns3nt.Util.Constants;
import in.softment.c0ns3nt.Util.MainApplication;
import in.softment.c0ns3nt.Util.ProgressHud;
import in.softment.c0ns3nt.Util.Services;


public class MembershipActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private String membershipType = "";
    private RadioButton freeRadio, goldRadio, platinumRadio, blackRadio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);


        sharedPreferences = getSharedPreferences("CONSENT_DB", MODE_PRIVATE);
        freeRadio = findViewById(R.id.freeRadio);
        goldRadio = findViewById(R.id.goldRadio);
        platinumRadio = findViewById(R.id.platinumRadio);
        blackRadio = findViewById(R.id.blackRadio);

        freeRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllRadio();
                freeRadio.setChecked(true);
                membershipType = "FREE";
            }
        });

        goldRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllRadio();
                goldRadio.setChecked(true);
                membershipType = "GOLD";
            }
        });

        platinumRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllRadio();
                platinumRadio.setChecked(true);
                membershipType = "PLATINUM";
            }
        });

       blackRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllRadio();
                blackRadio.setChecked(true);
                membershipType = "BLACK";
            }
        });

        findViewById(R.id.freeInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Services.showDialog(MembershipActivity.this,"FREE","Welcome to the Black App’s Free Tier. With this Tier you have access to all basic features. You will have access to your consensual history, emergency PIN along with GPS Contact Support if needed, and our 3 factor consent process. Your name and first initial as well as your profile picture will be stored in your consent partners history list.");
            }
        });
        findViewById(R.id.goldInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Services.showDialog(MembershipActivity.this,"GOLD","Welcome to the Black App’s Gold Tier! You will have all access to the Free Tier, but with the added benefit of having only your Unique ID number show up in your consensual partners history list. This helps ensure your privacy if you no longer wish to have consensual relations with this partner in the future.");
            }
        });
        findViewById(R.id.platinumInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Services.showDialog(MembershipActivity.this,"PLATINUM","Welcome to the Black App’s Platinum Tier! You have all access to the Free Tier, but with the added benefit of having 5 Unique ID to choose from for your consensual partner’s history list. This helps to ensure your privacy in multiple scenarios in the event that you no longer with to have consensual relations with this partner or many others in the future.");
            }
        });
        findViewById(R.id.blackInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Services.showDialog(MembershipActivity.this,"BLACK","Welcome to the highest level of the Black App’s Membership Tiers. You have access to all basic functions of the Black App along with the exclusive benefit of unlimited ID numbers as well as complete anonymity in your consensual partners history list. In this Tier the user must be verified and will remain at Platinum status until verification. Black Tier users show up as just a verified mark in their consensual partners history list. This tier is for users who absolutely must maintain the highest levels of discretion due to their job function, fame, wealth, or other circumstances.");
            }
        });

        findViewById(R.id.subscribeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (membershipType.isEmpty()) {
                    Services.showCenterToast(MembershipActivity.this,"Please select membership");

                }
                else {



                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("membershipType", "OTHER");
                    editor.apply();

                    ProgressHud.show(MembershipActivity.this,"");

                    if ("FREE".equals(membershipType)) {

                        editor.putString("membershipType", "FREE");
                        editor.apply();
                        setupUniqueId();
                    }
                    else if ("BLACK".equals(membershipType)) {
                        editor.putString("membershipType", "BLACK");
                        editor.apply();
                        setupUniqueId();

                    } else if ("PLATINUM".equals(membershipType)) {
                        editor.putString("membershipType", "PLATINUM");
                        editor.apply();
                        setupUniqueId();

                    } else if ("GOLD".equals(membershipType)) {
                        editor.putString("membershipType", "GOLD");
                        editor.apply();
                        setupUniqueId();
                    }
                    else {
                        Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsCallback() {


                        @Override
                            public void onReceived(@Nullable Offerings offerings) {
                            Package packageToPurchase = null;



                            if ("BLACK".equals(membershipType)) {
                                packageToPurchase = offerings.getCurrent().getAvailablePackages().get(0);
                            } else if ("PLATINUM".equals(membershipType)) {
                                packageToPurchase = offerings.getCurrent().getAvailablePackages().get(1);
                            } else if ("GOLD".equals(membershipType)) {
                                packageToPurchase = offerings.getCurrent().getAvailablePackages().get(2);
                            }

                            if (packageToPurchase != null) {
                                Purchases.getSharedInstance().purchase(
                                        new PurchaseParams.Builder(MembershipActivity.this, packageToPurchase).build(),
                                        new PurchaseCallback() {
                                            @Override
                                            public void onCompleted(@NonNull StoreTransaction storeTransaction, @NonNull CustomerInfo customerInfo) {
                                                if (customerInfo.getEntitlements().get("Premium").isActive()) {
                                                    Constants.expireDate = customerInfo.getEntitlements().get("Premium").getExpirationDate() != null ? customerInfo.getEntitlements().get("Premium").getExpirationDate() : new Date();
                                                    String identifier = customerInfo.getEntitlements().get("Premium").getProductIdentifier() != null ? customerInfo.getEntitlements().get("Premium").getProductIdentifier() : "FREE";

                                                    if ("in.softment.black".equals(identifier)) {
                                                        Constants.membershipType = "BLACK";
                                                        setupUniqueId();
                                                    } else if ("in.softment.platinum".equals(identifier)) {
                                                        Constants.membershipType = "PLATINUM";
                                                        setupUniqueId();
                                                    } else if ("in.softment.gold".equals(identifier)) {
                                                        Constants.membershipType = "GOLD";
                                                        setupUniqueId();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onError(@NonNull PurchasesError purchasesError, boolean b) {
                                                ProgressHud.dialog.dismiss();
                                                Services.showDialog(MembershipActivity.this,"ERROR",purchasesError.getMessage());
                                            }
                                        }
                                );
                            }
                        }

                            @Override
                            public void onError(@NonNull PurchasesError error) {
                                ProgressHud.dialog.dismiss();
                                Services.showDialog(MembershipActivity.this,"ERROR",error.getMessage());
                            }
                        });
                    }
                }
            }
        });


        findViewById(R.id.termsOfUse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://softment.in/terms-of-service/"));
                startActivity(browserIntent);
            }
        });

        findViewById(R.id.privacyPolicy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://softment.in/consent/privacy_policy.pdf"));
                startActivity(browserIntent);
            }
        });

//        findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ProgressHud.show(MembershipActivity.this, "Restoring...");
//
//
//                Purchases.getSharedInstance().restorePurchases(new ReceiveCustomerInfoCallback() {
//                    @Override
//                    public void onError(@NonNull PurchasesError purchasesError) {
//
//                    }
//
//                    @Override
//                    public void onReceived(@NonNull CustomerInfo customerInfo) {
//                        ProgressHud.dialog.dismiss();
//
//                        if (customerInfo.getEntitlements().get("Premium") != null && customerInfo.getEntitlements().get("Premium").isActive()) {
//                            Date expirationDate = customerInfo.getEntitlements().get("Premium").getExpirationDate();
//                            String membershipType = customerInfo.getEntitlements().get("Premium").getProductIdentifier();
//
//                            Constants.expireDate = expirationDate != null ? expirationDate : new Date();
//                            Constants.membershipType = membershipType != null ? membershipType : "FREE";
//
//                            FirebaseFirestore db = FirebaseFirestore.getInstance();
//                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//
//                            if (currentUser != null) {
//                                Map<String, Object> userData = new HashMap<>();
//                                userData.put("membershipType", Constants.membershipType);
//
//                                db.collection("Users").document(currentUser.getUid()).set(userData, SetOptions.merge());
//                            }
//
//                            Intent intent = new Intent(MembershipActivity.this, MainApplication.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                        } else {
//                            Services.showCenterToast(MembershipActivity.this,"No active membership found");
//                        }
//                    }
//                });
//            }
//        });
    }
    public void clearAllRadio(){
        freeRadio.setChecked(false);
        goldRadio.setChecked(false);
        platinumRadio.setChecked(false);
        blackRadio.setChecked(false);

    }

    private void setupUniqueId() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            Constants.membershipType = membershipType;
            Map map = new HashMap();
            map.put("membershipType",membershipType);
            db.collection("Users").document(currentUser.getUid())
                    .set(map, SetOptions.merge());

            db.collection("UniqueId").whereEqualTo("uid", currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                           Intent intent = new Intent(MembershipActivity.this,MainActivity.class);
                           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK  | Intent.FLAG_ACTIVITY_NEW_TASK);
                           startActivity(intent);
                        } else {
                            UniqueModel uniqueModel = new UniqueModel();
                            uniqueModel.uid = currentUser.getUid();
                            uniqueModel.uniqeId = Services.generateUniqueCode();
                            uniqueModel.date = new Date();

                            db.collection("UniqueId").document(uniqueModel.uniqeId)
                                    .set(uniqueModel)
                                    .addOnSuccessListener(aVoid -> {
                                        ProgressHud.dialog.dismiss();
                                        Intent intent = new Intent(MembershipActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK  | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        ProgressHud.dialog.dismiss();
                                        Services.showDialog(MembershipActivity.this,"ERROR",e.getMessage());
                                    });
                        }
                    });
        }
    }




}
