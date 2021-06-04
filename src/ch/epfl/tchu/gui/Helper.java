package ch.epfl.tchu.gui;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static javafx.application.Platform.runLater;


public final class Helper {
    private Helper(){

    }
    public enum Sound{
        POP(new AudioClip(ClassLoader.getSystemResource("pop.wav").toString())),
        CLAIM(new AudioClip(ClassLoader.getSystemResource("train-whistling-1.wav").toString())),
        SWITCH_ON(new AudioClip(ClassLoader.getSystemResource("switch-on.wav").toString())),
        SWITCH_OFF(new AudioClip(ClassLoader.getSystemResource("switch-off.wav").toString())),
        SHUFFLE(new AudioClip(ClassLoader.getSystemResource("shuffle.wav").toString())),
        DRAW(new AudioClip(ClassLoader.getSystemResource("draw.wav").toString()));

        private final AudioClip audioClip;
        Sound(AudioClip sound) {
            this.audioClip = sound;
        }

        private AudioClip getAudioClip(){
            return audioClip;
        }
    }

    public static void playSound(Sound sound){
        if(!InfoViewCreator.isMuted()){
            switch(sound){
                case POP:
                    Sound.POP.getAudioClip().play();
                    break;
                case CLAIM:
                    AudioClip claimSound = Sound.CLAIM.getAudioClip();
                    claimSound.setVolume(0.1);
                    claimSound.play();
                    break;
                case SWITCH_ON:
                    AudioClip switchOnSound = Sound.SWITCH_ON.getAudioClip();
                    switchOnSound.setVolume(0.3);
                    switchOnSound.play();
                    break;
                case SWITCH_OFF:
                    AudioClip switchOffSound = Sound.SWITCH_OFF.getAudioClip();
                    switchOffSound.setVolume(0.3);
                    switchOffSound.play();
                    break;
                case SHUFFLE:
                    Sound.SHUFFLE.getAudioClip().play();
                    break;
                case DRAW:
                    Sound.DRAW.getAudioClip().play();
                    break;
            }
        }
    }

    public static String txtReader(String file) {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(file);
            assert is != null;
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sB = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sB.append(line).append("\n");
            }
            br.close();
            return sB.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPublicIP() throws IOException {
        URL whatismyip = new URL("https://checkip.amazonaws.com");
        BufferedReader br = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        String result = br.readLine();
        br.close();
        return result;
    }

    public static String getPrivateIP() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        return socket.getLocalAddress().toString().substring(1);
    }

    public static void showInformation(Stage owner, String header, String file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("tCHu \u2014 Information");
        alert.setHeaderText(header);
        String text = txtReader(file);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void showError(Stage owner, String header, String file) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("tCHu \u2014 Erreur");
        alert.setHeaderText("Erreur : " + header);
        String text = txtReader(file);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void setLogo(Stage stage) {
        // logo for Windows and icon for both
        Image logoWin = new Image(ClassLoader.getSystemResource("switzerland.png").toString());
        stage.getIcons().add(logoWin);

        // logo for macOS
        if(System.getProperty("os.name").toLowerCase().contains("mac")) {
            try {
                Taskbar taskbar = Taskbar.getTaskbar();
                BufferedImage image = ImageIO.read(new File("resources/switzerland.png"));
                taskbar.setIconImage(image);
            } catch (IOException e) {
                System.out.println("logo not found");
            }
        }
    }

    public static void showGameNotFoundWindow() {
        runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("tCHu \u2014 Erreur");
            alert.setHeaderText("Erreur : Partie non trouvée");
            String errorExplanation = txtReader("game-not-found-error.txt");
            alert.setContentText(errorExplanation);
            alert.showAndWait();
        });
    }

}