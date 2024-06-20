package in.softment.c0ns3nt.Model;

import java.util.Date;

public class UniqueModel {

    public String uniqeId = "";
    public String uid = "";
    public Date date = new Date();

    public String getUniqeId() {
        return uniqeId;
    }

    public void setUniqeId(String uniqeId) {
        this.uniqeId = uniqeId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
