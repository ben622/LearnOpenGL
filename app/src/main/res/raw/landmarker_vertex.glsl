attribute vec2 mPosition;
void main() {
    gl_Position = vec4(mPosition,0.0,1.0);
    gl_PointSize = 30.0;
}
