#version 150

uniform float GameTime;

uniform float DissolutionStrength;
uniform float CellDensity;
uniform float Spacing;

uniform vec4 ColorModulate;
uniform float Alpha;

uniform vec4 HDRColor;

uniform float DiscardThreshold;

in vec2 texCoord;

out vec4 fragColor;

// 生成随机2D向量
vec2 random2(vec2 p) {
    float x = dot(p, vec2(6533.4, 244.5));
    float y = dot(p, vec2(3155.6, 326.7));
    vec2 noise = vec2(x, y);
    noise = sin(noise);
    noise = noise * 33758.5453;
    noise = fract(noise);
    return noise;
}

// Voronoi 噪声函数
float voronoi(vec2 uv, float angleOffset, float cellDensity, float spacing) {
    uv *= cellDensity;

    vec2 iuv = floor(uv);
    vec2 fuv = fract(uv);

    float minDist = 1.0;

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 neighbor = vec2(float(x), float(y));
            vec2 point = random2(iuv + neighbor);
            point = 0.5 + 0.5 * sin(angleOffset + 6.2831 * point);
            vec2 diff = neighbor + point - fuv;
            float dist = length(diff) * spacing;
            minDist = min(minDist, dist);
        }
    }

    return minDist;
}

void main() {
    vec2 uv = texCoord.xy;

    // 输入参数 - 使用时间变量使角度偏移随时间变化
    float iTime = GameTime * 1000.;
    float angleOffset = iTime * 3.0; // 随时间变化的角度偏移

    // Voronoi噪声
    float VoronoiNoise = voronoi(uv, angleOffset, CellDensity, Spacing);
    // 溶解后的Voronoi噪声
    float poweredVoronoi = pow(VoronoiNoise, DissolutionStrength);

    // 径向渐变
    vec2 delta = uv - vec2(0.5, 0.5);
    float RadialGradient = length(delta) * 2.0;
    RadialGradient = 1.0 - RadialGradient;
    RadialGradient = clamp(RadialGradient,0.0,1.0);
    RadialGradient = pow(RadialGradient , 2.85);

    float color = poweredVoronoi * RadialGradient;
    float alpha = ColorModulate.a * color * Alpha;

    vec4 compoundColor = vec4(vec3(color), clamp(alpha,0.0,1.0)) * ColorModulate;

    if (compoundColor.a < DiscardThreshold) discard;

    compoundColor.rgb *= HDRColor.a * HDRColor.rgb;

    fragColor = compoundColor;
}