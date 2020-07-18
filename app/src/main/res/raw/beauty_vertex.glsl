attribute vec4 vPosition;
//纹理坐标
attribute vec2 vCoord;
//输出片元着色器坐标
varying vec2 fCoord;

void main(){
    gl_Position = vPosition;
    fCoord = vCoord;
}