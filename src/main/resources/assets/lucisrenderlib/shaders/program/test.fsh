#version 150

uniform sampler2D SourceSampler;
uniform sampler2D StarColor;

uniform vec4 ColorModulate;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = texCoord - center;

    // Calculate distance from center for radial-based aberration
    float distanceFromCenter = length(offset);

    // 计算到屏幕边缘的最近距离（考虑所有四个边）
    vec2 edgeDistance = min(texCoord, 1.0 - texCoord);
    float nearestEdge = min(edgeDistance.x, edgeDistance.y);

    // 在靠近任何边缘时平滑衰减色差效果
    float edgeFalloff = smoothstep(0.0, 0.15, nearestEdge);

    // Apply quadratic falloff for more natural chromatic aberration with edge control
    float aberrationStrength = 0.02 * distanceFromCenter * edgeFalloff;

    // Normalize offset direction
    vec2 direction = distanceFromCenter > 0.0 ? offset / distanceFromCenter : vec2(0.0);

    // Sample with different offsets for each channel
    // Red shifts outward more, blue shifts inward more (simulating real lens dispersion)
    vec4 rSample = texture(SourceSampler, texCoord + direction * aberrationStrength * 1.2);
    vec4 gSample = texture(SourceSampler, texCoord + direction * aberrationStrength * 0.6);
    vec4 bSample = texture(SourceSampler, texCoord);

    // Weighted alpha blend for smoother edges
    float alpha = (rSample.a + gSample.a * 1.5 + bSample.a) / 3.5;

    if (alpha < 0.001) {
         discard;
    }

    vec4 color = vec4(rSample.r, gSample.g, bSample.b, alpha) * ColorModulate;

    fragColor = color;
}