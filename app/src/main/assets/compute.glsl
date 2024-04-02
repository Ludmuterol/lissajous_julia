#version 310 es
#ifdef GL_ES
precision highp float;
#endif
layout(local_size_x = 1, local_size_y = 1) in;
uniform uvec2 resolution;
uniform uint MAX_DEPTH;
uniform float xMin;
uniform float xMax;
uniform float yMin;
uniform float yMax;
uniform float x;
uniform float y;
layout(binding = 0, std430) buffer SSBO {
    uint depths[];
};
layout(binding = 1, std430) buffer SSBO2 {
    uint total;
    uint counts[];
};
float map(float value, float min1, float max1, float min2, float max2) {
    return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

uint julia(float yt, float xt) {
    float x2 = xt * xt;
    float y2 = yt * yt;
    uint depth = 0u;
    while (x2 + y2 <= 4.0 && depth < MAX_DEPTH){
        yt = (xt + xt) * yt + y;
        xt = x2 - y2 + x;
        x2 = xt * xt;
        y2 = yt * yt;
        depth++;
    }
    return depth;
}

void main() {

    uvec2 storePos = uvec2(gl_GlobalInvocationID.xy);
    // Calculate the global number of threads (size) for this
    uint gWidth = gl_WorkGroupSize.x * gl_NumWorkGroups.x;
    uint gHeight = gl_WorkGroupSize.y * gl_NumWorkGroups.y;
    uint gSize = gWidth * gHeight;
    // Since we have 1D array we need to calculate offset.
    uint offset = storePos.y * gWidth + storePos.x;

    vec2 position = vec2(float(storePos.x) / float(gWidth), float(storePos.y) / float(gHeight));
    uint depth = julia(map(position.x, 0.0, 1.0, xMin, xMax), map(position.y, 0.0, 1.0, yMin, yMax));

    if ( depth < MAX_DEPTH) {
        atomicAdd(counts[depth], 1u);
        atomicAdd(total, 1u);
    }
    depths[offset] = depth;
    //depths[offset] = uint(float(MAX_DEPTH - 1u) * position.x  * (1.0 - position.y) );//+ (sin(time / 2.0) + 1.0) / 2.0 * 100.0);
}
