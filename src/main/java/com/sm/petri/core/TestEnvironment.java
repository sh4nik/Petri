package com.sm.petri.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import processing.core.PApplet;

public class TestEnvironment extends PApplet {

    List<Bloop> bloops = new ArrayList<>();
    List<Food> foodList = new ArrayList<>();

    private static final int BLOB_COUNT = 1;
    private static final int FOOD_COUNT = 100;
    
    private double[] genes = {-0.5129484620861633,0.08843642387133266,0.11374556312345474,0.7884316144565984,0.7128725124501019,-0.23308464812807617,0.26705296692327085,0.029405804609226482,0.5007853465451828};

    public static void main(String[] args) {
        PApplet.main("com.sm.petri.core.TestEnvironment");
    }

    @Override
    public void settings() {
        size(1240, 660);
    }

    @Override
    public void setup() {
        for (int i = 0; i < BLOB_COUNT; i++) {
            bloops.add(new Bloop(this, foodList, genes));
        }
    }

    @Override
    public void draw() {

        frameRate(60);
        background(254);

        Iterator<Bloop> iter = bloops.iterator();

        while (iter.hasNext()) {
            Bloop bloop = iter.next();

            bloop.update();
            bloop.display();

            if (bloop.getHealth() <= 0) {
                iter.remove();
            }
        }

        for (int i = foodList == null ? 0 : foodList.size(); i < FOOD_COUNT; i++) {
            foodList.add(new Food(this));
        }

        for (Food food : foodList) {
            food.display();
        }

    }

}
