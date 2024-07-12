package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.game.cameras.Camera1A;

import java.util.Random;

public class Freddy extends Animatronic{
    private static Freddy INSTANCE;
    private Freddy(){
        setCurrentCamera(Camera1A.getInstance());
    }

    @Override
    public void movementOpportunity() {
        Random random = new Random();
        int roll = random.nextInt(19) + 1;
        System.out.println("Freddy rolled " + roll + " ai " + getAI_LEVEL());
        if(roll <= getAI_LEVEL()){
            System.out.println("Move");
        }
    }

    public static Freddy getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Freddy();
        }
        return INSTANCE;
    }
}
