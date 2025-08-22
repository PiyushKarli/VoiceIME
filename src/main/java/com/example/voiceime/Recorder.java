package com.example.voiceime;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Recorder {
    private MediaRecorder recorder;
    private File outputFile;

    public File start(File cacheDir) throws IOException{
        stop();

        outputFile = new File(cacheDir,"rec_" + System.currentTimeMillis() + ".m4a");

        recorder =  new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(64000);
        recorder.setAudioSamplingRate(16000);
        recorder.setAudioChannels(1);
        recorder.setOutputFile(outputFile.getAbsoluteFile());

        recorder.prepare();
        recorder.start();
        return outputFile;
    }


    public File stop(){
        try{
            if (recorder != null){
                recorder.stop();
                recorder.release();
            }
        } catch (Exception e) {
            Log.e("Recorder", "stop() error", e);
        }finally {
            recorder = null;
        }
        return outputFile;
    }
}
