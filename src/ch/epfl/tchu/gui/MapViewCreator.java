package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.text.Normalizer;
import java.util.*;

//---------- CONTIENT DU BONUS ----------

/**
 * Classe créatrice de la partie carte et routes de l'interface de tCHu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */

final class MapViewCreator {


    /**
     * interface imbriquée de selectioneur de cartes
     */
    @FunctionalInterface
    interface CardChooser{
        void chooseCards(List<SortedBag<Card>> options, ActionHandlers.ChooseCardsHandler handler);
    }

    private MapViewCreator () {
    }

    /**
     * créée la vue de la carte
     * @param gameState l'état de la partie
     * @param claimRouteHandlerProperty la propriété du gestionaire d'action de tenter de s'emparer d'une route
     * @param cardChooser le selectioneur de cartes
     * @return un noeud contenant la vue de la carte
     */
    public static Node createMapView(ObservableGameState gameState, ReadOnlyObjectProperty<ActionHandlers.ClaimRouteHandler> claimRouteHandlerProperty, CardChooser cardChooser){
        Pane view = new Pane();
        ImageView map = new ImageView();
        view.getStylesheets().add("map.css");
        view.getStylesheets().add("colors.css");
        view.getChildren().add(map);

        //----------BONUS----------
        Map<Station, Group> stationGroupMap = new HashMap<>();
        for(Station station : ChMap.stations()) {
            Group stationGroup = new Group();
            stationGroup.getStyleClass().add("station");
            stationGroup.getStyleClass().add(stationToString(station.name()));
            Circle circle = new Circle(GraphicalConstants.STATION_CIRCLE_RADIUS);
            circle.setVisible(false);
            stationGroup.getChildren().add(circle);
            stationGroupMap.put(station, stationGroup);
            view.getChildren().add(stationGroup);
        }

        DecksViewCreator.getStationsToShow().addListener((ListChangeListener<Station>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Station station : c.getAddedSubList()) {
                        Group stationGroup = stationGroupMap.get(station);
                        stationGroup.getChildren().get(stationGroup.getChildren().size() - 1).setVisible(true);
                    }

                } else if (c.wasRemoved()) {
                    for (Station station : c.getRemoved()) {
                        Group stationGroup = stationGroupMap.get(station);
                        stationGroup.getChildren().get(stationGroup.getChildren().size() - 1).setVisible(false);
                    }
                }
            }
        });
        //-------------------------


        for(Route route : ChMap.routes()){
            Group routeGroup = new Group();
            routeGroup.setId(route.id());
            routeGroup.getStyleClass().add("route");
            routeGroup.getStyleClass().add(route.level().name());
            routeGroup.getStyleClass().add(route.color() == null ? "NEUTRAL" : route.color().name());

            gameState.getRoutes().get(route).addListener((p, o, n) -> {
                if(o==null && n!=null){
                    routeGroup.getStyleClass().add(n.name());
                }
            });

            routeGroup.disableProperty().bind(claimRouteHandlerProperty.isNull().or(gameState.isClaimable(route).not()));

            routeGroup.setOnMouseClicked(e -> {
                List<SortedBag<Card>> possibleClaimCards = gameState.possibleClaimCards(route);
                ActionHandlers.ClaimRouteHandler claimRouteH = claimRouteHandlerProperty.get();
                if(possibleClaimCards.size() > 1){
                    ActionHandlers.ChooseCardsHandler chooseCardsH =
                            chosenCards -> claimRouteH.onClaimRoute(route, chosenCards);
                    cardChooser.chooseCards(possibleClaimCards, chooseCardsH);
                } else {
                    claimRouteH.onClaimRoute(route, possibleClaimCards.get(0));
                }

                //----------BONUS----------
                Helper.playSound(Helper.Sound.CLAIM);
                //-------------------------
            });


            for(int i = 1; i <= route.length(); ++i) {
                Group segmentGroup = new Group();
                segmentGroup.setId(String.format("%s_%d", route.id(), i));
                Rectangle way = new Rectangle(GraphicalConstants.SEGMENT_ROUTE_RECTANGLE_LENGTH, GraphicalConstants.SEGMENT_ROUTE_RECTANGLE_WIDTH);
                way.getStyleClass().add("track");
                way.getStyleClass().add("filled");
                Group carGroup = new Group();
                carGroup.getStyleClass().add("car");
                Rectangle rectangle = new Rectangle(GraphicalConstants.SEGMENT_ROUTE_RECTANGLE_LENGTH, GraphicalConstants.SEGMENT_ROUTE_RECTANGLE_WIDTH);
                rectangle.getStyleClass().add("filled");
                carGroup.getChildren().add(rectangle);
                carGroup.getChildren().add(new Circle(GraphicalConstants.CAR_CIRCLE_1_X, GraphicalConstants.CAR_CIRCLE_Y, GraphicalConstants.CAR_CIRCLE_RADIUS));
                carGroup.getChildren().add(new Circle(GraphicalConstants.CAR_CIRCLE_2_X, GraphicalConstants.CAR_CIRCLE_Y, GraphicalConstants.CAR_CIRCLE_RADIUS));
                segmentGroup.getChildren().add(way);
                segmentGroup.getChildren().add(carGroup);
                routeGroup.getChildren().add(segmentGroup);
            }
            view.getChildren().add(routeGroup);

        }

        return view;
    }

    //----------BONUS----------
    private static String stationToString (String station) {
        String stationString = Normalizer.normalize(station, Normalizer.Form.NFD);
        stationString = stationString.replaceAll("\\p{M}", "").replaceAll(" ", "-");
        return stationString;
    }
    //-------------------------

}
