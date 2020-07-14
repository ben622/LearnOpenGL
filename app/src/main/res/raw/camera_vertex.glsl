attribute vec4 vPosition;
//纹理坐标
attribute vec4 vCoord;
//surfaceTexture采样坐标
uniform mat4 vMatrix;
//输出片元着色器坐标
varying vec2 fCoord;

void main(){
    gl_Position = vPosition;
    fCoord = (vMatrix * vCoord).xy;
}