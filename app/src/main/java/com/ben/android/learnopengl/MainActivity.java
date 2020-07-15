package com.ben.android.learnopengl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.ben.android.learnopengl.util.AndroidUtilities;
import com.ben.android.learnopengl.widgets.CameraView;
import com.ben.android.learnopengl.widgets.RecordButton;

public class MainActivity extends AppCompatActivity {
    private RecordButton recordButton;
    private CameraView cameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.cameraView);
        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnRecordStateChangedListener(new RecordButton.OnRecordStateChangedListener() {
            @Override
            public void onRecordStart() {

            }

            @Override
            public void onLongPressRecordStart() {
                cameraView.startRecord();
            }

            @Override
            public void onRecordStop() {
                cameraView.stopRecord();
            }

            @Override
            public void onZoom(float percentage) {

            }
        });
    }

}
