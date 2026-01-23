package com.example.radiusapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.ToString;

@ToString
public class NodeStatistic {

    private String nodeId;
    private int totalAccounts;
    private int  onlineAccounts;
    private int onlineTerminals;




    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public int getOnlineAccounts() {
        return onlineAccounts;
    }

    public void setOnlineAccounts(int onlineAccounts) {
        this.onlineAccounts = onlineAccounts;
    }

    public int getOnlineTerminals() {
        return onlineTerminals;
    }

    public void setOnlineTerminals(int onlineTerminals) {
        this.onlineTerminals = onlineTerminals;
    }
}
