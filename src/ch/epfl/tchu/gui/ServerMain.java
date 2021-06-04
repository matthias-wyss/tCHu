package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.RemotePlayerProxy;

import ch.epfl.tchu.net.SpectatorProxy;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

//---------- CONTIENT DU BONUS ----------
/**
 * Programme principal de Serveur de tCHu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class ServerMain extends Application {
    /**
     * @param args arguments pour le lancement d'une partie de tCHu, dans l'ordre :
     *             le nom du premier joueur, le nom du deuxième joueur
     * @see ServerMain#start(Stage)
     */
    public static void main(String[] args) {
        launch(args);

    }

    /**
     * Crée le programme principal de Serveur de tCHu permettant de lancer un serveur qui gère la partie de tCHu
     * et l'interface graphique du premier joueur
     *
     * @param stage fenêtre de l'application JavaFx
     * @throws IllegalArgumentException si l'on passe trop d'arguments (>2) pour le lancement du programme
     */
    @Override
    public void start(Stage stage) {
        List<String> parameters = getParameters().getRaw();
        Map<PlayerId, String> playerNames;
        switch (parameters.size()) {
            case 2:
                playerNames = Map.of(PlayerId.PLAYER_1, parameters.get(0), PlayerId.PLAYER_2, parameters.get(1));
                break;
            case 1:
                playerNames = Map.of(PlayerId.PLAYER_1, parameters.get(0), PlayerId.PLAYER_2, "Charles");
                break;
            case 0:
                playerNames = Map.of(PlayerId.PLAYER_1, "Ada", PlayerId.PLAYER_2, "Charles");
                break;
            default:
                throw new IllegalArgumentException();
        }
        startServer(playerNames, GraphicalConstants.PORT, GraphicalConstants.SPECTATOR_PORT_SERVER);
    }


    //----------BONUS----------
    public static void startServer(Map<PlayerId, String> playerNames, int port, int spectatorPort) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            Player player1 = new GraphicalPlayerAdapter();
            Player player2 = new RemotePlayerProxy(socket);
            Map<PlayerId, Player> players = Map.of(PlayerId.PLAYER_1, player1, PlayerId.PLAYER_2, player2);
            new Thread(() -> Game.play(players, playerNames, SortedBag.of(ChMap.tickets()), new Random())).start();
            new Thread(() -> {

                try {
                    ServerSocket serverSocket1 = new ServerSocket(spectatorPort);
                    while (true) {
                        Socket specSocket = serverSocket1.accept();
                        player1.addSpectator(new SpectatorProxy(specSocket));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



