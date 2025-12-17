#version 150

uniform sampler2D SourceSampler;
uniform sampler2D StarColor;

uniform vec4 ColorModulate;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 color = texture(SourceSampler, texCoord);
    vec4 starColor = texture(StarColor, texCoord);

    if(color.a < 0.001){
        discard;
    }

    color = starColor * ColorModulate;

    // 计算亮度
    float luminance = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));

    // 只对亮的部分应用HDR (亮度阈值可以调整)
    if(luminance > 0.5) {
        vec4 HDRColor = vec4(1.0, 1.0, 1.0, 10.0);
        color.rgb *= HDRColor.a * HDRColor.rgb;
    }

    fragColor = color;
}
