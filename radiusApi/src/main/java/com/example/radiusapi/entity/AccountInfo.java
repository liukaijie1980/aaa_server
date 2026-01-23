package com.example.radiusapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
@ToString
public class AccountInfo
{
    private String id;
    private String UserName;
    private String realm;
    private String UserPassword;
    private short AuthMode;
    private boolean IsFrozen;
    private String AdminName;

    private   java.util.Date ValidDate;
    private   java.util.Date ModifyDate;

    private   java.util.Date ExpireDate;

    private  long SecondRemain;
    private long  ByteRemain;
    private int  SimualUseLimit;
    private int MaxSessionTimeout;
    private int InboundCar;
    private int OutboundCar;
    private String  QosProfile;
    private int UpdateInterval;
////////////////////////////////////////

    ////////////////////////////////

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getUserPassword() {
        return UserPassword;
    }

    public void setUserPassword(String userPassword) {
        UserPassword = userPassword;
    }

    public short getAuthMode() {
        return AuthMode;
    }

    public void setAuthMode(short authMode) {
        AuthMode = authMode;
    }
//注意这里如果用ide的自动生成代码功能，会被自作聪明的改成没有Is
    public boolean getIsFrozen() {
        return IsFrozen;
    }
    //注意这里如果用ide的自动生成代码功能，会被自作聪明的改成没有Is
    public void setIsFrozen(boolean frozen) {
        IsFrozen = frozen;
    }

    public String getAdminName() {
        return AdminName;
    }

    public void setAdminName(String adminName) {
        AdminName = adminName;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="Asia/Shanghai")
    public Date getValidDate() {
        return ValidDate;
    }
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="Asia/Shanghai")
    public void setValidDate(Date validDate) {
        ValidDate = validDate;
    }
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="Asia/Shanghai")
    public Date getModifyDate() {
        return ModifyDate;
    }
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="Asia/Shanghai")
    public void setModifyDate(Date modifyDate) {
        ModifyDate = modifyDate;
    }
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="Asia/Shanghai")
    public Date getExpireDate() {
        return ExpireDate;
    }
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="Asia/Shanghai")
    public void setExpireDate(Date expireDate) {
        ExpireDate = expireDate;
    }

    public long getSecondRemain() {
        return SecondRemain;
    }

    public void setSecondRemain(long secondRemain) {
        SecondRemain = secondRemain;
    }

    public long getByteRemain() {
        return ByteRemain;
    }

    public void setByteRemain(long byteRemain) {
        ByteRemain = byteRemain;
    }

    public int getSimualUseLimit() {
        return SimualUseLimit;
    }

    public void setSimualUseLimit(int simualUseLimit) {
        SimualUseLimit = simualUseLimit;
    }

    public int getMaxSessionTimeout() {
        return MaxSessionTimeout;
    }

    public void setMaxSessionTimeout(int maxSessionTimeout) {
        MaxSessionTimeout = maxSessionTimeout;
    }

    public int getInboundCar() {
        return InboundCar;
    }

    public void setInboundCar(int inboundCar) {
        InboundCar = inboundCar;
    }

    public int getOutboundCar() {
        return OutboundCar;
    }

    public void setOutboundCar(int outboundCar) {
        OutboundCar = outboundCar;
    }

    public String getQosProfile() {
        return QosProfile;
    }

    public void setQosProfile(String qosProfile) {
        QosProfile = qosProfile;
    }

    public int getUpdateInterval() {
        return UpdateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        UpdateInterval = updateInterval;
    }
}
