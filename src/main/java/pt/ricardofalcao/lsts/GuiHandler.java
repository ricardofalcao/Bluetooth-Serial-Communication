package pt.ricardofalcao.lsts;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

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

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/main/main.fxml"));

        primaryStage.setTitle("Bluetooth Serial Communication");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        Main.server.stopServerAsync().get();
    }
}
