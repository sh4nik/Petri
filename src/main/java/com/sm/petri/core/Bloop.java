package com.sm.petri.core;

import java.util.List;
import java.util.ListIterator;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import processing.core.PApplet;
import processing.core.PVector;

public class Bloop {

    //common bloop stuff
    public static int lastId = 0;
    private static float deathRate = 0.6f;
    private static float foodHealth = 200;

    //attributes of the individual bloop
    private int id;
    private float health;
    private BasicNetwork brain;
    private float size;
    private float maxSpeed;
    private float bodyColor = -1;
    public boolean isBest = false;
    
    private int r;
    private int g;
    private int b;

    //environment related stuff
    List<Food> foodList;
    Food nearestFood;
    PApplet parent;

    //placement related variables
    PVector position;
    PVector velocity;

    public Bloop(PApplet parent, List<Food> foodList, double[] providedGenes) {

        //initialize values
        this.id = lastId++;
        this.foodList = foodList;
        this.parent = parent;

        this.health = 500;
        this.size = 16;
        this.maxSpeed = 3;

        brain = new BasicNetwork();
        brain.addLayer(new BasicLayer(null, true, 4));
        brain.addLayer(new BasicLayer(new ActivationTANH(), true, 4));
        brain.addLayer(new BasicLayer(new ActivationTANH(), false, 6));
        brain.getStructure().finalizeStructure();
        brain.reset((int) Simulator.seed);

        if (providedGenes != null) {
            brain.decodeFromArray(providedGenes);
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

        if (isBest) {
            this.parent.fill(180, 100, 240);
            this.parent.stroke(40);
        } else {
            if (bodyColor < 0) {
                this.parent.fill(120, 150, 255);
                this.parent.stroke(40);
            } else {
                this.parent.fill(bodyColor);
                this.parent.stroke(40);
            }
        }

        this.parent.strokeWeight(2);
        this.parent.pushMatrix();
        this.parent.translate(position.x, position.y);
        this.parent.rotate(theta);
        this.parent.ellipse(0, 0, size, size);
        this.parent.line(0, 0, 0, -size / 2);
        if (health < 256) {
            if (health > 50) {
                this.parent.fill(health);
            } else {
                this.parent.fill(200, 80, 100);
            }
        } else {
            if (isBest) {
                this.parent.fill(255);
            } else {
                this.parent.fill(255);
            }
        }

        this.parent.ellipse(0, -size / 2, size / 3, size / 3);
        
        this.parent.fill(r, g, b);
        this.parent.rect(-4, 1, 8, 6);
        
        this.parent.strokeWeight(0);
        this.parent.fill(200, 30);
        this.parent.rect(-5, 8, 10, maxSpeed * 4);
        
        this.parent.rotate(-theta);
        this.parent.fill(100);
        //this.parent.text(id, -10, size * 1.5f);
        this.parent.popMatrix();
    }

    private void calculateNextMove() {
        if (nearestFood != null) {
            PVector dis = PVector.sub(nearestFood.getPosition(), position);
            PVector dif = PVector.sub(dis, velocity);
            dif.normalize();
            double[] senseData = {dif.x, dif.y, nearestFood.isPoison() ? 1 : -1, PApplet.map((float) health > 600 ? 600 : health, (float) 0, (float) 600, -1, 1)};
            double[] actionData = new double[6];
            brain.compute(senseData, actionData);
            velocity = velocity.add(new PVector((float) actionData[0], (float) actionData[1])); //.setMag((float) actionData[3] * 3)
            this.maxSpeed = PApplet.map((float) actionData[2], (float) -1, (float) 1, 0, 1) * 3f;
            r = (int) Math.floor((double) PApplet.map((float) actionData[3], (float) -1, (float) 1, 100, 255));
            g = (int) Math.floor((double) PApplet.map((float) actionData[4], (float) -1, (float) 1, 100, 255));
            b = (int) Math.floor((double) PApplet.map((float) actionData[5], (float) -1, (float) 1, 100, 255));
            
            moveAhead();
        }
    }

    private void updateMotion() {
        velocity.limit(maxSpeed);
        position.add(velocity);
    }

    private void updateHealth() {
        health -= (float) deathRate;
        //bodyColor = health;
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
                if (nearestFood.isPoison()) {
                    health -= (float) foodHealth * 2;
                } else {
                    health += (float) foodHealth;
                }
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
    
    public void setPosition(PVector position) {
        this.position = position;
    }
    
    public PVector getPosition() {
        return this.position;
    }
}
