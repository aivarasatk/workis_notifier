package com.workis.pranesejas.data;

public class JobObject
{
    public JobObject(){}
    public JobObject(String title, String description, String company, String date,
                     String start_datetime, String end_datetime, int expected_total,
                     double rate, String address, String city, String logo_url, int demand_status) {
        this.title = title;
        this.description = description;
        this.company = company;
        this.date = date;
        this.start_datetime = start_datetime;
        this.end_datetime = end_datetime;
        this.expected_total = expected_total;
        this.rate = rate;
        this.address = address;
        this.city = city;
        this.logo_url = logo_url;
        this.demand_status = demand_status;
    }

    private String title;
    private String description ;
    private String company ;
    private String date ;
    private String start_datetime ;
    private String end_datetime ;
    private int expected_total ;
    private double rate ;
    private String address ;
    private String city ;
    private String logo_url ;
    private int demand_status ;

    @Override
    public String toString() {
        return title + " " + company + " " + city + " " + rate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStart_datetime() {
        return start_datetime;
    }

    public void setStart_datetime(String start_datetime) {
        this.start_datetime = start_datetime;
    }

    public String getEnd_datetime() {
        return end_datetime;
    }

    public void setEnd_datetime(String end_datetime) {
        this.end_datetime = end_datetime;
    }

    public int getExpected_total() {
        return expected_total;
    }

    public void setExpected_total(int expected_total) {
        this.expected_total = expected_total;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLogo_url() {
        return logo_url;
    }

    public void setLogo_url(String logo_url) {
        this.logo_url = logo_url;
    }

    public int getDemand_status() {
        return demand_status;
    }

    public void setDemand_status(int demand_status) {
        this.demand_status = demand_status;
    }
}
