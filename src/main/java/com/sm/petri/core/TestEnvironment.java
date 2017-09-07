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
    private double[] genes = {-0.0013045072555541992, -0.7262663841247559, -0.8840100765228271, -0.5907706022262573, 0.6357961893081665, 0.12503492832183838, -0.8408528566360474, 0.6085801124572754, -0.8781139850616455, -0.04096841812133789, 0.67219078540802, 0.15846872329711914, 0.5857223272323608, -0.5766264200210571, 0.9106595516204834, -0.7600914239883423, 0.277873158454895, -0.6547071933746338, 0.06561994552612305, 0.018799662590026855, -0.08709228038787842, 0.6948109865188599, 0.41129839420318604, -0.6292638778686523, 0.7850799560546875, 0.9363541603088379, -0.05834007263183594, -0.42774486541748047, 0.5676037073135376, 0.8764630556106567, -0.4975544214248657, 0.07940232753753662, -0.9736713171005249, 0.8714430332183838, -0.2326977252960205, -0.5532844066619873, 0.5935088396072388, -0.5331325531005859, 0.8018975257873535, 0.4337880611419678, -0.8275949954986572, 0.9869563579559326, -0.8809911012649536, 0.559211015701294, -0.8299845457077026, -0.8607915639877319, -0.9089877605438232, -0.6292849779129028, -0.696702241897583, -0.7022396274149044};

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
            bloops.add(new Bloop(this, foodList, genes, bloops));
        }
    }

    @Override
    public void draw() {

        frameRate(60);
        background(40);

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
