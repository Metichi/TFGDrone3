package es.p32gocamuco.tfgdrone3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import es.p32gocamuco.tfgdrone3.R;

public class IniciarVuelo extends AppCompatActivity {

    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallback = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_vuelo);

        mReceivedVideoDataCallback = new VideoFeeder.VideoDataCallback() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if(mCodecManager != null){
                    mCodecManager.sendDataToDecoder(videoBuffer,size);
                }
            }
        };
    }

    protected void onProductChange(){
        initPreviewer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPreviewer();
        onProductChange();
    }

    private void initPreviewer(){
        BaseProduct
    }
}
