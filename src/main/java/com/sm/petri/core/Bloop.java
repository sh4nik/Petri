package com.sm.petri.core;

import java.util.List;
import java.util.ListIterator;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import processing.core.PApplet;
import processing.core.PVector;

public class Bloop {

    //common bloop stuff
    public static int lastId = 0;
    private static float deathRate = 1;
    private static float foodHealth = 500;

    //attributes of the individual bloop
    private int id;
    private float health;
    private float age;
    private BasicNetwork brain;
    private float size;
    private float maxSpeed;
    private final float rotationLimit = 0.3f;
    private float bodyColor = -1;
    public boolean isBest = false;
    private boolean hostile = false;

    private int r;
    private int g;
    private int b;

    //environment related stuff
    List<Food> foodList;
    List<Bloop> bloopList;
    Food nearestFood;
    Bloop nearestBloop;
    PApplet parent;

    //placement related variables
    PVector position;
    PVector velocity;

    public Bloop(PApplet parent, List<Food> foodList, double[] providedGenes, List<Bloop> bloopList) {

        //initialize values
        this.id = lastId++;
        this.foodList = foodList;
        this.bloopList = bloopList;
        this.parent = parent;

        this.health = 500;
        this.age = 8;
        this.size = 16;
        this.maxSpeed = 3;

        brain = new BasicNetwork();
        brain.addLayer(new BasicLayer(null, true, 10));
        brain.addLayer(new BasicLayer(new ActivationTANH(), true, 8));
        brain.addLayer(new BasicLayer(new ActivationTANH(), false, 7));
        brain.getStructure().finalizeStructure();
        brain.reset((int) this.parent.random(999999999));

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
//        blockBorders();
        processNearestFood();
        processNearestBloop();
    }

    public void display() {
        float theta = velocity.heading2D() + PApplet.PI / 2;

        if (isBest) {
            this.parent.fill(230, 42, 110);
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
                this.parent.fill(health);
            }
        } else {
            if (hostile) {
                this.parent.fill(200, 100, 120);
            } else {
                this.parent.fill(255);
            }
        }

        this.parent.ellipse(0, -size / 2, size / 3, size / 3);

        this.parent.fill(r, g, b);
//        this.parent.arc(0, 0, size, size, 0, this.parent.PI);
//        this.parent.line(-size / 2, 0, size / 2, 0);
        this.parent.rect(-size / 4, 0, size / 2, size / 2);

        this.parent.strokeWeight(0);
        this.parent.fill(200, 30);
        this.parent.rect(-size / 4, size / 3, size / 2, maxSpeed * 5);

        this.parent.rotate(-theta);
        this.parent.fill(100);
        //this.parent.text(id, -10, size * 1.5f);
        this.parent.popMatrix();
    }

    private void calculateNextMove() {

        if (nearestFood != null && nearestBloop != null) {
            PVector dis = PVector.sub(nearestFood.getPosition(), position);
            PVector dif = PVector.sub(dis, velocity);
            dif.normalize();

            PVector bdis = PVector.sub(nearestBloop.getPosition(), position);
            PVector bdif = PVector.sub(bdis, velocity);
            bdif.normalize();

            //Input
            double myHealth = PApplet.map(health > 600 ? 600 : health, 0, 600, -1, 1);
            double iAmHostile = PApplet.map(hostile ? 1 : 0, 0, 1, -1, 1);
            double nearestFoodVectorX = dif.x;
            double nearestFoodVectorY = dif.y;
            double nearestFoodIsPoison = nearestFood.isPoison() ? 1 : -1;
            double nearestBloopVectorX = bdif.x;
            double nearestBloopVectorY = bdif.y;
            double nearestBloopIsHostile = PApplet.map(nearestBloop.isHostile() ? 1 : 0, 0, 1, -1, 1);
            double nearestBloopR = PApplet.map(nearestBloop.getR(), 100, 255, -1, 1);
            double nearestBloopG = PApplet.map(nearestBloop.getG(), 100, 255, -1, 1);
            double nearestBloopB = PApplet.map(nearestBloop.getB(), 100, 255, -1, 1);

            double[] senseData = {
                myHealth,
                iAmHostile,
                nearestFoodVectorX,
                nearestFoodVectorY,
                nearestFoodIsPoison,
                nearestBloopVectorX,
                nearestBloopVectorY,
                nearestBloopIsHostile,
                nearestBloopR,
                nearestBloopG,
                nearestBloopB,};

            double[] actionData = new double[7];

            brain.compute(senseData, actionData);

            // Output
            double steeringVectorX = actionData[0];
            double steeringVectorY = actionData[1];
            double speed = actionData[2];
            double hostility = actionData[3];
            double myR = actionData[4];
            double myG = actionData[5];
            double myB = actionData[6];

            velocity = velocity.add(new PVector(PApplet.map((float) steeringVectorX, -1, 1, -rotationLimit, rotationLimit), PApplet.map((float) steeringVectorY, -1, 1, -rotationLimit, rotationLimit)));
            this.maxSpeed = PApplet.map((float) speed, -1, 1, 0, 1) * 2.2f;
            hostile = hostility > 0.5;
            r = (int) Math.floor((double) PApplet.map((float) myR, -1, 1, 100, 255));
            g = (int) Math.floor((double) PApplet.map((float) myG, -1, 1, 100, 255));
            b = (int) Math.floor((double) PApplet.map((float) myB, -1, 1, 100, 255));

            if (velocity.x == 0 && velocity.y == 0) {
                velocity = PVector.random2D();
            }
            velocity.setMag(maxSpeed);
        }
    }

    private void updateMotion() {
        velocity.limit(maxSpeed);
        position.add(velocity);
    }

    private void updateHealth() {
        health -= (float) deathRate * (hostile ? 3 : 1);
        age += 0.005;
        size = age < 16 ? age : 16;
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

    private void processNearestBloop() {
        ListIterator<Bloop> iter = bloopList.listIterator();
        float shortestDistance = parent.width;
        while (iter.hasNext()) {
            Bloop bloop = iter.next();

            float d = this.position.dist(bloop.getPosition());
            if (d < shortestDistance && bloop != this) {
                shortestDistance = d;
                nearestBloop = bloop;
            }

        }

        if (nearestBloop != null && shortestDistance < size * 1.5) {
            if (hostile) {
                health += foodHealth * 2;
                nearestBloop.setHealth(nearestBloop.getHealth() - (foodHealth * 5));
            } else {
                health += health < 50 ? deathRate * 0.6 : health < 500 ? deathRate * 0.4 : 0;
            }
        }
    }

    private void processNearestFood() {
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
            if (!hostile && nearestFood != null && d < ((size < nearestFood.getSize()) ? nearestFood.getSize() : size - nearestFood.getSize())) {
                iter.remove();
                if (nearestFood.isPoison()) {
                    health -= (float) foodHealth * 5;
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

    public float getAge() {
        return age;
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

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public boolean isHostile() {
        return hostile;
    }

    public void setHostile(boolean hostile) {
        this.hostile = hostile;
    }

}
