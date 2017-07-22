package es.p32gocamuco.tfgdrone3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
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
        File path = this.getFilesDir();
        LinearLayout cargarRutaMenu = (LinearLayout) findViewById(R.id.cargarRutaMenu);
        cargarRutaMenu.removeAllViewsInLayout();

        String[] list = path.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".adp");
            }
        });

        if (list.length == 0){
            TextView noFiles = new TextView(this);
            noFiles.setText("No hay archivos guardados");
            cargarRutaMenu.addView(noFiles);
        } else {
            for (String name : list){
                Button loadRoute = new Button(this);
                loadRoute.setText(name);
                loadRoute.setOnClickListener(this);
                cargarRutaMenu.addView(loadRoute);
            }
        }

    }

    @Override
    public void onClick(View view) {
        final String filename = ((Button) view).getText().toString();
        File path = CargarRuta.this.getFilesDir();
        final File file = new File(path,filename);
        new AlertDialog.Builder(this)
                .setTitle("Accion")
                .setPositiveButton("Cargar ruta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        RecordingRoute r = RecordingRoute.loadRoute(filename, CargarRuta.this);
                        Intent intent = new Intent(CargarRuta.this,CrearRuta.class);
                        intent.putExtra("RECORDING_ROUTE",r);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Borrar ruta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        file.delete();
                        updateUI();
                        dialogInterface.dismiss();
                    }
                })
                .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }
}
