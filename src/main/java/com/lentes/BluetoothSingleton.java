package com.lentes;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BluetoothSingleton {
    private static BluetoothSingleton mInstancia = null;

    private BluetoothSocket mSocket;
    private BluetoothAdapter mAdapter;
    private Context mContext;
    private TextView mTextView;

    // Ultimo: 0 conectar
    //         1 llamada entrante
    //         2 llamada perdida
    //         ...
    private String mUltimo = "";

    private BluetoothSingleton(Activity context, TextView textView){
        mContext = context;
        mTextView = textView;

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        int REQUEST_ENABLE_BT = 1;

        if (!mAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }

        mAdapter.startDiscovery();
}

    public static BluetoothSingleton getInstancia(Activity context, TextView textView) {
        if (mInstancia == null)
            mInstancia = new BluetoothSingleton(context, textView);

        return mInstancia;
    }

    public void conectar(AparatoBluetooth ab){
        (new ConectarBluetooth(ab)).execute();

        mUltimo="0";
    }

    private boolean establecerConexion(String dir){
        BluetoothDevice device = mAdapter.getRemoteDevice(dir);

        UUID uuid = UUID.randomUUID();

        try {
            mSocket = device.createRfcommSocketToServiceRecord(uuid);
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            mSocket = (BluetoothSocket) m.invoke(device, 1);
            mSocket.connect();
            return true;
        } catch (Exception e) {
            Toast.makeText(mContext, "create() failed ??? ", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    boolean mandarString(String str){
        if(str.contains("entrante"))
            mUltimo = "1";
        else if(str.contains("perdida"))
            mUltimo = "2";

        try {
            Log.d("rrr", "" + mSocket.isConnected());
            mSocket.getOutputStream().write(str.getBytes());
            mSocket.getOutputStream().flush();
            return true;
        } catch (Exception e) {
            Toast.makeText(mContext, "no mando str ?? ", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return false;
    }

    public String ultimaAccion(){
        return mUltimo;
    }

    private class ConectarBluetooth extends AsyncTask<String, Void, Boolean> {
        private AparatoBluetooth aparatoBluetooth;

        public ConectarBluetooth(AparatoBluetooth ab){
            super();
            aparatoBluetooth = ab;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(mContext, "Conectando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if(!establecerConexion(aparatoBluetooth.direccion))
                return false;

            int intetos = 0;
            while(!mSocket.isConnected() && intetos++ < 3){
                try {
                    Thread.sleep(300);
                } catch (Exception e) {}
            }

            return mSocket.isConnected();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mTextView.setText(result ? "Conectado a " + aparatoBluetooth.nombre : "No se pudo conectar. Intenta de nuevo.");

            SimpleDateFormat sdf = new SimpleDateFormat(" dd HH:mm");
            Date hoy = new Date();
            String date = sdf.format(hoy);

            Calendar cal = Calendar.getInstance();
            cal.setTime(hoy);

            String mes = "";

            switch (cal.get(Calendar.MONTH)){
                case 0:
                    mes = "Ene.";
                    break;
                case 1:
                    mes = "Feb.";
                    break;
                case 2:
                    mes = "Mar.";
                    break;
                case 3:
                    mes = "Abr.";
                    break;
                case 4:
                    mes = "May.";
                    break;
                case 5:
                    mes = "Jun.";
                    break;
                case 6:
                    mes = "Jul.";
                    break;
                case 7:
                    mes = "Ago.";
                    break;
                case 8:
                    mes = "Sep.";
                    break;
                case 9:
                    mes = "Oct.";
                    break;
                case 10:
                    mes = "Nov.";
                    break;
                default:
                    mes = "Dic.";
            }

            BluetoothSingleton.getInstancia(null,null).mandarString(mes + date);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
