#version 310 es
#ifdef GL_ES
precision highp float;
#endif

uniform uvec2 resolution;
uniform uint MAX_DEPTH;
uniform float time;
layout(binding = 0, std430) buffer SSBO {
    uint max_depth;
    uint depths[];
};

in vec4 color;
out vec4 FragColor;

float map(float value, float min1, float max1, float min2, float max2) {
    return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

void main() {
    uvec2 offsetFragCoord = uvec2(gl_FragCoord.xy - vec2(0.5));
    uint arrpos = offsetFragCoord.x + offsetFragCoord.y * resolution.x;
    vec3 col = vec3(20.0/255.0);
    float depth = float(depths[arrpos]);
    if ( depth < float(MAX_DEPTH)) {
        float dep = map(depth, 0.0, float(max_depth), 20.0 / 255.0, 1.0);
        col = vec3((sin(time / 2.0) + 1.0) / 2.0, (sin(time / 2.0 + 2.0) + 1.0) / 2.0, (sin(time / 2.0 + 4.0) + 1.0) / 2.0) * dep;
    }
    //FragColor = vec4(map(float(arrpos),0.0, float(resolution.x * resolution.y), 0.0, 1.0));
    FragColor = vec4(col, 1.0);
}
