package com.ben.android.learnopengl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.ben.android.learnopengl.filter.BigEyesFilter;
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
        recordButton.setOnLongClickListener(new RecordButton.OnLongClickListener() {
            @Override
            public void onLongClick() {
                cameraView.startRecord();
            }

            @Override
            public void onNoMinRecord(int currentTime) {

            }

            @Override
            public void onRecordFinishedListener() {
                cameraView.stopRecord();

            }
        });


    }

}
