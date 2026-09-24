#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;

// How aggressively bright pixels bloom. Tune from Kotlin via
// ShaderProgram.setUniformf("u_intensity", value).
uniform float u_intensity;

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);

    // luminance-based bright-pass: only strongly-lit pixels (our glow
    // cores and lasers) contribute to the bloom, so dark backgrounds
    // stay clean instead of washing out.
    float luminance = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    float threshold = 0.55;
    float brightAmount = smoothstep(threshold, 1.0, luminance);

    vec3 bloom = color.rgb * brightAmount * u_intensity;
    gl_FragColor = vec4(color.rgb + bloom, color.a);
}
