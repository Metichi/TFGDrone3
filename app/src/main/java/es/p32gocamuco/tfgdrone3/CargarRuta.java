package es.p32gocamuco.tfgdrone3;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.RecordingRoute;

public class CargarRuta extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_ruta);
        updateUI();
    }

    @Override
    protected void onResume() {
        updateUI();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        updateUI();
        super.onRestart();
    }

    private void updateUI(){
        ArrayList<Button> buttons = new ArrayList<>(0);
        String path = DJIApplication.getAppContext().getFilesDir().getPath();
        AssetManager mngr = getAssets();
        LinearLayout cargarRutaMenu = (LinearLayout) findViewById(R.id.cargarRutaMenu);
        cargarRutaMenu.removeAllViewsInLayout();
        try {
            String list[] = mngr.list(path);
            if (list.length == 0){
                TextView noFiles = new TextView(this);
                noFiles.setText("No hay archivos guardados");
                cargarRutaMenu.addView(noFiles);
            } else {
                for (String file : list){
                    String[] parts = file.split(".");
                    if (parts.length == 2){
                        String name = parts[0];
                        String ext = parts[0];
                        if (ext == "adp"){
                            Button button = new Button(this);
                            button.setText(name);
                            button.setOnClickListener(this);
                            buttons.add(button);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (buttons.size() == 0){
            TextView noFiles = new TextView(this);
            noFiles.setText("No hay archivos guardados");
            cargarRutaMenu.addView(noFiles);
        } else {
            for (Button button : buttons){
                cargarRutaMenu.addView(button);
            }
        }

    }

    @Override
    public void onClick(View view) {
        String filename = ((Button) view).getText().toString() + ".adp";
        RecordingRoute r = RecordingRoute.loadRoute(filename);
        Intent intent = new Intent(this,CrearRuta.class);
        intent.putExtra("RECORDING_ROUTE",r);
    }
}
