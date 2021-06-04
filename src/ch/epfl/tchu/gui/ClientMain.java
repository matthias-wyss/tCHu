package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.Spectator;
import ch.epfl.tchu.net.RemotePlayerClient;

import ch.epfl.tchu.net.SpectatorProxy;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

//---------- CONTIENT DU BONUS ----------

/**
 * Programme principal de Client de tCHu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class ClientMain extends Application {
    /**
     * @see ClientMain#start(Stage)
     * @param args arguments pour le lancement de la partie dans l'ordre,
     *             le nom d'hôte et le port du serveur sur lequel se connecter
     */
    public static void main(String[] args){
        launch(args);

    }

    /**
     * Crée le programme principal de client permettant de lancer un client qui se connecte à une partie de tCHu à distance
     * et gère l'interface graphique du joueur à distance (local pour l'ordinateur qui exécute ce programme)
     * @param stage fenêtre de l'application JavaFx
     * @throws IllegalArgumentException si l'on passe trop d'arguments (>2) pour le lancement du programme
     */
    @Override
    public void start(Stage stage) {
        List<String> parameters = getParameters().getRaw();
        String hostName = "localhost";
        int port = 5108;
        switch(parameters.size()){
            case 2:
                hostName = parameters.get(0);
                port = Integer.parseInt(parameters.get(1));
                break;
            case 1:
                hostName = parameters.get(0);
                break;
            case 0:
                break;
            default:
                throw new IllegalArgumentException();
        }
        startClient(hostName, port, GraphicalConstants.SPECTATOR_PORT_CLIENT);

    }


    //----------BONUS----------
    public static void startClient(String hostName, int port, int spectatorPort) {
        Player remotePlayer = new GraphicalPlayerAdapter();
        RemotePlayerClient remotePlayerClient = new RemotePlayerClient(remotePlayer, hostName, port);
        new Thread(remotePlayerClient::run).start();
        new Thread(() -> {

            try {
                ServerSocket serverSocket1 = new ServerSocket(spectatorPort);
                while (true) {
                    Socket specSocket = serverSocket1.accept();
                    Spectator spectator = new SpectatorProxy(specSocket);
                    remotePlayer.addSpectator(spectator);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }).start();
    }
}
