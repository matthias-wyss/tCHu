package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Constants;
import ch.epfl.tchu.game.PlayerId;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Classe créatrice de la partie informations de l'interface de tCHu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
final class InfoViewCreator {

    //----------BONUS----------
    private final static MediaPlayer backgroundMusic = new MediaPlayer(new Media(ClassLoader.getSystemResource("tCHu.wav").toString()));
    private final static double MUSIC_VOLUME_ON = 1.0;
    private static boolean effectsMuted = false;
    //-------------------------

    private InfoViewCreator() {

    }

    /**
     * Crée l'interface graphique contenant les informations du joueur
     * @param id l'ID du joueur pour lequel on crée l'information (affiché en premier)
     * @param playerNames table associative des noms des joueurs
     * @param gameState état observable du joueur de la partie
     * @param infos liste de messages d'informations à afficher
     * @param graphicalPlayer interface graphique du joueur
     * @return l'interface graphique avec les informations
     */
    public static Node createInfoView(PlayerId id, Map<PlayerId, String> playerNames, ObservableGameState gameState, ObservableList<Text> infos, ReadOnlyObjectProperty<GraphicalPlayer> graphicalPlayer){

        VBox view = new VBox();
        view.getStylesheets().add("info.css");
        view.getStylesheets().add("colors.css");

        //----------BONUS----------
        backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundMusic.setVolume(MUSIC_VOLUME_ON);
        backgroundMusic.play();

        CheckBox muteMusic = new CheckBox("Musique");
        muteMusic.setSelected(true);
        muteMusic.setOnMouseClicked(event -> {
            if(muteMusic.isSelected()) {
                backgroundMusic.setMute(false);
                Helper.playSound(Helper.Sound.SWITCH_ON);
            } else {
                backgroundMusic.setMute(true);
                Helper.playSound(Helper.Sound.SWITCH_OFF);
            }
        });

        CheckBox muteEffects = new CheckBox("Effets");
        muteEffects.setSelected(true);
        muteEffects.setOnMouseClicked(event -> {
            if(muteEffects.isSelected()) {
                setEffectsMuted(false);
                Helper.playSound(Helper.Sound.SWITCH_ON);
            } else {
                setEffectsMuted(true);
            }
        });

        HBox buttons = new HBox();
        buttons.getStylesheets().add("join-game.css");
        Button rules = new Button("Règles");
        rules.setOnMouseClicked(event -> {
            createRulesWindow();
            Helper.playSound(Helper.Sound.POP);
        });

        Button leaveGame = new Button("Quitter");
        leaveGame.setOnMouseClicked(event -> {
            Helper.playSound(Helper.Sound.POP);
            graphicalPlayer.get().showLeaveGameConfirmation();
        });
        leaveGame.visibleProperty().bind(graphicalPlayer.isNull().not());

        buttons.getChildren().add(rules);
        buttons.getChildren().add(leaveGame);
        //-------------------------

        TextFlow messageFlow = new TextFlow();
        messageFlow.setId("game-info");
        for(int i = 0; i < Constants.MAX_DISPLAYED_MESSAGES; ++i){
            Text text = new Text();
            messageFlow.getChildren().add(text);
        }
        Bindings.bindContent(messageFlow.getChildren(), infos);
        VBox playerStats = new VBox();
        playerStats.setId("player-stats");

        //----------BONUS----------
        HBox controls = new HBox();
        controls.setId("music-controls");

        controls.getChildren().add(muteMusic);
        controls.getChildren().add(muteEffects);

        playerStats.getChildren().add(controls);
        playerStats.getChildren().add(buttons);
        //-------------------------

        playerStats.getChildren().add(createPlayerInfo(id, playerNames.get(id), gameState));
        playerStats.getChildren().add(createPlayerInfo(id.next(), playerNames.get(id.next()), gameState));
        view.getChildren().add(playerStats);

        Separator sep = new Separator();
        view.getChildren().add(sep);
        view.getChildren().add(messageFlow);

        return view;
    }

    private static Node createPlayerInfo(PlayerId id, String playerName, ObservableGameState gameState){
        TextFlow playerText = new TextFlow();
        playerText.getStyleClass().add(id.name());
        Circle circle = new Circle(GraphicalConstants.PLAYER_INFO_CIRCLE_RADIUS);
        circle.getStyleClass().add("filled");
        Text nameAndStats = new Text();
        playerText.getChildren().add(circle);
        playerText.getChildren().add(nameAndStats);
        nameAndStats.textProperty().bind(Bindings.format(StringsFr.PLAYER_STATS, playerName,
                gameState.getPlayerTicketsCount(id), gameState.getPlayerCardsCount(id),
                gameState.getPlayerCarCount(id), gameState.getPlayerConstructionPoints(id)));
        return playerText;
    }


    //----------BONUS----------
    private static void createRulesWindow() {
        Stage rulesStage = new Stage();
        VBox view = new VBox();
        view.getStylesheets().add("rules.css");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rulesStage.initModality(Modality.WINDOW_MODAL);
        rulesStage.setTitle("tCHu \u2014 Règles du jeu");
        rulesStage.setMaxHeight(700.0);
        rulesStage.setMaxWidth(475.0);
        scrollPane.setPrefViewportHeight(700);
        scrollPane.setPrefViewportWidth(475);

        TextFlow textFlow1 = new TextFlow();
        String stringRules1 = Helper.txtReader("rules-part-1.txt");
        Text rules1 = new Text(stringRules1);
        textFlow1.getChildren().add(rules1);

        ImageView example = new ImageView("example.png");

        TextFlow textFlow2 = new TextFlow();
        String stringRules2 = Helper.txtReader("rules-part-2.txt");
        Text rules2 = new Text(stringRules2);
        textFlow2.getChildren().add(rules2);

        view.getChildren().add(textFlow1);
        view.getChildren().add(example);
        view.getChildren().add(textFlow2);
        scrollPane.setContent(view);
        rulesStage.setScene(new Scene(scrollPane));
        rulesStage.show();
    }

    private static void setEffectsMuted(boolean muted) {
        effectsMuted = muted;
    }

    public static boolean isMuted() {
        return effectsMuted;
    }

    public static MediaPlayer getBackgroundMusic() {
        return backgroundMusic;
    }
    //-------------------------

}