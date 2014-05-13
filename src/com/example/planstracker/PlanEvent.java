package com.example.planstracker;

import java.util.Calendar;
import java.util.Date;

public class PlanEvent{

    private int _id;
    private Date event_datetime;
    private String person_name = "";
    private String phone_number = "";
    private String email_address = "";
    private double money;
    private byte[] pic;
    private double hours;
    private String note = "";
    private EventLocation event_location;

    public PlanEvent(int _id, Date event_datetime, String person_name,
            String phone_number, String email_address, double money,
            byte[] pic, double hours, String note, EventLocation event_location) {
        super();
        this._id = _id;
        this.event_datetime = (event_datetime != null)? event_datetime : 
            Calendar.getInstance().getTime();
        this.person_name = (person_name != null) ? person_name : "";
        this.phone_number =  (phone_number != null) ? phone_number : "";
        this.email_address =  (email_address != null) ? email_address : "";
        this.money = money;
        this.pic = pic;
        this.hours = hours;
        this.note =  (note != null) ? note : "";
        this.event_location = event_location;
    }
    
    public int getId() {
        return _id;
    }

    public PlanEvent(){
        this.event_datetime = Calendar.getInstance().getTime();
        this.event_location = new EventLocation(0,0,"");
    }
    
    public Date getDate() {
        return event_datetime;
    }

    public void setDate(Date event_datetime) {
        this.event_datetime = event_datetime;
    }
    
    public void setDateLong(long event_datetime) {
        this.event_datetime = new Date(event_datetime);
    }

    public String getPerson_name() {
        return person_name;
    }

    public void setPerson_name(String person_name) {
        this.person_name = person_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public EventLocation getEvent_location() {
        return event_location;
    }

    public void setEvent_location(EventLocation event_location) {
        this.event_location = event_location;
    }

    public void setId(int newRowId) {
        this._id = newRowId;
    }
}

