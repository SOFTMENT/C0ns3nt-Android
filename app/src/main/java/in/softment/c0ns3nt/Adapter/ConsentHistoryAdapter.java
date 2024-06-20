package in.softment.c0ns3nt.Adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import in.softment.c0ns3nt.Model.HistoryModel;
import in.softment.c0ns3nt.Model.UserModel;
import in.softment.c0ns3nt.R;

public class ConsentHistoryAdapter extends RecyclerView.Adapter<ConsentHistoryAdapter.ConsentViewHolder> {

    private final List<HistoryModel> historyModels; // List of HistoryModel items to be displayed in the RecyclerView



    // Constructor of the adapter, initializing the list and the listener
    public ConsentHistoryAdapter(List<HistoryModel> historyModels) {
        this.historyModels = historyModels;

    }

    @NonNull
    @Override
    public ConsentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the item layout and creating a ViewHolder for it
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_consent_history_view, parent, false);
        return new ConsentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsentViewHolder holder, int position) {
        // Binding data to the ViewHolder
        HistoryModel historyModel = historyModels.get(position);
        holder.bind(historyModel);

    }

    @Override
    public int getItemCount() {
        // Returns the total number of items in the list
        return historyModels.size();
    }

    // ViewHolder class for the RecyclerView items
    static class ConsentViewHolder extends RecyclerView.ViewHolder {

        private final TextView mConsentId;
        private final TextView mStatus;
        private final TextView mName;
        private final TextView mTime; // TextViews in the item layout

        public ConsentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initializing the views from the layout
            mConsentId = itemView.findViewById(R.id.consentId);
            mStatus = itemView.findViewById(R.id.status);
            mName = itemView.findViewById(R.id.name);
            mTime = itemView.findViewById(R.id.time);
        }

        // Method to bind data to the views
        public void bind(HistoryModel historyModel) {
            mConsentId.setText(historyModel.getUniqueId());

            mTime.setText(timeAgoSinceDate(historyModel.getDate()));
            // Handling different statuses
            String status = historyModel.getStatus();
            if ("pending".equals(status)) {
                mStatus.setText("Pending");
            } else if ("deniedbyemergency".equals(status) || "deniedbyother".equals(status) || "denied".equals(status)) {
                mStatus.setText("Denied");
            } else if ("verified".equals(status)) {
                mStatus.setText("Verified");
            } else {
                mStatus.setText(status); // Default case
            }

            String uid;
            if (historyModel.getUserWhoScannedCode().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                // Use 'userWhichCodeScanned' if it's not null, otherwise use a default value
                uid = historyModel.getUserWhichCodeScanned();
            } else {
                // Use 'userWhoScannedCode' if it's not null, otherwise use a default value
                uid = historyModel.getUserWhoScannedCode();
            }

            FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        updateUserData(userModel);
                    }
                    else {
                        FirebaseFirestore.getInstance().collection("Users")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("History").document(historyModel.getId()).delete();
                    }
                }
            });

        }

        // Method to update user data in the view
        public void updateUserData(UserModel userModel) {
            if (userModel != null) {
                if ("FREE".equals(userModel.getMembershipType())) {
                    mName.setVisibility(View.VISIBLE);
                    String fullName = userModel.getFullName();

                    // Splitting the name to display only the first name and the initial of the last name
                    if (fullName != null && !fullName.trim().isEmpty()) {
                        String[] names = fullName.split(" ");
                        if (names.length > 1) {
                            mName.setText(String.format("%s %s.", names[0], names[1].substring(0, 1)));
                        } else {
                            mName.setText(names[0]);
                        }
                    } else {
                        mName.setText("Unknown"); // or set to empty or any default text
                    }
                } else {
                    mName.setVisibility(View.GONE);
                }
            } else {
                mName.setVisibility(View.GONE);
            }
        }
    }

    public static String timeAgoSinceDate(Date date) {
        long duration = new Date().getTime() - date.getTime();

        if (duration < DateUtils.MINUTE_IN_MILLIS) {
            return "a moment ago";
        } else if (duration < DateUtils.HOUR_IN_MILLIS) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        } else if (duration < DateUtils.DAY_IN_MILLIS) {
            long hours = TimeUnit.MILLISECONDS.toHours(duration);
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (duration < DateUtils.WEEK_IN_MILLIS) {
            long days = TimeUnit.MILLISECONDS.toDays(duration);
            return days == 1 ? "1 day ago" : days + " days ago";
        } else if (duration < DateUtils.YEAR_IN_MILLIS) {
            long months = TimeUnit.MILLISECONDS.toDays(duration) / 30;
            return months == 1 ? "1 month ago" : months + " months ago";
        } else {
            long years = TimeUnit.MILLISECONDS.toDays(duration) / 365;
            return years == 1 ? "1 year ago" : years + " years ago";
        }
    }
}


