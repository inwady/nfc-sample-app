package ru.iojs.troikagen;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, Runnable {
    byte[] lastUid = new byte[4];
    TextView log;

    PendingIntent pendingIntent;
    IntentFilter[] writeTagFilters = new IntentFilter[]{ new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };
    String[][] techLists = new String[][] { new String[] { MifareClassic.class.getName() }};
    NfcAdapter adapter;
    Tag tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = findViewById(R.id.main);
        view.setOnTouchListener(this);

        log = (TextView)findViewById(R.id.log);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        Intent intent = this.getIntent();
        if (intent != null){
            onNewIntent(intent);
        }

        adapter =  NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, techLists);
    }

    @Override
    protected void onPause() {
        if (adapter != null)
            adapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //приложили карточку
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag != null) {
                (new Thread(this)).start();
            }
        }
    }

    @Override
    public void run() {
        final byte[] uid = tag.getId();

        if (!Arrays.equals(uid, lastUid)) {
            //массив ключей для авторизации
            final byte[][] keys = {
                    {(byte)0xfb, (byte)0xf2, (byte)0x25, (byte)0xdc, (byte)0x5d, (byte)0x58,},
                    {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,},
                    {(byte)0xa0, (byte)0xa1, (byte)0xa2, (byte)0xa3, (byte)0xa4, (byte)0xa5,},
                    {(byte)0x7d, (byte)0xe0, (byte)0x2a, (byte)0x7f, (byte)0x60, (byte)0x25,},
            };

            MifareClassic MfTag = MifareClassic.get(tag);

            try {
                MfTag.connect();

                if (MfTag.isConnected()) {
                    //пытаемся пройти авторизацию по ключу B
                    for (int i = 0; i < keys.length && !MfTag.authenticateSectorWithKeyB(0, keys0[i]); ++i);

                    //читаем блок
                    block0 = MfTag.readBlock(0);
                    Log.d(this.toString(), Utils.getHexString(block0));

                    //пишем блок
                    MfTag.writeBlock(, block0);


                    MfTag.close();
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            msg("Тэг потерян");
                        }
                    });

                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    MfTag.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msg("Ошибка чтения");
                    }
                });

                return;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        lastUid[0] = 0x0;
        lastUid[1] = 0x0;
        lastUid[2] = 0x0;
        lastUid[3] = 0x0;

        return false;
    }

    public void msg(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
