attribute vec3 aPosition;   // Vertex position
attribute vec3 aNormal;     // Vertex normal (normalized)

attribute vec2 aTexCoord;  // Vertex UV-Map coords

uniform mat4 uModelMatrix;    // Model-View-Projection matrix
uniform mat4 uRotMatrix;    // Rotation matrix to rotate the normals
uniform mat4 uCamMatrix;    // The projection matrix * The RT (S) matrix of the camera
uniform vec3 uLightDir;     // Light direction in world space (normalized)

varying float vDiffuse;     // Diffuse intensity for fragment shader
varying vec2 vTexCoord;    // Passed to fragment shader

const float vDiffuseMin = 0.5;
void main() {
  gl_Position = uCamMatrix * uModelMatrix * vec4(aPosition.xyz, 1);

  // Calculate diffuse lighting (Lambert's cosine law)
  // Note: normals and lightDir should be normalized
  vec4 tempNorm = uRotMatrix * vec4(aNormal.xyz, 1);
  vDiffuse = max(dot(normalize(vec3(tempNorm.rgb)), normalize(uLightDir)), vDiffuseMin);
  vTexCoord = aTexCoord;
}