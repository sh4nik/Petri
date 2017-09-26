package com.sm.petri.core;

import processing.core.PApplet;
import processing.core.PVector;

public class Food {

    private float size = 5;
    private int color = 50;
    private boolean poison;

    private PVector position;
    private PApplet parent;

    public Food(PApplet parent) {
        this.parent = parent;
        float x = this.parent.random(1) * this.parent.width;
        float y = this.parent.random(1) * this.parent.height;
        this.position = new PVector(x, y);
        if (this.parent.random(1) > 0.2) {
            this.poison = false;
        } else {
            this.poison = true;
        }

    }

    public void display() {

        if (!poison) {
            this.parent.fill(100, 180, 100);
        } else {
            this.parent.fill(180, 100, 100);
        }

        this.parent.strokeWeight(0);
        this.parent.pushMatrix();
        this.parent.translate(position.x, position.y);
        this.parent.ellipse(0, 0, size, size);
        this.parent.popMatrix();
    }
    
    public void displayPainter() {

        if (!poison) {
            this.parent.fill(50, 180, 60);
        } else {
            this.parent.fill(180, 50, 60);
        }

        this.parent.strokeWeight(0);
        this.parent.pushMatrix();
        this.parent.translate(position.x, position.y);
        
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

    public boolean isPoison() {
        return poison;
    }

}
