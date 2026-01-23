package com.example.radiusapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.ToString;
@ToString
public class Radpostauth {

    //手动声明为主键
    @TableId(type = IdType.AUTO)
    private int id;
    private String username;
    private String realm;
    private  String pass;
    private  String reply;
    private String callingstationid;
    private String nasidentifier;
    private  String authdate;
    private String _class;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getCallingstationid() {
        return callingstationid;
    }

    public void setCallingstationid(String callingstationid) {
        this.callingstationid = callingstationid;
    }

    public String getNasidentifier() {
        return nasidentifier;
    }

    public void setNasidentifier(String nasidentifier) {
        this.nasidentifier = nasidentifier;
    }

    public String getAuthdate() {
        return authdate;
    }

    public void setAuthdate(String authdate) {
        this.authdate = authdate;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }
}


