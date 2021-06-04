package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.PlayerId;
import com.dosse.upnp.UPnP;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class Main extends Application {

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        createHomeWindow();
    }


    public static void createHomeWindow() {
        Stage homeStage = new Stage();

        Helper.setLogo(homeStage);

        VBox view = new VBox();
        view.getStylesheets().add("home.css");
        homeStage.initModality(Modality.WINDOW_MODAL);
        homeStage.setTitle("tCHu \u2014 Menu");

        HBox hBoxLogo = new HBox();

        ImageView logo = new ImageView("switzerland.png");
        logo.setFitWidth(100.0);
        logo.setFitHeight(100.0);

        TextFlow textFlowLogo = new TextFlow();
        Label labelLogo = new Label("tCHu");
        labelLogo.setTextFill(Color.RED);
        labelLogo.setId("label-logo");
        textFlowLogo.getChildren().add(labelLogo);

        hBoxLogo.getChildren().add(logo);
        hBoxLogo.getChildren().add(labelLogo);

        HBox hBoxButtons = new HBox();

        Button serverButton = new Button("Créer une partie");
        serverButton.setOnAction(event -> {
            launchCreateAGameWindow(homeStage);
            homeStage.hide();
        });

        Button clientButton = new Button("Rejoindre une partie");
        clientButton.setOnAction(event -> {
            showJoinOrSpectate(homeStage);
            homeStage.hide();
        });

        hBoxButtons.getChildren().add(serverButton);
        hBoxButtons.getChildren().add(clientButton);

        view.getChildren().add(hBoxLogo);
        view.getChildren().add(hBoxButtons);
        homeStage.setScene(new Scene(view));
        homeStage.show();
    }

    private static void showJoinOrSpectate(Stage homeStage) {
        Stage stage = new Stage();

        Helper.setLogo(stage);

        VBox view = new VBox();
        view.getStylesheets().add("home.css");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("tCHu \u2014 Menu");

        TextFlow textFlowInstruction = createInstructionTextFlow("Voulez vous rejoindre une partie en tant que joueur ou spectateur ?\n");

        HBox hBox = new HBox();

        Button goBack = createGoBackButton(stage, homeStage);

        Button playButton = new Button("Jouer");
        playButton.setOnAction(event -> {
            launchJoinAGameWindow(stage);
            stage.hide();
        });

        Button spectateButton = new Button("Regarder");
        spectateButton.setOnAction(event -> {
            launchSpectateAGameWindow(stage);
            stage.hide();
        });

        hBox.getChildren().addAll(goBack, playButton, spectateButton);

        view.getChildren().addAll(textFlowInstruction, hBox);

        stage.setScene(new Scene(view));
        stage.show();
    }

    private static void launchCreateAGameWindow(Stage homeStage) {

        Stage stage = new Stage();

        Helper.setLogo(stage);

        VBox view = new VBox();
        view.getStylesheets().add("create-game.css");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("tCHu \u2014 Créer une partie");

        TextFlow textFlowInstruction = createInstructionTextFlow("Veuillez remplir les cases ci-dessous :\n");

        TextField textFieldPlayer1 = new TextField();
        HBox hBoxPlayer1 = createHBox("Votre nom* : ", textFieldPlayer1);

        TextField textFieldPlayer2 = new TextField();
        HBox hBoxPlayer2 = createHBox("Nom de l'adversaire* : ", textFieldPlayer2);

        TextField textFieldPortServer = new TextField();
        HBox hBoxPortServer = createHBoxPort(stage, "Port à utiliser* : ", textFieldPortServer, "port-info.txt");

        TextField textFieldObservationPortServer = new TextField();
        HBox hBoxObservationPortServer = createHBoxPort(stage, "Port d'observation : ", textFieldObservationPortServer, "port-spectator-info.txt");

        HBox bottomHBox = new HBox();

        Button goBack = createGoBackButton(stage, homeStage);

        Button serverButton = new Button("Créer la partie");
        serverButton.setOnAction(event -> {
            String player1 = textFieldPlayer1.getText();
            String player2 = textFieldPlayer2.getText();
            String portString = textFieldPortServer.getText();
            String observationPortString = textFieldObservationPortServer.getText();
            Map<PlayerId, String> playerNames = Map.of(PlayerId.PLAYER_1, player1, PlayerId.PLAYER_2, player2);
            if(!player1.equals("") && !player2.equals("") && !portString.equals("")) {
                try {
                    int port = Integer.parseInt(portString);
                    int observationPort = observationPortString.isBlank() ? GraphicalConstants.SPECTATOR_PORT_SERVER : Integer.parseInt(observationPortString);
                    if(port >= 1024 && port <= 65535) {
                        new Thread(() -> ServerMain.startServer(playerNames, port, observationPort)).start();
                        Platform.setImplicitExit(false);
                        stage.hide();
                        homeStage.show();
                        showInformationAndIPAddress(homeStage, port);
                    } else {
                        Helper.showError(stage, "Port incorrect", "incorrect-port-error.txt");
                    }
                } catch (NumberFormatException e) {
                    Helper.showError(stage, "Port incorrect", "incorrect-port-error.txt");
                }
            } else {
                Helper.showError(stage, "La partie n'a pas pu être créée", "game-cannot-be-created-error.txt");
            }
        });

        bottomHBox.getChildren().addAll(goBack, serverButton);

        view.getChildren().addAll(textFlowInstruction, hBoxPlayer1, hBoxPlayer2, hBoxPortServer, hBoxObservationPortServer, bottomHBox);

        stage.setScene(new Scene(view));
        stage.show();

    }

    private static void launchJoinAGameWindow(Stage homeStage) {

        Stage stage = new Stage();

        Helper.setLogo(stage);

        VBox view = new VBox();
        view.getStylesheets().add("join-game.css");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("tCHu \u2014 Rejoindre une partie");

        TextFlow textFlowInstruction = createInstructionTextFlow("Veuillez remplir les cases ci-dessous :\n");

        TextField textFieldHostName = new TextField();
        HBox hBoxHostName = createHBox("Adresse IP de l'adversaire* : ", textFieldHostName);

        TextField textFieldPortClient = new TextField();
        HBox hBoxPortClient = createHBox("Port utilisé* : ", textFieldPortClient);

        TextField textFieldObservationPortClient = new TextField();
        HBox hBoxObservationPortClient = createHBoxPort(stage, "Port d'observation : ", textFieldObservationPortClient, "port-spectator-info.txt");

        HBox bottomHBox = new HBox();

        Button goBack = createGoBackButton(stage, homeStage);

        Button clientButton = new Button("Rejoindre la partie");
        clientButton.setOnAction(event -> {
            String hostName = textFieldHostName.getText();
            String portString = textFieldPortClient.getText();
            String observationPortString = textFieldObservationPortClient.getText();
            if(!hostName.equals("") && !portString.equals("")) {
                try {
                    int port = Integer.parseInt(portString);
                    int observationPort = observationPortString.isBlank() ? GraphicalConstants.SPECTATOR_PORT_CLIENT : Integer.parseInt(observationPortString);
                    if (port >= 1024 && port <= 65535) {
                        new Thread(() -> ClientMain.startClient(hostName, port, observationPort)).start();
                        Platform.setImplicitExit(false);
                        stage.hide();
                    } else {
                        Helper.showError(stage, "Port incorrect", "incorrect-port-error.txt");
                    }
                } catch (NumberFormatException e){
                    Helper.showError(stage, "Port incorrect", "incorrect-port-error.txt");
                }
            } else {
                Helper.showError(stage, "La partie n'a pas pu être recherchée", "game-cannot-be-searched-error.txt");
            }
        });

        bottomHBox.getChildren().addAll(goBack, clientButton);

        view.getChildren().addAll(textFlowInstruction, hBoxHostName, hBoxPortClient, hBoxObservationPortClient, bottomHBox);

        stage.setScene(new Scene(view));
        stage.show();
    }

    private static void launchSpectateAGameWindow(Stage homeStage) {

        Stage stage = new Stage();

        Helper.setLogo(stage);

        VBox view = new VBox();
        view.getStylesheets().add("join-game.css");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("tCHu \u2014 Regarder une partie");

        TextFlow textFlowInstruction = createInstructionTextFlow("Veuillez remplir les cases ci-dessous :\n");

        TextField textFieldHostName = new TextField();
        HBox hBoxHostName = createHBox("Adresse IP du joueur à regarder* : ", textFieldHostName);

        TextField textFieldPortSpectator = new TextField();
        HBox hBoxPortSpectator = createHBox("Port d'observation* : ", textFieldPortSpectator);

        HBox bottomHBox = new HBox();

        Button goBack = createGoBackButton(stage, homeStage);

        Button spectatorButton = new Button("Regarder la partie");
        spectatorButton.setOnAction(event -> {
            String hostName = textFieldHostName.getText();
            String portString = textFieldPortSpectator.getText();
            if(!hostName.equals("") && !portString.equals("")) {
                try {
                    int port = Integer.parseInt(portString);
                    if (port >= 1024 && port <= 65535) {
                        new Thread(() -> SpectatorClientInterface.startSpectator(hostName, port)).start();
                    } else {
                        Helper.showError(stage, "Port incorrect", "incorrect-port-error.txt");
                    }
                } catch (NumberFormatException e){
                    Helper.showError(stage, "Port incorrect", "incorrect-port-error.txt");
                }
            } else {
                Helper.showError(stage, "La partie n'a pas pu être recherchée", "game-cannot-be-searched-error.txt");
            }
        });

        bottomHBox.getChildren().addAll(goBack, spectatorButton);

        view.getChildren().addAll(textFlowInstruction, hBoxHostName, hBoxPortSpectator, bottomHBox);

        stage.setScene(new Scene(view));
        stage.show();
    }

    private static void showInformationAndIPAddress(Stage homeStage, int chosenPort) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(homeStage);
        alert.setTitle("tCHu \u2014 Information");
        alert.setHeaderText("La partie a été créée");
        try {
            StringBuilder sB = new StringBuilder(Objects.requireNonNull(Helper.txtReader("game-created-info.txt")));
            sB.append("\n\n");
            sB.append("Votre addresse IP locale : ").append(Helper.getPrivateIP()).append("\n");
            sB.append("Votre addresse IP externe : ").append(Helper.getPublicIP()).append("\n");
            sB.append("Le port choisi : ").append(Integer.valueOf(chosenPort));
            alert.setContentText(sB.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        alert.show();

        Game.hasGameStarted().addListener(c -> Platform.runLater(() -> {
            alert.hide();
            homeStage.hide();
        }));

    }

    //-----A SUPPRIMER-----
    private static void askToOpenPort(int port) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("tCHu \u2014 Information");
        alert.setHeaderText("Voulez vous ouvrir ce port automatiquement ?");
        String text = Helper.txtReader("ask-open-port-info.txt");
        alert.setContentText(text);
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            UPnP.openPortTCP(port);
        }
    }

    private static Button createTestPortButton(Stage stage, TextField textField) {
        Button portTest = new Button("Tester");
        portTest.setOnAction(event -> {
            String portString = textField.getText();
            try {
                int port = Integer.parseInt(portString);
                if(port >= 1024 && port <= 65535) {
                    if(!UPnP.isMappedTCP(port)) {
                        if(UPnP.isUPnPAvailable()) {
                            askToOpenPort(port);
                        } else {
                            Helper.showInformation(stage, "UPnP non disponible", "no-upnp-info.txt");
                        }
                    } else {
                        Helper.showInformation(stage, "Ce port est bien ouvert", "port-opened-info.txt");
                    }
                } else {
                    Helper.showError(stage, "Port incorrect", "incorrect-port-error.txt");
                }
            } catch (NumberFormatException e) {
                Helper.showError(stage, "Port incorrect", "incorrect-port-error.txt");
            }
        });
        return portTest;
    }

    //---------------------

    private static HBox createHBoxPort(Stage stage, String text, TextField textField, String file) {
        HBox hBox = new HBox();
        TextFlow textFlowPort = new TextFlow();
        Text textPort = new Text(text);
        textFlowPort.getChildren().add(textPort);
        ImageView informationLogo = new ImageView("information.png");
        informationLogo.setFitHeight(18.0);
        informationLogo.setFitWidth(18.0);
        Button informationButton = new Button();
        informationButton.setGraphic(informationLogo);
        informationButton.setOnAction(event -> Helper.showInformation(stage, "Comment choisir mon port ?", file));

        //-----A SUPPRIMER-----
        Button portTest = createTestPortButton(stage, textField);
        //---------------------

        hBox.getChildren().add(textFlowPort);
        hBox.getChildren().add(textField);
        hBox.getChildren().add(informationButton);

        //-----A SUPPRIMER-----
        hBox.getChildren().add(portTest);
        //---------------------

        return hBox;
    }

    private static Button createGoBackButton(Stage stageToHide, Stage stageToShow) {
        Button goBack = new Button("Retour");
        goBack.setOnAction(event -> {
            stageToHide.hide();
            stageToShow.show();
        });
        return goBack;
    }

    private static TextFlow createInstructionTextFlow(String instruction) {
        TextFlow textFlowInstruction = new TextFlow();
        textFlowInstruction.setId("instruction");
        Text textInstruction = new Text(instruction);
        textFlowInstruction.getChildren().add(textInstruction);
        return textFlowInstruction;
    }

    private static HBox createHBox(String string, TextField textField) {
        HBox hBox = new HBox();
        TextFlow textFlow = new TextFlow();
        Text text = new Text(string);
        textFlow.getChildren().add(text);
        hBox.getChildren().add(textFlow);
        hBox.getChildren().add(textField);
        return hBox;
    }

}