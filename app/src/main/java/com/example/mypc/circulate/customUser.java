package com.example.mypc.circulate;

public class customUser {
    private String branch;
    private String section;
    private String year;

    public customUser(){}

    public customUser(String branch,String section,String year){
        this.branch=branch;
        this.section=section;
        this.year=year;
    }

    public String getBranch(){ return branch; }
    public String getSection() {return section; }
    public String getYear() { return year; }
}
