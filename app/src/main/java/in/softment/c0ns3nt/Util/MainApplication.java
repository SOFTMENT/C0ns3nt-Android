package in.softment.c0ns3nt.Util;

import android.app.Application;

import com.revenuecat.purchases.LogLevel;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Purchases.setLogLevel(LogLevel.DEBUG);
        Purchases.configure(new PurchasesConfiguration.Builder(this, "goog_YGnTwObkEaZzpaaLywOxVvgmUOo").build());
    }
}
