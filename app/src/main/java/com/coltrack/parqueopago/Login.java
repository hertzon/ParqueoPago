package com.coltrack.parqueopago;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {
    SQLiteDatabase db;
    String LOGTAG="Track";
    EditText editText_telefono;
    EditText editText_pin;
    EditText editText_parqueadero;
    Button button_ingresar;
    CheckBox checkBox_recordar;
    boolean isError=false;
    boolean recordar=true;
    String IP="http://192.168.1.77";
    String LOGIN=IP+"/WebServiceParking/login/park";
    //String LOGIN=IP+"/login.php";
    ProgressBar progressBar;
    String telefono=null;
    String pin=null;
    String numeroParqueadero=null;
    String respuestaLogin=null;
    String islibre=null;
    String tiempoinicio=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.i(LOGTAG, "Iniciando Activity Login.java");
        editText_telefono=(EditText)findViewById(R.id.editText_telefono);
        editText_pin=(EditText)findViewById(R.id.editText_pin);
        editText_parqueadero=(EditText)findViewById(R.id.editText_parqueadero);
        button_ingresar=(Button)findViewById(R.id.button_ingresar);
        checkBox_recordar=(CheckBox)findViewById(R.id.checkBox_recordar);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);



        db = openOrCreateDatabase("controlRutas", MODE_PRIVATE, null);
        Cursor c;
        c=db.rawQuery("SELECT * FROM login",null);
        c.moveToFirst();
        int offset=0;
        int id=0;
        String strTelefono;
        String strPin;
        if (c.getCount()>0){
            if (c != null && c.getCount()>0) {
                do {
                    id=c.getInt(c.getColumnIndex("id"));
                    strTelefono=c.getString(c.getColumnIndex("telefono"));
                    strPin=c.getString(c.getColumnIndex("pin"));
                    Log.d(LOGTAG, "id: " + id + " " + "strTelefono: " + strTelefono + " " + "strPin: " + strPin);

                }while(c.moveToNext());
            }
        }

        button_ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isError=false;
                telefono=editText_telefono.getText().toString();
                pin=editText_pin.getText().toString();
                numeroParqueadero=editText_parqueadero.getText().toString();

                //Revision EdtiText de Telefono
                if (TextUtils.isEmpty(editText_telefono.getText())){
                    editText_telefono.setError("Ingrese Telefono!!");
                    isError=true;
                }
                if (telefono.length()!=10 && telefono.length()>0){
                    editText_telefono.setError("Numero Telefonico debe ser de 10 digitos!!");
                    isError=true;
                }
                //////////////////////////////////////////////////////////////////
                //Revision de EditText del PIN
                pin=editText_pin.getText().toString();
                pin=pin.replaceAll(" ","");
                if (TextUtils.isEmpty(editText_pin.getText())){
                    editText_pin.setError("Ingrese PIN!!");
                    isError=true;
                }
                if (pin.length()<4 && pin.length()>0){
                    editText_pin.setError("PIN debe ser mayor a cuatro digitos!!");
                    isError=true;
                }
                //////////////////////////////////////////////////////////////////
                //Revision de EditText del Numero del Parqueadero
                numeroParqueadero=editText_parqueadero.getText().toString();
                numeroParqueadero=numeroParqueadero.replaceAll(" ","");
                if (TextUtils.isEmpty(editText_parqueadero.getText())){
                    editText_parqueadero.setError("Ingrese PIN!!");
                    isError=true;
                }
                if (pin.length()<4 && pin.length()>0){
                    editText_parqueadero.setError("PIN debe ser mayor a cuatro digitos!!");
                    isError=true;
                }
                //////////////////////////////////////////////////////////////////

                if (!isError){
                    //Enviamos parametros por webService
                    ObtenerWebService envia=new ObtenerWebService();
                    //Enviamos el telefono, el servidor debe responder con ack o fail
                    envia.execute(LOGIN,telefono,pin,numeroParqueadero);



                    Log.i(LOGTAG,"No hay error, abriendo siguiente activity");
                    Intent i=new Intent(Login.this,Duracion.class);

                }else {
                    Toast.makeText(getApplicationContext(), "Revise datos de acceso!!!", Toast.LENGTH_LONG).show();
                }


            }
        });






    }
    public class ObtenerWebService extends AsyncTask<String,Integer,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(LOGTAG,"En Preexecute..");
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }
        @Override
        protected String doInBackground(String... strings) {
            Log.i(LOGTAG,"En doInBackground..");
            for (int i=0;i< strings.length;i++){
                Log.i(LOGTAG,"Parametro String entrante: "+i+" "+strings[i]);
            }
            HttpClient client = new DefaultHttpClient();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("telefono",strings[1]));
            params.add(new BasicNameValuePair("pin",strings[2]));
            params.add(new BasicNameValuePair("nparqueadero",strings[3]));

            HttpPost httpPost  = new HttpPost(strings[0]);
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                HttpResponse httpResponse = client.execute(httpPost);
                String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();

                JSONObject respuestaJSON = new JSONObject(jsonResult.toString());
                //Nodo Login:
                JSONObject respLogin=respuestaJSON.getJSONObject("login");
                respuestaLogin=respLogin.getString("respuesta");
                //Nodo Parking:
                numeroParqueadero=null;
                JSONObject respParking=respuestaJSON.getJSONObject("parking");
                numeroParqueadero=respParking.getString("numeroParqueadero");
                islibre=respParking.getString("islibre");
                tiempoinicio=respParking.getString("tiempoinicio");




                Log.d(LOGTAG, "Respuesta Login: " + respuestaLogin);
                Log.d(LOGTAG, "Respuesta numeroParqueadero: " + numeroParqueadero);
                Log.d(LOGTAG, "Respuesta islibre: " + islibre);
                Log.d(LOGTAG, "Respuesta tiempoinicio: " + tiempoinicio);



                //String resultJSON = respuestaJSON.getString("login");


                //String respuesta = resultJSON.getJSONObject(0).getString("respuesta");






            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.i(LOGTAG,"En onProgressUpdate.. Recibido:"+ values[0]);
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i(LOGTAG, "En PostExecute..");
            //Toast.makeText(getBaseContext(),"Hilo Terminado...",Toast.LENGTH_SHORT).show();
            if (respuestaLogin.equals("OK")){
                Toast.makeText(getApplicationContext(),"Logueo ok",Toast.LENGTH_SHORT).show();

                if (numeroParqueadero.equals("ND")){
                    Toast.makeText(getApplicationContext(),"Numero de parqueadero invalido",Toast.LENGTH_SHORT).show();
                    Log.d(LOGTAG,"Numero de parqueadero invalido");
                }else {
                    Log.d(LOGTAG,"Numero de parqueadero valido");
                    if (islibre.equals("si")){
                        Log.i(LOGTAG,"Parqueadero libre");
                    }else if(islibre.equals("no")){
                        Log.i(LOGTAG,"Parqueadero ocupado");
                    }else {
                        Log.i(LOGTAG,"Parqueadero libre error");
                    }



                }



            }else{
                Toast.makeText(getApplicationContext(),"Telefono y/o pin no valido",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            Log.i(LOGTAG, "En OnCancelled..");

        }




    }
    private StringBuilder inputStreamToString(InputStream is)
    {
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try
        {
            while ((rLine = rd.readLine()) != null)
            {
                answer.append(rLine);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return answer;
    }
}
