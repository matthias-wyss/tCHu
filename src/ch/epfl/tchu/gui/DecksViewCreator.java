package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.*;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.List;

//---------- CONTIENT DU BONUS ----------

/**
 * Classe créatrice des parties main du joueur et pioches (visibles ou non) de cartes et de billets
 * de l'interface de tCHu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */

final class DecksViewCreator {

    private DecksViewCreator() {}


    //----------BONUS----------
    private static final ObservableList<Station> stationsToShow = FXCollections.observableArrayList();

    public static ObservableList<Station> getStationsToShow() {
        return stationsToShow;
    }
    //-------------------------

    /**
     * créée la vue des cartes de la main du joueur
     * @param gameState l'état de la partie
     * @return un noeud contenant la vue de la partie
     */
    public static Node createHandView(ObservableGameState gameState){

        HBox view = new HBox();
        view.getStylesheets().add("decks.css");
        view.getStylesheets().add("colors.css");
        HBox handPane = new HBox();
        handPane.setId("hand-pane");

        ListView<Ticket> listView = new ListView<>(gameState.getPlayerTickets());

        listView.setOnMouseClicked(event -> {
            if(! listView.getSelectionModel().isEmpty()) {
                List<Trip> tripsList = listView.getSelectionModel().getSelectedItem().getTrips();
                stationsToShow.clear();
                if(listView.getSelectionModel().getSelectedItem().containsCountry()) {
                    stationsToShow.addAll(ChMap.countries());
                    if(listView.getSelectionModel().getSelectedItem().cityToCountry()) {
                        stationsToShow.add(tripsList.get(0).from());
                    }
                } else {
                    stationsToShow.add(tripsList.get(0).from());
                    stationsToShow.add(tripsList.get(tripsList.size() - 1).to());
                }
            } else {
                stationsToShow.clear();
            }
        });

        view.getChildren().add(listView);
        view.getChildren().add(handPane);
        listView.setId("tickets");

        for(Card card : Card.ALL){
            ReadOnlyIntegerProperty count = gameState.getPlayerCards().get(card);
            StackPane stack = new StackPane();
            stack.getStyleClass().add(card.color() == null ? "NEUTRAL" : card.color().name());

            stack.getStyleClass().add("card");
            stack.getChildren().addAll(createRectangles());
            Text counter = new Text();

            counter.textProperty().bind(Bindings.convert(count));
            counter.visibleProperty().bind(Bindings.greaterThan(count, 1));
            counter.getStyleClass().add("count");
            stack.visibleProperty().bind(Bindings.greaterThan(count, 0));
            stack.getChildren().add(counter);
            handPane.getChildren().add(stack);
        }

        return view;
    }

    /**
     * créée la vue des pioches des cartes et billets
     * @param gameState l'état de la partie
     * @param ticketsHandlerProperty la propriété du gestionnaire d'action de tirage de billets
     * @param cardHandlerProperty la propriété du gestionnaire d'action de tirage de cartes
     * @return un noeud contenant la vue des pioche des cartes et billets
     */
    public static Node createCardsView(ObservableGameState gameState, ReadOnlyObjectProperty<ActionHandlers.DrawTicketsHandler> ticketsHandlerProperty, ReadOnlyObjectProperty<ActionHandlers.DrawCardHandler> cardHandlerProperty){
        VBox view = new VBox();
        view.getStylesheets().add("decks.css");
        view.getStylesheets().add("colors.css");
        view.setId("card-pane");
        Button ticketsDeck = createButton(gameState.getTicketsRatio(), StringsFr.TICKETS);
        ticketsDeck.disableProperty().bind(ticketsHandlerProperty.isNull());
        ticketsDeck.setOnMouseClicked(e -> {
            ticketsHandlerProperty.get().onDrawTickets();

            //----------BONUS----------
            Helper.playSound(Helper.Sound.DRAW);
            //-------------------------
        });

        Button cardDeck = createButton(gameState.getCardsRatio(), StringsFr.CARDS);
        cardDeck.disableProperty().bind(cardHandlerProperty.isNull());
        cardDeck.setOnMouseClicked(e -> {
                cardHandlerProperty.get().onDrawCard(Constants.DECK_SLOT);

                //----------BONUS----------
                Helper.playSound(Helper.Sound.DRAW);
                //-------------------------
        });
        view.getChildren().add(ticketsDeck);

        for(int i = 0; i < Constants.FACE_UP_CARDS_COUNT; ++i) {
            StackPane stack = new StackPane();
            stack.getStyleClass().add("card");
            gameState.getFaceUpCards().get(i).addListener((p, o, n) -> {
            if(o != null){
                if(o.color() != n.color()){
                    String previousString = o.color() == null ? "NEUTRAL" : o.color().name();
                    String newString = n.color() == null ? "NEUTRAL" : n.color().name();
                    stack.getStyleClass().set(stack.getStyleClass().indexOf(previousString), newString);
                }
            } else {
                stack.getStyleClass().add(n.color() == null ? "NEUTRAL" : n.color().name());
            }
            });

            stack.getChildren().addAll(createRectangles());
            int currentI = i;
            stack.setOnMouseClicked(e -> {
                if(cardHandlerProperty.get() !=null){
                    cardHandlerProperty.get().onDrawCard(currentI);
                    //----------BONUS----------
                    Helper.playSound(Helper.Sound.DRAW);
                    //-------------------------
                }
            });
            view.getChildren().add(stack);

        }
        view.getChildren().add(cardDeck);
        return view;
    }

    private static List<Rectangle> createRectangles(){
        Rectangle outsideRectangle = new Rectangle(GraphicalConstants.DECK_OUTSIDE_RECTANGLE_WIDTH, GraphicalConstants.DECK_OUTSIDE_RECTANGLE_LENGTH);
        outsideRectangle.getStyleClass().add("outside");
        Rectangle insideRectangle = new Rectangle(GraphicalConstants.DECK_INSIDE_RECTANGLE_WIDTH, GraphicalConstants.DECK_INSIDE_RECTANGLE_LENGTH);
        insideRectangle.getStyleClass().add("inside");
        insideRectangle.getStyleClass().add("filled");
        Rectangle trainRectangle = new Rectangle(GraphicalConstants.DECK_INSIDE_RECTANGLE_WIDTH, GraphicalConstants.DECK_INSIDE_RECTANGLE_LENGTH);
        trainRectangle.getStyleClass().add("train-image");
        return List.of(outsideRectangle, insideRectangle, trainRectangle);
    }

    private static Button createButton(ReadOnlyIntegerProperty gaugeProperty, String name){
        Button button = new Button(name);
        button.getStyleClass().add("gauged");
        Rectangle backgroundRectangle = new Rectangle(GraphicalConstants.DECK_BUTTON_RECTANGLE_WIDTH, GraphicalConstants.DECK_BUTTON_RECTANGLE_LENGTH);
        backgroundRectangle.getStyleClass().add("background");
        Rectangle foregroundRectangle = new Rectangle(GraphicalConstants.DECK_BUTTON_RECTANGLE_WIDTH, GraphicalConstants.DECK_BUTTON_RECTANGLE_LENGTH);
        foregroundRectangle.getStyleClass().add("foreground");

        foregroundRectangle.widthProperty().bind(gaugeProperty.multiply(GraphicalConstants.DECK_GAUGE_FACTOR).divide(100));

        Group graphic = new Group(backgroundRectangle, foregroundRectangle);
        button.setGraphic(graphic);
        return button;
    }
}
