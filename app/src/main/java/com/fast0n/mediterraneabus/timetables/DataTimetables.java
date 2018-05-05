package com.fast0n.mediterraneabus.timetables;

public class DataTimetables {

    private String ride;
    private String time;
    private String time1;
    private String name_time;
    private String name_time1;
    private String duration;

    DataTimetables(String ride, String time, String time1, String name_time, String name_time1, String duration) {
        this.ride = ride;
        this.time = time;
        this.time1 = time1;
        this.name_time = name_time;
        this.name_time1 = name_time1;
        this.duration = duration;

    }

    public String getRide() {
        return ride;
    }

    public String getTime() {
        return time;
    }

    public String getTime1() {
        return time1;
    }

    public String getName_time() {
        return name_time;
    }

    public String getName_time1() {
        return name_time1;
    }

    public String getDuration() {
        return duration;
    }

}