package in.softment.c0ns3nt.Util;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.EntitlementInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import in.softment.c0ns3nt.CreateProfileActivity;
import in.softment.c0ns3nt.FailedActivity;
import in.softment.c0ns3nt.MainActivity;
import in.softment.c0ns3nt.MembershipActivity;
import in.softment.c0ns3nt.Model.HistoryModel;
import in.softment.c0ns3nt.Model.UserModel;
import in.softment.c0ns3nt.PinNumberVerificationActivity;
import in.softment.c0ns3nt.R;
import in.softment.c0ns3nt.SignInActivity;
import in.softment.c0ns3nt.WelcomeActivity;

public class Services {


    public static void addUserDataOnServer(Context context, UserModel userModel){

        ProgressHud.show(context,"");
        FirebaseFirestore.getInstance().collection("Users").document(userModel.getUid()).set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                 getUserData(context,userModel.uid,true);
                }
                else {
                    Services.showDialog(context,"ERROR",task.getException().getLocalizedMessage());
                }
            }
        });
    }

    public static void fullScreen(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = activity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }

    public static void showCenterToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.show();
    }

    public static Date convertStringToDate(String sDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(sDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Date();
    }
    public static String convertDateToDayName(Date date) {
        if (date == null) {
            date = new Date();
        }
        date.setTime(date.getTime());
        String pattern = "EEEE";
        DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
        return  df.format(date);
    }
    public static  String convertDateToTimeString(Date date) {
        if (date == null) {
            date = new Date();
        }
        date.setTime(date.getTime());
        String pattern = "dd-MMM-yyyy, hh:mm a";
        DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
        return  df.format(date);
    }

    public static int getAge(Date dob) {
        long diff = new Date().getTime() - dob.getTime();
        return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) / 365;
    }

    public static  String convertDateToString(Date date) {
        if (date == null) {
            date = new Date();
        }
        date.setTime(date.getTime());
        String pattern = "dd-MMM-yyyy";
        DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
        return  df.format(date);
    }



    public static void showDialog(Context context,String title,String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        Activity activity = (Activity) context;
        View view = activity.getLayoutInflater().inflate(R.layout.error_message_layout, null);
        TextView titleView = view.findViewById(R.id.title);
        TextView msg = view.findViewById(R.id.message);
        titleView.setText(title);

        msg.setText(message);
        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager manager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", message);
                manager.setPrimaryClip(clipData);
                Services.showDialog(context,"Copié","Le texte a été copié");

            }
        });
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();


            }
        });

        if(!((Activity) context).isFinishing())
        {
            alertDialog.show();

        }

    }

    public static void logout(Context context) {

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(context, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

    }

    public static void cleanUser(){
       UserModel.data = null;
    }



    public static String generateUniqueCode() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder randomString = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int rand = random.nextInt(letters.length());
            char nextChar = letters.charAt(rand);
            randomString.append(nextChar);
        }

        return randomString.toString();
    }

    public  static void cancelConsent(Context context, HistoryModel historyModel, boolean technicalIssue){

          ProgressHud.show(context, "");

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String docId = historyModel.getId() != null ? historyModel.getId() : "123";
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Users").document(userId).collection("History").document(docId)
                    .set(Collections.singletonMap("status", "denied"), SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        String uid;
                        if (historyModel.getUserWhoScannedCode().equals(userId)) {
                            uid = historyModel.getUserWhichCodeScanned() != null ? historyModel.getUserWhichCodeScanned() : "123";
                        } else {
                            uid = historyModel.getUserWhoScannedCode() != null ? historyModel.getUserWhoScannedCode() : "123";
                        }

                        db.collection("Users").document(uid).collection("History").document(docId)
                                .set(Collections.singletonMap("status", "deniedbyother"), SetOptions.merge())
                                .addOnSuccessListener(aVoid1 -> {
                                   ProgressHud.dialog.dismiss();
                                    Intent intent = new Intent(context, FailedActivity.class);

                                    if (technicalIssue) {
                                        intent.putExtra("RESPONSE","deniedbyemergency");
                                    }


                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                })
                                .addOnFailureListener(e -> ProgressHud.dialog.dismiss());
                    })
                    .addOnFailureListener(e -> ProgressHud.dialog.dismiss());

    }
    public static void getUserData(Context activity, String uid, boolean showProgress) {

        if (showProgress) {
            ProgressHud.show(activity,"");
        }

        FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (showProgress) {
                    ProgressHud.dialog.dismiss();
                }

                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            UserModel.data = documentSnapshot.toObject(UserModel.class);

                            if (UserModel.data.getUsername().isEmpty()) {

                                Intent intent = new Intent(activity, CreateProfileActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                            }
                            else {

//                                Purchases.getSharedInstance().getCustomerInfo(new ReceiveCustomerInfoCallback() {
//                                    @Override
//                                    public void onReceived(@NonNull CustomerInfo customerInfo) {
//                                        String membershipType =  activity.getSharedPreferences("CONSENT_DB", MODE_PRIVATE).getString("membershipType","OTHER");
//                                        if (customerInfo.getEntitlements().get("Premium") != null && customerInfo.getEntitlements().get("Premium").isActive()) {
//                                            Constants.expireDate = customerInfo.getEntitlements().get("Premium").getExpirationDate();
//                                            String identifier = customerInfo.getEntitlements().get("Premium").getProductIdentifier();
//
//                                            if ("in.softment.black".equals(identifier)) {
//                                                Constants.membershipType = "BLACK";
//                                            } else if ("in.softment.platinum".equals(identifier)) {
//                                                Constants.membershipType = "PLATINUM";
//                                            } else if ("in.softment.gold".equals(identifier)) {
//                                                Constants.membershipType = "GOLD";
//                                            }
//
//                                            Intent intent = new Intent(activity, MainActivity.class);
//                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                            activity.startActivity(intent);
//                                        }
                                String membershipType =  activity.getSharedPreferences("CONSENT_DB", MODE_PRIVATE).getString("membershipType","OTHER");
                                         if (membershipType.equals("FREE")) {
                                            Constants.membershipType = "FREE";
                                            Intent intent = new Intent(activity, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            activity.startActivity(intent);
                                        }
                                else if (membershipType.equals("GOLD")) {
                                    Constants.membershipType = "GOLD";
                                    Intent intent = new Intent(activity, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent);
                                }
                               else if (membershipType.equals("BLACK")) {
                                    Constants.membershipType = "BLACK";
                                    Intent intent = new Intent(activity, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent);
                                }
                                else if (membershipType.equals("PLATINUM")) {
                                    Constants.membershipType = "PLATINUM";
                                    Intent intent = new Intent(activity, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent);
                                }
                                        else {
                                            Intent intent = new Intent(activity, MembershipActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            activity.startActivity(intent);
                                        }
                                    }

//                                    @Override
//                                    public void onError(@NonNull PurchasesError purchasesError) {
//                                        Services.showDialog(activity,"ERROR",purchasesError.getMessage());
//                                    }
//                                });


                            //}

                        }
                        else {
                            Intent intent = new Intent(activity, SignInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
                        }

                    }
                    else {
                        Services.showDialog(activity, "ERROR", "Something went wrong");

                    }
                }
                else {
                    Services.showDialog(activity, "ERROR", task.getException().getLocalizedMessage());

                }

            }
        });
    }
}
