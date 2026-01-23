package com.example.radiusapi.utils;

import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
@ToString
public class Result {
    private Boolean success;
    private Integer code;
    private String message;
    private Map<String,Object> data =new HashMap<>();


    public Result()
    {

    }


    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }



    public  void ok(){

        setSuccess(true);
        setCode(ResultCode.SUCCESS);
        setMessage("sucess");

    }
    public  void error(){

        setSuccess(false);
        setCode(ResultCode.ERROR);
        setMessage("fail");

    }

    public  Result data(String key,Object value){
        this.data.put(key,value);
        return this;
    }

    public Result data (Map<String,Object>map){
        this.setData(map);
        return this;
    }
}
