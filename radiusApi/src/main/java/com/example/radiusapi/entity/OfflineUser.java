package com.example.radiusapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;



@ToString
public class OfflineUser {
    //手动声明为主键
    @TableId(type = IdType.AUTO)
    private int radacctid;
    private String acctsessionid;
    private String acctuniqueid;
    private String username;
    private String realm;

    private String nasidentifier;
    private String nasipaddress;
    private String nasportid;
    private String nasporttype;
    private java.util.Date acctstarttime;
    private java.util.Date acctupdatetime;
    private java.util.Date acctstoptime;
    private int     acctinterval;
    private int  acctsessiontime;
    private String acctauthentic;
    private String connectinfo_start;
    private String connectinfo_stop;
    private long acctinputoctets;
    private long acctoutputoctets;
    private String calledstationid;
    private String callingstationid;
    private  String  acctterminatecause;
    private  String  servicetype;
    private  String framedprotocol;
    private  String framedipaddress;
    private  String framedipv6address;
    private  String   framedipv6prefix;
    private  String   framedinterfaceid;
    private  String   delegatedipv6prefix;
    private  String   _class;
    private  int kickme;

    public int getKickme() {
        return kickme;
    }

    public void setKickme(int kickme) {
        this.kickme = kickme;
    }

    public int getRadacctid() {
        return radacctid;
    }

    public void setRadacctid(int radacctid) {
        this.radacctid = radacctid;
    }

    public String getAcctsessionid() {
        return acctsessionid;
    }

    public void setAcctsessionid(String acctsessionid) {
        this.acctsessionid = acctsessionid;
    }

    public String getAcctuniqueid() {
        return acctuniqueid;
    }

    public void setAcctuniqueid(String acctuniqueid) {
        this.acctuniqueid = acctuniqueid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getNasidentifier() {
        return nasidentifier;
    }

    public void setNasidentifier(String nasidentifier) {
        this.nasidentifier = nasidentifier;
    }

    public String getNasipaddress() {
        return nasipaddress;
    }

    public void setNasipaddress(String nasipaddress) {
        this.nasipaddress = nasipaddress;
    }

    public String getNasportid() {
        return nasportid;
    }

    public void setNasportid(String nasportid) {
        this.nasportid = nasportid;
    }

    public String getNasporttype() {
        return nasporttype;
    }

    public void setNasporttype(String nasporttype) {
        this.nasporttype = nasporttype;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getAcctstarttime() {
        return acctstarttime;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public void setAcctstarttime(Date acctstarttime) {
        this.acctstarttime = acctstarttime;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getAcctupdatetime() {
        return acctupdatetime;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public void setAcctupdatetime(Date acctupdatetime) {
        this.acctupdatetime = acctupdatetime;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getAcctstoptime() {
        return acctstoptime;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public void setAcctstoptime(Date acctstoptime) {
        this.acctstoptime = acctstoptime;
    }

    public int getAcctinterval() {
        return acctinterval;
    }

    public void setAcctinterval(int acctinterval) {
        this.acctinterval = acctinterval;
    }

    public int getAcctsessiontime() {
        return acctsessiontime;
    }

    public void setAcctsessiontime(int acctsessiontime) {
        this.acctsessiontime = acctsessiontime;
    }

    public String getAcctauthentic() {
        return acctauthentic;
    }

    public void setAcctauthentic(String acctauthentic) {
        this.acctauthentic = acctauthentic;
    }

    public String getConnectinfo_start() {
        return connectinfo_start;
    }

    public void setConnectinfo_start(String connectinfo_start) {
        this.connectinfo_start = connectinfo_start;
    }

    public String getConnectinfo_stop() {
        return connectinfo_stop;
    }

    public void setConnectinfo_stop(String connectinfo_stop) {
        this.connectinfo_stop = connectinfo_stop;
    }

    public long getAcctinputoctets() {
        return acctinputoctets;
    }

    public void setAcctinputoctets(long acctinputoctets) {
        this.acctinputoctets = acctinputoctets;
    }

    public long getAcctoutputoctets() {
        return acctoutputoctets;
    }

    public void setAcctoutputoctets(long acctoutputoctets) {
        this.acctoutputoctets = acctoutputoctets;
    }

    public String getCalledstationid() {
        return calledstationid;
    }

    public void setCalledstationid(String calledstationid) {
        this.calledstationid = calledstationid;
    }

    public String getCallingstationid() {
        return callingstationid;
    }

    public void setCallingstationid(String callingstationid) {
        this.callingstationid = callingstationid;
    }

    public String getAcctterminatecause() {
        return acctterminatecause;
    }

    public void setAcctterminatecause(String acctterminatecause) {
        this.acctterminatecause = acctterminatecause;
    }

    public String getServicetype() {
        return servicetype;
    }

    public void setServicetype(String servicetype) {
        this.servicetype = servicetype;
    }

    public String getFramedprotocol() {
        return framedprotocol;
    }

    public void setFramedprotocol(String framedprotocol) {
        this.framedprotocol = framedprotocol;
    }

    public String getFramedipaddress() {
        return framedipaddress;
    }

    public void setFramedipaddress(String framedipaddress) {
        this.framedipaddress = framedipaddress;
    }

    public String getFramedipv6address() {
        return framedipv6address;
    }

    public void setFramedipv6address(String framedipv6address) {
        this.framedipv6address = framedipv6address;
    }

    public String getFramedipv6prefix() {
        return framedipv6prefix;
    }

    public void setFramedipv6prefix(String framedipv6prefix) {
        this.framedipv6prefix = framedipv6prefix;
    }

    public String getFramedinterfaceid() {
        return framedinterfaceid;
    }

    public void setFramedinterfaceid(String framedinterfaceid) {
        this.framedinterfaceid = framedinterfaceid;
    }

    public String getDelegatedipv6prefix() {
        return delegatedipv6prefix;
    }

    public void setDelegatedipv6prefix(String delegatedipv6prefix) {
        this.delegatedipv6prefix = delegatedipv6prefix;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }
}
