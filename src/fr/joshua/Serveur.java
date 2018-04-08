package fr.joshua;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Executable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by joshu on 31/01/2018.
 */
public class Serveur implements Runnable {
    int port;
    int nbReq;
    int id;

    public Serveur(int id, int port) {
        this.port = port;
        nbReq = 0;
        this.id = id;
    }

    @Override
    public void run() {
      // exo1();
        //1
        //exo21();
        //3
         exo23();
    }

    public void exo1(){
        Socket sock;
        DataOutputStream os;
        try {
            ServerSocket ss = new ServerSocket(port);
            while(true){
                log("En attente de client ...");
                sock = ss.accept();
                log("Client trouvé !");
                os = new DataOutputStream(sock.getOutputStream());
                os.writeChars(++nbReq + " "+InetAddress.getLocalHost());
                log(nbReq+" envoyé !");
                sock.close();

            }
        } catch (IOException e) {
            log(e.getMessage());
        }

    }

    //Lire et renvoyer + reagir
    public void exo21(){
        Socket sock;
        DataOutputStream os;
        try {
            ServerSocket ss = new ServerSocket(port);
             boolean finish = false;
            while(!finish){
                log("En attente de client ...");
                sock = ss.accept();
                log("Client trouvé !");
                BufferedReader br = new BufferedReader (new InputStreamReader(sock.getInputStream()));
                os = new DataOutputStream(sock.getOutputStream());
                log("En attente du message client");
                String s =  br.readLine();
                log("Message client reçu (" + s+")");

                finish = s.equalsIgnoreCase( "finish");
                os.writeChars("ECHO "+ s );

                sock.close();


            }
            log("Arrêt du serveur.");
        } catch (IOException e) {
            //log(e.getMessage());
            e.printStackTrace();
        }
    }

    //Lire plusieurs lignes et renvoyer
    public void exo23(){
        Socket sock;
        DataOutputStream os;
        try {
            ServerSocket ss = new ServerSocket(port);
            boolean finish = false;
            while(!finish){
                log("En attente de client ...");
                sock = ss.accept();
                log("Client trouvé !");
                BufferedReader br = new BufferedReader (new InputStreamReader(sock.getInputStream()));
                os = new DataOutputStream(sock.getOutputStream());

                StringBuilder sb = new StringBuilder();
                String s;
                do{
                    s =  br.readLine();
                    sb.append(s);
                    log(s);
                }
                while (s.length()!=0);

                System.out.println("Fin, envoi du recap");
                try{
                    os.writeChars("ECHO "+ sb.toString());
                }
                catch (SocketException e){
                    System.out.println("Erreur lors de l'envoi");
                }

                sock.close();
            }
            log("Arrêt du serveur.");
        } catch (IOException e) {
            //log(e.getMessage());
            e.printStackTrace();
        }
    }
    public void log(String log){
        System.out.println("[SERVER "+id + "] " + log);
    }
}
