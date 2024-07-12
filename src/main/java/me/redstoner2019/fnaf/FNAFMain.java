package me.redstoner2019.fnaf;

import me.redstoner2019.audio.Sound;
import me.redstoner2019.audio.SoundManager;
import me.redstoner2019.fnaf.game.DoorState;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Foxy;
import me.redstoner2019.fnaf.game.animatronics.Freddy;
import me.redstoner2019.fnaf.game.cameras.*;
import me.redstoner2019.graphics.Renderer;
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
    private boolean showDebug = true;
    private HashMap<String,Texture> textures = new HashMap<>();
    public static HashMap<String, Sound> sounds = new HashMap<>();
    private Renderer renderer;
    private double mouseX[] = new double[1];
    private double mouseY[] = new double[1];
    private boolean isMouseClicked = false;
    private Menu menu = Menu.MAIN_MENU;
    private float deltaTime = 1;
    private int nightNumber = 0;
    private SoundManager soundManager;
    private float scroll = 0f;
    private Camera currentCamera = Camera1A.getInstance();
    private int cameraRandomness = 0;
    private Random random = new Random();

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
                if (key == GLFW_KEY_F1 && action == GLFW_RELEASE) {
                    menu = Menu.OFFICE;
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

                //renderer.renderTexture((scroll * 0.25f)+1.1f,-0.385f,w*0.6f,h,textures.get(textureRight),true,false,0);
                //renderer.renderTexture((scroll * 0.25f)-1.25f,-0.385f,w*0.6f,h,textures.get(textureLeft),true,false,0);

                float mx = (float) ((mouseX[0] / width) * 2) - 1;
                float my = (float) ((mouseY[0] / height) * 2) - 1;

                Office office = Office.getInstance();

                if(menu == Menu.OFFICE) {
                    mx-=scroll;
                    if(between(-1.9f,-1.95f,mx) && between(-0.5,0.05,my)){
                        if(office.isLeftDoor()) {
                            office.setLeftDoorState(DoorState.OPENING);
                            office.setLeftDoorAnimation(12);
                        }
                        else {
                            office.setLeftDoorState(DoorState.CLOSING);
                            office.setLeftDoorAnimation(0);
                        }
                        office.setLeftDoor(!office.isLeftDoor());
                        sounds.get("door.ogg").setVolume(1);
                        sounds.get("door.ogg").stop();
                        sounds.get("door.ogg").play();
                    }
                    if(between(-1.9f,-1.95f,mx) && between(0.13,0.23,my)){
                        office.setLeftLight(!office.isLeftLight());
                        if(office.isRightLight()) office.setRightLight(false);
                        if(office.isLeftLight() || office.isRightLight()){
                            sounds.get("lights.ogg").play();
                            sounds.get("lights.ogg").setRepeating(true);
                            if(office.getLeftDoorAnimatronic() != null){
                                sounds.get("windowscare.ogg").play();
                            }
                        } else {
                            sounds.get("lights.ogg").stop();
                        }
                    }

                    if(between(1.87f,1.93f,mx) && between(-0.5,0.05,my)){
                        if(office.isRightDoor()) {
                            office.setRightDoorState(DoorState.OPENING);
                            office.setRightDoorAnimation(12);
                        }
                        else {
                            office.setRightDoorState(DoorState.CLOSING);
                            office.setRightDoorAnimation(0);
                        }
                        office.setRightDoor(!office.isRightDoor());
                        sounds.get("door.ogg").setVolume(1);
                        sounds.get("door.ogg").stop();
                        sounds.get("door.ogg").play();
                    }
                    if(between(1.87f,1.93f,mx) && between(0.13,0.23,my)){
                        office.setRightLight(!office.isRightLight());
                        if(office.isLeftLight()) office.setLeftLight(false);
                        if(office.isLeftLight() || office.isRightLight()){
                            sounds.get("lights.ogg").play();
                            sounds.get("lights.ogg").setRepeating(true);
                            if(office.getRightDoorAnimatronic() != null){
                                sounds.get("windowscare.ogg").play();
                            }
                        } else {
                            sounds.get("lights.ogg").stop();
                        }
                    }
                } else if (menu == Menu.CAMERAS){
                    //System.out.println(mx + " " + my);
                    my = my*-1;
                    float x = .625f;
                    float y = -.225f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera1A.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    x = .6f;
                    y = -.35f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera1B.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    x = .47f;
                    y = -.42f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera5.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    x = .57f;
                    y = -.525f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera1C.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    x = .52f;
                    y = -.75f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera3.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    x = .625f;
                    y = -.82f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera2A.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    y = -.9f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera2B.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    x = .774f;
                    y = -.82f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera4A.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    y = -.9f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera4B.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    x = .9f;
                    y = -.725f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera6.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }

                    y = -.42f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        currentCamera = Camera7.getInstance();
                        cameraRandomness = random.nextInt(100);
                    }
                }

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

        renderer.setHeight(height);
        renderer.setWidth(width);

        loadingTexture = Texture.loadTexture("src\\main\\resources\\textures\\jump.jpg");
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

        boolean mainmenuFreddyTwitching = false;
        boolean noiseSwell = false;
        int mainMenuFreddyTwitchStage = 0;
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

        boolean hasExitedCameraButton = true;
        boolean nightRunning = false;
        int lastMovement = 0;
        int fan_stage = 0;
        int camera_flip = 0;
        long nightStart = 0;
        long nightEnd = 0;

        /**
         * Camera Data
         */

        /**
         * Loading Files
         */

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        renderer.renderTexture(-1,-1,2,2,loadingTexture,true,false,0);

        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();

        for(String s : listFiles("src\\main\\resources\\textures")){
            File f = new File(s);
            /*try {
                System.out.println(f.getAbsolutePath());
                BufferedImage image = ImageIO.read(f);
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        if(image.getRGB(x,y) == Color.BLACK.getRGB()) image.setRGB(x,y,Color.TRANSLUCENT);
                    }
                }
                ImageIO.write(image,f.getName().toUpperCase().substring(f.getName().length()-3),f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
            textures.put(f.getName(),Texture.loadTexture(f.getAbsolutePath()));
        }

        soundManager = new SoundManager();

        System.out.println();

        for(String s : listFiles("src\\main\\resources\\audio")){
            Sound so = new Sound(s,false);
            sounds.put(new File(s).getName(),so);
        }

        sounds.get("Static2.ogg").play();
        sounds.get("Mainmenu1.ogg").play();
        sounds.get("Mainmenu1.ogg").setRepeating(true);

        while (!GLFW.glfwWindowShouldClose(window)) {
            double start = glfwGetTime();

            /**
             * Random Event Generator
             */

            if(System.currentTimeMillis() - lastRandomChange >= 10){
                lastRandomChange = System.currentTimeMillis();
                if(mainmenuFreddyTwitching){
                    if(mainMenuFreddyTwitchStage > 3){
                        mainMenuFreddyTwitchStage = 0;
                        mainmenuFreddyTwitching = false;
                    } else if(random.nextInt(10) == 2){
                        mainMenuFreddyTwitchStage++;
                        if(mainMenuFreddyTwitchStage == 4) {
                            mainMenuFreddyTwitchStage = 0;
                            mainmenuFreddyTwitching = false;
                        }
                    }
                } else if(random.nextInt(60) == 5){
                    mainmenuFreddyTwitching = true;
                }

                if(random.nextInt(120) == 1){
                    if(!noiseSwell) {
                        noiseSwell = true;
                        noiseSwellStart = System.currentTimeMillis();
                    }
                }

                fan_stage++;

                /**
                 * Door Management
                 */

                Office office = Office.getInstance();

                if(office.getLeftDoorState() == DoorState.CLOSING){
                    office.setLeftDoorAnimation(office.getLeftDoorAnimation() + 1);
                    if(office.getLeftDoorAnimation() == 14) office.setLeftDoorState(DoorState.CLOSED);
                }
                if(office.getLeftDoorState() == DoorState.OPENING){
                    office.setLeftDoorAnimation(office.getLeftDoorAnimation() - 1);
                    if(office.getLeftDoorAnimation() == -1) office.setLeftDoorState(DoorState.OPEN);
                }

                if(office.getRightDoorState() == DoorState.CLOSING){
                    office.setRightDoorAnimation(office.getRightDoorAnimation() + 1);
                    if(office.getRightDoorAnimation() == 14) office.setRightDoorState(DoorState.CLOSED);
                }
                if(office.getRightDoorState() == DoorState.OPENING){
                    office.setRightDoorAnimation(office.getRightDoorAnimation() - 1);
                    if(office.getRightDoorAnimation() == -1) office.setRightDoorState(DoorState.OPEN);
                }

                if(fan_stage == 2) fan_stage = 0;
            }

            /**
             * Updating
             */

            int timeInSeconds = (int) (System.currentTimeMillis() - nightStart) / 1000;

            if(nightRunning && timeInSeconds % 5 == 0 && lastMovement != timeInSeconds){
                lastMovement = timeInSeconds;
                Bonnie.getInstance().movementOpportunity();
                Freddy.getInstance().movementOpportunity();
                Chica.getInstance().movementOpportunity();
                Foxy.getInstance().movementOpportunity();
            }

            if (GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL) {
                GLFW.glfwGetCursorPos(window, mouseX, mouseY);
                switch (menu) {
                    case MAIN_MENU -> {
                        int selection = menuSelection;
                        int offset = 25;

                        if(mouseX[0] >= 140 && mouseX[0] <= 440) if(mouseY[0] >= 350 - offset && mouseY[0] <= 400 - offset){
                            selection = 0;
                            if(isMouseClicked) {
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                                nightNumber += 1;
                            }
                        } else if(mouseY[0] >= 410 - offset && mouseY[0] <= 460 - offset){
                            selection = 1;
                            if(isMouseClicked) {
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                                nightNumber = 1;
                            }
                        } else if(mouseY[0] >= 470 - offset && mouseY[0] <= 520 - offset){
                            selection = 2;
                            if(isMouseClicked) {
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                                nightNumber = 6;
                            }
                        } else if(mouseY[0] >= 530 - offset && mouseY[0] <= 580 - offset){
                            selection = 3;
                            if(isMouseClicked) menu = Menu.CUSTOM_NIGHT;
                        }

                        if(isMouseClicked) {
                            isMouseClicked = false;
                        }

                        if(menuSelection != selection){
                            menuSelection = selection;
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                        }
                    }
                    case OFFICE -> {
                        float xPos = (float) (((mouseX[0] / width) * 2) - 1);

                        float slowSpeed = 0.03f * deltaTime;
                        float fastSpeed = 0.08f * deltaTime;

                        boolean positive = xPos > 0;
                        if(between(0.25,0.75,Math.abs(xPos))){
                            if(positive) scroll-=slowSpeed; else scroll+=slowSpeed;
                        } else if(between(0.75,1,Math.abs(xPos))){
                            if(positive) scroll-=fastSpeed; else scroll+=fastSpeed;
                        }
                        if(scroll > 1) scroll = 1;
                        if(scroll < -1) scroll = -1;

                        if(between(0.25f,0.75f,mouseX[0] / width) && between(0.85f,100f,mouseY[0] / height)){
                            if(hasExitedCameraButton) {
                                menu = Menu.CAMERAS;
                                cameraRandomness = random.nextInt(100);
                                sounds.get("cameraFlip.oga").play();
                                sounds.get("blip.ogg").stop();
                                sounds.get("cameras.oga").play();
                                sounds.get("cameras.oga").setRepeating(true);
                            }
                            hasExitedCameraButton = false;
                        } else {
                            hasExitedCameraButton = true;
                        }
                    }
                    case CAMERAS -> {
                        if(between(0.25f,0.75f,mouseX[0] / width) && between(0.85f,100f,mouseY[0] / height)){
                            if(hasExitedCameraButton) {
                                menu = Menu.OFFICE;
                                sounds.get("blip.ogg").play();
                                sounds.get("cameraFlip.oga").stop();
                                sounds.get("cameras.oga").stop();
                            }
                            hasExitedCameraButton = false;
                        } else {
                            hasExitedCameraButton = true;
                        }
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

                    switch (mainMenuFreddyTwitchStage) {
                        case 0: {
                            renderer.renderTexture(-1, -1, 2, 2, textures.get("freddy.twitch.1.png"), true, true, menuNoise);
                            break;
                        }
                        case 1: {
                            renderer.renderTexture(-1, -1, 2, 2, textures.get("freddy.twitch.2.png"), true, true, menuNoise);
                            break;
                        }
                        case 2: {
                            renderer.renderTexture(-1, -1, 2, 2, textures.get("freddy.twitch.3.png"), true, true, menuNoise);
                            break;
                        }
                        case 3: {
                            renderer.renderTexture(-1, -1, 2, 2, textures.get("freddy.twitch.4.png"), true, true, menuNoise);
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

                    renderer.renderText("v0.0.1 - alpha", 10, height-20,20, Color.WHITE);
                }
                case PRE_GAME -> {
                    long timeSinceStart = System.currentTimeMillis() - startTime;
                    if(timeSinceStart > 0 && timeSinceStart < 3000) renderer.renderTexture(-1,-1,2,2,textures.get("help_wanted.png"),true,false,0);
                    if(timeSinceStart > 3000 && timeSinceStart < 6000) {
                        stopAllSounds();
                        if(timeSinceStart < 3100)sounds.get("blip.ogg").play();
                        renderer.renderText("12:00 AM",(width - renderer.textWidth("12:00 AM", 60)) / 2,(float) ((height-120) / 2), 60,Color.WHITE);
                        renderer.renderText("Night " + nightNumber,(width - renderer.textWidth("Night " + nightNumber, 60)) / 2,(float) ((height-120) / 2) - 60, 60,Color.WHITE);
                    }
                    if(timeSinceStart > 6000) {
                        menu = Menu.OFFICE;
                        nightStart = System.currentTimeMillis();
                        nightRunning = true;
                        sounds.get("fan.oga").play();
                        sounds.get("fan.oga").setRepeating(true);
                        sounds.get("Ambiance1.ogg").play();
                        sounds.get("Ambiance1.ogg").setRepeating(true);
                    }
                }
                case OFFICE -> {
                    Office office = Office.getInstance();

                    String officeTexture = "office.png";

                    if(office.isLeftLight() && Math.random() < 0.95) {
                        if(office.getLeftDoorAnimatronic() == null){
                            officeTexture = "office.light.left.png";
                        } else {
                            officeTexture = "office.light.bonnie.png";
                        }
                    }
                    if(office.isRightLight() && Math.random() < 0.95) {
                        if(office.getRightDoorAnimatronic() == null){
                            officeTexture = "office.light.right.png";
                        } else {
                            officeTexture = "office.light.chica.png";
                        }
                    }

                    renderer.renderTexture(-1.25f + (scroll * 0.25f),-1,2.5f,2,textures.get(officeTexture),true,false,0);

                    if(office.getLeftDoorState() == DoorState.CLOSED) {
                        renderer.renderTexture(-1.15f + (scroll * 0.25f), -1, 0.36f, 2.01f, textures.get("102.png"), true, false, 0);
                    } else if(office.getLeftDoorState() == DoorState.CLOSING || office.getLeftDoorState() == DoorState.OPENING) {
                        renderer.renderTexture(-1.15f + (scroll * 0.25f), -1, 0.36f, 2.01f, textures.get((office.getLeftDoorAnimation() + 88) + ".png"), true, false, 0);
                    }

                    if(office.getRightDoorState() == DoorState.CLOSED) {
                        renderer.renderTexture(0.75f + (scroll * 0.25f), -1, 0.4f, 2.1f, textures.get("118.png"), true, false, 0);
                    } else if(office.getRightDoorState() == DoorState.CLOSING || office.getRightDoorState() == DoorState.OPENING) {
                        renderer.renderTexture(0.75f + (scroll * 0.25f), -1, 0.4f, 2.1f, textures.get((office.getRightDoorAnimation() + 104) + ".png"), true, false, 0);
                    }

                    renderer.renderTexture(-0.5f,-.9f,1,.15f,textures.get("camera.button.png"),true,false,0);
                    float w0 = .17f;
                    float w = w0*1.25f;
                    float h = w0*textures.get("fan.1.png").getAspectRatio();
                    h = w0*3.2f;
                    switch (fan_stage) {
                        case 0: {
                            renderer.renderTexture((scroll * 0.25f) - 0.03f,-0.385f,w,h,textures.get("fan.1.png"),true,false,0);
                            break;
                        }
                        case 1: {
                            renderer.renderTexture((scroll * 0.25f) - 0.03f,-0.385f,w,h,textures.get("fan.2.png"),true,false,0);
                        }
                    }
                    String textureLeft;
                    String textureRight;

                    if(office.isLeftDoor()){
                        if(office.isLeftLight()){
                            textureLeft = "130.png";
                        } else {
                            textureLeft = "124.png";
                        }
                    } else {
                        if(office.isLeftLight()){
                            textureLeft = "125.png";
                        } else {
                            textureLeft = "122.png";
                        }
                    }

                    if(office.isRightDoor()){
                        if(office.isRightLight()){
                            textureRight = "47.png";
                        } else {
                            textureRight = "135.png";
                        }
                    } else {
                        if(office.isRightLight()){
                            textureRight = "131.png";
                        } else {
                            textureRight = "134.png";
                        }
                    }

                    renderer.renderTexture((scroll * 0.25f)+1.1f,-0.385f,w*0.6f,h,textures.get(textureRight),true,false,0);
                    renderer.renderTexture((scroll * 0.25f)-1.25f,-0.385f,w*0.6f,h,textures.get(textureLeft),true,false,0);

                    int ti = timeInSeconds / 60;

                    if(ti == 6) {
                        stopAllSounds();
                        nightRunning = false;
                        menu = Menu.NIGHT_END;
                        sounds.get("chimes 2.ogg").play();
                        sounds.get("cheer.ogg").play();
                        nightEnd = System.currentTimeMillis();
                    }
                    renderer.renderText(ti == 0 ? ("12 PM") : (ti + " AM") ,width - 200,60,80,Color.WHITE);
                }
                case CAMERAS -> {
                    float cameraScroll = (float) (Math.sin(glfwGetTime() / 5));

                    //System.out.println(currentCamera.getImage(cameraRandomness));

                    renderer.renderTexture(-1.25f + (cameraScroll * 0.25f),-1,2.5f,2,textures.get(currentCamera.getImage(cameraRandomness)),true,true,.2f);

                    renderer.renderTexture(0.5f,-1f,0.5f,0.9f,textures.get("layout.png"),true,false,0);
                    renderer.renderTexture(-0.5f,-.9f,1,.15f,textures.get("camera.button.png"),true,false,0);

                    float x = .625f;
                    float y = -.225f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.1a.png"),true,false,0);

                    x = .6f;
                    y = -.35f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.1b.png"),true,false,0);

                    x = .47f;
                    y = -.42f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.5.png"),true,false,0);

                    x = .57f;
                    y = -.525f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.1c.png"),true,false,0);

                    x = .52f;
                    y = -.75f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.3.png"),true,false,0);

                    x = .625f;
                    y = -.82f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.2a.png"),true,false,0);

                    y = -.9f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.2b.png"),true,false,0);

                    x = .775f;
                    y = -.82f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.4a.png"),true,false,0);

                    y = -.9f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.4b.png"),true,false,0);

                    x = .9f;
                    y = -.725f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.6.png"),true,false,0);

                    y = -.42f;

                    renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.7.png"),true,false,0);

                    int ti = timeInSeconds / 60;
                    renderer.renderText(ti == 0 ? ("12 PM") : (ti + " AM") ,width - 200,60,80,Color.WHITE);
                }
                case NIGHT_END -> {
                    renderer.renderText("6 AM" ,(width - renderer.textWidth("6 AM", 80)) / 2, (float) ((height-60) / 2.0),80,Color.WHITE);
                    if(System.currentTimeMillis() - nightEnd > 8000) {
                        stopAllSounds();
                        menu = Menu.MAIN_MENU;
                        sounds.get("Static2.ogg").play();
                        sounds.get("Mainmenu1.ogg").play();
                        sounds.get("Mainmenu1.ogg").setRepeating(true);
                    }
                }
                case CUSTOM_NIGHT -> {}
            }

            if(showDebug){
                renderer.renderText("FPS: " + fps, 10, 20,20, Color.WHITE);
                renderer.renderText("Render Size: " + width + " / " + height, 10, 40,20, Color.WHITE);
                renderer.renderText("Last Frame Time: " + String.format("%.2f ms",lastFrameTime*1000), 10, 60,20, Color.WHITE);
                renderer.renderText("Time: " + String.format("%.4fs",glfwGetTime()), 10, 80,20, Color.WHITE);
                renderer.renderText("Components Drawn: " + componentsDrawn, 10, 100,20, Color.WHITE);
                renderer.renderText("Delta Time: " + String.format("%.2fs",deltaTime), 10, 120,20, Color.WHITE);
                renderer.renderText("Scroll: " + String.format("%.2f",scroll), 10, 140,20, Color.WHITE);
                int y = 160;
                for(Sound s : sounds.values()){
                    if(s.isPlaying()) renderer.renderText("Sound '" + s.getFilepath() + "' " + s.getCurrentTime() + " / " + s.getTotalLength(), 10, y,20, Color.WHITE);
                    if(s.isPlaying()) y+=20;
                }
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

    private void stopAllSounds(){
        for(Sound s : sounds.values()) s.stop();
    }

    public String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
