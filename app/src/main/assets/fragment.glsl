#version 310 es
#ifdef GL_ES
precision highp float;
#endif

uniform uvec2 resolution;
uniform uint MAX_DEPTH;
uniform float time;
layout(binding = 0, std430) buffer SSBO {
    float depths[];
};
layout(binding = 1, std430) buffer SSBO2 {
    uint total;
    uint counts[];
};
layout(binding = 2, std430) buffer SSBO3 {
    float size;
    float colors[];
};
in vec4 color;
out vec4 FragColor;

float map(float value, float min1, float max1, float min2, float max2) {
    return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

void main() {
//    vec3 col1 = vec3(82.0 / 255.0, 72.0/ 255.0, 156.0/ 255.0);
//    vec3 col2 = vec3(64.0/ 255.0, 98.0/ 255.0, 187.0/ 255.0);
//    vec3 col3 = vec3(89.0/ 255.0, 195.0/ 255.0, 195.0/ 255.0);
//    vec3 col4 = vec3(235.0/ 255.0, 235.0/ 255.0, 235.0/ 255.0);
//    vec3 col5 = vec3(244.0/ 255.0, 91.0/ 255.0, 105.0/ 255.0);
//    vec3 col6 = vec3(217.0/ 255.0, 30.0/ 255.0, 54.0/ 255.0);
    uvec2 offsetFragCoord = uvec2(gl_FragCoord.xy - vec2(0.5));
    uint arrpos = offsetFragCoord.x + offsetFragCoord.y * resolution.x;
    vec3 col = vec3(0.0);
    float depth = depths[arrpos];
    float hue = 0.0;
    for (uint i = 0u; i <= uint(depth); i++) {
        hue += float(counts[i]) / float(total);
    }
    hue += fract(depth) * float(counts[uint(depth) + 1u]) / float(total);
    if ( uint(depth) < MAX_DEPTH) {
        //float dep = map(depth, 0.0, float(max_depth), 20.0 / 255.0, 1.0);
        //col = vec3((sin(time / 2.0) + 1.0) / 2.0, (sin(time / 2.0 + 2.0) + 1.0) / 2.0, (sin(time / 2.0 + 4.0) + 1.0) / 2.0) * hue;
//        if(hue < 0.2) {
//            col = mix(col1, col2, map(hue, 0.0, 0.2, 0.0, 1.0));
//        } else if (hue < 0.4) {
//            col = mix(col2, col3, map(hue, 0.2, 0.4, 0.0, 1.0));
//        } else if (hue < 0.6) {
//            col = mix(col3, col4, map(hue, 0.4, 0.6, 0.0, 1.0));
//        } else if (hue < 0.95) {
//            col = mix(col4, col5, map(hue, 0.6, 0.95, 0.0, 1.0));
//        } else {
//            col = mix(col5, col6, map(hue, 0.95, 1.0, 0.0, 1.0));
//        }
        uint lowerIndex = uint(hue * (size - 1.0));
        uint higherIndex = (lowerIndex + 1u) * 3u;
        lowerIndex *= 3u;
        col = mix(vec3(
        float(colors[lowerIndex]),
        float(colors[lowerIndex + 1u]),
        float(colors[lowerIndex + 2u])),
        vec3(
        float(colors[higherIndex]),
        float(colors[higherIndex + 1u]),
        float(colors[higherIndex + 2u])), map(hue, float(lowerIndex / 3u) / float(size - 1.0), float(higherIndex / 3u) / float(size - 1.0), 0.0, 1.0 ));//(hue - floor(hue * float(size - 1u))) * float(size - 1u));
        //col = mix(vec3(0.0,0.0,0.0), vec3(colors[0],colors[1],colors[2]), hue);

    }
    //FragColor = vec4(map(float(arrpos),0.0, float(resolution.x * resolution.y), 0.0, 1.0));
    FragColor = vec4(col, 1.0);
}
