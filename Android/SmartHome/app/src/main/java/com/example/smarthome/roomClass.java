package com.example.smarthome;

public class roomClass {
    private String Name;
    private String Device;
    private int Image;
    public roomClass(String name, String device, int image){
        Name = name;
        Device = device;
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDevice() {
        return Device;
    }

    public void setDevice(String device) {
        Device = device;
    }

    public int getImage() {
        return Image;
    }

    public void setImage(int image) {
        Image = image;
    }
}
