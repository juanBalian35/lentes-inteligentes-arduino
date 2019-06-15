package com.lentes;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.Date;

public class HandlerLlamadas extends PhonecallReceiver {
    public HandlerLlamadas(){ }

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        if(number != null) {
            String contacto = getContactName(number, ctx);

            if(contacto.length() == 0)
                contacto = number;

            BluetoothSingleton.getInstancia(null, null).mandarString("Llamada entrante: \n" + contacto);
        }
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {

    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        String ultima = BluetoothSingleton.getInstancia(null, null).ultimaAccion();

        //TODO: ESTO
        if(!ultima.equals("2"))
            BluetoothSingleton.getInstancia(null, null).mandarString("???");
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        if(number != null) {
            String contacto = getContactName(number, ctx);

            if(contacto.length() == 0)
                contacto = number;

            BluetoothSingleton.getInstancia(null, null).mandarString("Llamada perdida: \n" + contacto);
        }
    }

    private String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

}