precision mediump float;
uniform sampler2D uTexture;
varying vec2 vTexCoord;

varying float vDiffuse;    // Diffuse lighting intensity from vertex shader
uniform vec2 uTexSize;     // The screen size

const float lodFactor = 0.7;

uniform vec4 fogColor; // Fog color
uniform float fogStart;  // Start distance for fog effect
uniform float uFar;   // End distance for fog effect
uniform float uNear;   // End distance for fog effect

void main() {
    float depth = gl_FragCoord.z;
    float lod = float(pow(depth, 5.5) * lodFactor);

    // Calculate the fog factor based on depth
    float fogFactor = ((uFar - uNear) * depth - fogStart) / (uFar - fogStart);
    fogFactor = fogFactor < 0.0 ? 0.0 : fogFactor;

    // Blend the final color with the fog color
    vec4 color = texture2D(uTexture, vTexCoord, lod);
    vec4 pureFogColor = color * (1.0 - fogFactor) + fogColor * (fogFactor);
    // Modulate base color by diffuse lighting
    gl_FragColor = vec4(pureFogColor.rgb * vDiffuse, 1);
}