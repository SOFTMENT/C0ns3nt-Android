package in.softment.c0ns3nt.Model;

import java.io.Serializable;
import java.util.Date;

public class HistoryModel implements Serializable {
    public String userWhichCodeScanned = "";
    public String userWhoScannedCode = "";
    public String id = "";
    public String status = "";
    public Date date = new Date();
    public String uniqueId = "";

    public HistoryModel() {
    }

    public String getUserWhichCodeScanned() {
        return userWhichCodeScanned;
    }

    public void setUserWhichCodeScanned(String userWhichCodeScanned) {
        this.userWhichCodeScanned = userWhichCodeScanned;
    }

    public String getUserWhoScannedCode() {
        return userWhoScannedCode;
    }

    public void setUserWhoScannedCode(String userWhoScannedCode) {
        this.userWhoScannedCode = userWhoScannedCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
