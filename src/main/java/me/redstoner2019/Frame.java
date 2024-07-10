package me.redstoner2019;

import me.redstoner2019.graphics.Renderer;
import me.redstoner2019.graphics.general.Shader;
import me.redstoner2019.graphics.general.ShaderProgram;
import me.redstoner2019.graphics.general.Texture;
import me.redstoner2019.graphics.font.Font;
import org.lwjgl.glfw.*;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

import static java.sql.Types.NULL;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL2.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Frame {

    // The window handle
    private long window;
    private Texture texture;
    public static int width;
    public static int height;
    public static float aspectRatio;
    public static ShaderProgram textureShader;
    public static int vao;
    private Font font;
    private boolean showDebug = true;

    public void run() throws IOException {
        init();
        loop();
    }

    private void init() throws IOException {
        System.out.println("debug");

        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        String[] titles = Util.readFile("misc/titles.txt").split("\n");

        String title = titles[new Random().nextInt(titles.length)];

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        width = 800;
        height = 600;
        window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(0);

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
            public void invoke(long window, int width, int height) {
                Frame.width = width;
                Frame.height = height;
                GL11.glViewport(0, 0, width, height);
                updateProjectionMatrix();
            }
        });

        GLFW.glfwShowWindow(window);
        GL.createCapabilities();

        vao = createVertexArray();

        GL11.glEnable(GL13.GL_MULTISAMPLE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        updateProjectionMatrix();

        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER,"shader/vertex.vert");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER,"shader/fragment.frag");

        textureShader = new ShaderProgram();
        textureShader.attachShader(vertexShader);
        textureShader.attachShader(fragmentShader);
        textureShader.link();

        texture = Texture.loadTexture("C:\\Users\\Redstoner_2019\\Projects\\LWJGL\\src\\main\\resources\\textures\\optadata.jpg");
        font = new Font();
    }

    private void loop() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        long lastUpdate = System.currentTimeMillis();
        int frames = 0;
        int fps = 0;
        double lastFrameTime = 0;
        int componentsDrawn = 0;

        while (!GLFW.glfwWindowShouldClose(window)) {
            double start = glfwGetTime();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if(showDebug){
                Renderer.renderText("Dies ist ein Test!", 20, 0, 20,Color.GREEN);

                Renderer.renderText("FPS: " + fps, 20, 20,20, new Color(1f, 0f, 0f));
                Renderer.renderText("Last Frame Time: " + String.format("%.2f ms",lastFrameTime*1000), 20, 40,20, new Color(1f, 0, 0));
                Renderer.renderText("Time: " + String.format("%.4fs",glfwGetTime()), 20, 60,20, new Color(1f, 1f, 1f));
                Renderer.renderText("Components Drawn: " + componentsDrawn, 20, 80,20, new Color(1f, 1f, 1f));
            }

            Renderer.renderTexture(0,0,1,1,texture,true);

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            frames++;
            if(System.currentTimeMillis() - lastUpdate >= 1000){
                fps = frames;
                frames = 0;
                lastUpdate = System.currentTimeMillis();
            }
            lastFrameTime = glfwGetTime() - start;
        }
    }

    public static void main(String[] args) throws IOException {
        new Frame().run();
    }
    private void updateProjectionMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        aspectRatio = (float) width / height;
        GL11.glOrtho(-aspectRatio, aspectRatio, -1f, 1.0f, -1f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    public int createVertexArray() {
        System.out.println("Creating Vertex Array");
        float[] vertices = {
                // Positions      // Texture Coords
                -0.5f,  0.5f, 0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, 0.0f,  0.0f, 0.0f,
                0.5f, -0.5f, 0.0f,  1.0f, 0.0f,
                0.5f,  0.5f, 0.0f,  1.0f, 1.0f
        };

        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        System.out.println(vao);

        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        int ebo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);

        return vao;
    }
}
