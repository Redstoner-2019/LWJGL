#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

uniform vec2 texOffset;
uniform vec2 texScale;
uniform vec3 color = vec3(1,1,1);
uniform float noiseLevel = .5;
uniform float seed = 0;



float random(vec2 st)
{
    return ((fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123)));
}

vec4 lerp(vec4 a, vec4 b, float t) {
    return a + (b - a) * t;
}


void main() {
    vec2 textureCoord = (fragTexCoord * texScale) + texOffset;

    vec2 txc = textureCoord;

    float distance = texture(textureSampler, textureCoord).r;
    float alpha = smoothstep(0.5 - 0.5 / 16.0, 0.5 + 0.5 / 16.0, distance);

    vec4 color = texture(textureSampler, textureCoord) * vec4(color,1);
    vec4 randomColor = vec4(random(vec2(seed,seed+1) + txc),random(vec2(seed,seed+2) + txc),random(vec2(seed,seed+3) + txc),random(vec2(seed,seed+4) + txc));

    //color = vec4(color.r + (color.r * random(vec2(seed,seed+1) + txc,noiseLevel)),color.g + (color.g * random(vec2(seed,seed+2) + txc,noiseLevel)),color.b + (color.b * random(vec2(seed,seed+3) + txc,noiseLevel)),color.a + (color.a * random(vec2(seed,seed+4) + txc,noiseLevel)));

    fragColor = lerp(color,randomColor,noiseLevel);

    //fragColor = vec4(textureCoord,0,0);
}
