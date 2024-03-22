#ifdef GL_ES
precision highp float;
#endif

//uniform float time;
uniform vec2 resolution;
uniform int MAX_DEPTH;
uniform float xMin;
uniform float xMax;
uniform float yMin;
uniform float yMax;
uniform float x;
uniform float y;
uniform float time;

float map(float value, float min1, float max1, float min2, float max2) {
  return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

float julia(float yt, float xt) {
  float x2 = xt * xt;
  float y2 = yt * yt;
  int depth = 0;
  while (x2 + y2 <= 4.0 && depth < MAX_DEPTH){
    yt = (xt + xt) * yt + y;
    xt = x2 - y2 + x;
    x2 = xt * xt;
    y2 = yt * yt;
    depth++;
  }
  return float(depth);
}

void main( void ) {

	vec2 position = gl_FragCoord.xy / resolution.xy;
  float depth = julia(map(position.x, 0.0, 1.0, xMin, xMax), map(position.y, 0.0, 1.0, yMin, yMax));
  vec3 col = vec3(20.0/255.0);
  if (depth < float(MAX_DEPTH)) {
    float dep = map(depth, 0.0, float(MAX_DEPTH - 1), 20.0 / 255.0, 1.0);
    col = vec3((sin(time / 2.0) + 1.0) / 2.0, (sin(time / 2.0 + 2.0) + 1.0) / 2.0, (sin(time / 2.0 + 4.0) + 1.0) / 2.0) * dep;
  }
	gl_FragColor = vec4(col, 1.0 );

}
