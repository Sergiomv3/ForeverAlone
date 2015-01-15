package com.example.sergio.foreveralone;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sergio.foreveralone.com.example.sergio.chatterbox.ChatterBot;
import com.example.sergio.foreveralone.com.example.sergio.chatterbox.ChatterBotFactory;
import com.example.sergio.foreveralone.com.example.sergio.chatterbox.ChatterBotSession;
import com.example.sergio.foreveralone.com.example.sergio.chatterbox.ChatterBotType;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {
    private boolean reproductor = false;
    private int IDActividadTTS = 1;
    private int IDActividadHablar= 2;
    private TextToSpeech tts;
    private EditText etFrase;
    private TextView tvRespuesta;
    private ChatterBotSession cb;
    private String fraseHablada;
    private String respuesta;
    private String idioma = "es-ES";
    private Spinner sp1;
    private Spinner sp2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etFrase = (EditText)findViewById(R.id.etFrase);
        tvRespuesta = (TextView)findViewById(R.id.tvRespuesta);
        sp1 = (Spinner)findViewById(R.id.spinner);
        sp2 = (Spinner)findViewById(R.id.spinner2);



        ArrayAdapter<CharSequence> adaptador =ArrayAdapter.createFromResource(this,R.array.pitch, android.R.layout.simple_spinner_item);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp1.setAdapter(adaptador);
         adaptador =ArrayAdapter.createFromResource(this,R.array.velocidad, android.R.layout.simple_spinner_item);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp2.setAdapter(adaptador);
        ChatterBotFactory factory = new ChatterBotFactory();
        try {
            ChatterBot bot = factory.create(ChatterBotType.CLEVERBOT);
            cb = bot.createSession();
        } catch (Exception e) {
            Log.v("Error", e.toString());
        }

        // NO ES ESTRICTAMENTE NECESARIO LANZAR ESTE INTENT
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, IDActividadTTS);

    }

        @Override
        protected void onStart() {
            super.onStart();
            Intent intent = new Intent();

            intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);

            startActivityForResult(intent, IDActividadTTS);

            sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    try {
                        tts.setPitch(Float.valueOf(sp1.getItemAtPosition(position).toString()));
                    }catch (NullPointerException e){

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
            sp2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    try{
                        tts.setSpeechRate(Float.valueOf(sp2.getItemAtPosition(position).toString()));
                    }catch (NullPointerException e){

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IDActividadTTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                // SI NO, INSTALAMOS.
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }else{
            if(requestCode==IDActividadHablar && resultCode == RESULT_OK){
                ArrayList<String> textos = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                etFrase.setText(textos.get(0).toString());
            }
            if(reproductor){
                if(tts.isSpeaking()){
                }else{
                    tvRespuesta.setText(respuesta);
                    Botchat hf = new Botchat();
                    hf.execute();
                }

            }else{
                //TOSTADA
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            reproductor = true;
            tts.setPitch(1); //tono
            tts.setSpeechRate(1); //velocidad

        } else {
            reproductor = false;
            Toast.makeText(this, "No se puede usar TTS",Toast.LENGTH_SHORT).show();
        }

    }
    // LIBERAR RECURSOS PARA MEJORAR RENDIMIENTO

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();

            tts = null;
        }
    }
    public void reproducir(View v){
        if(reproductor){
            if(tts.isSpeaking()){
            }else{
                tvRespuesta.setText(respuesta);
                Botchat hf = new Botchat();
                hf.execute();
            }

        }else{
            //TOSTADA
        }
    }
    public void hablar(View v){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, idioma);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla ahora");
        i.putExtra(RecognizerIntent.
                        EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                3000);
        startActivityForResult(i, IDActividadHablar);

    }

    class Botchat extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                fraseHablada = etFrase.getText().toString();
                System.out.println("La frase hablada es "+fraseHablada);
                respuesta = cb.think(fraseHablada);

            } catch (Exception e) {
                Log.v("Think", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(reproductor){
                if(tts.isSpeaking()){

                }else{
                    tvRespuesta.setText(respuesta);
                    tts.speak(respuesta, TextToSpeech.QUEUE_FLUSH, null);

                }

            }else{
                //TOSTADA
            }
        }
    }

    public void espaniol(View v){
        idioma = "es-ES";
        Toast.makeText(getApplicationContext(), "¡Ahora te reconozco en español!", Toast.LENGTH_SHORT).show();
        tts.setLanguage((new Locale("es", "ES")));
    }
    public void ingles(View v){
        idioma = "en-EN";
        Toast.makeText(getApplicationContext(), "I hear you in English!", Toast.LENGTH_SHORT).show();
        tts.setLanguage(Locale.ENGLISH);
    }
    public void eventoNulo(View v){
    }

}
