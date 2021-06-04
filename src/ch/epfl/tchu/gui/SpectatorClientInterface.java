package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.SpectatorClient;
import javafx.application.Application;
import javafx.stage.Stage;

public class SpectatorClientInterface extends Application {
        public static void main(String[] args){
            launch(args);
        }


    @Override
    public void start(Stage stage) {
        SpectatorClient spectatorClient = new SpectatorClient(new GraphicalSpectator(), "localhost", 5108);
        new Thread(spectatorClient::run).start();

    }

    public static void startSpectator(String hostName, int port) {
        SpectatorClient spectatorClient = new SpectatorClient(new GraphicalSpectator(), hostName, port);
        new Thread(spectatorClient::run).start();
    }
}

