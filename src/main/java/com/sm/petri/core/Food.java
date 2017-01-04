package com.sm.petri.core;

import processing.core.PApplet;
import processing.core.PVector;

public class Food {

    private float size = 5;
    private int color = 50;
    
    private PVector position;
    private PApplet parent;

    public Food(PApplet parent) {
        this.parent = parent;
        float x = this.parent.random(1) * this.parent.width;
        float y = this.parent.random(1) * this.parent.height;
        this.position = new PVector(x, y);

    }

    public void display() {
        this.parent.fill(color);
        this.parent.strokeWeight(1);
        this.parent.pushMatrix();
        this.parent.translate(position.x, position.y);
        this.parent.ellipse(0, 0, size, size);
        this.parent.popMatrix();
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public PVector getPosition() {
        return position;
    }

    public void setPosition(PVector position) {
        this.position = position;
    }
}
