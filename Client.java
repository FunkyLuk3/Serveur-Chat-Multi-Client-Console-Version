import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    //definition des variables global
    public static BufferedWriter os = null;
    public static BufferedReader is = null;
    public static Socket socketOfClient = null;
    public static Scanner scanner = new Scanner(System.in);
    public static String pseudo;

    public static void main(String[] args) {

        String serverHost;

        // personnification du client
        System.out.println("Entrez votre pseudo (default : User): ");
        pseudo = Client.scanner.nextLine();
        if (pseudo == "") {
            pseudo = "User";
        }
        System.out.println("Entrez l'ip du serveur (default : localhost) : ");
        String ip = Client.scanner.nextLine();
        if (ip == "") {
            serverHost = "127.0.0.1";
        } else {
            serverHost = ip;
        }

        int port = 5000;

        try {
            // connexion au serveur
            Client.socketOfClient = new Socket(serverHost, port);

            Client.os = new BufferedWriter(new OutputStreamWriter(Client.socketOfClient.getOutputStream()));

            Client.is = new BufferedReader(new InputStreamReader(Client.socketOfClient.getInputStream()));

        } catch (UnknownHostException e) {
            System.err.println("Ne connais pas l'host " + serverHost);
            return;
        } catch (IOException e) {
            System.err.println("Ne peut pas récupérer la connection I/O de " + serverHost);
            return;
        }

        try {
            // renvoie d'un message lorque la connexion est bien réussite
            Client.os.write(pseudo + " est arrivé(e) !!!");
            Client.os.newLine();
            Client.os.flush();

            // création d'un Thread pour l'envoi de message
            Thread envoieMessage = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (true) {
                            // on print sur System.err pour éviter le coté bloquant du scanner
                            System.err.print("\033[H\033[2J");
                            System.err.flush();
                            String msg = Client.scanner.nextLine();
                            Client.os.write(pseudo + " : " + msg);
                            Client.os.newLine();
                            Client.os.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            
            // création d'un Thread pour la réception de message
            Thread recoieMessage = new Thread(new Runnable() {
                public void run() {
                    String responseLine;
                    try {
                        while (true) {
                            // tant que l'ont reçoit un message on l'affiche
                            while ((responseLine = Client.is.readLine()) != null) {
                                // on print sur System.err pour éviter le coté bloquant du scanner
                                System.err.println(responseLine);
                                if (responseLine.indexOf("OK") != -1) {
                                    break;
                                }
                            }
                        }

                    } catch (Exception e) {
                    }
                }
            });

            // lancement des Threads
            envoieMessage.start();
            recoieMessage.start();

            Thread.currentThread().wait();

            // fermeture des buffer et de la connexion
            os.close();
            is.close();
            socketOfClient.close();

        } catch (UnknownHostException e) {
            System.err.println("Essaie de connection à un host inconnu: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        } catch (InterruptedException e) {
        }
    }

}