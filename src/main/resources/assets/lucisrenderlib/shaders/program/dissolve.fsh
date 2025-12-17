#version 150

uniform sampler2D DiffuseSampler;

uniform vec4 ColorModulate;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 color = texture(DiffuseSampler, texCoord) * ColorModulate;
    vec4 HDRColor = vec4(0.298,0.529,1.0,10.0);
    color.rgb *= HDRColor.a * HDRColor.rgb;

    fragColor = color;
}
