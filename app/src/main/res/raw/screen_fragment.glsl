//SurfaceTexture比较特殊
//float数据是什么精度的
precision mediump float;

//采样点的坐标
varying vec2 fCoord;

//采样器
uniform sampler2D vTexture;

void main(){
    gl_FragColor = texture2D(vTexture,fCoord);
}