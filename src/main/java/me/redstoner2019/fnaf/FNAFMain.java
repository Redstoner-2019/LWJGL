package me.redstoner2019.fnaf;

import me.redstoner2019.graphics.general.ShaderProgram;
import me.redstoner2019.graphics.general.Texture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FNAFMain {

    private long window;
    private Texture loadingTexture;
    public static int width;
    public static int height;
    public static float aspectRatio;
    public static ShaderProgram textureShader;
    public static int vao;
    private boolean showDebug = true;
    private HashMap<String,Texture> textures = new HashMap<>();
    private boolean loadingComplete = false;
    private List<String> files = new ArrayList<>();

    public void run() throws IOException {
        /*init();
        loop();*/
    }
    public static void main(String[] args) {
        while (true){
            try {
                new FNAFMain().run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
