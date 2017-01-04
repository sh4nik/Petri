package com.sm.petri.core;

import java.util.List;
import java.util.ListIterator;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import processing.core.PApplet;
import processing.core.PVector;

public class Bloop {

    //common bloop stuff
    private static int lastId = 0;
    private static float deathRate = 0.6f;
    private static float foodHealth = 30;

    //attributes of the individual bloop
    private int id;
    private float health;
    private BasicNetwork brain;
    private float size;
    private float maxSpeed;
    private float bodyColor;

    //environment related stuff
    List<Food> foodList;
    Food nearestFood;
    PApplet parent;

    //placement related variables
    PVector position;
    PVector velocity;

    public Bloop(PApplet parent, List<Food> foodList, BasicNetwork providedBrain) {

        //initialize values
        this.id = lastId++;
        this.foodList = foodList;
        this.parent = parent;

        this.health = 255;
        this.size = 16;
        this.maxSpeed = 3;

        if (providedBrain == null) {
            brain = new BasicNetwork();
            brain.addLayer(new BasicLayer(null, true, 2));
            brain.addLayer(new BasicLayer(new ActivationSigmoid(), true, 2));
            brain.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
            brain.getStructure().finalizeStructure();
            brain.reset();
        } else {
            brain = providedBrain;
        }

        float x = this.parent.random(1) * this.parent.width;
        float y = this.parent.random(1) * this.parent.height;
        velocity = new PVector(0, 0);
        position = new PVector(x, y);
    }

    public void update() {
        updateHealth();
        calculateNextMove();
        updateMotion();
        wrapBorders();
        processFood();
    }

    public void display() {
        float theta = velocity.heading2D() + PApplet.PI / 2;
        this.parent.fill(bodyColor);
        this.parent.stroke(0);
        this.parent.strokeWeight(1);
        this.parent.pushMatrix();
        this.parent.translate(position.x, position.y);
        this.parent.rotate(theta);
        this.parent.ellipse(0, 0, size, size);
        this.parent.line(0, 0, 0, -size / 2);
        this.parent.popMatrix();
    }

    private void calculateNextMove() {
        if (nearestFood != null) {
            PVector dif = PVector.sub(nearestFood.getPosition(), position);
            dif.normalize();
            double[] senseData = {dif.x, dif.y};
            double[] actionData = new double[1];
            brain.compute(senseData, actionData);
            rotateVector(velocity, PApplet.map((float) actionData[0], (float) 0, (float) 1, -45, 45));
            moveAhead();
        }
    }

    private void updateMotion() {
        velocity.limit(maxSpeed);
        position.add(velocity);
    }

    private void updateHealth() {
        health -= (float) deathRate;
        bodyColor = health;
    }

    void wrapBorders() {
        if (position.x < 0) {
            position.x = parent.width;
        }
        if (position.y < 0) {
            position.y = parent.height;
        }
        if (position.x > parent.width) {
            position.x = 0;
        }
        if (position.y > parent.height) {
            position.y = 0;
        }
    }

    void blockBorders() {
        if (position.x < 0) {
            position.x = 0;
        }
        if (position.y < 0) {
            position.y = 0;
        }
        if (position.x > parent.width) {
            position.x = parent.width;
        }
        if (position.y > parent.height) {
            position.y = parent.height;
        }
    }

    private void moveAhead() {
        if (velocity.x == 0 && velocity.y == 0) {
            velocity = PVector.random2D();
        }
        velocity.setMag(maxSpeed);
    }

    private void rotateVector(PVector vector, float angle) {
        float rotX, rotY, origX, origY;
        origX = rotX = vector.x;  // subtract to get relative position
        origY = rotY = vector.y;  // or with other words, to get origin (anchor/rotation) point to 0,0

        vector.x -= rotX;
        vector.y -= rotY;
        angle = PApplet.radians(angle);
        rotX = origX * PApplet.cos(angle) - origY * PApplet.sin(angle);
        rotY = origX * PApplet.sin(angle) + origY * PApplet.cos(angle);
        vector.x += rotX; // get it back to absolute position on screen
        vector.y += rotY;
    }

    private void processFood() {
        ListIterator<Food> iter = foodList.listIterator();
        float shortestDistance = parent.width;
        while (iter.hasNext()) {
            Food foodItem = iter.next();
            foodItem.setColor(50);
            float d = this.position.dist(foodItem.getPosition());
            if (d < shortestDistance) {
                shortestDistance = d;
                nearestFood = foodItem;
            }
            if (d < this.size - foodItem.getSize()) {
                iter.remove();
                health += (float) foodHealth;
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public BasicNetwork getBrain() {
        return brain;
    }

    public void setBrain(BasicNetwork brain) {
        this.brain = brain;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getBodyColor() {
        return bodyColor;
    }

    public void setBodyColor(float bodyColor) {
        this.bodyColor = bodyColor;
    }

    public static float getDeathRate() {
        return deathRate;
    }

    public static void setDeathRate(float deathRate) {
        Bloop.deathRate = deathRate;
    }

    public static float getFoodHealth() {
        return foodHealth;
    }

    public static void setFoodHealth(float foodHealth) {
        Bloop.foodHealth = foodHealth;
    }
}