package com.sm.petri.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import processing.core.PApplet;

public class TestEnvironment extends PApplet {

    List<Bloop> bloops = new ArrayList<>();
    List<Food> foodList = new ArrayList<>();

    private static final int BLOB_COUNT = 1;
    private static final int FOOD_COUNT = 10;
    
//    private double[] genes = {-0.38746798038482666,-0.8490146398544312,-0.7334953546524048,0.641432523727417,0.7474620342254639,0.7084528207778931,-0.15198874473571777,0.7329657077789307,-0.5510956048965454,-0.46473968029022217,-0.014219284057617188,-0.8311880553104549};
    private double[] genes = {0.9369839429855347,-0.8343830108642578,-0.8295278549194336,0.9627845287322998,0.9877475500106812,0.17621302604675293,0.4944499731063843,-0.7827118635177612,0.9640790224075317,0.7881630659103394,0.9138119220733643,-0.49324917793273926,-0.19570350646972656,0.5310683250427246,-0.648759010869816};
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
