package com.lentes;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements GpsInterface {
    private ListAdapter mListAdapter;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                AparatoBluetooth aparato = new AparatoBluetooth();
                aparato.nombre = device.getName();
                aparato.direccion = device.getAddress(); // MAC
                mListAdapter.add(aparato);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(tienePermisos()){
            inicializar();
        }
        else{
            pedirPermisos();
        }
    }

    void inicializar(){

        BluetoothSingleton.getInstancia(this, (TextView)findViewById(R.id.tv));

        List<AparatoBluetooth> l = new ArrayList<>();

        final ListView listView=(ListView)findViewById(R.id.listView);
        listView.setClickable(true);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                AparatoBluetooth ba = (AparatoBluetooth)listView.getItemAtPosition(position);
                BluetoothSingleton.getInstancia(null,null).conectar(ba);

            }
        });

        mListAdapter = new ListAdapter(this, R.layout.elemento_lista,l);
        listView.setAdapter(mListAdapter);

        IntentFilter ifilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, ifilter);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);


        Button buttonVelocity = findViewById(R.id.buttonVelocity);
        buttonVelocity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //BluetoothSingleton.getInstancia(null, null).mandarString("");
            }
        });

        Button buttonTime = findViewById(R.id.buttonVelocity);
        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    boolean tienePermisos(){
        boolean permisoReadPhoneState = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean permisoReadCallLog = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
        boolean permisoProcessOutgoingCalls = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED;
        boolean permisoAccessCoarseLocation = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean permisoBluetooth = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
        boolean permisoBluetoothAdmin = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        boolean permisoReadContacts = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;

        return permisoAccessCoarseLocation && permisoBluetooth && permisoBluetoothAdmin &&permisoProcessOutgoingCalls && permisoReadCallLog && permisoReadPhoneState && permisoReadContacts;
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(tienePermisos()){
            inicializar();
        }
        else{
            pedirPermisos();
        }
    }

    void pedirPermisos(){
        ArrayList<String> permisos = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.READ_PHONE_STATE);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            permisos.add(Manifest.permission.READ_CALL_LOG);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED){
            permisos.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permisos.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED){
            permisos.add(Manifest.permission.BLUETOOTH);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED){
            permisos.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            permisos.add(Manifest.permission.READ_CONTACTS);
        }

        if(!permisos.isEmpty()){
            ActivityCompat.requestPermissions(MainActivity.this,
                    permisos.toArray(new String[0]),
                    1);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        float nCurrentSpeed = 0;

        if(location != null)
        {
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String  strUnits = "meters/second";


        Log.d("vvv",strCurrentSpeed + " " + strUnits);
    }


    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        if(location != null)
        {
            CLocation myLocation = new CLocation(location, true);
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGpsStatusChanged(int event) {
        // TODO Auto-generated method stub

    }
}
