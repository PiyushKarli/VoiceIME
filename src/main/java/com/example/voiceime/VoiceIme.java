package com.example.voiceime;


import android.inputmethodservice.InputMethodService;
import android.os.Looper;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class VoiceIme extends InputMethodService {

    private Button record;
    private TextView status;
    private Recorder recorder;
    private File lastAudio;
    private Handler handler =  new Handler(Looper.getMainLooper());

    private final java.util.concurrent.ExecutorService networkExecuter = java.util.concurrent.Executors.newSingleThreadExecutor();


    private enum UIState {
        IDLE,
        RECORDING,
        PROCESSING,
        DONE,
        ERROR
    }

    private void setUI(UIState state){
        switch (state){
            case IDLE:
                record.setEnabled(true);
                record.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2A9D8F));
                record.setText("Hold To Speak");
                status.setText("Idle");
                break;

            case RECORDING:
                record.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE63946));
                record.setText("Recording...Relese to Stop");
                status.setText("Recording");
                break;

            case PROCESSING:
                record.setEnabled(false);
                record.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF4A261));
                record.setText("Processing");
                status.setText("Processing");
                break;

            case DONE:
                record.setEnabled(true);
                record.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2A9D8F));
                record.setText("Hold To Speak");
                status.setText("Inserted!");
                break;

            case ERROR:
                record.setEnabled(true);
                record.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF9E2A2A));
                record.setText("Error! Try Again");
                status.setText("Error");
                break;
        }
    }
    @Override
    public View onCreateInputView() {
        View v = getLayoutInflater().inflate(R.layout.keyboard,null);
        record = v.findViewById(R.id.record);
        status = v.findViewById(R.id.status);
        recorder =  new Recorder();

        record.setTypeface(ResourcesCompat.getFont(this, R.font.alice));
        status.setTypeface(ResourcesCompat.getFont(this, R.font.alice));



        record.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    return true;

                case MotionEvent.ACTION_UP:
                    stopRecording();
                    return true;
            }
            return false;
        });
        return v;
    }

    private void startRecording(){
        try {
            lastAudio = recorder.start(getCacheDir());
            setUI(UIState.RECORDING);
            Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            setUI(UIState.ERROR);
            e.printStackTrace();
        }
    }

    private void stopRecording(){
        recorder.stop();
        setUI(UIState.PROCESSING);
        Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show();

        if (lastAudio != null && lastAudio.exists()){
            networkExecuter.execute(() -> {
                try {
                    String result = sendToGroq(lastAudio);
                    handler.post(() -> {
                       if (result != null && !result.isEmpty()){
                           InputConnection ic = getCurrentInputConnection();
                           if (ic != null){
                               ic.commitText(result,1);
                               handler.postDelayed(() -> setUI(UIState.DONE),2000);
                           }else {
                               status.setText("No Input Field");
                           }
                       }else{
                           status.setText("Empty Result");
                       }
                    });
                }catch (Exception e){
                    handler.post(() -> setUI(UIState.ERROR));
                }
            });
        }else{
            status.setText("No Audio File");
        }
    }


    private String sendToGroq(File audioFile) throws IOException{

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("audio/m4a");
        RequestBody fileBody = RequestBody.create(audioFile,mediaType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file",audioFile.getName(), fileBody)
                .addFormDataPart("model", "whisper-large-v3-turbo")
                .addFormDataPart("response_format", "json")
                .addFormDataPart("language", "en")
                .build();

        Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer "+ BuildConfig.GROQ_API_KEY)
                .post(requestBody)
                .build();

        try(Response response = client.newCall(request).execute()){
            if (!response.isSuccessful()){
                throw new IOException("HTTP"+ response.code() + ": " + response.message());
            }

            String body = response.body() != null ? response.body().string() : "";
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            return json.has("text") ? json.get("text").getAsString() : "";

        }
    }
}
