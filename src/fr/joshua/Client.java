package fr.joshua;
import java.io.DataOutputStream;
import java.util.Random;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by joshu on 31/01/2018.
 */
public class Client implements Runnable{

    private String adress;
    private int port;
    private int id;
    public Client(int id,String adress, int port) {
        this.adress = adress;
        this.port = port;
        this.id = id;
    }

    @Override
    public void run() {
       //exo1();
        //1
        exo21();
    }

    public void exo1(){
        while(true){
            try {
                Socket sock = new Socket();
                sock.setSoTimeout(2000);
                sock.connect(new InetSocketAddress(adress,port),2000);
                BufferedReader br = new BufferedReader (new InputStreamReader(sock.getInputStream()));
                log("Reçu : " + br.readLine());
                sock.close();
                Thread.sleep(5000);

            } catch (IOException | InterruptedException e) {
                log("Erreur de connexion");
            }
        }
    }
    //Lire et renvoyer
    public void exo21(){
        try {
            Socket sock = new Socket();
            sock.setSoTimeout(5000);

            log("Connexion au serveur ...");
            sock.connect(new InetSocketAddress(adress,port),2000);
            log("Connecté au serveur !");

            DataOutputStream os = new DataOutputStream(sock.getOutputStream());
            BufferedReader br = new BufferedReader (new InputStreamReader(sock.getInputStream()));

            log("Envoi de la chaine");
            os.writeChars("Bonjour " + new Random().nextInt(100) +"\n");

            log("Chaine envoyée !");
            log("Reçu : " + br.readLine());
            sock.close();
            Thread.sleep(5000);

        } catch (IOException | InterruptedException e) {
            log(e.getMessage());
        }
    }

    //Exec une commande
    public void exec(String arg){
        try {
            Socket sock = new Socket();
            sock.setSoTimeout(5000);

            log("Connexion au serveur ...");
            sock.connect(new InetSocketAddress(adress,port),2000);
            log("Connecté au serveur !");

            DataOutputStream os = new DataOutputStream(sock.getOutputStream());
            BufferedReader br = new BufferedReader (new InputStreamReader(sock.getInputStream()));

            log("Envoi de la chaine");
            os.writeBytes(arg+"\n");

            log("Chaine envoyée !");
            log("Reçu : " + br.readLine());
            sock.close();

        } catch (IOException e) {
            log(e.getMessage());
        }
    }

    //Exec avec plusieurs lignes
    public void exec(String[] arg){
        try {
            Socket sock = new Socket();
            sock.setSoTimeout(5000);

            log("Connexion au serveur ...");
            sock.connect(new InetSocketAddress(adress,port),2000);
            log("Connecté au serveur !");

            DataOutputStream os = new DataOutputStream(sock.getOutputStream());
            BufferedReader br = new BufferedReader (new InputStreamReader(sock.getInputStream()));

            for(String s : arg){
                log("Envoi de la chaine " + s);
                os.writeBytes(s+"\n") ;
                log("Chaine envoyée !");
            }
            os.writeBytes("\n");
            log("Reçu : " + br.readLine());
            sock.close();

        } catch (IOException e) {
            log(e.getMessage());
        }
    }
    public void log(String log){
        System.err.println("[CLIENT " + id + "] " + log);
    }
}
