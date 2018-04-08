package fr.joshua;

public class Main {

    public static void main(String[] args) {
        //1
       //repeatClient();

        //onlyClient();
        //multiClientMultiServer();
        //multiServerOnSamePort();

        //2
        //clientWithString();

        //3
        clientWithMultipleLines();




    }

    private static void clientWithString() {
        new Thread(new Serveur(1,64000)).start();
        new Client(1,"localhost",64000).exec("hello server");
        new Client(1,"localhost",64000).exec("finish");
    }

    private static void clientWithMultipleLines() {
        new Thread(new Serveur(1,64000)).start();
        new Client(1,"localhost",64000).exec(new String[]{"Bonjour le monde !","Je suis le super programme","Je suis trop bien"});

    }


    private static void repeatClient(){
        new Thread(new Client(1,"localhost",64000)).start();
        new Thread(new Serveur(1,64000)).start();
    }

    private static void onlyClient(){
        new Thread(new Client(1,"localhost",64000)).start();
    }
    private static void multiClientMultiServer(){
        new Thread(new Serveur(1,64000)).start();
        new Thread(new Serveur(2,64001)).start();
        new Thread(new Client(1,"localhost",64001)).start();
        new Thread(new Client(2,"localhost",64000)).start();
    }
    private static void multiServerOnSamePort(){
        new Thread(new Serveur(1,64000)).start();
        new Thread(new Serveur(2,64000)).start();

    }

}
