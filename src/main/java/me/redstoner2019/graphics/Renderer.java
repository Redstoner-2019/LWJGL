package me.redstoner2019.graphics;

import me.redstoner2019.Frame;
import me.redstoner2019.chatgptbs.TrueTypeFontExample;
import me.redstoner2019.graphics.font.Font;
import me.redstoner2019.graphics.font.Glyph;
import me.redstoner2019.graphics.general.Texture;
import org.joml.Vector2f;
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

import static me.redstoner2019.Frame.aspectRatio;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;

public class Renderer {
    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;

    private static Map<Float, Renderer.FontData> fontDataMap = new HashMap<>();
    private static String fontPath = "C:\\Users\\Redstoner_2019\\Projects\\LWJGL\\src\\main\\resources\\fonts\\TNR.ttf";

    public static void init(){

    }

    public static void renderTexture(float x, float y, float w, float h,float sectionX, float sectionY, float sectionW, float sectionH, Texture texture, boolean overrideAspectRatio, Color color){


        GL20.glUseProgram(Frame.textureShader.id);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
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

        GL30.glBindVertexArray(Frame.vao);
        System.out.println(Frame.vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

        /*GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(tv1.x, tv1.y); GL11.glVertex2f(v1.x* aspectRatio, v1.y);
        GL11.glTexCoord2f(tv2.x, tv2.y); GL11.glVertex2f(v2.x* aspectRatio, v2.y);
        GL11.glTexCoord2f(tv3.x, tv3.y); GL11.glVertex2f(v3.x* aspectRatio, v3.y);
        GL11.glTexCoord2f(tv4.x, tv4.y); GL11.glVertex2f(v4.x* aspectRatio, v4.y);
        GL11.glEnd();*/

        GL20.glUseProgram(0);
    }
    public static void renderTexture(float x, float y, float w, float h, Texture texture, boolean overrideAspectRatio){
        if(!overrideAspectRatio && w / h != texture.getAspectRatio()){
            h = h * (texture.getAspectRatio());
        }
        GL11.glColor3f(1,1,1);
        renderTexture(x,y,w,h,0,0,1,1, texture,true, Color.WHITE);
    }

    /*public static void renderTextureSection(float x, float y, float w, float h,float sectionX, float sectionY, float sectionW, float sectionH, Texture texture, boolean overrideAspectRatio){
        if(!overrideAspectRatio && w / h != texture.getAspectRatio()){
            h = h * (texture.getAspectRatio());
        }
        renderTexture(new Vector2f(-w + x,-h + y),
                new Vector2f(w + x,-h + y),
                new Vector2f(w + x,h + y),
                new Vector2f(-w + x,h + y),
                new Vector2f(sectionX,sectionY + sectionH),
                new Vector2f(sectionX + sectionW,sectionY + sectionH),
                new Vector2f(sectionX + sectionW,sectionY),
                new Vector2f(sectionX,sectionY),
                texture);
    }*/

    public static void renderTextureSectionScreen(float x, float y, float w, float h,float sectionX, float sectionY, float sectionW, float sectionH, Texture texture, boolean overrideAspectRatio){
        float f = 1;
        renderTexture(x / Frame.width * f,y / Frame.height *f,w / Frame.width *f,h / Frame.height*f,sectionX / texture.getWidth(),sectionY / texture.getHeight(),sectionW / texture.getWidth(),sectionH / texture.getHeight(),texture,overrideAspectRatio, Color.WHITE);
    }
    public static void drawText(CharSequence text, float x, float y, Font font) {
        drawText(text, x, y, Color.WHITE,font);
    }
    public static void drawText(CharSequence text, float x, float y, Color c, Font font) {
        drawText(text,x,y,c,15,font);
    }
    public static void drawText(CharSequence text, float x, float y, Color c, float fontSize, Font font) {
        float drawX = x;
        float drawY = y;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                drawY -= font.fontHeight;
                drawX = x;
                continue;
            }
            if (ch == '\r') {
                continue;
            }
            Glyph g = font.glyphs.get(ch);
            GL11.glColor3f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f);
            renderTexture(drawX, drawY,g.width*fontSize/4,g.height*fontSize*-1, g.x, 0, g.width, 1,font.texture,true, c);
            drawX+=g.width*fontSize/4;
        }
    }

    public static void renderText(String text, float x, float y, float fontSize, Color c) {
        renderText(text,x,y,fontSize,c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f);
    }

    public static void renderText(String text, float x, float y, float fontSize, float r, float g, float b) {
        FontData fontData = fontDataMap.get(fontSize);

        if (fontData == null) {
            // Load the font texture for the given font size if it doesn't exist
            loadFontTexture(fontPath, fontSize);
            fontData = fontDataMap.get(fontSize);
        }

        glColor3f(r, g, b); // Set the color for the text

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

    public static ByteBuffer loadFont(String path) throws IOException {
        Path fontPath = Paths.get(path);
        ByteBuffer buffer;
        try (FileChannel fc = (FileChannel) Files.newByteChannel(fontPath, StandardOpenOption.READ)) {
            buffer = ByteBuffer.allocateDirect((int) fc.size());
            while (fc.read(buffer) > 0);
            buffer.flip();
        }
        return buffer;
    }
    public static void loadFontTexture(String fontPath, float fontSize) {
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
