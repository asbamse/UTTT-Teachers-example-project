package dk.easv.gui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import dk.easv.bll.bot.IBot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class AppController implements Initializable{
    public JFXButton btnTrash;
    public JFXButton btnDiamond;
    @FXML
    private JFXTextField txtHumanNameLeft;
    @FXML
    private JFXRadioButton radioRightAI;
    @FXML
    private JFXTextField txtHumanNameRight;
    @FXML
    private JFXRadioButton radioLeftAI;
    @FXML
    private JFXRadioButton radioRightHuman;
    @FXML
    private  ToggleGroup toggleLeft;
    @FXML
    private  ToggleGroup toggleRight;

    @FXML
    private JFXButton btnStart;
    @FXML
    private AnchorPane anchorMain;
    @FXML
    private JFXComboBox<IBot> comboBotsRight;
    @FXML
    private JFXComboBox<IBot> comboBotsLeft;
    @FXML
    private JFXRadioButton radioLeftHuman;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<IBot> bots = FXCollections.observableArrayList();

        Path dir = FileSystems.getDefault().getPath( "./src/dk/easv/bll/bot" );
        try(DirectoryStream<Path> stream = Files.newDirectoryStream( dir, "*.java" )) {
            for (Path path : stream) {
                String classPathAndName = "dk.easv.bll.bot."+getFilenameNoExtension(path);
                // Klassen skal være på ClassPath for at dette virker...
                URL[] urls = {path.toFile().toURI().toURL()};
                ClassLoader cl = new URLClassLoader(urls);
                Class clazz = cl.loadClass(classPathAndName);
                if(!clazz.isInterface())
                {
                    IBot bot = (IBot) clazz.newInstance();
                //IBot bot = (IBot) Class.forName("dk.easv.bll.bot."+getFilenameNoExtension(path)).newInstance();
                bots.add(bot);}
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } ;

        comboBotsLeft.setButtonCell(new CustomIBotListCell());
        comboBotsLeft.setCellFactory(p -> new CustomIBotListCell());
        comboBotsLeft.setItems(bots);
        comboBotsRight.setButtonCell(new CustomIBotListCell());
        comboBotsRight.setCellFactory(p -> new CustomIBotListCell());
        comboBotsRight.setItems(bots);
        btnStart.setDisableVisualFocus(true);
        btnDiamond.setGraphic(getFontAwesomeIconFromPlayerId("1"));
        btnTrash.setGraphic(getFontAwesomeIconFromPlayerId("0"));

        radioLeftAI.selectedProperty().addListener((observable, oldValue, newValue) -> comboBotsLeft.setDisable(!newValue));
        radioLeftHuman.selectedProperty().addListener((observable, oldValue, newValue) -> txtHumanNameLeft.setDisable(!newValue));
        radioRightAI.selectedProperty().addListener((observable, oldValue, newValue) -> comboBotsRight.setDisable(!newValue));
        radioRightHuman.selectedProperty().addListener((observable, oldValue, newValue) -> txtHumanNameRight.setDisable(!newValue));
        comboBotsLeft.getSelectionModel().selectFirst();
        comboBotsLeft.setDisable(true);
        comboBotsRight.getSelectionModel().selectFirst();
        comboBotsRight.setDisable(true);

    }

    private Text getFontAwesomeIconFromPlayerId(String playerId) throws RuntimeException {
        Text fontAwesomeIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ASTERISK);
        if(playerId.equals("0"))
            return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.DIAMOND);
        else if (playerId.equals("1"))
            return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.TRASH);
        else if(playerId.equals("TIE"))
            return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.BLACK_TIE);
        else
            throw new RuntimeException("PlayerId not valid");
    }

    public void leftAISelected(ActionEvent actionEvent) {
    }

    public void leftHumanSelected(ActionEvent actionEvent) {
    }

    public void rightHumanSelected(ActionEvent actionEvent) {
    }

    public void rightAISelected(ActionEvent actionEvent) {
    }

    private class CustomIBotListCell extends ListCell<IBot> {
        @Override protected void updateItem(IBot item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                setText(item.getBotName());
            } else {
                setText(null);
            }
        }
    }

    private String getFilenameNoExtension(Path path){
        String fileName = path.getFileName().toFile().getName();
        if (fileName.indexOf(".") > 0)
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return fileName;
    }



    public void clickStart(ActionEvent actionEvent) throws IOException {
        Stage primaryStage = new Stage();
        primaryStage.initModality(Modality.WINDOW_MODAL);
        FXMLLoader fxLoader = new FXMLLoader(getClass().getResource("UTTTGame.fxml"));

        Parent root = fxLoader.load();

        UTTTGameController controller = ((UTTTGameController)fxLoader.getController());

        if(toggleLeft.getSelectedToggle().equals(radioLeftAI) &&
                toggleRight.getSelectedToggle().equals(radioRightAI))     {
            controller.setupGame(comboBotsLeft.getSelectionModel().getSelectedItem(),  comboBotsRight.getSelectionModel().getSelectedItem());
        }
        else if(toggleLeft.getSelectedToggle().equals(radioLeftHuman) &&
                toggleRight.getSelectedToggle().equals(radioRightAI))     {
            controller.setupGame(txtHumanNameLeft.getText(),  comboBotsRight.getSelectionModel().getSelectedItem());
        }
        else if(toggleLeft.getSelectedToggle().equals(radioLeftAI) &&
                toggleRight.getSelectedToggle().equals(radioRightHuman))     {
            controller.setupGame(comboBotsLeft.getSelectionModel().getSelectedItem(),  txtHumanNameRight.getText());
        }
        else if(toggleLeft.getSelectedToggle().equals(radioLeftHuman) &&
                toggleRight.getSelectedToggle().equals(radioRightHuman))     {
            controller.setupGame(txtHumanNameLeft.getText(),  txtHumanNameRight.getText());
        }
        controller.startGame();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.showAndWait();

    }
}
