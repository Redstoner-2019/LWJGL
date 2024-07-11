package me.redstoner2019.graphics;

import me.redstoner2019.Frame;
import me.redstoner2019.graphics.font.Font;
import me.redstoner2019.graphics.font.Glyph;
import me.redstoner2019.graphics.general.Texture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;

public class Renderer {
    private final int BITMAP_W = 512;
    private final int BITMAP_H = 512;

    private Map<Float, Renderer.FontData> fontDataMap = new HashMap<>();
    private String fontPath = "C:\\Users\\l.paepke\\Projects\\LWJGL\\src\\main\\resources\\fonts\\TNR.ttf";

    public void init(){

    }

    public void renderTexture(float x, float y, float w, float h,float sectionX, float sectionY, float sectionW, float sectionH, Texture texture, boolean overrideAspectRatio, Color color, boolean hasNoise, float noiseStrength){
        renderTexture(x,y,w,h,sectionX,sectionY,sectionW,sectionH,texture.getId(),overrideAspectRatio,color,hasNoise,noiseStrength);
    }

    public void renderTexture(float x, float y, float w, float h,float sectionX, float sectionY, float sectionW, float sectionH, int texture, boolean overrideAspectRatio, Color color, boolean hasNoise, float noiseStrength){
        GL20.glUseProgram(Frame.textureShader.id);
        // Enable blending
        glEnable(GL_BLEND);

        // Set the blending function
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Random random = new Random();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        int textureUniformLocation = GL20.glGetUniformLocation(Frame.textureShader.id, "textureSampler");
        GL20.glUniform1i(textureUniformLocation, 0);

        int texOffsetLocation = GL20.glGetUniformLocation(Frame.textureShader.id, "texOffset");
        GL20.glUniform2f(texOffsetLocation, sectionX, sectionY);

        int texScaleLocation = GL20.glGetUniformLocation(Frame.textureShader.id, "texScale");
        GL20.glUniform2f(texScaleLocation, sectionW, sectionH);

        int offsetLocation = GL20.glGetUniformLocation(Frame.textureShader.id, "offset");
        GL20.glUniform2f(offsetLocation, x, y);

        int offsetScaleLocation = GL20.glGetUniformLocation(Frame.textureShader.id, "offsetScale");
        GL20.glUniform2f(offsetScaleLocation, w, h);

        int colorLocation = GL20.glGetUniformLocation(Frame.textureShader.id, "color");
        GL20.glUniform3f(colorLocation, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);

        int seedLocation = GL20.glGetUniformLocation(Frame.textureShader.id, "seed");
        GL20.glUniform1f(seedLocation, random.nextFloat());

        float f;
        if(!hasNoise) f = 0;
        else f = noiseStrength;
        int noiseStrengthLocation = GL20.glGetUniformLocation(Frame.textureShader.id, "noiseLevel");
        GL20.glUniform1f(noiseStrengthLocation, f);

        GL30.glBindVertexArray(Frame.vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

        GL20.glUseProgram(0);
    }
    public void renderTexture(float x, float y, float w, float h, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTexture(x,y,w,h,0,0,1,1, texture,overrideAspectRatio, Color.WHITE,hasNoise,noiseStrength);
    }
    public void renderTexture(float x, float y, float w, float h, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        if(!overrideAspectRatio && w / h != texture.getAspectRatio()){
            h = h * (texture.getAspectRatio());
        }
        renderTexture(x,y,w,h,0,0,1,1, texture,true, Color.WHITE, hasNoise,noiseStrength);
    }

    public void renderTextureSectionScreen(float x, float y, float w, float h,float sectionX, float sectionY, float sectionW, float sectionH, Texture texture, boolean overrideAspectRatio){
        float f = 1;
        renderTexture(x / Frame.width * f,y / Frame.height *f,w / Frame.width *f,h / Frame.height*f,sectionX / texture.getWidth(),sectionY / texture.getHeight(),sectionW / texture.getWidth(),sectionH / texture.getHeight(),texture,overrideAspectRatio, Color.WHITE, false,0);
    }

    public void renderText(String text, float x, float y, float fontSize, Color c) {
        renderText(text,x,y,fontSize,c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f);
    }

    public void renderText(String text, float x, float y, float fontSize, float r, float g, float b) {
        FontData fontData = fontDataMap.get(fontSize);

        if (fontData == null) {
            loadFontTexture(fontPath, fontSize);
            fontData = fontDataMap.get(fontSize);
        }

        glColor3f(r, g, b);

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, fontData.textureID);
        glBegin(GL_QUADS);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            float[] xPos = {x};
            float[] yPos = {y};

            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
                stbtt_GetBakedQuad(fontData.charData, BITMAP_W, BITMAP_H, c - 32, xPos, yPos, quad, true);

                //System.out.println(quad.x0() + " " +  quad.y0() + " " + quad.x1() + " " +  quad.y1());

                float w = 1;
                float h = 1;

                /*float x0 = quad.x0() / w;
                float y0 = quad.y0() / h;
                float x1 = quad.x1() / w;
                float y1 = quad.y1() / h;

                fontSize = 1;*/

                //renderTexture(x0,y0,x1-x0,y1-y0,quad.s0(), quad.t0(),quad.s1()-quad.s0(),quad.t1()-quad.t0(),fontData.textureID,false,new Color(r,g,b));
                //renderTexture(x0*fontSize,y0*fontSize,(x1-x0) * fontSize,(y1-y0) * fontSize,1,false);

                //renderTexture(0,0,200,200,4,true);

                glTexCoord2f(quad.s0(), quad.t0());
                glVertex2f(quad.x0(), quad.y0());

                glTexCoord2f(quad.s1(), quad.t0());
                glVertex2f(quad.x1(), quad.y0());

                glTexCoord2f(quad.s1(), quad.t1());
                glVertex2f(quad.x1(), quad.y1());

                glTexCoord2f(quad.s0(), quad.t1());
                glVertex2f(quad.x0(), quad.y1());
            }
        }
        glEnd();
    }

    public ByteBuffer loadFont(String path) throws IOException {
        Path fontPath = Paths.get(path);
        ByteBuffer buffer;
        try (FileChannel fc = (FileChannel) Files.newByteChannel(fontPath, StandardOpenOption.READ)) {
            buffer = ByteBuffer.allocateDirect((int) fc.size());
            while (fc.read(buffer) > 0);
            buffer.flip();
        }
        return buffer;
    }
    public void loadFontTexture(String fontPath, float fontSize) {
        ByteBuffer ttfBuffer = null;
        try {
            ttfBuffer = loadFont(fontPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Create a bitmap for the font texture
        ByteBuffer bitmap = MemoryUtil.memAlloc(BITMAP_W * BITMAP_H);

        STBTTBakedChar.Buffer charData = STBTTBakedChar.malloc(96);
        stbtt_BakeFontBitmap(ttfBuffer, fontSize, bitmap, BITMAP_W, BITMAP_H, 32, charData);

        // Create OpenGL texture
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        MemoryUtil.memFree(bitmap);

        // Store the font data in the map
        fontDataMap.put(fontSize, new Renderer.FontData(textureID, charData));
    }

    private static class FontData {
        int textureID;
        STBTTBakedChar.Buffer charData;

        FontData(int textureID, STBTTBakedChar.Buffer charData) {
            this.textureID = textureID;
            this.charData = charData;
        }
    }
}
