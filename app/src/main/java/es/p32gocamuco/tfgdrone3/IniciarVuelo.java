package es.p32gocamuco.tfgdrone3;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.Toast;

import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import es.p32gocamuco.tfgdrone3.R;

public class IniciarVuelo extends AppCompatActivity {

    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallback = null;
    protected DJICodecManager mCodecManager;

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
        BaseProduct product = DJIApplication.getProductInstance();
        TextureView mVideoSurface = (TextureView) findViewById(R.id.videoView);
        if(product == null || !product.isConnected()){
            Toast.makeText(this,getString(R.string.noConectado),Toast.LENGTH_SHORT).show();
        } else {
            mVideoSurface.setSurfaceTextureListener(surfaceTextureListener);
            if(!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)){
                if (VideoFeeder.getInstance().getVideoFeeds() != null
                        && VideoFeeder.getInstance().getVideoFeeds().size()>0){
                    VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(mReceivedVideoDataCallback);
                }
            }
        }
    }
    private void uninitPreviewer(){
        Camera camera = DJIApplication.getProductInstance().getCamera();
        if(camera != null){
            VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(null);
        }
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (mCodecManager == null){
                mCodecManager = new DJICodecManager(IniciarVuelo.this,surfaceTexture,width,height);
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            mCodecManager = new DJICodecManager(IniciarVuelo.this,surfaceTexture,width,height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (mCodecManager != null){
                mCodecManager.cleanSurface();
                mCodecManager = null;
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };
}
