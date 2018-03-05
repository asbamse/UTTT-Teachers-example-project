package dk.easv.gui;

import com.jfoenix.controls.JFXButton;

import dk.easv.bll.bot.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class UTTTGameController implements Initializable {
    private static final long BOTDELAY = 500;
    @FXML
    private GridPane gridMacro;

    @FXML
    private AnchorPane paneWinner;

    @FXML
    private StackPane stackMain;

    private GridPane[][] gridMicros = new GridPane[3][3];
    private JFXButton[][] jfxButtons = new JFXButton[9][9];

    BoardModel model;
    IBot bot0 = null;
    IBot bot1 = null;
    String player0 = null;
    String player1 = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gridMacro.toFront(); // Or the buttons will not work
        createMicroGridPanes();
    }

    public void startGame() {
        if (model != null) model.removeListener(observable -> update());
        model.addListener(observable -> update());

        // HumanVsHuman
        if(player0!=null && player1!=null){

        }
        // HumanVsAI
        else if(bot1!=null && player0!=null) {

        }
        // AIvsHuman
        else if(bot0!=null && player1!=null) {
            int currentPlayer = model.getCurrentPlayer();
            Boolean valid = model.doMove();
            checkAndLockIfGameEnd(currentPlayer);
        }
        // AIvsAI
        else if(bot0!=null && bot1!=null) {
            Thread t = new Thread(()->{
                while(model.getGameOverState() == GameManager.GameOverState.Active)
                {
                    int currentPlayer = model.getCurrentPlayer();
                    Boolean valid = model.doMove();
                    checkAndLockIfGameEnd(currentPlayer);
                    try {
                        Thread.sleep(BOTDELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true); // Stops thread when main thread dies
            t.start();
        }
    }

    private void showWinnerPane(String winner)
    {
        Label lbl = new Label();
        lbl.getStyleClass().add("winmsg");
        lbl.getStyleClass().add("player" + winner);

        lbl.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
        lbl.setAlignment(Pos.CENTER);

        Text fontAwesomeIcon = getFontAwesomeIconFromPlayerId(winner);
        lbl.setGraphic(fontAwesomeIcon);
        stackMain.setAlignment(Pos.CENTER);
        Platform.runLater(() -> stackMain.getChildren().add(lbl));

    }

    private void createMicroGridPanes()
    {
        for (int i = 0; i < 3; i++) {
            gridMacro.addRow(i);
            for (int k = 0; k < 3; k++) {
                GridPane gp =  new GridPane();
                for (int m = 0; m < 3; m++) {
                    gp.addColumn(m);
                    gp.addRow(m);
                }
                gridMicros[i][k] = gp;
                for (int j = 0; j < 3; j++) {
                    ColumnConstraints cc = new ColumnConstraints();
                    cc.setPercentWidth(33);
                    cc.setHgrow(Priority.ALWAYS) ; // allow column to grow
                    cc.setFillWidth(true); // ask nodes to fill space for column
                    gp.getColumnConstraints().add(cc);

                    RowConstraints rc = new RowConstraints();
                    rc.setVgrow(Priority.ALWAYS) ; // allow row to grow
                    rc.setFillHeight(true);
                    rc.setPercentHeight(33);
                    gp.getRowConstraints().add(rc);
                }

                gp.setGridLinesVisible(true);
                gridMacro.addColumn(k);
                gridMacro.add(gp,i,k);
            }
        }
        insertButtonsIntoGridPanes();
    }
    private void insertButtonsIntoGridPanes() {
        for (int i = 0; i < 3; i++)
            for (int k = 0; k < 3; k++) {
                GridPane gp = gridMicros[i][k];
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        JFXButton btn = new JFXButton("");
                        btn.setButtonType(JFXButton.ButtonType.RAISED);
                        btn.getStyleClass().add("tictaccell");
                        btn.setUserData(new Move(x+i*3, y+k*3));

                        btn.setOnMouseClicked(
                                event -> {
                                    doMove((IMove)btn.getUserData()); // Player move

                                    boolean isHumanVsBot = player0!=null ^ player1!=null;
                                    if(isHumanVsBot)
                                    {
                                        int currentPlayer = model.getCurrentPlayer();
                                        Boolean valid = model.doMove();
                                        checkAndLockIfGameEnd(currentPlayer);
                                    }
                                }
                        );
                        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        gp.add(btn,x,y);
                        jfxButtons[x+i*3][y+k*3]=btn;
                    }
                }
        }
    }

    private void checkAndLockIfGameEnd(int currentPlayer)
    {
        if (model.getGameOverState() != GameManager.GameOverState.Active){
            System.out.println("GAME OVER + " + model.getGameOverState() + " " + currentPlayer);
            String[][] macroboard = model.getMacroboard();
            // Lock game
            for (int i = 0; i < 3; i++) {
                for (int k = 0; k < 3; k++) {
                    if(macroboard[i][k].equals(IField.AVAILABLE_FIELD))
                        macroboard[i][k] = IField.EMPTY_FIELD;
                }
            }
            if(model.getGameOverState().equals(GameManager.GameOverState.Tie))
                showWinnerPane("TIE");
            else
                showWinnerPane(currentPlayer+"");
        }
    }

    private boolean doMove(IMove move){
        int currentPlayer = model.getCurrentPlayer();
        boolean validMove = model.doMove(move);
        checkAndLockIfGameEnd(currentPlayer);
        return validMove;
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

    private void updateGUI() throws RuntimeException {
        String[][] board = model.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int k = 0; k < board[i].length; k++) {
                if(board[i][k].equals(IField.EMPTY_FIELD))
                    jfxButtons[i][k].getStyleClass().add("empty");
                else {
                    Text fontAwesomeIcon = getFontAwesomeIconFromPlayerId(board[i][k]);
                    jfxButtons[i][k].getStyleClass().add("player"+ board[i][k]);
                    jfxButtons[i][k].setGraphic(fontAwesomeIcon);
                }

            }
        }
        String[][] macroBoard = model.getMacroboard();
        for (int i = 0; i < macroBoard.length; i++) {
            for (int k = 0; k < macroBoard[i].length; k++) {
                if(gridMicros[i][k]!=null) {
                    if (macroBoard[i][k].equals(IField.AVAILABLE_FIELD) )
                        gridMicros[i][k].getStyleClass().add("highlight");
                    else
                        gridMicros[i][k].getStyleClass().removeAll("highlight");
                }
            }
        }

        for (int i = 0; i < macroBoard.length; i++) {
            for (int k = 0; k < macroBoard[i].length; k++) {
                // There is a win
                if(!macroBoard[i][k].equals(IField.AVAILABLE_FIELD) &&
                        !macroBoard[i][k].equals(IField.EMPTY_FIELD) &&
                        gridMicros[i][k] != null) {
                    gridMacro.getChildren().remove(gridMicros[i][k]);
                    Label lbl = new Label("");
                    Text fontAwesomeIcon = getFontAwesomeIconFromPlayerId(macroBoard[i][k]);
                    lbl.setGraphic(fontAwesomeIcon);
                    lbl.getStyleClass().add("winner-label");
                    lbl.getStyleClass().add("player" + macroBoard[i][k]);
                    lbl.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
                    gridMicros[i][k] = null;
                    gridMacro.add(lbl,i,k);
                }

            }
        }

    }

    private void updateConsole() {
        String[][] board = model.getGameState().getField().getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int k = 0; k < board[i].length; k++) {
                System.out.print("|"+board[i][k]+"|");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void update() {
        //updateConsole();
        Platform.runLater(() -> updateGUI());
    }

    public void setupGame(IBot bot0, IBot bot1) {
        model = new BoardModel(bot0, bot1);
        this.bot0=bot0;
        this.bot1=bot1;
    }

    public void setupGame(String humanName, IBot bot1) {
        model = new BoardModel(bot1,true);
        this.bot1 = bot1;
        this.player0=humanName;
    }

    public void setupGame(IBot bot0, String humanName) {
        model = new BoardModel(bot0,false);
        this.bot0 = bot0;
        this.player1=humanName;
    }

    public void setupGame(String humanName0, String humanName1) {
        model = new BoardModel();
        this.player0 = humanName0;
        this.player1 = humanName1;
    }
}
