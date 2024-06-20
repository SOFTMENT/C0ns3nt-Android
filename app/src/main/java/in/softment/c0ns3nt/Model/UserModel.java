package in.softment.c0ns3nt.Model;


import java.util.Date;

public class UserModel {

    public String fullName = "";
    public String username = "";
    public String email = "";
    public Date registredAt = new Date();
    public String regiType = "";
    public String gender = "";
    public Integer age = 0;
    public String profilePic = "";
    public String uid = "";
    public String notificationToken =  "";
    public String phoneNumber = "";
    public String pinNumber = "";
    public String emergencyPhoneNumber1 = "";
    public String emergencyPhoneNumber2 = "";
    public String emergencyPhoneNumber3 = "";
    public String emergencyPIN = "";
    public String geoHash = "";
    public Double latitude = 0.0;
    public Double longitude = 0.0;
    public Integer codePosition = 0;
    public Boolean isOnline = false;
    public String membershipType = "";




    public static UserModel data  = null;

    public UserModel() {
        if (data == null) {
            data = this;
        }

    }

    public String getUid() {
        return uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getRegistredAt() {
        return registredAt;
    }

    public void setRegistredAt(Date registredAt) {
        this.registredAt = registredAt;
    }

    public String getRegiType() {
        return regiType;
    }

    public void setRegiType(String regiType) {
        this.regiType = regiType;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
    }

    public String getEmergencyPhoneNumber1() {
        return emergencyPhoneNumber1;
    }

    public void setEmergencyPhoneNumber1(String emergencyPhoneNumber1) {
        this.emergencyPhoneNumber1 = emergencyPhoneNumber1;
    }

    public String getEmergencyPhoneNumber2() {
        return emergencyPhoneNumber2;
    }

    public void setEmergencyPhoneNumber2(String emergencyPhoneNumber2) {
        this.emergencyPhoneNumber2 = emergencyPhoneNumber2;
    }

    public String getEmergencyPhoneNumber3() {
        return emergencyPhoneNumber3;
    }

    public void setEmergencyPhoneNumber3(String emergencyPhoneNumber3) {
        this.emergencyPhoneNumber3 = emergencyPhoneNumber3;
    }

    public String getEmergencyPIN() {
        return emergencyPIN;
    }

    public void setEmergencyPIN(String emergencyPIN) {
        this.emergencyPIN = emergencyPIN;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getCodePosition() {
        return codePosition;
    }

    public void setCodePosition(Integer codePosition) {
        this.codePosition = codePosition;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }
}
