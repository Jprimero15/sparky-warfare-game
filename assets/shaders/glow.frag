#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_intensity;
uniform vec2 u_texelSize;

void main() {
    vec4 center = texture2D(u_texture, v_texCoords);
    float luminance = dot(center.rgb, vec3(0.2126, 0.7152, 0.0722));
    float threshold = smoothstep(0.45, 0.95, luminance);
    vec3 sum = vec3(0.0);
    sum += texture2D(u_texture, v_texCoords + vec2(-2.0, 0.0) * u_texelSize).rgb;
    sum += texture2D(u_texture, v_texCoords + vec2( 2.0, 0.0) * u_texelSize).rgb;
    sum += texture2D(u_texture, v_texCoords + vec2(0.0, -2.0) * u_texelSize).rgb;
    sum += texture2D(u_texture, v_texCoords + vec2(0.0,  2.0) * u_texelSize).rgb;
    sum += texture2D(u_texture, v_texCoords + vec2(-1.4, -1.4) * u_texelSize).rgb;
    sum += texture2D(u_texture, v_texCoords + vec2( 1.4, -1.4) * u_texelSize).rgb;
    sum += texture2D(u_texture, v_texCoords + vec2(-1.4,  1.4) * u_texelSize).rgb;
    sum += texture2D(u_texture, v_texCoords + vec2( 1.4,  1.4) * u_texelSize).rgb;
    sum *= 0.125;
    vec3 bloom = (sum + center.rgb * 0.35) * threshold * u_intensity;
    gl_FragColor = vec4(bloom, max(center.a, threshold * 0.45)) * v_color;
}
