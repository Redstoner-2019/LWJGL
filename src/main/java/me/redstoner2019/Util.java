package me.redstoner2019;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Util {
    public static String readFile(String path) throws IOException {
        return new String(Util.class.getClassLoader().getResourceAsStream(path).readAllBytes());
    }

    public static int loadShader(String source, int type) {
        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, source);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            System.err.println("Shader compilation failed: " + GL20.glGetShaderInfoLog(shaderID));
            return -1;
        }

        return shaderID;
    }
    public static int loadTexture(String filePath) {
        int width, height;
        ByteBuffer buffer;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // Load image
            buffer = STBImage.stbi_load(filePath, w, h, comp, 4);
            if (buffer == null) {
                throw new RuntimeException("Failed to load texture file!" + System.lineSeparator() + STBImage.stbi_failure_reason());
            }

            // Get width and height of image
            width = w.get();
            height = h.get();
        }

        // Create a new OpenGL texture
        int textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Generate Mip Map
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        // Free the memory
        STBImage.stbi_image_free(buffer);

        return textureID;
    }
}
