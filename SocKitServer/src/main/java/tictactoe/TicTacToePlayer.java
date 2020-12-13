package tictactoe;

import io.sockit.sockitserver.JsonUtil;
import javax.json.JsonObject;
import io.sockit.sockitserver.Player;
import io.sockit.sockitserver.Session;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class TicTacToePlayer extends Player{

    CellState playerToken;// X or 0
    
    public TicTacToePlayer(Session session, int seatNo) {
        super(session, seatNo);
        //if seatNo is 1 set playerToken to X else set it to 0
        playerToken = seatNo == 1 ? CellState.x : CellState.o;
    }
    

    @Override
    protected JsonObject prepareJsonForOtherClients() {
        return prepareJsonForSelfClient();//as data is same for others as well as self
    }

    @Override
    protected JsonObject prepareJsonForSelfClient() {
        JsonObjectBuilder json = JsonUtil.createObjectBuilder();
        json.add("playerToken", playerToken.toString());        
        return json.build();
    }

    @Override
    protected void resetData() {
        
    }

    @Override
    protected boolean canJoinGamePlay() {
        return true;
    }

    @Override
    protected boolean canPlayTurn() {
        return true;
    }
    
}
