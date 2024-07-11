package me.redstoner2019.fnaf;

import me.redstoner2019.graphics.Renderer;
import me.redstoner2019.graphics.general.ShaderProgram;
import me.redstoner2019.graphics.general.Texture;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.opengl.GL11.*;

public class FNAFMain {

    private long window;
    private Texture loadingTexture;
    public static int width = 1280;
    public static int height = 720;
    public static float aspectRatio;
    private boolean showDebug = false;
    private HashMap<String,Texture> textures = new HashMap<>();
    private boolean loadingComplete = false;
    private List<String> files = new ArrayList<>();
    private Renderer renderer;
    private double mouseX[] = new double[1];
    private double mouseY[] = new double[1];
    private boolean isMouseClicked = false;
    private Menu menu = Menu.MAIN_MENU;
    private float deltaTime = 1;
    private int nightNumber = 0;

    public void run() throws IOException {
        init();
        loop();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(width, height, "OD Five Nights at Freddy's", MemoryUtil.NULL, MemoryUtil.NULL);

        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(0); //VSYNC

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (key == GLFW_KEY_F3 && action == GLFW_RELEASE) {
                    showDebug = !showDebug;
                }
            }
        });

        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                width = w;
                height = h;
                renderer.setHeight(h);
                renderer.setWidth(w);
                GL11.glViewport(0, 0, width, height);
                updateProjectionMatrix();
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                isMouseClicked = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                isMouseClicked = false;
            }
        });

        GLFW.glfwShowWindow(window);
        GL.createCapabilities();

        GL11.glEnable(GL13.GL_MULTISAMPLE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        updateProjectionMatrix();

        renderer = new Renderer();

        loadingTexture = Texture.loadTexture("C:\\Users\\l.paepke\\Projects\\LWJGL\\src\\main\\resources\\textures\\jump.jpg");

        files.addAll(listFiles("C:\\Users\\l.paepke\\Projects\\LWJGL\\src\\main\\resources\\textures"));
    }

    private void loop() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Random random = new Random();
        int id = 0;
        long lastRandomChange = System.currentTimeMillis();

        /**
         * Performance Monitoring
         */
        long lastUpdate = System.currentTimeMillis();
        int frames = 0;
        int fps = 0;
        double lastFrameTime = 0;
        int componentsDrawn = 0;

        /**
         * Main Menu Data
         */

        boolean mainmenuFreddyMove = false;
        boolean noiseSwell = false;
        int mainMenuFreddyStage = 0;
        float baseNoise = .4f;
        float menuNoise = baseNoise;
        long noiseSwellStart = 0;
        int menuSelection = 0;

        /**
         * PRE_GAME data
         */

        long startTime = 0;

        /**
         * OFFICE data
         */

        float scroll = -1f;

        while (!GLFW.glfwWindowShouldClose(window)) {
            double start = glfwGetTime();

            /**
             * Random Event Generator
             */

            if(menu == Menu.MAIN_MENU) if(System.currentTimeMillis() - lastRandomChange >= 20){
                lastRandomChange = System.currentTimeMillis();
                if(mainmenuFreddyMove){
                    if(mainMenuFreddyStage > 3){
                        mainMenuFreddyStage = 0;
                        mainmenuFreddyMove = false;
                    } else if(random.nextInt(5) == 2){
                        mainMenuFreddyStage++;
                        if(mainMenuFreddyStage == 4) {
                            mainMenuFreddyStage = 0;
                            mainmenuFreddyMove = false;
                        }
                    }
                } else if(random.nextInt(40) == 5){
                    mainmenuFreddyMove = true;
                }

                if(random.nextInt(100) == 1){
                    if(!noiseSwell) {
                        noiseSwell = true;
                        noiseSwellStart = System.currentTimeMillis();
                    }
                }
            }

            /**
             * Loading next Texture
             */

            if(id < files.size()) {
                File f = new File(files.get(id));
                if(f.getName().endsWith(".png") || f.getName().endsWith(".jpg")) {
                    textures.put(f.getName(),Texture.loadTexture(f.getAbsolutePath()));
                }
                id++;
            } else loadingComplete = true;

            /**
             * Updating
             */

            if (GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL) {

                GLFW.glfwGetCursorPos(window, mouseX, mouseY);

                switch (menu) {
                    case MAIN_MENU -> {
                        int selection = menuSelection;
                        int offset = 25;

                        if(mouseX[0] >= 140 && mouseX[0] <= 440) if(mouseY[0] >= 350 - offset && mouseY[0] <= 400 - offset){
                            selection = 0;
                            if(isMouseClicked && loadingComplete) {
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                                nightNumber += 1;
                            }
                        } else if(mouseY[0] >= 410 - offset && mouseY[0] <= 460 - offset){
                            selection = 1;
                            if(isMouseClicked && loadingComplete) {
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                                nightNumber = 1;
                            }
                        } else if(mouseY[0] >= 470 - offset && mouseY[0] <= 520 - offset){
                            selection = 2;
                            if(isMouseClicked && loadingComplete) {
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                                nightNumber = 6;
                            }
                        } else if(mouseY[0] >= 530 - offset && mouseY[0] <= 580 - offset){
                            selection = 3;
                            if(isMouseClicked && loadingComplete) menu = Menu.CUSTOM_NIGHT;
                        }

                        if(isMouseClicked && loadingComplete) {
                            isMouseClicked = false;
                        }

                        if(menuSelection != selection){
                            menuSelection = selection;
                        }
                    }
                    case OFFICE -> {
                        float xPos = (float) (((mouseX[0] / width) * 2) - 1);

                        float slowSpeed = 0.01f * deltaTime;
                        float fastSpeed = 0.03f * deltaTime;

                        boolean positive = xPos > 0;
                        if(between(0.25,0.75,Math.abs(xPos))){
                            if(positive) scroll-=slowSpeed; else scroll+=slowSpeed;
                        } else if(between(0.75,1,Math.abs(xPos))){
                            if(positive) scroll-=fastSpeed; else scroll+=fastSpeed;
                        }
                        if(scroll > 1) scroll = 1;
                        if(scroll < -1) scroll = -1;
                    }
                }
            }

            /**
             * Start rendering process
             */

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, width, height, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);

            switch (menu) {
                case Menu.MAIN_MENU -> {
                    if(noiseSwell) {
                        if(System.currentTimeMillis() - noiseSwellStart >= 5000){
                            noiseSwell = false;
                        } else {
                            float noiseTime = (float) ((System.currentTimeMillis() - noiseSwellStart) / 5000.0);
                            menuNoise = (float) (Math.sin(noiseTime * Math.PI)*0.2f + baseNoise);
                        }
                    }

                    if(loadingComplete) {

                        switch (mainMenuFreddyStage) {
                            case 0: {
                                renderer.renderTexture(-1, -1, 2, 2, textures.get("431.png"), true, true, menuNoise);
                                break;
                            }
                            case 1: {
                                renderer.renderTexture(-1, -1, 2, 2, textures.get("440.png"), true, true, menuNoise);
                                break;
                            }
                            case 2: {
                                renderer.renderTexture(-1, -1, 2, 2, textures.get("441.png"), true, true, menuNoise);
                                break;
                            }
                            case 3: {
                                renderer.renderTexture(-1, -1, 2, 2, textures.get("442.png"), true, true, menuNoise);
                                break;
                            }
                        }

                        renderer.renderText("Five", 140, 100,50, Color.WHITE);
                        renderer.renderText("Nights", 140, 150,50, Color.WHITE);
                        renderer.renderText("at", 140, 200,50, Color.WHITE);
                        renderer.renderText("Freddy's", 140, 250,50, Color.WHITE);

                        renderer.renderText("New Game", 140, 350,50, Color.WHITE);
                        renderer.renderText("Continue", 140, 420,50, Color.WHITE);
                        renderer.renderText("6th Night", 140, 490,50, Color.GRAY);
                        renderer.renderText("Custom Night", 140, 560,50, Color.GRAY);

                        switch (menuSelection) {
                            case 0 -> renderer.renderText(">>", 70, 350,50, Color.WHITE);
                            case 1 -> renderer.renderText(">>", 70, 420,50, Color.WHITE);
                            case 2 -> renderer.renderText(">>", 70, 490,50, Color.GRAY);
                            case 3 -> renderer.renderText(">>", 70, 560,50, Color.GRAY);
                        }
                    }
                    else renderer.renderTexture(-1, -1,2,2,loadingTexture,true,false,0);

                    renderer.renderText("v0.0.1 - alpha", 10, height-20,20, Color.WHITE);
                }
                case PRE_GAME -> {
                    long timeSinceStart = System.currentTimeMillis() - startTime;
                    if(timeSinceStart > 0 && timeSinceStart < 6000) renderer.renderTexture(-1,-1,2,2,textures.get("539.png"),true,false,0);
                    if(timeSinceStart > 6000 && timeSinceStart < 9000) {
                        renderer.renderText("12:00 AM",(width - renderer.textWidth("12:00 AM", 60)) / 2,(float) ((height-120) / 2), 60,Color.WHITE);
                        renderer.renderText("Night " + nightNumber,(width - renderer.textWidth("Night " + nightNumber, 60)) / 2,(float) ((height-120) / 2) - 60, 60,Color.WHITE);
                    }
                    if(timeSinceStart > 9000) menu = Menu.OFFICE;
                }
                case OFFICE -> {
                    renderer.renderTexture(-2 + scroll,-1,4,2,textures.get("39.png"),true,false,0);
                }
                case CAMERAS -> {}
                case CUSTOM_NIGHT -> {}
            }

            if(showDebug){
                renderer.renderText("FPS: " + fps, 10, 20,20, Color.WHITE);
                renderer.renderText("Render Size: " + width + " / " + height, 10, 40,20, Color.WHITE);
                renderer.renderText("Last Frame Time: " + String.format("%.2f ms",lastFrameTime*1000), 10, 60,20, Color.WHITE);
                renderer.renderText("Time: " + String.format("%.4fs",glfwGetTime()), 10, 80,20, Color.WHITE);
                renderer.renderText("Components Drawn: " + componentsDrawn, 10, 100,20, Color.WHITE);
                renderer.renderText("Delta Time: " + String.format("%.2fs",deltaTime), 10, 120,20, Color.WHITE);
            }

            deltaTime = (float) (lastFrameTime / (1.0/60.0));

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            /**
             * Performance monitoring updating
             */

            frames++;
            if(System.currentTimeMillis() - lastUpdate >= 1000){
                fps = frames;
                frames = 0;
                lastUpdate = System.currentTimeMillis();
            }
            lastFrameTime = glfwGetTime() - start;
        }
    }

    public static void main(String[] args){
        try {
            new FNAFMain().run();
        } catch (IOException e) {
            main(args);
        }
    }

    public Set<String> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
    }

    private void updateProjectionMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        aspectRatio = (float) width / height;
        GL11.glOrtho(-aspectRatio, aspectRatio, -1f, 1.0f, -1f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    private boolean between(double val1, double val2, double toCheck){
        return Math.min(val1,val2) <= toCheck && Math.max(val1,val2) >= toCheck;
    }
}
