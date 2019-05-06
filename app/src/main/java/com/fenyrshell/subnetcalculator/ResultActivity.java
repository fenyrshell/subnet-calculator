package com.fenyrshell.subnetcalculator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private TextView result_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.result_view = findViewById(R.id.textView);

        calculate();
    }

    private void calculate() {
        ArrayList<Integer> subnets = getIntent().getIntegerArrayListExtra("subnets");
        String address = getIntent().getStringExtra("address");
        
        if (subnets.size()>0) {
            VariableLenghtSubnetMask vlsm = new VariableLenghtSubnetMask();
            ArrayList<Subnet> result = vlsm.calculateVlsm(address, subnets);

            String steps = "";

            String dataInput = "Datos proporcionados para el subneteo."
                    + "\n\nDirección IP: " + address
                    + "\nHosts solicitados por subred: " + subnets + ".\n\n";

            String step1 = "Ordenar las subredes de mayor a menor hosts solicitados."
                    + "\n\nSubredes ordenadas " + vlsm.sortSubnets(subnets) + ".";

            String[] net = address.split("/");
            String IP = net[0];
            int mask = Integer.parseInt(net[1]);
            String IPBin = vlsm.quartetToBinary(IP);
            String maskBin = vlsm.quartetToBinary(vlsm.toDecMask(mask));
            String dirNetwork = vlsm.convertIpToQuartet(vlsm.findFirstIp(address));
            String dirNetworkBin = vlsm.quartetToBinary(dirNetwork);

            String step2 = "\n\nEncontrar la dirección de red de la IP " + address + "."
                    + "\n\nPara encontrar la dirección de red se debe convertir la IP a binario al "
                    + "igual que la mascara (/" + mask + " indica el "
                    + "numero de bits encendidos de la "
                    + "parte de red en la mascara)."
                    + "\n\n" + IPBin + " IP"
                    + "\n" + maskBin + " Mascara"
                    + "\n\nUna vez obtenido la ip y la mascara en binario, realizar la operación "
                    + "boleana AND con la ip y la mascara, el resultado convertirlo a decimal y "
                    + "esta será la dirección de red de donde se iniciará el subneteo."
                    + "\n\n" + IPBin + " IP"
                    + "\n" + maskBin + " Mascara"
                    + "\n----------------------------------------------------------------------"
                    + "\n" + dirNetworkBin + " Dirección"
                    + "\n\nDirección de red: " + dirNetwork + "\n\n";

            steps += dataInput + step1 + step2;

            int i = 1;

            for (Subnet subnet : result) {

                String step3 = "<<<<<<<<<<<<<<< Inicio Subred " + i + " >>>>>>>>>>>>>>>"
                        + "\n\nEncontrar los bits para ser utilizados en la parte de hosts "
                        + "\npara alcanzar los hosts solicitados, con la siguiente formula:"
                        + "\n\n(2^n)-2 >= h"
                        + "\n\ndonde n es el numero de bits apagados de la parte de hosts para "
                        + "encontrar los hosts permitidos y h es el numero de hosts solicitados."
                        + "\n\n(2^" + subnet.hostsBits + ") - 2 >= " + subnet.requiredHosts
                        + "\n" + (int) (Math.pow(2, subnet.hostsBits) - 2) + " >= " + subnet.requiredHosts
                        + "\n\nHosts Requeridos: " + subnet.requiredHosts
                        + "\nHosts Permitidos: " + subnet.usableHosts
                        + "\nBits de hosts: " + subnet.hostsBits;

                String step4 = "\n\nEncontrar la nueva mascara de red de la dirección actual."
                        + "\nLa nueva mascara resultará de apagar la cantidad de bits utilizados "
                        + "para cubrir los hosts solicitados del paso anterior (Bits de hosts)."
                        + "\n\nDirección actual: " + dirNetwork
                        + "\nBits de hosts: " + subnet.hostsBits
                        + "\nMascara: /" + subnet.mask
                        + "\nMascara: " + vlsm.quartetToBinary(vlsm.toDecMask(subnet.mask))
                        + "\nMascara: " + subnet.maskDec;

                String step5 = "\n\nCalcular el salto de red."
                        + "\n\nEl salto de red se calcula con la siguiente formula: 256 – el valor "
                        + "del octeto de la mascara de red donde se esta operando "
                        + "\n(entre 1 y 8 bits primer octeto, entre 9 y 16 bits segundo octeto, "
                        + "\nentre 17 y 24 bits tercer octeto, 25 en adelante cuarto octeto)."
                        + "\n\nMascara: " + subnet.maskDec
                        + "\nOcteto Utilizado: " + subnet.usedOctet
                        + "\n\n256 – " + subnet.usedOctetValue + " = " + subnet.networkJump
                        + "\n\nSalto de red: " + subnet.networkJump;

                String step6 = "\n\nEncontrar la primera y la ultima dirección de red utilizable y "
                        + "el broadcast. La primera dirección de red utilizable es una mayor a la "
                        + "dirección de red."
                        + "\n\nDirección de red: " + dirNetwork
                        + "\nPrimera dirección: " + subnet.firstUsableHost
                        + "\n\nEl broadcast es una dirección menos que la nueva dirección de red. La "
                        + "nueva dirección de red es igual a la dirección de red mas el salto de red "
                        + "(el salto de red se da en el mismo octeto que se utilizo para calcular el "
                        + "salto de red pero ahora en la dirección de red)."
                        + "\n\nDirección de red: " + dirNetwork
                        + "\nSalto de red: " + subnet.networkJump
                        + "\nOcteto utilizado: " + subnet.usedOctet
                        + "\nNueva dirección de red: " + subnet.nextSubnet
                        + "\nBroadcast: " + subnet.broadcast
                        + "\n\nLa ultima dirección de red utilizable es una dirección menos que el broadcast."
                        + "\n\nUltima dirección: " + subnet.lastUsableHost
                        + "\n\n<<<<<<<<<<<<<<< Fin Subnet " + i + " >>>>>>>>>>>>>>>\n\n";

                dirNetwork = subnet.nextSubnet;

                steps += step3 + step4 + step5 + step6;

                i++;

            }

            this.result_view.setText(steps);
        }
    }
}
