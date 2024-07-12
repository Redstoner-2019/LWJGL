package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.game.cameras.Camera1C;

import java.util.Random;

public class Foxy extends Animatronic{
    private static Foxy INSTANCE;
    private int stage = 0;

    public int getStage() {
        return stage;
    }

    private Foxy(){
        setCurrentCamera(Camera1C.getInstance());
    }

    @Override
    public void movementOpportunity() {
        Random random = new Random();
        int roll = random.nextInt(19) + 1;
        System.out.println("Foxy rolled " + roll + " ai " + getAI_LEVEL());
        if(roll <= getAI_LEVEL()){
            stage++;
            if(stage == 4) stage = 3;
        }
    }

    public static Foxy getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Foxy();
        }
        return INSTANCE;
    }
}
