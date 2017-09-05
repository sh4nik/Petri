package com.sm.petri.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import processing.core.PApplet;
import static processing.core.PApplet.println;
import processing.core.PVector;

public class Simulator extends PApplet {

    List<Bloop> bloops = new ArrayList<>();
    List<Food> foodList = new ArrayList<>();

    private static final int BLOB_COUNT = 2;
    private static final int MAX_BLOB_COUNT = 50;
    private static final int FOOD_COUNT = 100;
    private static final int EVO_FORCE_THRESHOLD_POP = 2;
    private static final float REPRODUCTION_PROBABILITY = 0.0015f;
    private static final float MUTATION_RATE = 0.3f;

    private boolean render = true;

    private int tthg = 0;

    public static long seed;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private PVector previousPlotPoint;
    boolean screenCleared = false;
    int graphTime = 0;

    public static void main(String[] args) {
        PApplet.main("com.sm.petri.core.Simulator");
    }

    @Override
    public void settings() {
        size(1900, 900);
//        seed = 2147483647;
        randomSeed(System.currentTimeMillis());
        seed = floor(random(999999999));

        
        println("Seed: " + seed);
        randomSeed(seed);
//        fullScreen();
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
            if (!screenCleared) {
                background(50);
                screenCleared = true;
                graphTime = 0;
                previousPlotPoint = new PVector(0, 1000);
            }
        } else {
            frameRate(60);
            background(50);
            screenCleared = false;
        }

        Collections.sort(bloops, new Comparator<Bloop>() {
            @Override
            public int compare(Bloop blobA, Bloop blobB) {
                if (blobB.getHealth() != blobA.getHealth()) {
                    return blobB.getHealth() > blobA.getHealth() ? 1 : -1;
                } else {
                    return -1;
                }
            }
        });

        Iterator<Bloop> iter = bloops.iterator();

        List<Bloop> newBloops = new ArrayList<>();

        while (iter.hasNext()) {
            Bloop bloop = iter.next();

            bloop.update();
            bloop.isBest = false;
            bloops.get(0).isBest = true;

            if (render) {
                bloop.display();
            }

            if (bloop.getHealth() <= 0) {
                iter.remove();
                if (bloops.size() < EVO_FORCE_THRESHOLD_POP) {
                    newBloops.add(getNextGenBloop(bloops));
                }
            }

//            if (bloop.getHealth() < 300) {
            if (bloops.size() < MAX_BLOB_COUNT && random(1) < REPRODUCTION_PROBABILITY) {
                newBloops.add(getNextGenBloop(bloop, bloops.get(0)));
                bloop.setHealth(bloop.getHealth() - 50);
            }
//            }
        }

        logStats(bloops);

        for (Bloop newBloop : newBloops) {
            bloops.add(newBloop);
        }
//        if (foodList == null || foodList.size() < 6) {
        for (int i = foodList == null ? 0 : foodList.size(); i < FOOD_COUNT; i++) {
            foodList.add(new Food(this));
        }
//        }

        if (render) {
            for (Food food : foodList) {
                food.display();
            }
        }

        if (!render) {
            stroke(100, 180, 150);
            strokeWeight(3);
            float newX = graphTime / 5;
            graphTime++;
            float newY = (height - ((bloops.get(0).getHealth() / 2) % 500));
            line(previousPlotPoint.x, previousPlotPoint.y, newX, newY);
            previousPlotPoint.x = newX;
            previousPlotPoint.y = newY;
            if (previousPlotPoint.x > width) {
                screenCleared = false;
            }
        }

    }

    Bloop getNextGenBloop(Bloop bloop) {

        int brainSize = bloop.getBrain().encodedArrayLength();
        double[] genes = new double[brainSize];
        bloop.getBrain().encodeToArray(genes);

        boolean mut = false;

        if (random(1) < MUTATION_RATE) {
            //System.out.println(ANSI_RED + "*************************************************************************************************************" + ANSI_RESET);
            genes[floor(random(0, brainSize - 1))] = random(-1, 1);
            mut = true;
        }

        Bloop bloopKid = new Bloop(this, foodList, genes);

        if (mut) {
            bloopKid.setBodyColor(200);
        }

        return bloopKid;
    }

    Bloop getNextGenBloop(Bloop bloopDad, Bloop bloopMom) {

        int j = floor(random(2));

        int brainSize = bloopDad.getBrain().encodedArrayLength();
        double[] dadGenes = new double[brainSize];
        bloopDad.getBrain().encodeToArray(dadGenes);

        double[] momGenes = new double[brainSize];
        bloopMom.getBrain().encodeToArray(momGenes);

        double[] kidGenes = new double[brainSize];

        int splicePoint = (int) floor(random(brainSize / 4, brainSize / 4 * 3));

        for (int i = 0; i < brainSize; i++) {
            if (i < splicePoint) {
                kidGenes[i] = dadGenes[i];
            } else {
                kidGenes[i] = momGenes[i];
            }
        }

        boolean mut = false;

        if (random(1) < MUTATION_RATE) {
            //System.out.println(ANSI_RED + "*************************************************************************************************************" + ANSI_RESET);
            kidGenes[floor(random(0, brainSize - 1))] = random(-1, 1);
            mut = true;
        }

        Bloop bloopKid = new Bloop(this, foodList, kidGenes);

        if (mut) {
            bloopKid.setBodyColor(200);
        }

//        bloopKid.setPosition(bloopDad.getPosition().copy());
        return bloopKid;
    }

    Bloop getNextGenBloop(List<Bloop> bloops) {

        if (bloops.size() < 2) {
            return new Bloop(this, foodList, null);
        }

        int j = floor(random(2));

        Bloop bloopDad = bloops.get(j);
        int brainSize = bloopDad.getBrain().encodedArrayLength();
        double[] dadGenes = new double[brainSize];
        bloopDad.getBrain().encodeToArray(dadGenes);

        Bloop bloopMom = bloops.get(1 - j);
        double[] momGenes = new double[brainSize];
        bloopMom.getBrain().encodeToArray(momGenes);

        double[] kidGenes = new double[brainSize];

        int splicePoint = 6;

        for (int i = 0; i < brainSize; i++) {
            if (i < splicePoint) {
                kidGenes[i] = dadGenes[i];
            } else {
                kidGenes[i] = momGenes[i];
            }
        }

        if (random(1) < MUTATION_RATE) {
            //System.out.println(ANSI_RED + "*************************************************************************************************************" + ANSI_RESET);
            kidGenes[floor(random(0, brainSize - 1))] = random(-1, 1);
        }

        return new Bloop(this, foodList, kidGenes);
    }

    private void logStats(List<Bloop> bloops) {
//        String log = "TPS " + floor(frameRate) + " : POP " + bloops.size() + " : THG " + (floor(bloops.get(0).getHealth()) - 255) + " / " + tthg + " : BID " + bloops.get(0).getId() + " : " + bloops.get(0).getBrain().dumpWeights();
//        if ((floor(bloops.get(0).getHealth()) - 255) > tthg) {
//            tthg = (floor(bloops.get(0).getHealth()) - 255);
//            println(ANSI_PURPLE + log + ANSI_RESET);
//        } else {
//            println(log);
//        }
        println("Fittest Brain [" + bloops.get(0).getId() + "]: " + bloops.get(0).getBrain().dumpWeights());
    }

    @Override
    public void mousePressed() {
        render = !render;

    }

}
