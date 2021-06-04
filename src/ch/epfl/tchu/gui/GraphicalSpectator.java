package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static javafx.application.Platform.runLater;

public class GraphicalSpectator implements Spectator {

    private ObservableGameState gameState;
    private ObservableList<Text> infoList;
    private Stage primaryStage;

    public GraphicalSpectator() {
        runLater(() -> {
            infoList = FXCollections.observableArrayList();
            primaryStage = new Stage();
        });
    }

    @Override
    public void initSpectator(PlayerId playerId, Map<PlayerId, String> playerNames) {
        gameState = new ObservableGameState(playerId);
        runLater(() -> createSpectatorWindow(playerId, playerNames));
    }

    private void createSpectatorWindow(PlayerId id, Map<PlayerId, String> playerNames) {
        Node mapView = MapViewCreator
                .createMapView(gameState, new SimpleObjectProperty<>(), null);
        Node cardsView = DecksViewCreator
                .createCardsView(gameState, new SimpleObjectProperty<>(), new SimpleObjectProperty<>());
        Node handView = DecksViewCreator
                .createHandView(gameState);
        Node infoView = InfoViewCreator
                .createInfoView(id, playerNames, gameState, infoList, new SimpleObjectProperty<>());

        BorderPane mainPane = new BorderPane(mapView, null, cardsView, handView, infoView);

        primaryStage.setScene(new Scene(mainPane));

        primaryStage.setTitle("tCHu \u2014 Spectateur (" + playerNames.get(id)+")");

        Helper.setLogo(primaryStage);

        runLater(() -> Helper.showInformation(primaryStage, "Veuillez patienter", "waiting.txt"));
    }

    @Override
    public void setState(PublicGameState gameState, PlayerState playerState) {
        this.gameState.setState(gameState, playerState);
        runLater(() -> primaryStage.show());
    }

    @Override
    public void receiveInfo(String info) {
        runLater(() -> {
            infoList.add(new Text(info + "\n"));
            if(infoList.size() > Constants.MAX_DISPLAYED_MESSAGES) {
                infoList.remove(0);
            }});

    }

}