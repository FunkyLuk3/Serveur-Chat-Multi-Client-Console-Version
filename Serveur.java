import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

    // Chat
    static String chat = "";
    // upTpDate : permet de savoir si le chat est mise à jour
    static boolean upToDate = true;

    public static synchronized boolean getUpToDate() {
        return upToDate;
    }

    public static void main(String args[]) throws IOException {

        // definition des variables du serveur
        ServerSocket listener = null;

        System.out.println("En attente de client...");
        int clientNumber = 0;

        try {
            // lancement de l'écoute sur le port 5000
            listener = new ServerSocket(5000);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            while (true) {
                // tant que le serveur fonctionne on accept les clients qui s'y connect
                Socket socketOfServer = listener.accept();
                // création du Thread de gestion d'un client => un par client
                new ServiceThread(socketOfServer, clientNumber++).start();
            }
        } finally {
            listener.close();
        }

    }

    // affiche les logs dans le serveur
    private static void log(String message) {
        System.out.println(message);
    }

    private static class ServiceThread extends Thread {
        // création des données importantes du client pour le serveur
        private int clientNumber;
        private Socket socketOfServer;
        BufferedReader is = null;
        BufferedWriter os = null;

        // constructeur de la classe ServiceThread
        public ServiceThread(Socket socketOfServer, int clientNumber) throws IOException {
            this.clientNumber = clientNumber;
            this.socketOfServer = socketOfServer;

            // les buffer pour écrire et écouter le client
            this.is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            this.os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            // Log
            log("Nouvelle connection client# " + this.clientNumber + " à " + socketOfServer);
        }

        @Override
        public void run() {

            boolean listening = true;
            try {

                // appel de la class Thread chatUpdate
                chatUpdate chatUp = new chatUpdate(this.os);
                // démarrage de chatUpdate
                chatUp.start();

                // tant que le serveur écoute on lui fait afficher les messages échangés
                while (listening) {
                    String line = is.readLine();
                    System.out.println("Message reçu de " + this.clientNumber + " : " + line);
                    chat += line + "\n";
                    upToDate = false;

                    // Si QUIT est entrer par un utilisateur il ne peut plus écrire de message
                    if (line.equals("QUIT")) {
                        os.write("Deconnexion de " + this.clientNumber);
                        os.write("OK");
                        os.newLine();
                        os.flush();
                        listening = false;
                    }
                }

            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
            this.interrupt();
        }
    }

    // class chetUpdate permet de mettre a jour le chat de tous les client
    private static class chatUpdate extends Thread {

        private BufferedWriter out = null;

        // constructeur de la class
        public chatUpdate(BufferedWriter out) {
            this.out = out;
        }

        public void run() {
            while (true) {
                // quand le chat est update on l'envoie au client
                if (!getUpToDate()) {
                    try {
                        this.out.write("\033[H\033[2J");
                        this.out.newLine();
                        this.out.flush();
                        this.out.write(chat);
                        this.out.newLine();
                        this.out.flush();
                        System.out.println("message");
                        upToDate = true;
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }
    }

}
