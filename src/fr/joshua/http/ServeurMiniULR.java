package fr.joshua.http;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by joshu on 31/01/2018.
 */
public class ServeurMiniULR{
    // constante a positionner pour controler le niveau d'impressions
    // de controle (utilisee dans la methode debug(s,n)
    static final int DEBUG = 255;
    // comptage du nombre de sessions
    static int nbSessions = 0;
    // chaines de caracteres formant la reponse HTTP
    static String serverLine = "Server: Simple Serveur de TP ULR";
    static String statusLine = null;
    static String contentTypeLine = null;
    static String entityBody = null;
    static String contentLengthLine = null;

    // Lancement du serveur
    // Le serveur est en boucle infinie, et ne s'arrete que si il y aune
    // erreur d'Entree/Sortie. Il y a fermeture de socket apres chaque
    // requete.
    public static void go (int port, int ssl_port){
        Thread server = new Thread(new Runnable(){
            public void run(){
                ServerSocket srvk;
                Integer id_server = nbSessions++;
                try {
                    srvk = new ServerSocket(port);
                    System.out.println("Serveur en attente ("+ id_server +":"+ port +")");
                    listen_server(srvk);
                }catch(IOException e) {
                    System.out.println("ERREUR IO"+ e);
                }
            } // run
        }); //Thread server

        Thread ssl_server = new Thread(new Runnable(){

            public void run(){

                Integer id_server = nbSessions++;
                try {
                    java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                    System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

                    SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
                    SSLServerSocket srvk = (SSLServerSocket)sslServerSocketFactory.createServerSocket(ssl_port);

                    System.out.println("Serveur en attente ("+ id_server +":"+ ssl_port +")");

                    listen_server(srvk);
                }catch(IOException e) {
                    System.out.println("ERREUR IO"+ e);
                }
            } // run
        }); //Thread server
        server.start();
        ssl_server.start();
    } // go

    private static void listen_server(ServerSocket srvk) throws IOException {
        boolean stop = false;
        while (!stop){

            Socket sck = srvk.accept();
            Thread client = new Thread(new Runnable(){
                @Override
                public void run() {
                    DataOutputStream os = null;
                    BufferedReader br = null;
                    try {
                        os = new DataOutputStream(sck.getOutputStream());
                        br = new BufferedReader(new InputStreamReader(sck.getInputStream()));
                        traiterRequete(br, os);
                        sck.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }); // Thread client
            client.start();
        }
    }

    // Methode utile pour demander ou non des print de Trace a l'execution
    public static void debug(String s, int n) {
        if ((DEBUG & n) != 0)
            System.out.println("("+n+") - "+s);
    } // debug

    public static String lireLigne(String p, BufferedReader br) throws IOException {
        String s ;
        s =br.readLine();
        debug(s,1);
        return s;
    } // lireLigne

    public static void traiterRequete(BufferedReader br, DataOutputStream dos) throws IOException {

        String http = null;
        String method = null;
        String file = null;
        String line="";
         do{
             line = lireLigne(line,br);
             if(line.startsWith("GET")){
                 method = "GET";
                 file = line.split(" ")[1];
                 if(file.equals("/"))
                     file = "/index.html";
                 http = line.split(" ")[2];
             }else if(line.startsWith("POST")){
                 method = "POST";
                 file = line.split(" ")[1];
                 http = line.split(" ")[2];
             }
         }while (line.length() != 0 && !line.equals("\n\r"));

         if(http == null)
             debug("NOT HTTP :(((", 3);
         else if(method.equals("GET")) {
             retourFichier(file, dos);
         }else if(method.equals("POST")){
             retourCGIPOST(file,br,dos);
         }else if(contentType(file).equals("text/html")){
             retourFichier(file, dos);
         }
    } // traiterRequete

    private static void retourFichier(String f,DataOutputStream dos)
            throws IOException {

        if(Files.exists(Paths.get("./ressources"+ f))){
            debug(Paths.get("./ressources"+ f).toString(), 2);

            FileInputStream fis = new FileInputStream("./ressources"+ f);
            if(fis != null){
                ServeurMiniULR.statusLine = "200";
                ServeurMiniULR.contentTypeLine = contentType(f);
                ServeurMiniULR.contentLengthLine = String.valueOf(fis.available());

                entete(dos);
                envoiFichier(fis, dos);
            }
        }else{
            ServeurMiniULR.statusLine = "404";
            ServeurMiniULR.contentLengthLine = "0";
            ServeurMiniULR.contentTypeLine = "text/html";
            entete(dos);
        }



 /*
 - Si le fichier existe, on prepare les infos status,

 contentType, contentLineLength qui conviennent on les envoit, et
on envoit le fichier (methode envoiFichier)
 - Si le fichier n'existe pas on prepare les infos qui
conviennent
 et on les envoit
 */
    } // retourFichier

    private static void envoiFichier(FileInputStream fis,
                                     DataOutputStream os)
            throws IOException {
        byte[] buffer = new byte[1024];
        int bytes = 0 ;
        while ((bytes = fis.read(buffer)) != -1 ) {
            os.write(buffer, 0, bytes);
        }
        envoi("\r\n",os);
    } // envoiFichier

    private static String executer(String f) throws IOException, InterruptedException {
        String R = "";
        if(Files.exists(Paths.get("./resources"+ f))) {
            Runtime rt = Runtime.getRuntime();
            Process cgi = rt.exec("./resources" + f);
            InputStream out = cgi.getInputStream();

            cgi.waitFor();

            R = new String(out.readAllBytes());
            debug(R, 7);
        }else throw new IOException("No CGI files");

        /*
         Lance l'execution de la commande "f", et lit toutes les lignes
         qui lui sont retournees par l'execution de cette commande. On
         lit ligne ‡ ligne jusqu'a avoir une valeur de chaine null.
         Toutes ces lignes sont accumulees dans une chaine qui
         est retournee en fin d'execution.
         */
        return R;
    } // executer


    private static void retourCGIPOST(String f, BufferedReader br, DataOutputStream dos) throws IOException {

        String res = null;
        try {
            res = executer(f);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        envoi(res, dos);
     /*
     On lit toutes les lignes jusqu'a trouver une ligne commencant
    par Content-Length
     Lorsque cette ligne est trouvee, on extrait le nombre qui suit
    (nombre
     de caracteres a lire).
     On lit une ligne vide
     On lit les caracteres dont le nombre a ete trouve ci-dessus
     on les range dans une chaine,
     On appelle la methode 'executer' en lui donnant comme
     parametre une chaine qui est la concatenation du nom de fichier, d'un
     espace et de la chaine de parametres.
     'executer' retourne une chaine qui est la reponse ‡ renvoyer
     au client, apres avoir envoye les infos status,
    contentTypeLine, ....
     */
    }
    private static void envoi(String m, DataOutputStream dos) throws IOException {
        dos.write(m.getBytes());
    } //envoi

    private static void entete(DataOutputStream dos) throws IOException {
        debug("statusLine: "+ statusLine, 5);
        debug("serverLine: "+ serverLine, 5);
        debug("contentTypeLine: "+ contentTypeLine, 5);
        debug("contentLengthLine: "+ contentLengthLine, 5);

        envoi("HTTP/1.1 " + statusLine+" OK\r\n",dos);
        envoi("Content-Type: "+ contentTypeLine+"\r\n",dos);
        envoi("Content-Length: "+ contentLengthLine+"\r\n",dos);
        envoi("\r\n",dos);
    } // entete

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") ||
                fileName.endsWith(".html")) {
            return "text/html";
        }else if(fileName.endsWith(".css")){
            return "text/css";
        }else if(fileName.endsWith(".png")){
            return "image/png";
        }else if(fileName.endsWith(".jpg")){
            return "image/jpg";
        }else if(fileName.endsWith(".bmp")){
            return "image/bmp";
        }else if(fileName.endsWith(".gif")){
            return "image/gif";
        }else if(fileName.endsWith(".cgi")){
            return "exe/cgi";
        }
        return "";
    } // contentType

    public static void main (String args []) throws IOException {
        go (1234, 4123);
    }
}