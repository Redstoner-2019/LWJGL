#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

uniform vec2 texOffset;
uniform vec2 texScale;
uniform vec3 color = vec3(1,1,1);

void main() {
    vec2 textureCoord = (fragTexCoord * texScale) + texOffset;

    float distance = texture(textureSampler, textureCoord).r;
    float alpha = smoothstep(0.5 - 0.5 / 16.0, 0.5 + 0.5 / 16.0, distance);
    fragColor = texture(textureSampler, textureCoord) * vec4(color,alpha);

    //fragColor = vec4(textureCoord,0,0);
}
