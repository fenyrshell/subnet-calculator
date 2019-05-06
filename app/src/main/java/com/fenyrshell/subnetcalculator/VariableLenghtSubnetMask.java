package com.fenyrshell.subnetcalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class VariableLenghtSubnetMask {

    public ArrayList<Subnet> calculateVlsm(String network, ArrayList<Integer> subnets) {

        // Ordena las redes de mayor a menor.
        ArrayList<Integer> sortedSubnets = this.sortSubnets(subnets);

        ArrayList<Subnet> vlsm = new ArrayList<>();

        int currentIp = findFirstIp(network);

        for (int i = 0; i < sortedSubnets.size(); i++) {

            Subnet subnet = new Subnet();

            subnet.address = convertIpToQuartet(currentIp);

            int requiredHosts = sortedSubnets.get(i);
            subnet.requiredHosts = requiredHosts;

            int mask = calcCIDRMask(requiredHosts);
            String maskDec = toDecMask(mask);
            subnet.mask = mask;
            subnet.maskDec = maskDec;

            int usableHosts = usableHosts(mask);
            subnet.usableHosts = usableHosts;

            subnet.hostsBits = 32 - mask;

            int usedOctet = usedOctet(mask);
            int usedOctetValue = Integer.parseInt(maskDec.split("\\.")[usedOctet - 1]);
            subnet.usedOctet = usedOctet;
            subnet.usedOctetValue = usedOctetValue;
            subnet.networkJump = networkJump(mask, usedOctet);

            subnet.broadcast = convertIpToQuartet(currentIp + usableHosts + 1);

            String firstUsableHost = convertIpToQuartet(currentIp + 1);
            String lastUsableHost = convertIpToQuartet(currentIp + usableHosts);

            subnet.firstUsableHost = firstUsableHost;
            subnet.lastUsableHost = lastUsableHost;
            subnet.range = firstUsableHost + " - " + lastUsableHost;

            vlsm.add(subnet);

            currentIp += usableHosts + 2;

            subnet.nextSubnet = convertIpToQuartet(currentIp);

        }

        return vlsm;
    }

    /**
     * Ordena las subredes de mayor a menor.
     * @param subnets Lista de subredes desordenada.
     * @return Lista de subredes ordenada de mayor a menor.
     */
    public ArrayList<Integer> sortSubnets(ArrayList<Integer> subnets) {
        Comparator<Integer> comparator = Collections.reverseOrder();
        Collections.sort(subnets, comparator);
        return subnets;
    }

    /**
     * Convierte una IPv4 en formato "192.168.1.0/24" a tipo entero
     * @param ipAddress IPv4 en formato "192.168.1.0/24"
     * @return IPv4 convertida a entero.
     */
    public int convertIpAddressToInt(String ipAddress) {
        String[] ip = ipAddress.split("\\.|/");

        int octet1 = Integer.parseInt(ip[0]);
        int octet2 = Integer.parseInt(ip[1]);
        int octet3 = Integer.parseInt(ip[2]);
        int octet4 = Integer.parseInt(ip[3]);

        int ipAddressInt = octet1;
        ipAddressInt = (ipAddressInt << 8) + octet2;
        ipAddressInt = (ipAddressInt << 8) + octet3;
        ipAddressInt = (ipAddressInt << 8) + octet4;

        return ipAddressInt;
    }

    /**
     * Encuentra la primera IP utilizable.
     * @param network IPv4 en formato "192.168.1.0/24"
     * @return IPv4 convertida a entero.
     */
    public int findFirstIp(String network) {
        String[] ip = network.split("/");
        int mask = Integer.parseInt(ip[1]);
        int bitsHost = 32 - mask;
        int address = convertIpAddressToInt(network);
        int firstIp = (address >> bitsHost) << bitsHost;
        return firstIp;
    }

    /**
     *
     * @param neededSize
     * @return
     */
    public int calcCIDRMask(int neededSize) {
        int allowedHost = 0, neededBist = 1;

        do {
            neededBist ++;
            allowedHost = (int) Math.pow(2, neededBist) - 2;
        } while (allowedHost < neededSize);

        return 32 - neededBist;
    }

    /**
     * Calcula la cantidad de hosts utilizables segun la mascara de red.
     * @param mask Mascara de red.
     * @return Cantidad de hosts utilizables
     */
    public int usableHosts(int mask) {
        return (int) Math.pow(2, 32 - mask) - 2;
    }

    /**
     * Calcula el salto de red.
     * @param mask Mascara de red.
     * @param usedOctet Octeto utilizado de la IP.
     * @return Salto de red.
     */
    public int networkJump(int mask, int usedOctet) {

        String[] maskDec = toDecMask(mask).split("\\.");

        return 256 - Integer.parseInt(maskDec[usedOctet - 1]);
    }

    /**
     * Calcula el octeto usado de la IPv4 segun la mascara.
     * @param mask Mascara de red en formato CIDR.
     * @return Octeto usado.
     */
    public int usedOctet(int mask) {
        return (mask >= 1 && mask <= 8) ? 1 :
                (mask >= 9 && mask <= 16) ? 2 :
                        (mask >= 17 && mask <= 24) ? 3 : 4;
    }

    /**
     * Convierte la mascara en formato CIDR a formato decimal.
     * @param maskCIDR Mascara de red en formato CIDR.
     * @return Mascara de red en formato decimal. Ejemplo: "255.255.255.0"
     */
    public String toDecMask(int maskCIDR) {
        if (maskCIDR == 0) {
            return "0.0.0.0";
        }

        int displacedBits = -1 << (32 - maskCIDR);
        return convertIpToQuartet(displacedBits);
    }

    /**
     * Convierte IPv4 a formato decimal punteado. Ejemplo: "192.168.1.0"
     * @param ipAddress IPv4 en formato entero
     * @return IPv4 en formato decimal punteado.
     */
    public String convertIpToQuartet(int ipAddress) {
        int octet1 = (ipAddress >> 24) & 255;
        int octet2 = (ipAddress >> 16) & 255;
        int octet3 = (ipAddress >> 8) & 255;
        int octet4 = ipAddress & 255;
        return octet1 + "." + octet2 + "." + octet3 + "." + octet4;
    }

    /**
     * Convierte IPv4 en formato decimal punteado a binario.
     * @param quartet IPv4 en formato decimal punteado. Ejemplo: "192.168.1.0".
     * @return IPv4 en binario
     */
    public String quartetToBinary(String quartet) {
        String[] ip = quartet.split("\\.|/");

        String octet1 = Integer.toBinaryString((Integer.parseInt(ip[0])));
        String octet2 = Integer.toBinaryString((Integer.parseInt(ip[1])));
        String octet3 = Integer.toBinaryString((Integer.parseInt(ip[2])));
        String octet4 = Integer.toBinaryString((Integer.parseInt(ip[3])));

        String output1 = String.format("%8s", octet1).replace(' ', '0');
        String output2 = String.format("%8s", octet2).replace(' ', '0');
        String output3 = String.format("%8s", octet3).replace(' ', '0');
        String output4 = String.format("%8s", octet4).replace(' ', '0');

        return output1 + "." + output2 + "." + output3 + "." + output4;
    }
}
