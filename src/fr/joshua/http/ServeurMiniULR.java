package fr.joshua.http;

import javax.swing.text.html.MinimalHTMLWriter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

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
    public static void go (int port){
	boolean stop = false;
	Thread server = new Thread(new Runnable(){
		public void run(){
			ServerSocket srvk;
			Integer id_server = nbSessions++;
			try {
			    srvk = new ServerSocket (port);
			    while (!stop){
                    System.out.println("Serveur en attente ("+ id_server +":"+ port +")");

                    Socket sck = srvk.accept ();
                    Thread client = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            DataOutputStream os = null;
                            BufferedReader br = null;
                            try {
                                os = new DataOutputStream(sck.getOutputStream ());
                                br = new BufferedReader(new InputStreamReader
                                        (sck.getInputStream()));
                                traiterRequete(br, os);
                                sck.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }); // Thread client
                    client.start();
			    }
			}catch(IOException e) {
			    System.out.println("ERREUR IO"+ e);
			}
		} // run
	}); //Thread server
	server.start();

    } // go

    // Methode utile pour demander ou non des print de Trace a l'execution
    public static void debug(String s, int n) {
        if ((DEBUG & n) != 0)
            System.out.println("("+n+")"+s);
    } // debug

    public static String lireLigne(String p, BufferedReader br)
            throws IOException {
        String s ;
        s =br.readLine();
        debug(p+" "+s,2);
        return s;
    } // lireLigne

    public static void traiterRequete(BufferedReader br, DataOutputStream dos) throws IOException {


        String line="";
         do{
             line = lireLigne(line,br);
             if(line.startsWith("GET")){
                 if(contentType(line).equals("text/html")){
                     retourFichier(line.split(" ")[1],dos);
                 }
             }
             else if(line.startsWith("POST")){
                 if(contentType(line).equals("text/html")){
                     retourFichier(line.split(" ")[1],dos);
                 }
                 else{
                     retourCGIPOST(line.split(" ")[1],br,dos);
                 }
             }
             else{
                 System.out.println("NO HTTP :(((");
             }
         }
         while (line.length() != 0 && !line.equals("\n\r"));
    } // traiterRequete

    private static void retourFichier(String f,DataOutputStream dos)
            throws IOException {
        if(Files.exists(Paths.get(f))){
            FileInputStream fis = new FileInputStream(f);
            if(fis != null){
                ServeurMiniULR.statusLine = "200";
                ServeurMiniULR.contentTypeLine = "text/html";
//                ServeurMiniULR.

            }
        }
        else{
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
        byte[] buffer = new byte[1024] ;
        int bytes = 0 ;
        while ((bytes = fis.read(buffer)) != -1 ) {
            os.write(buffer, 0, bytes);
        }
        envoi("\r\n",os);
    } // envoiFichier

    private static String executer(String f) throws IOException {
        String R = "";
 /*
 Lance l'execution de la commande "f", et lit toutes les lignes
 qui lui sont retournees par l'execution de cette commande. On
 lit ligne ‡ ligne jusqu'a avoir une valeur de chaine null.
 Toutes ces lignes sont accumulees dans une chaine qui
 est retournee en fin d'execution.
 */
        return R;
    } // executer


    private static void retourCGIPOST(String f, BufferedReader br,
                                      DataOutputStream dos)
            throws IOException {
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
parametre

une chaine qui est la concatenation du nom de fichier, d'un
espace
 et de la chaine de parametres.
 'executer' retourne une chaine qui est la reponse ‡ renvoyer
 au client, apres avoir envoye les infos status,
contentTypeLine, ....
 */
    }
    private static void envoi(String m, DataOutputStream dos)
            throws IOException {
        dos.write(m.getBytes());
    } //envoi

    private static void entete(DataOutputStream dos) throws
            IOException {
        envoi(statusLine+"\r\n",dos);
        envoi(serverLine+"\r\n",dos);
        envoi(contentTypeLine+"\r\n",dos);
        envoi(contentLengthLine+"\r\n",dos);
        envoi("\r\n",dos);
    } // entete

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") ||
                fileName.endsWith(".html")) {
            return "text/html";
        }
        return "";
    } // contentType

    public static void main (String args []) throws IOException {
        go (1234);
        System.out.println("ARRET DU SERVEUR");
    }
}
