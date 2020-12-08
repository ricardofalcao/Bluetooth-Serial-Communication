package pt.ricardofalcao.lsts.bsc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GuiHandler extends Application {

    @Getter
    private Stage primaryStage;

    /*

     */

    @Override
    public void init() throws Exception {
        Main.gui = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        for(int size : new int[] {16, 24, 32, 48, 64, 96, 128, 256, 512}) {
            try (InputStream reader = getClass().getResourceAsStream(String.format("icons/%d.png", size))){
                primaryStage.getIcons().add(new Image(reader));
            } catch (IOException e) {
                e.printStackTrace();
            };
        }

        Parent root = FXMLLoader.load(getClass().getResource("fxml/main/main.fxml"));

        primaryStage.setTitle("Bluetooth Serial Communication");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
