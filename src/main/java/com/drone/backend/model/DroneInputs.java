
package com.drone.backend.model;

import lombok.Data;


@Data
public class DroneInputs {
    private float t;          
    private float p;          
    private float r;         
    private float y;         
    private long timestamp;   
}