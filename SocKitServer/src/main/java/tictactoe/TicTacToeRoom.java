package tictactoe;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import io.sockit.sockitserver.Game;
import io.sockit.sockitserver.JsonUtil;
import io.sockit.sockitserver.Player;
import io.sockit.sockitserver.Room;
import io.sockit.sockitserver.RoomType;
import io.sockit.sockitserver.Session;
import java.util.Arrays;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
public class TicTacToeRoom  extends Room{
    
    // variable to represent 3 x 3 = 9 cell grid
    //1st row will be cells {0,1,2}. 2nd row will be cells {3,4,5} and 3rd row will be {6,7,8}
    //all cells will be set to unmarked in the beginning
    CellState[] grid;
    
    //the winner of the previous game play
    Player previousWinner;
    //player who started the previous game play (first turn player)
    Player previousFirstPlayer;
    //winner of the current game play - set to null before game play starts
    Player winner;
    //player who started the current game play - set to null before game play starts
    Player firstPlayer;
    //winning cell combination - set to null before game play starts
    String winningCells;
    

    public TicTacToeRoom(Game game, String roomName, RoomType roomType, int turnDurationInSecs, int delayAfterGameEnded, boolean enableDebug) {
        super(game, roomName, roomType, 2, turnDurationInSecs, delayAfterGameEnded, enableDebug);
        //initializes grid to an array of 9 elements
        grid = new CellState[9];
        //initializes every element in the array to unmarked
        Arrays.fill(grid, CellState.unmarked);
    }

    @Override
    protected Player newPlayer(Session session, int seatNo, JsonObject jo) {
        return new TicTacToePlayer(session, seatNo);
    }

    @Override
    protected void resetData() {
        //sets every cell in the grid to unmarked
        Arrays.fill(grid, CellState.unmarked);        
    }

    @Override
    protected JsonObject prepareJsonForClient() {
        //converts the grid to json so that it can be sent to the game clients
        JsonObjectBuilder json =JsonUtil.createObjectBuilder();
        JsonArrayBuilder jsonGrid = JsonUtil.createArrayBuilder();
        for(int counter=0; counter < grid.length; counter++){
            jsonGrid.add(grid[counter].toString());
        }
        json.add("grid", jsonGrid);
        
        return json.build();
    }

    @Override
    protected JsonObject getAdditionalRoomConfig() {
        return null;// return null as there is no additional configuration for a room of this Game
    }

    @Override
    protected void setAdditionalRoomConfig(JsonObject additionalRoomConfig) {
        //does nothing as there is no additional configuration for a room of this Game
    }

    @Override
    protected JsonObject getNextTurnData(Player nextTurnPlayer) {
        return null;
    }

    @Override
    protected void beforeSeatLeft(Player player, boolean isCurTurnPlayer, boolean isLeavingRoom, boolean isLoggingOut) {
        //if game play is in progress end game play and 
        //set the remaining player as the winner
        if(this.isGamePlayInProgress()){
            this.winningCells=null;
            this.winner=this.getNextActivePlayer(player.seatNo);
            this.endGamePlay();
        }
    }

    @Override
    protected void onTurnTimedOut(Player turnPlayer) {
        //end game play and set the other player as the winner
        this.winningCells=null;
        this.winner=this.getNextActivePlayer(turnPlayer.seatNo);
        this.endGamePlay();
    }

    @Override
    protected Player afterGamePlayStarted() {
        //if previous winner is still in the room set the first player to the previous winner 
        //else if previous first player is still in the room set the first player to other than previous first player
        //if previous winner is still in the room
        if(this.previousWinner != null && this.previousWinner.isActive()){
            //set the first player to the previous winner 
            this.firstPlayer = this.previousWinner;
        }else{
            //if previous first player is still in the room
            if(this.previousFirstPlayer != null && this.previousFirstPlayer.isActive() ){              
                //set the first player to one next to (other than) the previous first player
                this.firstPlayer = this.getNextActivePlayer(previousFirstPlayer.seatNo);
            }else{
                //set the first player to the one seated on the first seat
                this.firstPlayer = this.getFirstActivePlayer();
            }           
        }
        
        return this.firstPlayer;
        
    }

    @Override
    protected void beforeGamePlayStarted() {
        //set the previous first player to first player of the game that just ended
        this.previousFirstPlayer = this.firstPlayer;
        //set the previous winner to winner of the game that just ended
        this.previousWinner = this.winner;

        this.winner = null;
        this.firstPlayer = null;
        this.winningCells=null;
    }

    @Override
    protected void onPlayerAction(Player player, String action, JsonObject data, boolean isOutOfTurn) { 
        //if out of turn do nothing
        if(isOutOfTurn){
            return;
        }
        if(action.equals("cellClicked")){
            //get the index of the cell clicked
            int cellIndex = data.getInt("cellIndex");

            //check if the cell index is valid
            if(cellIndex > 8 || cellIndex < 0){
                this.invalidateAction("Cell Dosen't exist", null);
                return;
            }

            JsonObjectBuilder sendData;

            //if the cell index was already marked
            //send invalid action message to the client and exit function
            if(grid[cellIndex] != CellState.unmarked){
                sendData = JsonUtil.createObjectBuilder();
                sendData.add("cellIndex", cellIndex);
                sendData.add("cellValue", grid[cellIndex].toString());
                this.invalidateAction("Can't Click That!", sendData.build());
                return;
            }

            //set the cell that was clicked to the player's token
            TicTacToePlayer ticTacToePlayer = (TicTacToePlayer) player;
            grid[cellIndex] = ticTacToePlayer.playerToken;
            
            //send turn played message to the server
            sendData = JsonUtil.createObjectBuilder();
            sendData.add("cellIndex", cellIndex);
            sendData.add("cellValue", grid[cellIndex].toString());
            this.actionPlayed("cellIndex", sendData.build());
        }
    }

    @Override
    protected Player afterTurnPlayed(Player player, String action, JsonObject actionData) {
        //check if game is over i.e. a player has 3 in a row or all the cells have been marked and it's a draw
        //if game is over end game play
        if(isGameOver()){
            this.endGamePlay();
            return null;
        }
        
        //return the player whose turn is next
        return this.getNextActivePlayer();        
    }

    @Override
    protected JsonObject beforeGamePlayEnded() {
        //build the json that will be sent to the client with the gamePlayEnded message
        JsonObjectBuilder json = JsonUtil.createObjectBuilder();
        json.add("isDraw", this.winner == null);
        json.add("winningSeatNo", this.winner == null ? 0 : this.winner.seatNo);
        if(winningCells==null)
            json.addNull("winningCells");
        else
            json.add("winningCells", winningCellsToJsonArray());
        return json.build();        
    }
    
    //converts the winning cells to json
    JsonArray winningCellsToJsonArray(){
        JsonArrayBuilder jsonWinningCells=JsonUtil.createArrayBuilder();
        jsonWinningCells.add(Integer.parseInt(winningCells.substring(0, 1)));
        jsonWinningCells.add(Integer.parseInt(winningCells.substring(2, 3)));
        jsonWinningCells.add(Integer.parseInt(winningCells.substring(4, 5)));
        return jsonWinningCells.build();
    }
    
    //checks if cells in a straight line are same and sets the winningCells and winner
    //returns true if cells in a straight line are same else false
    private boolean checkWinner(){
        if(areCellsSame(0, 1, 2)){
            winningCells="0,1,2";
            winner = getPlayerByToken(grid[0]);
            return true;
        }
        if(areCellsSame(3, 4, 5)){
            winningCells="3,4,5";
            winner = getPlayerByToken(grid[3]);
            return true;
        }
        if(areCellsSame(6, 7, 8)){
            winningCells="6,7,8";
            winner = getPlayerByToken(grid[6]);
            return true;
        }
        if(areCellsSame(0, 3, 6)){
            winningCells="0,3,6";
            winner = getPlayerByToken(grid[0]);
            return true;
        }
        if(areCellsSame(1, 4, 7)){
            winningCells="1,4,7";
            winner = getPlayerByToken(grid[1]);
            return true;
        }
        if(areCellsSame(2, 5, 8)){
            winningCells="2,5,8";
            winner = getPlayerByToken(grid[2]);
            return true;
        }
        if(areCellsSame(2, 4, 6)){
            winningCells="2,4,6";
            winner = getPlayerByToken(grid[2]);
            return true;
        }
        if(areCellsSame(0, 4, 8)){
            winningCells="0,4,8";
            winner = getPlayerByToken(grid[0]);
            return true;
        }
        return false;
    }
   
    private boolean areCellsSame(int cell1, int cell2, int cell3){
        return grid[cell1] != CellState.unmarked && grid[cell1] == grid[cell2] && grid[cell1] == grid[cell3]; 
    }
    
    //returns player whose token matches the specified token
    private TicTacToePlayer getPlayerByToken(CellState gameToken){
        TicTacToePlayer ticTacToePlayer;
        for(Player player:this.getActivePlayers()){
            ticTacToePlayer = (TicTacToePlayer) player;
            if(ticTacToePlayer.playerToken == gameToken){
                return ticTacToePlayer;
            }
        }
        return null;
    }
    
    //checks if game is over or not
    private boolean isGameOver(){
        if(checkWinner()){
            return true;
        }
        
        for(CellState type:grid){
            if(type == CellState.unmarked){
                return false;
            }
        }
        
        return true;
    }    
}
