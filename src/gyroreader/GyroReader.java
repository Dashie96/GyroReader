/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gyroreader;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 *
 * @author eslaweski
 */
public class GyroReader implements SerialPortEventListener
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException
    {
        GyroReader main = new GyroReader();
        main.setPortName("COM6");
        main.initialize();
        Thread t = new Thread();
        Thread.sleep(4000);
        System.out.println("Started");
    }
        SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private String portName = "";
    /**
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private BufferedReader input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 57600;

    public void initialize()
    {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }  
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }
        else {
            System.out.println("Port ID: " + portId);
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     *
     * @param oEvent
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent)
    {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = input.readLine();
                
                String wordOne = inputLine.charAt(2) + "" + 
                        inputLine.charAt(3) + "" + inputLine.charAt(4) + "" + 
                        inputLine.charAt(5);
                int decimalX = Integer.parseInt(wordOne, 16);
                if(decimalX > 32767)
                    decimalX -= 65536;
                
                String wordTwo = inputLine.charAt(9) +""+ inputLine.charAt(10)
                        +""+ inputLine.charAt(11) +""+ inputLine.charAt(12);
                int decimalY = Integer.parseInt(wordTwo, 16);
                if(decimalY > 32767)
                    decimalY -= 65536;
                
                String wordThree = inputLine.charAt(16) +""+ inputLine.charAt(17)
                        +""+ inputLine.charAt(18) +""+ inputLine.charAt(19);
                int decimalZ = Integer.parseInt(wordThree, 16);
                if(decimalZ > 32767)
                    decimalZ -= 65536;
                
                double doubX= (double)decimalX/261;
                double doubY= (double)decimalY/257;
                double doubZ= (double)decimalZ/234;
                System.out.println("X is: " + doubX + "\nY is: " + doubY + "\nZ is: " + doubZ);
            }
            catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public void send(String string)
    {
        try {
            System.out.println("Output bytes: " + string);
            output.write(string.getBytes());
        }
        catch (IOException e) {
        }
    }
    
    public void setPortName(String name)
    {
        portName = name;
    }    
}
