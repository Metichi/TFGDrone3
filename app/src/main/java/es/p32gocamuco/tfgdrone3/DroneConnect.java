package es.p32gocamuco.tfgdrone3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import dji.sdk.sdkmanager.DJISDKManager;

public class DroneConnect extends AppCompatActivity {
    public static final int DISCONNECTED = 2;
    public static final int BRIDGE_CONNECTED = 1;
    public static final int USB_CONNECTED = 0;

    TextView connectionStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_connect);
        connectionStatus = (TextView) findViewById(R.id.connection_status);
        if((DJIApplication.getProductInstance()!=null) && DJIApplication.getProductInstance().isConnected()) {
            setConnectionStatus(USB_CONNECTED);
        } else {
            setConnectionStatus(DISCONNECTED);
        }

        RecyclerView connectionOptions = (RecyclerView) findViewById(R.id.connectionOptions);
        connectionOptions.setLayoutManager(new LinearLayoutManager(this));

        View[] cards = generateCards();
        ConnectorAdapter adapter = new ConnectorAdapter(cards);
        connectionOptions.setAdapter(adapter);
    }

    private View[] generateCards(){
        GridLayout usbConnection = (GridLayout) LayoutInflater.from(this).inflate(R.layout.activity_drone_connect_usb,null);
        Button connectUSBButton = (Button) usbConnection.findViewById(R.id.usbConnectButton);
        connectUSBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DJISDKManager.getInstance().startConnectionToProduct()){
                    setConnectionStatus(USB_CONNECTED);
                    setResultToToast("Conectado con exito");
                } else {
                    setConnectionStatus(DISCONNECTED);
                    setResultToToast("Fallo en la conexión");
                }
            }
        });



        GridLayout bridgeConnection = (GridLayout) LayoutInflater.from(this).inflate(R.layout.activity_drone_connect_bridge,null);
        EditText bridgeIP = (EditText) bridgeConnection.findViewById(R.id.bridgeIP);
        Button connectBridgeButton = (Button) bridgeConnection.findViewById(R.id.bridgeConnection);


        /**
         * This class is used to store the IP from the editText as it changes and use it when
         * the button is pressed.
         */
         class BridgeConnectionClickListener implements View.OnClickListener{
            public String ip;

            public BridgeConnectionClickListener(){}
            @Override
            public void onClick(View view) {
                DJISDKManager.getInstance().enableBridgeModeWithBridgeAppIP(ip);
                setResultToToast("Comprueba la conexión en la BridgeApp");
            }
         }
         final BridgeConnectionClickListener listener = new BridgeConnectionClickListener();

        bridgeIP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                listener.ip = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        connectBridgeButton.setOnClickListener(listener);

        View[] cards = new View[]{usbConnection,bridgeConnection};
        return cards;
    }

    private void setConnectionStatus(int status) {
        String[] connectionModes = getResources().getStringArray(R.array.connection_modes);
        connectionStatus.setText(connectionModes[status]);
    }

    private void setResultToToast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    public static class ConnectorAdapter extends RecyclerView.Adapter<ConnectorAdapter.ViewHolder>{
        private View[] connectionCards;

        public static class ViewHolder extends RecyclerView.ViewHolder{
            public View layout;
            public CardView card;
            public ViewHolder(View v){
                super(v);
                layout = v;
                card = (CardView) v.findViewById(R.id.connectionDescriptionCard);
            }
        }

        public ConnectorAdapter(View[] v){
            connectionCards = v;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View cardView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_drone_connect_cardview,parent,false);
            ViewHolder vh = new ViewHolder(cardView);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.card.addView(connectionCards[position]);
        }

        @Override
        public int getItemCount() {
            return connectionCards.length;
        }
    }
}
