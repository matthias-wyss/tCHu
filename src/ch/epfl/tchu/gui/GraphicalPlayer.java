package ch.epfl.tchu.gui;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import java.util.*;
import java.util.List;

import static javafx.application.Platform.isFxApplicationThread;

//---------- CONTIENT DU BONUS ----------
/**
 * Classe représentant l'interface graphique d'un joueur de tCHu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class GraphicalPlayer {

    private final PlayerId id;
    private final Map<PlayerId, String> playerNames;
    private final ObservableGameState gameState;
    private final ObjectProperty<ActionHandlers.DrawTicketsHandler> dTHandler;
    private final ObjectProperty<ActionHandlers.DrawCardHandler> dCHandler;
    private final ObjectProperty<ActionHandlers.ClaimRouteHandler> cRHandler;
    private final ObservableList<Text> infoList;
    private final Stage primaryStage;

    //----------BONUS----------
    private final List<Spectator> spectators;
    //-------------------------

    /**
     * Construit l'interface graphique d'un joueur
     * @param id l'ID du joueur pour lequel on construit l'interface
     * @param playerNames table associative des joueurs et de leurs noms
     */
    public GraphicalPlayer(PlayerId id, Map<PlayerId, String> playerNames) {
        assert isFxApplicationThread();
        this.id = id;
        this.playerNames = playerNames;
        gameState = new ObservableGameState(id);
        dTHandler = new SimpleObjectProperty<>();
        dCHandler = new SimpleObjectProperty<>();
        cRHandler = new SimpleObjectProperty<>();
        infoList = FXCollections.observableArrayList();
        primaryStage = new Stage();
        createPlayerWindow(primaryStage);

        //----------BONUS----------
        spectators = new ArrayList<>();
        //-------------------------

    }
    //----------BONUS----------
    public void addSpectator(Spectator spectator){
        spectators.add(spectator);
        spectator.initSpectator(id, playerNames);
    }
    //-------------------------

    /**
     * Modifie l'état du joueur
     * @param pGS état public de la partie
     * @param pS état complet du joueur
     */
    public void setState(PublicGameState pGS, PlayerState pS) {
        assert isFxApplicationThread();
        gameState.setState(pGS, pS);

        //----------BONUS----------
        spectators.forEach(s -> s.setState(pGS, pS));
        //-------------------------
    }

    /**
     * Est appelée chaque fois qu'une information doit être communiquée au joueur courant de la partie
     * @param infoString information que l'on souhaite communiquer au joueur courant
     */
    public void receiveInfo(String infoString){
        assert isFxApplicationThread();
        infoList.add(new Text(infoString + "\n"));
        if(infoList.size() > Constants.MAX_DISPLAYED_MESSAGES) {
            infoList.remove(0);
        }

        //----------BONUS----------
        spectators.forEach(s -> s.receiveInfo(infoString));
        //-------------------------
    }

    /**
     * Commence un nouveau tour pour le joueur, modifiant les gestionnaires d'action en accord avec les
     * actions que le joueur peut effectuer actuellement, lui permettant d'intéragir (ou non) avec certains composants
     * de l'inteface.
     * @param drawTicketsHandler gestionnaire d'action pour la pioche de billets
     * @param drawCardsHandler gestionnaire d'action pour la pioche de cartes
     * @param claimRouteHandler gestionnaire d'action pour la prise de route
     */
    public void startTurn(ActionHandlers.DrawTicketsHandler drawTicketsHandler, ActionHandlers.DrawCardHandler drawCardsHandler, ActionHandlers.ClaimRouteHandler claimRouteHandler) {
        assert isFxApplicationThread();
        ActionHandlers.DrawTicketsHandler newTicketHandler = () -> {
            drawTicketsHandler.onDrawTickets();
            dTHandler.setValue(null);
            dCHandler.setValue(null);
            cRHandler.setValue(null);
        };

        ActionHandlers.DrawCardHandler newCardHandler = (x) -> {
            drawCardsHandler.onDrawCard(x);
            dTHandler.setValue(null);
            dCHandler.setValue(null);
            cRHandler.setValue(null);
        };
        ActionHandlers.ClaimRouteHandler newClaimRouteHandler = (x,y) -> {
            claimRouteHandler.onClaimRoute(x,y);
            dTHandler.setValue(null);
            dCHandler.setValue(null);
            cRHandler.setValue(null);
        };
        if(gameState.canDrawTickets().get() && gameState.canDrawCards().get()){
            dTHandler.setValue(newTicketHandler);
            dCHandler.setValue(newCardHandler);
            cRHandler.setValue(newClaimRouteHandler);
        }
        else if(gameState.canDrawTickets().get()) {
            dTHandler.setValue(newTicketHandler);
            dCHandler.setValue(null);
            cRHandler.setValue(newClaimRouteHandler);
        } else if(gameState.canDrawCards().get()){
            dTHandler.setValue(null);
            dCHandler.setValue(newCardHandler);
            cRHandler.setValue(newClaimRouteHandler);
        } else {
            dTHandler.setValue(null);
            dCHandler.setValue(null);
            cRHandler.setValue(newClaimRouteHandler);
        }
    }

    /**
     * Crée l'interface permettant au joueur de choisir un ou plusieurs billets
     * @param ticketsBag ensemble trié de billets parmi lesquels choisir
     * @param ticketChoiceHandler gestionnaire de choix de billets
     */
    public void chooseTickets(SortedBag<Ticket> ticketsBag, ActionHandlers.ChooseTicketsHandler ticketChoiceHandler){
        assert isFxApplicationThread();
        Preconditions.checkArgument(ticketsBag.size() == Constants.IN_GAME_TICKETS_COUNT || ticketsBag.size() == Constants.INITIAL_TICKETS_COUNT);
        createTicketsChooser(primaryStage, ticketsBag.toList(), ticketChoiceHandler);
    }

    /**
     * Autorise le joueur à tirer une seconde carte après en avoir tiré une depuis la pioche (visible ou non) de cartes
     * @param drawCardsHandler gestionnaire d'action pour la pioche de cartes
     */
    public void drawCard(ActionHandlers.DrawCardHandler drawCardsHandler) {
        assert isFxApplicationThread();
        ActionHandlers.DrawCardHandler newDrawCardHandler = (x) -> {
            drawCardsHandler.onDrawCard(x);
            dTHandler.setValue(null);
            cRHandler.setValue(null);
            dCHandler.setValue(null);
        };
        dCHandler.setValue(newDrawCardHandler);

    }

    /**
     * Crée l'interface permettant au joueur de choisir un ensemble de cartes <i>initiales</i> pour la prise d'une route
     * @param initialCards liste d'ensembles choix de cartes initiales
     * @param cardsChoiceHandler gestionnaire de choix d'ensemble de cartes
     */
    public void chooseClaimCards(List<SortedBag<Card>> initialCards, ActionHandlers.ChooseCardsHandler cardsChoiceHandler){
        assert isFxApplicationThread();
        createCardsChooser(primaryStage, initialCards, cardsChoiceHandler, false);
    }

    /**
     * Crée l'interface permettant au joueur de choisir (ou non) un ensemble de cartes <i>additionnelles</i> pour la
     * prise de possession d'une route
     * @param possibleAdditionalCards liste d'ensembles de cartes additionnelles possibles
     * @param cardsChoiceHandler gestionnaire de choix d'ensemble de carte
     */
    public void chooseAdditionalCards(List<SortedBag<Card>> possibleAdditionalCards, ActionHandlers.ChooseCardsHandler cardsChoiceHandler) {
        assert isFxApplicationThread();
        createCardsChooser(primaryStage, possibleAdditionalCards, cardsChoiceHandler, true);
    }



    private void createPlayerWindow(Stage primaryStage) {
        Node mapView = MapViewCreator
                .createMapView(gameState, cRHandler, (x,y) -> {
                    this.chooseClaimCards(x,y);
                    dTHandler.setValue(null);
                    dCHandler.setValue(null);
                });
        Node cardsView = DecksViewCreator
                .createCardsView(gameState, dTHandler, dCHandler);
        Node handView = DecksViewCreator
                .createHandView(gameState);
        Node infoView = InfoViewCreator
                .createInfoView(id, playerNames, gameState, infoList, new SimpleObjectProperty<>(this));

        BorderPane mainPane = new BorderPane(mapView, null, cardsView, handView, infoView);

        primaryStage.setScene(new Scene(mainPane));

        primaryStage.setTitle("tCHu \u2014 " + playerNames.get(id));

        //----------BONUS----------
        Helper.setLogo(primaryStage);
        //-------------------------

        primaryStage.show();
    }




    private void createTicketsChooser(Stage primaryStage, List<Ticket> tickets, ActionHandlers.ChooseTicketsHandler ticketChoiceHandler) {
        ListView<Ticket> listView = new ListView<>();
        boolean chooseMultiple = tickets.size() == Constants.INITIAL_TICKETS_COUNT;
        if(chooseMultiple) {
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }
        listView.getItems().addAll(tickets);
        Button button = new Button(StringsFr.CHOOSE);
        int minTicketNumber = tickets.size() - Constants.DISCARDABLE_TICKETS_COUNT;
        String messageString = String.format(StringsFr.CHOOSE_TICKETS, minTicketNumber, StringsFr.plural(minTicketNumber));
        //-----BONUS-----
        if(chooseMultiple) messageString += StringsFr.MULTIPLE_SELECTION;
        //---------------
        Stage ticketChooser = createChooserGraphScene(primaryStage, StringsFr.TICKETS_CHOICE, messageString, listView,
                minTicketNumber, button);
        ticketChooser.show();
        ticketChooser.setOnCloseRequest(Event::consume);
        button.setOnAction(event -> {
            ticketChooser.hide();
            List<Ticket> chosenTickets = chooseMultiple ? listView.getSelectionModel().getSelectedItems() : Collections.singletonList(listView.getSelectionModel().getSelectedItem());
            ticketChoiceHandler.onChooseTickets(SortedBag.of(chosenTickets));
        });
    }

    private void createCardsChooser(Stage primaryStage, List<SortedBag<Card>> sortedBags, ActionHandlers.ChooseCardsHandler cardChoiceHandler, boolean isAdditionalCards) {
        ListView<SortedBag<Card>> listView = new ListView<>();
        listView.setCellFactory(v ->
                new TextFieldListCell<>(new CardStringBagConverter()));
        listView.getItems().addAll(sortedBags);
        Button button = new Button(StringsFr.CHOOSE);
        Stage cardChooser = createChooserGraphScene(primaryStage, StringsFr.CARDS_CHOICE, isAdditionalCards ? StringsFr.CHOOSE_ADDITIONAL_CARDS : StringsFr.CHOOSE_CARDS, listView,
                isAdditionalCards ? Constants.MIN_CHOSEN_ADDITIONAL_CARDS : Constants.MIN_CHOSEN_INITIAL_CARDS, button);
        cardChooser.show();
        cardChooser.setOnCloseRequest(Event::consume);
        button.setOnAction(event -> {
            cardChooser.hide();
            SortedBag<Card> chosenAdditionallyCards = listView.getSelectionModel().isEmpty() ? SortedBag.of() : listView.getSelectionModel().getSelectedItem();
            cardChoiceHandler.onChooseCards(chosenAdditionallyCards);
        });
    }


    private static class CardStringBagConverter extends StringConverter<SortedBag<Card>> {
        @Override
        public String toString(SortedBag<Card> cards) {
            return Info.formatCards(cards);
        }

        @Override
        public SortedBag<Card> fromString(String s) {
            throw new UnsupportedOperationException();

        }
    }

    private <T> Stage createChooserGraphScene(Stage primaryStage, String name, String introduction, ListView<T> listView, int minValue, Button button) {
        Stage chooserStage = new Stage(StageStyle.UTILITY);
        chooserStage.initOwner(primaryStage);
        chooserStage.initModality(Modality.WINDOW_MODAL);
        VBox vBox = new VBox();
        TextFlow textFlow = new TextFlow();
        button.disableProperty().bind(Bindings.size(listView.getSelectionModel().getSelectedItems()).lessThan(minValue));
        Text text = new Text(introduction);
        textFlow.getChildren().add(text);
        vBox.getChildren().add(textFlow);
        vBox.getChildren().add(listView);
        vBox.getChildren().add(button);
        Scene chooser = new Scene(vBox);
        chooser.getStylesheets().add("chooser.css");
        chooserStage.setScene(chooser);
        chooserStage.setTitle(name);

        return chooserStage;
    }



    //----------BONUS----------
    public Player.TurnKind showEndScreen(String text) {
        ButtonType playAgain = new ButtonType("Rejouer");
        ButtonType leaveGame = new ButtonType("Quitter");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, playAgain, leaveGame);
        alert.setTitle("tCHu \u2014 Information");
        alert.setHeaderText("La partie est terminée");
        alert.setContentText(text);
        ImageView logo = new ImageView("switzerland.png");
        logo.setFitWidth(50);
        logo.setFitHeight(50);
        alert.setGraphic(logo);

        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == playAgain) {
            return Player.TurnKind.PLAY_AGAIN;
        } else {
            alert.hide();
            primaryStage.hide();
            return Player.TurnKind.QUIT;
        }
    }

    public void showLeaveGameConfirmation() {
        ButtonType cancel = new ButtonType("Annuler");
        ButtonType leaveGame = new ButtonType("Quitter");
        String text = "Voulez vous vraiment quitter la partie ?";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, cancel, leaveGame);
        alert.setTitle("tCHu \u2014 Information");
        alert.setHeaderText("Quitter la partie");
        alert.setContentText(text);
        ImageView logo = new ImageView("switzerland.png");
        logo.setFitWidth(50);
        logo.setFitHeight(50);
        alert.setGraphic(logo);

        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == cancel) {
            Helper.playSound(Helper.Sound.POP);
            alert.hide();
        } else if (option.get() == leaveGame) {
            alert.hide();
            Helper.playSound(Helper.Sound.POP);
            InfoViewCreator.getBackgroundMusic().setMute(true);
            primaryStage.hide();
            Main.createHomeWindow();
        }
    }

    public void endGame() {
        primaryStage.hide();
    }


}
