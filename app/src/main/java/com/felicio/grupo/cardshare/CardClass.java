package com.felicio.grupo.cardshare;

import java.util.Date;

public class CardClass {

    public String user_id;
    public String image_url;
    public String desc,cargo,contact,email,endereco;
    public String image_thumb;
    public Date timestamp;

    public CardClass(){}

    public CardClass(String user_id, String image_url, String desc, String image_thumb,Date timestamp,String contact,String cargo,String email,String endereco) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.contact = contact;
        this.cargo = cargo;
        this.email = email;
        this.endereco = endereco;
        this.image_thumb = image_thumb;
        this.timestamp = timestamp;
    }
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}
