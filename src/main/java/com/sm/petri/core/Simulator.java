package com.sm.petri.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import processing.core.PApplet;
import static processing.core.PApplet.println;

public class Simulator extends PApplet {

    List<Bloop> bloops = new ArrayList<>();
    List<Food> foodList = new ArrayList<>();

    private static final int BLOB_COUNT = 1000;
    private static final int FOOD_COUNT = 200;
    private static final int EVO_START_THRESHOLD_POP = 20;
    private static final int SELECTION_THRESHOLD_RANKING = 2;
    
    private boolean render = true;

    public static void main(String[] args) {
        PApplet.main("com.sm.petri.core.Simulator");
    }

    @Override
    public void settings() {
        size(1240, 660);
    }

    @Override
    public void setup() {
        for (int i = 0; i < BLOB_COUNT; i++) {
            bloops.add(new Bloop(this, foodList, null));
        }
    }

    @Override
    public void draw() {
        if (!render) {
            frameRate(600);
        } else {
            frameRate(60);
        }
        background(254);

        Collections.sort(bloops, new Comparator<Bloop>() {
            @Override
            public int compare(Bloop blobA, Bloop blobB) {
                return (int) (blobB.getHealth() - blobA.getHealth());
            }
        });

        Iterator<Bloop> iter = bloops.iterator();

        List<Bloop> newBloops = new ArrayList<>();

        while (iter.hasNext()) {
            Bloop bloop = iter.next();

            bloop.update();

            if (render) {
                bloop.display();
            }

            if (bloop.getHealth() <= 0) {
                iter.remove();
                if (bloops.size() < EVO_START_THRESHOLD_POP) {
                    newBloops.add(getNextGenBlob());
                }
            }

        }

        println(frameRate + " : " + bloops.size() + " : " + bloops.get(0).getHealth());

        for (Bloop newBloop : newBloops) {
            bloops.add(newBloop);
        }

        for (int i = foodList == null ? 0 : foodList.size(); i < FOOD_COUNT; i++) {
            foodList.add(new Food(this));
        }

        if (render) {
            for (Food food : foodList) {
                food.display();
            }
        }
    }

    Bloop getNextGenBlob() {
        Bloop selectedBloop = bloops.get((int) Math.floor(random(SELECTION_THRESHOLD_RANKING)));
        return new Bloop(this, foodList, selectedBloop.getBrain());
    }

    @Override
    public void mousePressed() {
        render = !render;

    }

}
