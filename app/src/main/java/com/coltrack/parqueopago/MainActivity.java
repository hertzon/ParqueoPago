package com.coltrack.parqueopago;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button btn_siguiente;
    EditText editText_telefono;
    EditText editText_pin;
    EditText editText_tarjeta;
    EditText editText_expiracion;
    String telefono=null;
    String pin=null;
    String tarjeta=null;
    String expiracion=null;
    CheckBox condiciones;
    boolean isError=false;
    boolean aceptaCondiciones=true;
    String LOGTAG="Track";
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        btn_siguiente=(Button)findViewById(R.id.button_siguiente);
        editText_telefono=(EditText)findViewById(R.id.editText_telefono);
        editText_pin=(EditText)findViewById(R.id.editText_pin);
        editText_tarjeta=(EditText)findViewById(R.id.editText_tarjeta);
        editText_expiracion=(EditText)findViewById(R.id.editText_expiracion);
        condiciones=(CheckBox)findViewById(R.id.checkBoxCondiciones);

        Log.i(LOGTAG, "Creando table Login en Database...");
        //Guardando datos en DB

        db = openOrCreateDatabase("controlRutas", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + "login"
                + " (id INTEGER, telefono VARCHAR(100)PRIMARY KEY NOT NULL, pin VARCHAR(100) NOT NULL);");
        Log.i(LOGTAG, "Tabla Creada....");


        Cursor c;
        c=db.rawQuery("SELECT * FROM login", null);
        int filas=0;
        filas=c.getCount();

        Log.i(LOGTAG,"Numero filas db: "+filas);

        if (filas>1){
            //Abrimos siguiente activity si ya fue registrado el usuario
            Log.i(LOGTAG,"Usuario creado, lanzando actividad login.class");
            Intent i = new Intent(MainActivity.this, Login.class);
            startActivity(i);
        }



        condiciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!condiciones.isChecked()){
                    aceptaCondiciones=false;
                }else {
                    aceptaCondiciones=true;
                }
            }
        });

        btn_siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isError=false;
                telefono=editText_telefono.getText().toString();
                telefono=telefono.replaceAll(" ","");

                //Revision de EditText del Telefono
                if (TextUtils.isEmpty(editText_telefono.getText())){
                    editText_telefono.setError("Ingrese Telefono!!");
                    isError=true;
                }
                if (telefono.length()!=10 && telefono.length()>0){
                    editText_telefono.setError("Numero Telefonico debe ser de 10 digitos!!");
                    isError=true;
                }
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





                if (!isError){
                    if (aceptaCondiciones) {
                        //db.execSQL("DROP TABLE IF EXISTS login");//borramos tabla
                        db.execSQL("INSERT OR REPLACE INTO " +
                                        "login"
                                        + "(telefono,pin) "
                                        + "VALUES (" + telefono + ", " + pin + ");"

                        );





                        Log.i(LOGTAG,"Cerrando conexion a Database..." );
                        db.close();

                        Intent i = new Intent(MainActivity.this, Login.class);
                        startActivity(i);
                    }else {
                        Toast.makeText(getApplicationContext(), "Debe aceptar las condiciones!!", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Revise datos de acceso!!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
