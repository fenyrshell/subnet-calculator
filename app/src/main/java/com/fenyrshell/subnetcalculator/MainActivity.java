package com.fenyrshell.subnetcalculator;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private long subnetCount;
    private EditText ip_octet1;
    private EditText ip_octet2;
    private EditText ip_octet3;
    private EditText ip_octet4;
    private EditText ip_mask;
    private ListView subnets_listview;
    private ArrayList<String> subnets_listview_data;
    private ArrayList<Integer> subnets;
    private ArrayAdapter<String> subnets_adapter;
    private EditText network_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtiene los editText's de la vista.
        this.ip_octet1 = findViewById(R.id.editTextOctetOne);
        this.ip_octet2 = findViewById(R.id.editTextOctetTwo);
        this.ip_octet3 = findViewById(R.id.editTextOctetThree);
        this.ip_octet4 = findViewById(R.id.editTextOctetFour);
        this.ip_mask = findViewById(R.id.editTextMask);

        // Configura el valor maximo admitido en los editText's.
        this.editTextMaxValue(this.ip_octet1, 255);
        this.editTextMaxValue(this.ip_octet2, 255);
        this.editTextMaxValue(this.ip_octet3, 255);
        this.editTextMaxValue(this.ip_octet4, 255);
        this.editTextMaxValue(this.ip_mask, 30);

        // Configura la vista de la lista.
        this.subnets_listview = findViewById(R.id.listViewSubnets);
        this.subnets_listview_data = new ArrayList<>();
        subnets_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subnets_listview_data);
        this.subnets_listview.setAdapter(subnets_adapter);

        this.subnets = new ArrayList<>();
        this.network_size = findViewById(R.id.editTextNetworkSize);

        this.subnetCount = 0;
    }

    /**
     * Agrega la cantidad de hosts por subred que se requiere.
     * @param view Vista
     */
    public void addSubnet(View view) {

        String subnet = network_size.getText().toString();

        if (!subnet.isEmpty()) {
            subnetCount++;
            subnets.add(Integer.parseInt(subnet));
            subnets_listview_data.add("Subred " + subnetCount + ":  " + subnet);
            subnets_adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Subred agregada", Toast.LENGTH_SHORT).show();
            this.network_size.setText(null);
        }else {
            Toast.makeText(getApplicationContext(), "Tamaño de subred no valido", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Envia los datos a la actividad Result para calcular los resltados.
     * @param view
     */
    public void calculate(View view) {

        try {

            int octet1 = Integer.parseInt(ip_octet1.getText().toString());
            int octet2 = Integer.parseInt(ip_octet2.getText().toString());
            int octet3 = Integer.parseInt(ip_octet3.getText().toString());
            int octet4 = Integer.parseInt(ip_octet4.getText().toString());
            int mask = Integer.parseInt(ip_mask.getText().toString());

            if (subnets.size()>0) {
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("address", octet1 + "." + octet2 + "." + octet3 + "." + octet4 + "/" + mask);
                intent.putIntegerArrayListExtra("subnets", this.subnets);
                this.startActivity(intent);
            }else {
                Toast.makeText(getApplicationContext(), "Subredes no configuradas", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.completeIP, Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Configura el maximo valor que admitira un editText.
     * @param editText EditText a configurar.
     * @param max_value Valor maximo que admitira el editText.
     */
    private void editTextMaxValue(final EditText editText, final int max_value) {

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    int value = Integer.parseInt(editText.getText().toString());

                    if (value > max_value) {
                        editText.setText(Integer.toString(max_value));
                    }

                }catch (Exception e) {}

                return false;
            }
        });
    }

    /**
     * Elimina los datos de los EditText's
     *
     * @param view Vista a eliminar los datos.
     */
    public void clearData(View view) {
        // Crea un dialogo de confirmacion.
        AlertDialog.Builder confirm = new AlertDialog.Builder(this);

        confirm.setMessage(R.string.confirm_title);

        confirm.setPositiveButton(R.string.confirm_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ip_octet1.setText(null);
                ip_octet2.setText(null);
                ip_octet3.setText(null);
                ip_octet4.setText(null);
                ip_mask.setText(null);
                subnets.clear();
                subnets_listview_data.clear();
                subnets_adapter.notifyDataSetChanged();
                subnetCount = 0;
                Toast.makeText(MainActivity.this, "Datos eliminados!!!", Toast.LENGTH_SHORT).show();
            }
        });

        confirm.setNegativeButton(R.string.confirm_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        confirm.create().show();

    }
}
